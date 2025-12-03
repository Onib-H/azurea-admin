package com.example.azureaadmin.ui.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azureaadmin.data.models.AdminStatsResponse
import com.example.azureaadmin.data.models.AreaBookingResponse
import com.example.azureaadmin.data.models.AreaRevenueResponse
import com.example.azureaadmin.data.models.BookingStatusCounts
import com.example.azureaadmin.data.models.DailyBookingsResponse
import com.example.azureaadmin.data.models.DailyCancellationsResponse
import com.example.azureaadmin.data.models.DailyCheckInCheckoutResponse
import com.example.azureaadmin.data.models.DailyNoShowRejectedResponse
import com.example.azureaadmin.data.models.DailyRevenueResponse
import com.example.azureaadmin.data.models.MonthlyReportResponse
import com.example.azureaadmin.data.models.RoomBookingResponse
import com.example.azureaadmin.data.models.RoomRevenueResponse
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

data class DailyAnalyticsTrends(
    val revenueTrend: Pair<TrendType, String>,
    val bookingsTrend: Pair<TrendType, String>,
    val cancellationsTrend: Pair<TrendType, String>,
    val checkInsTrend: Pair<TrendType, String>,
    val checkOutsTrend: Pair<TrendType, String>,
    val noShowsTrend: Pair<TrendType, String>,
    val rejectedTrend: Pair<TrendType, String>
)

class DashboardViewModel(private val repository: AdminRepository) : ViewModel() {

    // Monthly stats and pie chart (controlled by month header)
    private val _stats = MutableStateFlow<AdminStatsResponse?>(null)
    val stats: StateFlow<AdminStatsResponse?> = _stats

    private val _bookingStatusCounts = MutableStateFlow<List<BookingStatusCounts>?>(null)
    val bookingStatusCounts: StateFlow<List<BookingStatusCounts>?> = _bookingStatusCounts

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    // Room and Area Revenue data
    private val _roomRevenue = MutableStateFlow<RoomRevenueResponse?>(null)
    val roomRevenue: StateFlow<RoomRevenueResponse?> = _roomRevenue

    private val _roomBookings = MutableStateFlow<RoomBookingResponse?>(null)
    val roomBookings: StateFlow<RoomBookingResponse?> = _roomBookings

    private val _areaRevenue = MutableStateFlow<AreaRevenueResponse?>(null)
    val areaRevenue: StateFlow<AreaRevenueResponse?> = _areaRevenue

    private val _areaBookings = MutableStateFlow<AreaBookingResponse?>(null)
    val areaBookings: StateFlow<AreaBookingResponse?> = _areaBookings

    // Monthly Report for PDF generation
    private val _monthlyReport = MutableStateFlow<MonthlyReportResponse?>(null)
    val monthlyReport: StateFlow<MonthlyReportResponse?> = _monthlyReport

    private val _reportLoading = MutableStateFlow(false)
    val reportLoading: StateFlow<Boolean> = _reportLoading

    private val _reportError = MutableStateFlow<String?>(null)
    val reportError: StateFlow<String?> = _reportError

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

    private val _previousDailyRevenue = MutableStateFlow<DailyRevenueResponse?>(null)
    private val _previousDailyBookings = MutableStateFlow<DailyBookingsResponse?>(null)
    private val _previousDailyCancellations = MutableStateFlow<DailyCancellationsResponse?>(null)
    private val _previousDailyCheckInsCheckOuts = MutableStateFlow<DailyCheckInCheckoutResponse?>(null)
    private val _previousDailyNoShowRejected = MutableStateFlow<DailyNoShowRejectedResponse?>(null)

