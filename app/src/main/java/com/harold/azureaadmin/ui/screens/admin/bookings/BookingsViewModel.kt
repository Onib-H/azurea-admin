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
                    _bookings.value = body.data
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
    // BOOKING STATUS UPDATES
    // ============================================================================

    fun updateBookingStatus(
        bookingId: Int,
        newStatus: String,
        downPayment: Double? = null,
        reason: String? = null,
        setAvailable: Boolean? = null
    ) {
        viewModelScope.launch {
            _error.value = null

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

                        // Update the booking in the list optimistically
                        updateBookingInList(body.data)
                    }
                } else {
                    _error.value = "Unable to update booking status: ${response.code()}"
                }

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error updating booking status"
            }
        }
    }

    fun recordPaymentAndCheckIn(bookingId: Int, amount: Double) {
        viewModelScope.launch {
            _error.value = null

            try {
                // Step 1: Record payment
                val paymentResponse = repository.recordPayment(bookingId, amount)

                if (!paymentResponse.isSuccessful) {
                    _error.value = "Failed to record payment: ${paymentResponse.code()}"
                    return@launch
                }

                // Step 2: Update status to checked_in
                val statusRequest = UpdateBookingStatusRequest(status = "checked_in")
                val statusResponse = repository.updateBookingStatus(bookingId, statusRequest)

                if (statusResponse.isSuccessful) {
                    statusResponse.body()?.let { body ->
                        _bookingDetails.value = body.data
                        _statusUpdateMessage.value = "Guest checked in successfully"

                        // Update the booking in the list optimistically
                        updateBookingInList(body.data)
                    }
                } else {
                    _error.value = "Payment recorded but failed to check in: ${statusResponse.code()}"
                }

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error during check-in process"
            }
        }
    }

    // Optimistically update booking in the list without refetching
    private fun updateBookingInList(updatedDetails: BookingDetails) {
        _bookings.value = _bookings.value.map { booking ->
            if (booking.id == updatedDetails.id) {
                // Map BookingDetails to BookingData
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