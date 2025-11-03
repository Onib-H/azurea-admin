package com.example.azureaadmin.ui.screens.admin.bookings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azureaadmin.data.models.BookingData
import com.example.azureaadmin.data.models.BookingDetails
import com.example.azureaadmin.data.models.Pagination
import com.example.azureaadmin.data.models.UpdateBookingStatusRequest
import com.example.azureaadmin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingViewModel(private val repository: AdminRepository) : ViewModel() {

    private val _bookings = MutableStateFlow<List<BookingData>>(emptyList())
    val bookings: StateFlow<List<BookingData>> = _bookings

    private val _pagination = MutableStateFlow<Pagination?>(null)
    val pagination: StateFlow<Pagination?> = _pagination

    private val _bookingDetails = MutableStateFlow<BookingDetails?>(null)
    val bookingDetails: StateFlow<BookingDetails?> = _bookingDetails

    private val _statusUpdateMessage = MutableStateFlow<String?>(null)
    val statusUpdateMessage: StateFlow<String?> = _statusUpdateMessage

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private var currentPage = 1
    private var currentStatus: String? = null

    fun fetchBookings(page: Int = 1, status: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getBookings(page = page, pageSize = 9, status = status)
                if (response.isSuccessful) {
                    val data = response.body()
                    _bookings.value = data?.data ?: emptyList()
                    _pagination.value = data?.pagination
                    _error.value = null
                    currentPage = data?.pagination?.current_page ?: 1
                    currentStatus = status
                } else {
                    _error.value = "Failed to fetch bookings: ${response.code()}"
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
        val nextPage = (pagination.value?.current_page ?: 1) + 1
        val totalPages = pagination.value?.total_pages ?: 1
        if (nextPage <= totalPages) {
            fetchBookings(nextPage, currentStatus)
        }
    }

    fun filterByStatus(status: String?) {
        fetchBookings(page = 1, status = status)
    }

    fun getBookingDetails(bookingId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = repository.getBookingDetails(bookingId)
                if (response.isSuccessful) {
                    val details = response.body()?.data
                    _bookingDetails.value = details
                    _error.value = null

                    // ✅ Log what we received from backend
                    Log.d("BookingViewModel", "📥 Received booking details:")
                    Log.d("BookingViewModel", "   Booking ID: ${details?.id}")
                    Log.d("BookingViewModel", "   Status: ${details?.status}")
                    Log.d("BookingViewModel", "   Down Payment: ${details?.down_payment}")
                    Log.d("BookingViewModel", "   Total Price: ${details?.total_price}")
                } else {
                    _error.value = "Failed to load booking details: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error loading booking details"
                Log.e("BookingViewModel", "Error getting booking details", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateBookingStatus(
        bookingId: Int,
        newStatus: String,
        downPayment: Double? = null,
        reason: String? = null,
        setAvailable: Boolean? = null
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // ✅ Log what we're sending to backend
                Log.d("BookingViewModel", "📤 Updating booking status:")
                Log.d("BookingViewModel", "   Booking ID: $bookingId")
                Log.d("BookingViewModel", "   New Status: $newStatus")
                Log.d("BookingViewModel", "   Down Payment: $downPayment")
                Log.d("BookingViewModel", "   Reason: $reason")
                Log.d("BookingViewModel", "   Set Available: $setAvailable")

                val request = UpdateBookingStatusRequest(
                    status = newStatus,
                    down_payment = downPayment,
                    reason = reason,
                    set_available = setAvailable
                )

                val response = repository.updateBookingStatus(bookingId, request)
                if (response.isSuccessful) {
                    val updatedData = response.body()?.data
                    _statusUpdateMessage.value = response.body()?.message
                    _bookingDetails.value = updatedData
                    _error.value = null

                    // ✅ Log what backend returned
                    Log.d("BookingViewModel", "✅ Backend response:")
                    Log.d("BookingViewModel", "   Message: ${response.body()?.message}")
                    Log.d("BookingViewModel", "   Updated Down Payment: ${updatedData?.down_payment}")
                    Log.d("BookingViewModel", "   Updated Status: ${updatedData?.status}")
                } else {
                    _error.value = "Failed to update status: ${response.code()}"
                    Log.e("BookingViewModel", "❌ Update failed with code: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error updating booking status"
                Log.e("BookingViewModel", "❌ Exception during update", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearStatusMessage() {
        _statusUpdateMessage.value = null
    }

    fun clearError() {
        _error.value = null
    }

    // ✅ New function to record payment then check in
    fun recordPaymentAndCheckIn(bookingId: Int, amount: Double) {
        viewModelScope.launch {
            _loading.value = true
            try {
                Log.d("BookingViewModel", "📝 Recording payment of $amount for booking $bookingId")

                // First, record the payment using the record_payment endpoint
                val paymentResponse = repository.recordPayment(bookingId, amount)

                if (paymentResponse.isSuccessful) {
                    Log.d("BookingViewModel", "✅ Payment recorded successfully")

                    // Then update status to checked_in
                    val statusResponse = repository.updateBookingStatus(
                        bookingId,
                        UpdateBookingStatusRequest(status = "checked_in")
                    )

                    if (statusResponse.isSuccessful) {
                        _statusUpdateMessage.value = "Guest checked in successfully"
                        _bookingDetails.value = statusResponse.body()?.data
                        _error.value = null
                        Log.d("BookingViewModel", "✅ Status updated to checked_in")
                    } else {
                        _error.value = "Payment recorded but failed to check in: ${statusResponse.code()}"
                        Log.e("BookingViewModel", "❌ Check-in failed: ${statusResponse.code()}")
                    }
                } else {
                    _error.value = "Failed to record payment: ${paymentResponse.code()}"
                    Log.e("BookingViewModel", "❌ Payment recording failed: ${paymentResponse.code()}")
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error during check-in process"
                Log.e("BookingViewModel", "❌ Exception during check-in", e)
            } finally {
                _loading.value = false
            }
        }
    }
}