    private val _dailyAnalyticsTrends = MutableStateFlow<DailyAnalyticsTrends?>(null)
    val dailyAnalyticsTrends: StateFlow<DailyAnalyticsTrends?> = _dailyAnalyticsTrends

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
        fetchRoomAndAreaRevenue(month.monthValue, month.year)
    }

    private fun fetchRoomAndAreaRevenue(month: Int, year: Int) {
        viewModelScope.launch {
            try {
                // Fetch Room Revenue and Bookings
                val roomRevenueResp = repository.getRoomRevenue(month, year)
                val roomBookingsResp = repository.getRoomBookings(month, year)

                if (roomRevenueResp.isSuccessful) _roomRevenue.value = roomRevenueResp.body()
                if (roomBookingsResp.isSuccessful) _roomBookings.value = roomBookingsResp.body()

                // Fetch Area Revenue and Bookings
                val areaRevenueResp = repository.getAreaRevenue(month, year)
                val areaBookingsResp = repository.getAreaBookings(month, year)

                if (areaRevenueResp.isSuccessful) _areaRevenue.value = areaRevenueResp.body()
                if (areaBookingsResp.isSuccessful) _areaBookings.value = areaBookingsResp.body()

            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Room/Area revenue error", e)
            }
        }
    }

    // Fetch monthly report for PDF generation
    fun fetchMonthlyReport() {
        viewModelScope.launch {
            _reportLoading.value = true
            _reportError.value = null
            try {
                val month = _selectedMonth.value
                val response = repository.getMonthlyReport(month.monthValue, month.year)

                if (response.isSuccessful && response.body() != null) {
                    _monthlyReport.value = response.body()
                } else {
                    _reportError.value = "Failed to fetch report: ${response.code()}"
                }
            } catch (e: Exception) {
                _reportError.value = "Report error: ${e.localizedMessage ?: "Unknown error"}"
                android.util.Log.e("DashboardVM", "Monthly report error", e)
            } finally {
                _reportLoading.value = false
            }
        }
    }

    fun clearReportError() {
        _reportError.value = null
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
                val month = _selectedDate.value.monthValue
                val year = _selectedDate.value.year

                // Fetch current month data
                val revenueResp = repository.getDailyRevenue(month, year)
                val bookingsResp = repository.getDailyBookings(month, year)
                val cancellationsResp = repository.getDailyCancellations(month, year)
                val checkInsResp = repository.getDailyCheckinsCheckouts(month, year)
                val noShowResp = repository.getDailyNoShowRejected(month, year)

                if (revenueResp.isSuccessful) _dailyRevenue.value = revenueResp.body()
                if (bookingsResp.isSuccessful) _dailyBookings.value = bookingsResp.body()
                if (cancellationsResp.isSuccessful) _dailyCancellations.value = cancellationsResp.body()
                if (checkInsResp.isSuccessful) _dailyCheckInsCheckOuts.value = checkInsResp.body()
                if (noShowResp.isSuccessful) _dailyNoShowRejected.value = noShowResp.body()

                // Fetch previous month for trend comparison
                fetchPreviousDailyData(month, year)

            } catch (e: Exception) {
                android.util.Log.e("DashboardVM", "Daily data error", e)
            } finally {
                _dailyDataLoading.value = false
            }
        }
    }

    private suspend fun fetchPreviousDailyData(currentMonth: Int, currentYear: Int) {
        try {
            val (prevMonth, prevYear) = if (currentMonth == 1) {
                Pair(12, currentYear - 1)
            } else {
                Pair(currentMonth - 1, currentYear)
            }

            val prevRevenueResp = repository.getDailyRevenue(prevMonth, prevYear)
            val prevBookingsResp = repository.getDailyBookings(prevMonth, prevYear)
            val prevCancellationsResp = repository.getDailyCancellations(prevMonth, prevYear)
            val prevCheckInsResp = repository.getDailyCheckinsCheckouts(prevMonth, prevYear)
            val prevNoShowResp = repository.getDailyNoShowRejected(prevMonth, prevYear)

            if (prevRevenueResp.isSuccessful) _previousDailyRevenue.value = prevRevenueResp.body()
            if (prevBookingsResp.isSuccessful) _previousDailyBookings.value = prevBookingsResp.body()
            if (prevCancellationsResp.isSuccessful) _previousDailyCancellations.value = prevCancellationsResp.body()
            if (prevCheckInsResp.isSuccessful) _previousDailyCheckInsCheckOuts.value = prevCheckInsResp.body()
            if (prevNoShowResp.isSuccessful) _previousDailyNoShowRejected.value = prevNoShowResp.body()

            // Calculate trends
            calculateDailyAnalyticsTrends()

        } catch (e: Exception) {
            android.util.Log.e("DashboardVM", "Previous daily data error", e)
        }
    }

    private fun calculateDailyAnalyticsTrends() {
        val currentRevenue = _dailyRevenue.value
        val previousRevenue = _previousDailyRevenue.value
        val currentBookings = _dailyBookings.value
        val previousBookings = _previousDailyBookings.value
        val currentCancellations = _dailyCancellations.value
        val previousCancellations = _previousDailyCancellations.value
        val currentCheckIns = _dailyCheckInsCheckOuts.value
        val previousCheckIns = _previousDailyCheckInsCheckOuts.value
        val currentNoShow = _dailyNoShowRejected.value
        val previousNoShow = _previousDailyNoShowRejected.value

        // Calculate monthly totals
        val currMonthRevenue = currentRevenue?.data?.take(currentRevenue.days_in_month)?.sum() ?: 0.0
        val prevMonthRevenue = previousRevenue?.data?.take(previousRevenue.days_in_month)?.sum() ?: 0.0

        val currMonthBookings = currentBookings?.data?.take(currentBookings.days_in_month)?.sum() ?: 0
        val prevMonthBookings = previousBookings?.data?.take(previousBookings.days_in_month)?.sum() ?: 0

        val currMonthCancellations = currentCancellations?.data?.take(currentCancellations.days_in_month)?.sum() ?: 0
        val prevMonthCancellations = previousCancellations?.data?.take(previousCancellations.days_in_month)?.sum() ?: 0

        val currMonthCheckIns = currentCheckIns?.checkins?.take(currentCheckIns.days_in_month)?.sum() ?: 0
        val prevMonthCheckIns = previousCheckIns?.checkins?.take(previousCheckIns.days_in_month)?.sum() ?: 0

        val currMonthCheckOuts = currentCheckIns?.checkouts?.take(currentCheckIns.days_in_month)?.sum() ?: 0
        val prevMonthCheckOuts = previousCheckIns?.checkouts?.take(previousCheckIns.days_in_month)?.sum() ?: 0

        val currMonthNoShows = currentNoShow?.no_shows?.take(currentNoShow.days_in_month)?.sum() ?: 0
        val prevMonthNoShows = previousNoShow?.no_shows?.take(previousNoShow.days_in_month)?.sum() ?: 0

        val currMonthRejected = currentNoShow?.rejected?.take(currentNoShow.days_in_month)?.sum() ?: 0
        val prevMonthRejected = previousNoShow?.rejected?.take(previousNoShow.days_in_month)?.sum() ?: 0

        _dailyAnalyticsTrends.value = DailyAnalyticsTrends(
            revenueTrend = calculateTrend(currMonthRevenue, prevMonthRevenue),
            bookingsTrend = calculateTrend(currMonthBookings.toDouble(), prevMonthBookings.toDouble()),
            cancellationsTrend = calculateTrend(currMonthCancellations.toDouble(), prevMonthCancellations.toDouble()),
            checkInsTrend = calculateTrend(currMonthCheckIns.toDouble(), prevMonthCheckIns.toDouble()),
            checkOutsTrend = calculateTrend(currMonthCheckOuts.toDouble(), prevMonthCheckOuts.toDouble()),
            noShowsTrend = calculateTrend(currMonthNoShows.toDouble(), prevMonthNoShows.toDouble()),
            rejectedTrend = calculateTrend(currMonthRejected.toDouble(), prevMonthRejected.toDouble())
        )
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
                    calculateTrends()
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

        trends.add(
            StatWithTrend(
                label = "Active Bookings",
                value = current.active_bookings.toString(),
                trend = null,
                trendValue = ""
            )
        )

        trends.add(
            StatWithTrend(
                label = "Pending Bookings",
                value = current.pending_bookings.toString(),
                trend = null,
                trendValue = ""
            )
        )

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

        val revenueTrend = calculateTrend(
            current = current.revenue,
            previous = previous?.revenue
        )
        trends.add(
            StatWithTrend(
                label = "Monthly Revenue",
                value = FormatPrice.formatRevenue(current.revenue.toString()),
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

        val trendValue = when {
            percentageChange.absoluteValue >= 100 -> {
                if (percentageChange >= 0) "+${percentageChange.toInt()}%"
                else "${percentageChange.toInt()}%"
            }
            percentageChange.absoluteValue >= 10 -> {
                if (percentageChange >= 0) "+${percentageChange.toInt()}%"
                else "${percentageChange.toInt()}%"
            }
            else -> {
                val rounded = (percentageChange * 10).toInt() / 10.0
                if (rounded == rounded.toInt().toDouble()) {
                    if (percentageChange >= 0) "+${rounded.toInt()}%"
                    else "${rounded.toInt()}%"
                } else {
                    if (percentageChange >= 0) "+${"%.1f".format(rounded)}%"
                    else "${"%.1f".format(rounded)}%"
                }
            }
        }

        return Pair(trend, trendValue)
    }
}