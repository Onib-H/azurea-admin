package com.example.azureaadmin.ui.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azureaadmin.data.models.AdminStatsResponse
import com.example.azureaadmin.data.models.BookingStatusCounts
import com.example.azureaadmin.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: AdminRepository) : ViewModel() {

    private val _stats = MutableStateFlow<AdminStatsResponse?>(null)
    val stats: StateFlow<AdminStatsResponse?> = _stats

    private val _bookingStatusCounts = MutableStateFlow<List<BookingStatusCounts>?>(null)
    val bookingStatusCounts: StateFlow<List<BookingStatusCounts>?> = _bookingStatusCounts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _statsLoading = MutableStateFlow(false)
    val statsLoading: StateFlow<Boolean> = _statsLoading

    private val _bookingLoading = MutableStateFlow(false)
    val bookingLoading: StateFlow<Boolean> = _bookingLoading

    val loading: StateFlow<Boolean> = combine(_statsLoading, _bookingLoading) { stats, booking ->
        stats || booking
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun fetchStats() {
        viewModelScope.launch {
            _statsLoading.value = true
            try {
                val response = repository.getStats()
                if (response.isSuccessful && response.body() != null) {
                    _stats.value = response.body()
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch stats: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Stats error: ${e.localizedMessage ?: "Unknown error"}"
                android.util.Log.e("DashboardVM", "Stats fetch error", e)
            } finally {
                _statsLoading.value = false
            }
        }
    }

    fun fetchBookingStatusCounts(month: Int = -1, year: Int = -1) {
        viewModelScope.launch {
            _bookingLoading.value = true
            try {
                val response = if (month == -1 || year == -1) {
                    repository.getBookingStatusCountsForCurrentMonth()
                } else {
                    repository.getBookingStatusCounts(month, year)
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val counts = repository.mapBookingStatusToList(body)
                        if (counts.isNotEmpty()) {
                            _bookingStatusCounts.value = counts
                            _error.value = null
                        } else {
                            _error.value = "No booking data available"
                        }
                    } else {
                        _error.value = "Booking response is empty"
                    }
                } else {
                    _error.value = "Failed to fetch booking status: ${response.code()}"
                    android.util.Log.e("DashboardVM", "Booking API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.value = "Booking error: ${e.localizedMessage ?: "Unknown error"}"
                android.util.Log.e("DashboardVM", "Booking fetch exception", e)
            } finally {
                _bookingLoading.value = false
            }
        }
    }
}