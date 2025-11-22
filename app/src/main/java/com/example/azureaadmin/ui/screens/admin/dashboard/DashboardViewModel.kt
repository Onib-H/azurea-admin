package com.example.azureaadmin.ui.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azureaadmin.data.models.AdminStatsResponse
import com.example.azureaadmin.data.models.BookingStatusCounts
import com.example.azureaadmin.data.models.DailyBookingsResponse
import com.example.azureaadmin.data.models.DailyCancellationsResponse
import com.example.azureaadmin.data.models.DailyCheckInCheckoutResponse
import com.example.azureaadmin.data.models.DailyNoShowRejectedResponse
import com.example.azureaadmin.data.models.DailyRevenueResponse
import com.example.azureaadmin.data.repository.AdminRepository
import com.example.azureaadmin.utils.FormatPrice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.absoluteValue

enum class DailyViewMode {
    DAY, WEEK, MONTH
}

data class StatWithTrend(
    val label: String,
    val value: String,
    val trend: TrendType?,
    val trendValue: String
)


class DashboardViewModel(private val repository: AdminRepository) : ViewModel() {

    // Monthly stats and pie chart (controlled by month header)
    private val _stats = MutableStateFlow<AdminStatsResponse?>(null)
    val stats: StateFlow<AdminStatsResponse?> = _stats

    private val _bookingStatusCounts = MutableStateFlow<List<BookingStatusCounts>?>(null)
    val bookingStatusCounts: StateFlow<List<BookingStatusCounts>?> = _bookingStatusCounts

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    // Daily analytics (separate navigation)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _dailyViewMode = MutableStateFlow(DailyViewMode.DAY)
    val dailyViewMode: StateFlow<DailyViewMode> = _dailyViewMode

    private val _dailyRevenue = MutableStateFlow<DailyRevenueResponse?>(null)
    val dailyRevenue: StateFlow<DailyRevenueResponse?> = _dailyRevenue

    private val _dailyBookings = MutableStateFlow<DailyBookingsResponse?>(null)
    val dailyBookings: StateFlow<DailyBookingsResponse?> = _dailyBookings

    private val _dailyCancellations = MutableStateFlow<DailyCancellationsResponse?>(null)
    val dailyCancellations: StateFlow<DailyCancellationsResponse?> = _dailyCancellations

    private val _dailyCheckInsCheckOuts = MutableStateFlow<DailyCheckInCheckoutResponse?>(null)
    val dailyCheckInsCheckOuts: StateFlow<DailyCheckInCheckoutResponse?> = _dailyCheckInsCheckOuts

    private val _dailyNoShowRejected = MutableStateFlow<DailyNoShowRejectedResponse?>(null)
    val dailyNoShowRejected: StateFlow<DailyNoShowRejectedResponse?> = _dailyNoShowRejected

    private val _previousStats = MutableStateFlow<AdminStatsResponse?>(null)
    val previousStats: StateFlow<AdminStatsResponse?> = _previousStats

    private val _statsWithTrends = MutableStateFlow<List<StatWithTrend>>(emptyList())
    val statsWithTrends: StateFlow<List<StatWithTrend>> = _statsWithTrends

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _statsLoading = MutableStateFlow(false)
    private val _bookingLoading = MutableStateFlow(false)
    private val _dailyDataLoading = MutableStateFlow(false)

    val loading: StateFlow<Boolean> = combine(
        _statsLoading,
        _bookingLoading,
        _dailyDataLoading
    ) { stats, booking, daily ->
        stats || booking || daily
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    // Monthly navigation (for stats and pie chart only)
    fun navigateToPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
        fetchDataForSelectedMonth()
    }

