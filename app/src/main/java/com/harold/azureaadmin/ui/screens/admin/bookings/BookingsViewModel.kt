package com.harold.azureaadmin.ui.screens.admin.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.models.*
import com.harold.azureaadmin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    // ============================================================================
    // STATE MANAGEMENT
    // ============================================================================

    private val _bookings = MutableStateFlow<List<BookingData>>(emptyList())
    val bookings: StateFlow<List<BookingData>> = _bookings.asStateFlow()

    private val _pagination = MutableStateFlow<Pagination?>(null)
    val pagination: StateFlow<Pagination?> = _pagination.asStateFlow()

    private val _bookingDetails = MutableStateFlow<BookingDetails?>(null)
    val bookingDetails: StateFlow<BookingDetails?> = _bookingDetails.asStateFlow()

    private val _statusUpdateMessage = MutableStateFlow<String?>(null)
    val statusUpdateMessage: StateFlow<String?> = _statusUpdateMessage.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // NEW: Notification count for new/pending bookings
    private val _notificationCount = MutableStateFlow(0)
    val notificationCount: StateFlow<Int> = _notificationCount.asStateFlow()

    // Store previous booking IDs to detect new ones
    private var previousBookingIds = setOf<Int>()
    private var isFirstLoad = true

    // Cache current filter state
    private var currentPage = 1
    private var currentStatus: String? = null

    // ============================================================================
    // BOOKING LIST OPERATIONS
    // ============================================================================

    fun fetchBookings(page: Int = 1, status: String? = null) {
        viewModelScope.launch {
            if (_loading.value) return@launch

            _loading.value = true
            _error.value = null

            try {
                val response = repository.getBookings(page, pageSize = 9, status)

                response.body()?.let { body ->
                    val newBookings = body.data

                    // Calculate notification count
                    if (!isFirstLoad) {
                        calculateNotificationCount(newBookings)
                    } else {
                        // On first load, just count pending bookings
                        _notificationCount.value = newBookings.count {
                            it.status.equals("pending", ignoreCase = true)
                        }
                        isFirstLoad = false
                    }

                    // Update previous IDs for next comparison
                    previousBookingIds = newBookings.map { it.id }.toSet()

                    _bookings.value = newBookings
                    _pagination.value = body.pagination
                    currentPage = page
                    currentStatus = status
                } ?: run {
                    _error.value = "No data available"
                }

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error fetching bookings"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Calculate notification count based on new bookings and pending status
     */
    private fun calculateNotificationCount(newBookings: List<BookingData>) {
        val currentIds = newBookings.map { it.id }.toSet()

        // Find truly new booking IDs (not in previous list)
        val newBookingIds = currentIds - previousBookingIds

        // Count new bookings + existing pending bookings
        val newCount = newBookings.count { booking ->
            // Either it's a new booking OR it has pending status
            newBookingIds.contains(booking.id) ||
                    booking.status.equals("pending", ignoreCase = true)
        }

        _notificationCount.value = newCount
    }

    fun refreshBookings() {
        fetchBookings(currentPage, currentStatus)
    }

    fun loadNextPage() {
        val pagination = _pagination.value ?: return
        val nextPage = pagination.current_page + 1

        if (nextPage <= pagination.total_pages) {
            fetchBookings(nextPage, currentStatus)
        }
    }

    fun filterByStatus(status: String?) {
        currentPage = 1
        currentStatus = status
        fetchBookings(page = 1, status = status)
    }

    /**
     * Clear notification count (call when user views notifications)
     */
    fun clearNotificationCount() {
        _notificationCount.value = 0
    }

    // ============================================================================
    // BOOKING DETAILS OPERATIONS
    // ============================================================================

    fun getBookingDetails(bookingId: Int) {
        viewModelScope.launch {
            if (_loading.value) return@launch

            _loading.value = true
            _error.value = null

            try {
                val response = repository.getBookingDetails(bookingId)

                if (response.isSuccessful) {
                    _bookingDetails.value = response.body()?.data
                } else {
                    _error.value = "Failed to load booking details: ${response.code()}"
                }

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error loading booking details"
            } finally {
                _loading.value = false
            }
        }
    }

    // ============================================================================
    // OPTIMIZED BOOKING STATUS UPDATES
    // ============================================================================

    fun updateBookingStatus(
        bookingId: Int,
        newStatus: String,
        downPayment: Double? = null,
        reason: String? = null,
        setAvailable: Boolean? = null
    ) {
        viewModelScope.launch {
            if (_isProcessing.value) return@launch

            _isProcessing.value = true
            _error.value = null

            _bookingDetails.value?.let { currentDetails ->
                val optimisticUpdate = currentDetails.copy(
                    status = newStatus,
                    down_payment = downPayment ?: currentDetails.down_payment
                )
                _bookingDetails.value = optimisticUpdate
                updateBookingInList(optimisticUpdate)
            }

            try {
                val request = UpdateBookingStatusRequest(
                    status = newStatus,
                    down_payment = downPayment,
                    reason = reason,
                    set_available = setAvailable
                )

                val response = repository.updateBookingStatus(bookingId, request)

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        _bookingDetails.value = body.data
                        _statusUpdateMessage.value = body.message
                        updateBookingInList(body.data)

                        // Recalculate notification count after status change
                        recalculateNotificationCount()
                    }
                } else {
                    _error.value = "Unable to update booking status: ${response.code()}"
                    getBookingDetails(bookingId)
                }

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error updating booking status"
                getBookingDetails(bookingId)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun recordPaymentAndReserve(bookingId: Int, amount: Double) {
        viewModelScope.launch {
            if (_isProcessing.value) return@launch

            _isProcessing.value = true
            _error.value = null

            _bookingDetails.value?.let { currentDetails ->
                val optimisticUpdate = currentDetails.copy(
                    status = "reserved",
                    down_payment = amount,
                    total_amount = amount
                )
                _bookingDetails.value = optimisticUpdate
                updateBookingInList(optimisticUpdate)
            }

            try {
                val paymentResponse = repository.recordPayment(bookingId, amount)

                if (!paymentResponse.isSuccessful) {
                    _error.value = "Failed to record payment: ${paymentResponse.code()}"
                    getBookingDetails(bookingId)
                    return@launch
                }

                val statusRequest = UpdateBookingStatusRequest(
                    status = "reserved",
                    down_payment = amount
                )
                val statusResponse = repository.updateBookingStatus(bookingId, statusRequest)

                if (statusResponse.isSuccessful) {
                    statusResponse.body()?.let { body ->
                        _bookingDetails.value = body.data
                        _statusUpdateMessage.value = "Booking reserved successfully"
                        updateBookingInList(body.data)
                        recalculateNotificationCount()
                    }
                } else {
                    _error.value = "Payment recorded but failed to reserve: ${statusResponse.code()}"
                    getBookingDetails(bookingId)
                }

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error during reservation process"
                getBookingDetails(bookingId)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun recordPaymentAndCheckIn(bookingId: Int, amount: Double) {
        viewModelScope.launch {
            if (_isProcessing.value) return@launch

            _isProcessing.value = true
            _error.value = null

            _bookingDetails.value?.let { currentDetails ->
                val optimisticUpdate = currentDetails.copy(
                    status = "checked_in",
                    total_amount = (currentDetails.down_payment ?: 0.0) + amount
                )
                _bookingDetails.value = optimisticUpdate
                updateBookingInList(optimisticUpdate)
            }

            try {
                if (amount > 0) {
                    val paymentResponse = repository.recordPayment(bookingId, amount)

                    if (!paymentResponse.isSuccessful) {
                        _error.value = "Failed to record payment: ${paymentResponse.code()}"
                        getBookingDetails(bookingId)
                        return@launch
                    }
                }

                val statusRequest = UpdateBookingStatusRequest(status = "checked_in")
                val statusResponse = repository.updateBookingStatus(bookingId, statusRequest)

                if (statusResponse.isSuccessful) {
                    statusResponse.body()?.let { body ->
                        _bookingDetails.value = body.data
                        _statusUpdateMessage.value = "Guest checked in successfully"
                        updateBookingInList(body.data)
                        recalculateNotificationCount()
                    }
                } else {
                    _error.value = if (amount > 0) {
                        "Payment recorded but failed to check in: ${statusResponse.code()}"
                    } else {
                        "Failed to check in: ${statusResponse.code()}"
                    }
                    getBookingDetails(bookingId)
                }

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error during check-in process"
                getBookingDetails(bookingId)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun updateBookingInList(updatedDetails: BookingDetails) {
        _bookings.value = _bookings.value.map { booking ->
            if (booking.id == updatedDetails.id) {
                booking.copy(
                    status = updatedDetails.status,
                    down_payment = updatedDetails.down_payment,
                    total_amount = updatedDetails.total_amount
                )
            } else {
                booking
            }
        }
    }

    /**
     * Recalculate notification count from current bookings list
     */
    private fun recalculateNotificationCount() {
        _notificationCount.value = _bookings.value.count {
            it.status.equals("pending", ignoreCase = true)
        }
    }

    // ============================================================================
    // STATE CLEARING
    // ============================================================================

    fun clearStatusMessage() {
        _statusUpdateMessage.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun clearBookingDetails() {
        _bookingDetails.value = null
    }
}