    fun navigateToNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
        fetchDataForSelectedMonth()
    }

    fun fetchDataForSelectedMonth() {
        val month = _selectedMonth.value
        fetchStats(month.monthValue, month.year)
        fetchPreviousMonthStats(month.monthValue, month.year)
        fetchBookingStatusCounts(month.monthValue, month.year)
    }

    // Daily navigation (for daily analytics only)
    fun setDailyViewMode(mode: DailyViewMode) {
        _dailyViewMode.value = mode
    }

    fun navigateDailyPrevious() {
        when (_dailyViewMode.value) {
            DailyViewMode.DAY -> _selectedDate.value = _selectedDate.value.minusDays(1)
            DailyViewMode.WEEK -> _selectedDate.value = _selectedDate.value.minusWeeks(1)
            DailyViewMode.MONTH -> _selectedDate.value = _selectedDate.value.minusMonths(1)
        }
        fetchDailyData()
    }

    fun navigateDailyNext() {
        when (_dailyViewMode.value) {
            DailyViewMode.DAY -> _selectedDate.value = _selectedDate.value.plusDays(1)
            DailyViewMode.WEEK -> _selectedDate.value = _selectedDate.value.plusWeeks(1)
            DailyViewMode.MONTH -> _selectedDate.value = _selectedDate.value.plusMonths(1)
        }
        fetchDailyData()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        fetchDailyData()
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        fetchDataForSelectedMonth()
    }


    fun fetchDailyData() {
        viewModelScope.launch {
            _dailyDataLoading.value = true
            try {
                val revenueResp = repository.getDailyRevenue()
                val bookingsResp = repository.getDailyBookings()
                val cancellationsResp = repository.getDailyCancellations()
                val checkInsResp = repository.getDailyCheckinsCheckouts()
                val noShowResp = repository.getDailyNoShowRejected()

                logResponse("Revenue", revenueResp)
                logResponse("Bookings", bookingsResp)
                logResponse("Cancellations", cancellationsResp)
                logResponse("Check-ins/outs", checkInsResp)
                logResponse("No-shows/rejected", noShowResp)

                if (revenueResp.isSuccessful) _dailyRevenue.value = revenueResp.body()
                if (bookingsResp.isSuccessful) _dailyBookings.value = bookingsResp.body()
                if (cancellationsResp.isSuccessful) _dailyCancellations.value = cancellationsResp.body()
                if (checkInsResp.isSuccessful) _dailyCheckInsCheckOuts.value = checkInsResp.body()
                if (noShowResp.isSuccessful) _dailyNoShowRejected.value = noShowResp.body()

            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Daily data error", e)
            } finally {
                _dailyDataLoading.value = false
            }
        }
    }

    private fun <T> logResponse(label: String, response: retrofit2.Response<T>) {
        if (response.isSuccessful) {
            android.util.Log.d("DashboardVM", "$label Response: ${response.body()}")
        } else {
            android.util.Log.w("DashboardVM", "$label Failed: ${response.code()} - ${response.errorBody()?.string()}")
        }
    }





    private fun fetchStats(month: Int, year: Int) {
        viewModelScope.launch {
            _statsLoading.value = true
            try {
                val response = repository.getStats(month, year)
                if (response.isSuccessful && response.body() != null) {
                    _stats.value = response.body()
                    calculateTrends()
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch stats: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Stats error: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _statsLoading.value = false
            }
        }
    }

    fun fetchBookingStatusCounts(month: Int, year: Int) {
        viewModelScope.launch {
            _bookingLoading.value = true
            try {
                val response = repository.getBookingStatusCounts(month, year)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val counts = repository.mapBookingStatusToList(body)
                        if (counts.isNotEmpty()) {
                            _bookingStatusCounts.value = counts
                            _error.value = null
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Booking error: ${e.localizedMessage}"
            } finally {
                _bookingLoading.value = false
            }
        }
    }

    private fun fetchPreviousMonthStats(month: Int, year: Int) {
        viewModelScope.launch {
            try {
                val previousMonth = if (month == 1) {
                    Pair(12, year - 1)
                } else {
                    Pair(month - 1, year)
                }

                val response = repository.getStats(previousMonth.first, previousMonth.second)
                if (response.isSuccessful && response.body() != null) {
                    _previousStats.value = response.body()
                    calculateTrends() // Calculate trends after fetching previous stats
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Previous stats error", e)
            }
        }
    }

    private fun calculateTrends() {
        val current = _stats.value ?: return
        val previous = _previousStats.value

        val trends = mutableListOf<StatWithTrend>()

        // Active Bookings Trend
        val activeBookingsTrend = calculateTrend(
            current = current.active_bookings.toDouble(),
            previous = previous?.active_bookings?.toDouble()
        )
        trends.add(
            StatWithTrend(
                label = "Active Bookings",
                value = current.active_bookings.toString(),
                trend = null,
                trendValue = ""
            )
        )

        // Pending Bookings Trend
        val pendingBookingsTrend = calculateTrend(
            current = current.pending_bookings.toDouble(),
            previous = previous?.pending_bookings?.toDouble()
        )
        trends.add(
            StatWithTrend(
                label = "Pending Bookings",
                value = current.pending_bookings.toString(),
                trend = null,
                trendValue = ""            )
        )

        // Total Bookings Trend
        val totalBookingsTrend = calculateTrend(
            current = current.total_bookings.toDouble(),
            previous = previous?.total_bookings?.toDouble()
        )
        trends.add(
            StatWithTrend(
                label = "Total Bookings",
                value = current.total_bookings.toString(),
                trend = totalBookingsTrend.first,
                trendValue = totalBookingsTrend.second
            )
        )

        // Revenue Trend - Use the raw 'revenue' field (Double), not formatted_revenue
        val revenueTrend = calculateTrend(
            current = current.revenue,
            previous = previous?.revenue
        )
        trends.add(
            StatWithTrend(
                label = "Monthly Revenue",
                value = FormatPrice.formatRevenue(current.revenue.toString()), // Use the formatted string for display
                trend = revenueTrend.first,
                trendValue = revenueTrend.second
            )
        )

        _statsWithTrends.value = trends
    }

    private fun calculateTrend(current: Double, previous: Double?): Pair<TrendType, String> {
        if (previous == null || previous == 0.0) {
            return Pair(TrendType.NEUTRAL, "0%")
        }

        val percentageChange = ((current - previous) / previous) * 100

        val trend = when {
            percentageChange > 0.5 -> TrendType.UP
            percentageChange < -0.5 -> TrendType.DOWN
            else -> TrendType.NEUTRAL
        }

        // Format trend value with max 2 characters after sign (e.g., "+11%", "-8%", "+2%")
        val trendValue = when {
            percentageChange.absoluteValue >= 100 -> {
                // For values >= 100, show without decimal
                if (percentageChange >= 0) "+${percentageChange.toInt()}%"
                else "${percentageChange.toInt()}%"
            }
            percentageChange.absoluteValue >= 10 -> {
                // For 10-99, show whole number (e.g., "+11%", "-25%")
                if (percentageChange >= 0) "+${percentageChange.toInt()}%"
                else "${percentageChange.toInt()}%"
            }
            else -> {
                // For < 10, show one decimal if needed (e.g., "+2%", "+8.5%")
                val rounded = (percentageChange * 10).toInt() / 10.0
                if (rounded == rounded.toInt().toDouble()) {
                    // No decimal needed (e.g., 2.0 -> "2")
                    if (percentageChange >= 0) "+${rounded.toInt()}%"
                    else "${rounded.toInt()}%"
                } else {
                    // Keep one decimal (e.g., 8.5)
                    if (percentageChange >= 0) "+${"%.1f".format(rounded)}%"
                    else "${"%.1f".format(rounded)}%"
                }
            }
        }

        return Pair(trend, trendValue)
    }


}