package com.harold.azureaadmin.ui.screens.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harold.azureaadmin.data.models.*
import com.harold.azureaadmin.data.repository.AdminRepository
import com.harold.azureaadmin.utils.FormatPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.absoluteValue

enum class DailyViewMode { DAY, WEEK, MONTH }

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

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    // ============================================================================
    // MONTHLY DATA (Stats, Pie Chart, Revenue/Bookings)
    // ============================================================================

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    private val _stats = MutableStateFlow<AdminStatsResponse?>(null)
    val stats: StateFlow<AdminStatsResponse?> = _stats

    private val _previousStats = MutableStateFlow<AdminStatsResponse?>(null)
    val previousStats: StateFlow<AdminStatsResponse?> = _previousStats

    private val _statsWithTrends = MutableStateFlow<List<StatWithTrend>>(emptyList())
    val statsWithTrends: StateFlow<List<StatWithTrend>> = _statsWithTrends

    private val _bookingStatusCounts = MutableStateFlow<List<BookingStatusCounts>?>(null)
    val bookingStatusCounts: StateFlow<List<BookingStatusCounts>?> = _bookingStatusCounts

    private val _roomRevenue = MutableStateFlow<RoomRevenueResponse?>(null)
    val roomRevenue: StateFlow<RoomRevenueResponse?> = _roomRevenue

    private val _roomBookings = MutableStateFlow<RoomBookingResponse?>(null)
    val roomBookings: StateFlow<RoomBookingResponse?> = _roomBookings

    private val _areaRevenue = MutableStateFlow<AreaRevenueResponse?>(null)
    val areaRevenue: StateFlow<AreaRevenueResponse?> = _areaRevenue

    private val _areaBookings = MutableStateFlow<AreaBookingResponse?>(null)
    val areaBookings: StateFlow<AreaBookingResponse?> = _areaBookings

    // ============================================================================
    // MONTHLY REPORT (PDF Generation)
    // ============================================================================

    private val _monthlyReport = MutableStateFlow<MonthlyReportResponse?>(null)
    val monthlyReport: StateFlow<MonthlyReportResponse?> = _monthlyReport

    private val _reportLoading = MutableStateFlow(false)
    val reportLoading: StateFlow<Boolean> = _reportLoading

    private val _reportError = MutableStateFlow<String?>(null)
    val reportError: StateFlow<String?> = _reportError

    // ============================================================================
    // DAILY ANALYTICS
    // ============================================================================

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

    private val _previousDailyRevenue = MutableStateFlow<DailyRevenueResponse?>(null)
    private val _previousDailyBookings = MutableStateFlow<DailyBookingsResponse?>(null)
    private val _previousDailyCancellations = MutableStateFlow<DailyCancellationsResponse?>(null)
    private val _previousDailyCheckInsCheckOuts = MutableStateFlow<DailyCheckInCheckoutResponse?>(null)
    private val _previousDailyNoShowRejected = MutableStateFlow<DailyNoShowRejectedResponse?>(null)

    private val _dailyAnalyticsTrends = MutableStateFlow<DailyAnalyticsTrends?>(null)
    val dailyAnalyticsTrends: StateFlow<DailyAnalyticsTrends?> = _dailyAnalyticsTrends

    // ============================================================================
    // LOADING & ERROR STATES
    // ============================================================================

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

    // ============================================================================
    // MONTHLY NAVIGATION & DATA FETCHING
    // ============================================================================

    fun navigateToPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
        fetchDataForSelectedMonth()
    }

    fun navigateToNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
        fetchDataForSelectedMonth()
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        fetchDataForSelectedMonth()
    }

    fun fetchDataForSelectedMonth() {
        val month = _selectedMonth.value

        viewModelScope.launch {
            if (_statsLoading.value || _bookingLoading.value) return@launch

            _statsLoading.value = true
            _bookingLoading.value = true

            try {
                // Calculate previous month for trend comparison
                val (prevMonth, prevYear) = if (month.monthValue == 1) {
                    12 to (month.year - 1)
                } else {
                    (month.monthValue - 1) to month.year
                }

                // Parallel API calls
                val statsDeferred = async {
                    repository.getStats(month.monthValue, month.year)
                }
                val prevStatsDeferred = async {
                    repository.getStats(prevMonth, prevYear)
                }
                val bookingCountsDeferred = async {
                    repository.getBookingStatusCounts(month.monthValue, month.year)
                }
                val roomRevenueDeferred = async {
                    repository.getRoomRevenue(month.monthValue, month.year)
                }
                val roomBookingsDeferred = async {
                    repository.getRoomBookings(month.monthValue, month.year)
                }
                val areaRevenueDeferred = async {
                    repository.getAreaRevenue(month.monthValue, month.year)
                }
                val areaBookingsDeferred = async {
                    repository.getAreaBookings(month.monthValue, month.year)
                }

                // Await all results
                val statsResp = statsDeferred.await()
                val prevStatsResp = prevStatsDeferred.await()
                val bookingCountsResp = bookingCountsDeferred.await()
                val roomRevenueResp = roomRevenueDeferred.await()
                val roomBookingsResp = roomBookingsDeferred.await()
                val areaRevenueResp = areaRevenueDeferred.await()
                val areaBookingsResp = areaBookingsDeferred.await()

                // Assign results
                _stats.value = statsResp.body()
                _previousStats.value = prevStatsResp.body()
                _bookingStatusCounts.value = bookingCountsResp.body()?.let {
                    repository.mapBookingStatusToList(it)
                }
                _roomRevenue.value = roomRevenueResp.body()
                _roomBookings.value = roomBookingsResp.body()
                _areaRevenue.value = areaRevenueResp.body()
                _areaBookings.value = areaBookingsResp.body()

                // Calculate trends after data is loaded
                calculateTrends()
                _error.value = null

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error loading dashboard data"
            } finally {
                _statsLoading.value = false
                _bookingLoading.value = false
            }
        }
    }

    // ============================================================================
    // MONTHLY REPORT
    // ============================================================================

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
            } finally {
                _reportLoading.value = false
            }
        }
    }

    fun clearReportError() {
        _reportError.value = null
    }

    // ============================================================================
    // DAILY ANALYTICS NAVIGATION & DATA FETCHING
    // ============================================================================

    fun setDailyViewMode(mode: DailyViewMode) {
        _dailyViewMode.value = mode
    }

    fun navigateDailyPrevious() {
        _selectedDate.value = when (_dailyViewMode.value) {
            DailyViewMode.DAY -> _selectedDate.value.minusDays(1)
            DailyViewMode.WEEK -> _selectedDate.value.minusWeeks(1)
            DailyViewMode.MONTH -> _selectedDate.value.minusMonths(1)
        }
        fetchDailyData()
    }

    fun navigateDailyNext() {
        _selectedDate.value = when (_dailyViewMode.value) {
            DailyViewMode.DAY -> _selectedDate.value.plusDays(1)
            DailyViewMode.WEEK -> _selectedDate.value.plusWeeks(1)
            DailyViewMode.MONTH -> _selectedDate.value.plusMonths(1)
        }
        fetchDailyData()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        fetchDailyData()
    }

    fun fetchDailyData() {
        viewModelScope.launch {
            if (_dailyDataLoading.value) return@launch

            _dailyDataLoading.value = true

            try {
                val month = _selectedDate.value.monthValue
                val year = _selectedDate.value.year

                // Parallel API calls for current month
                val revenueDeferred = async {
                    repository.getDailyRevenue(month, year)
                }
                val bookingsDeferred = async {
                    repository.getDailyBookings(month, year)
                }
                val cancellationsDeferred = async {
                    repository.getDailyCancellations(month, year)
                }
                val checkInsDeferred = async {
                    repository.getDailyCheckinsCheckouts(month, year)
                }
                val noShowDeferred = async {
                    repository.getDailyNoShowRejected(month, year)
                }

                // Await results
                val revenueResp = revenueDeferred.await()
                val bookingsResp = bookingsDeferred.await()
                val cancellationsResp = cancellationsDeferred.await()
                val checkInsResp = checkInsDeferred.await()
                val noShowResp = noShowDeferred.await()

                // Assign current month data
                _dailyRevenue.value = revenueResp.body()
                _dailyBookings.value = bookingsResp.body()
                _dailyCancellations.value = cancellationsResp.body()
                _dailyCheckInsCheckOuts.value = checkInsResp.body()
                _dailyNoShowRejected.value = noShowResp.body()

                // Fetch previous month for trend comparison
                fetchPreviousDailyData(month, year)

            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Error loading daily data"
            } finally {
                _dailyDataLoading.value = false
            }
        }
    }

    private fun fetchPreviousDailyData(currentMonth: Int, currentYear: Int) {
        viewModelScope.launch {
            try {
                val (prevMonth, prevYear) = if (currentMonth == 1) {
                    12 to (currentYear - 1)
                } else {
                    (currentMonth - 1) to currentYear
                }

                // Parallel API calls for previous month
                val prevRevenueDeferred = async {
                    repository.getDailyRevenue(prevMonth, prevYear)
                }
                val prevBookingsDeferred = async {
                    repository.getDailyBookings(prevMonth, prevYear)
                }
                val prevCancellationsDeferred = async {
                    repository.getDailyCancellations(prevMonth, prevYear)
                }
                val prevCheckInsDeferred = async {
                    repository.getDailyCheckinsCheckouts(prevMonth, prevYear)
                }
                val prevNoShowDeferred = async {
                    repository.getDailyNoShowRejected(prevMonth, prevYear)
                }

                // Await and assign results
                _previousDailyRevenue.value = prevRevenueDeferred.await().body()
                _previousDailyBookings.value = prevBookingsDeferred.await().body()
                _previousDailyCancellations.value = prevCancellationsDeferred.await().body()
                _previousDailyCheckInsCheckOuts.value = prevCheckInsDeferred.await().body()
                _previousDailyNoShowRejected.value = prevNoShowDeferred.await().body()

                // Calculate trends after previous data is loaded
                calculateDailyAnalyticsTrends()

            } catch (e: Exception) {
                // Previous data is optional for trends, so just log the error
                android.util.Log.e("DashboardVM", "Previous daily data error", e)
            }
        }
    }


    private fun calculateTrends() {
        val current = _stats.value ?: return
        val previous = _previousStats.value

        _statsWithTrends.value = listOf(
            StatWithTrend(
                label = "Active Bookings",
                value = current.active_bookings.toString(),
                trend = null,
                trendValue = ""
            ),
            StatWithTrend(
                label = "Pending Bookings",
                value = current.pending_bookings.toString(),
                trend = null,
                trendValue = ""
            ),
            StatWithTrend(
                label = "Total Bookings",
                value = current.total_bookings.toString(),
                trend = calculateTrend(
                    current.total_bookings.toDouble(),
                    previous?.total_bookings?.toDouble()
                ).first,
                trendValue = calculateTrend(
                    current.total_bookings.toDouble(),
                    previous?.total_bookings?.toDouble()
                ).second
            ),
            StatWithTrend(
                label = "Monthly Revenue",
                value = FormatPrice.formatRevenue(current.revenue.toString()),
                trend = calculateTrend(current.revenue, previous?.revenue).first,
                trendValue = calculateTrend(current.revenue, previous?.revenue).second
            )
        )
    }

    private fun calculateDailyAnalyticsTrends() {
        val current = _dailyRevenue.value ?: return
        val previous = _previousDailyRevenue.value

        // Helper function to safely sum data arrays
        fun sumData(data: List<Double>?, days: Int): Double {
            return data?.take(days)?.sum() ?: 0.0
        }

        fun sumDataInt(data: List<Int>?, days: Int): Int {
            return data?.take(days)?.sum() ?: 0
        }

        // Calculate monthly totals
        val currRevenue = sumData(_dailyRevenue.value?.data, current.days_in_month)
        val prevRevenue = sumData(previous?.data, previous?.days_in_month ?: 0)

        val currBookings = sumDataInt(_dailyBookings.value?.data, _dailyBookings.value?.days_in_month ?: 0)
        val prevBookings = sumDataInt(_previousDailyBookings.value?.data, _previousDailyBookings.value?.days_in_month ?: 0)

        val currCancellations = sumDataInt(_dailyCancellations.value?.data, _dailyCancellations.value?.days_in_month ?: 0)
        val prevCancellations = sumDataInt(_previousDailyCancellations.value?.data, _previousDailyCancellations.value?.days_in_month ?: 0)

        val currCheckIns = sumDataInt(_dailyCheckInsCheckOuts.value?.checkins, _dailyCheckInsCheckOuts.value?.days_in_month ?: 0)
        val prevCheckIns = sumDataInt(_previousDailyCheckInsCheckOuts.value?.checkins, _previousDailyCheckInsCheckOuts.value?.days_in_month ?: 0)

        val currCheckOuts = sumDataInt(_dailyCheckInsCheckOuts.value?.checkouts, _dailyCheckInsCheckOuts.value?.days_in_month ?: 0)
        val prevCheckOuts = sumDataInt(_previousDailyCheckInsCheckOuts.value?.checkouts, _previousDailyCheckInsCheckOuts.value?.days_in_month ?: 0)

        val currNoShows = sumDataInt(_dailyNoShowRejected.value?.no_shows, _dailyNoShowRejected.value?.days_in_month ?: 0)
        val prevNoShows = sumDataInt(_previousDailyNoShowRejected.value?.no_shows, _previousDailyNoShowRejected.value?.days_in_month ?: 0)

        val currRejected = sumDataInt(_dailyNoShowRejected.value?.rejected, _dailyNoShowRejected.value?.days_in_month ?: 0)
        val prevRejected = sumDataInt(_previousDailyNoShowRejected.value?.rejected, _previousDailyNoShowRejected.value?.days_in_month ?: 0)

        _dailyAnalyticsTrends.value = DailyAnalyticsTrends(
            revenueTrend = calculateTrend(currRevenue, prevRevenue),
            bookingsTrend = calculateTrend(currBookings.toDouble(), prevBookings.toDouble()),
            cancellationsTrend = calculateTrend(currCancellations.toDouble(), prevCancellations.toDouble()),
            checkInsTrend = calculateTrend(currCheckIns.toDouble(), prevCheckIns.toDouble()),
            checkOutsTrend = calculateTrend(currCheckOuts.toDouble(), prevCheckOuts.toDouble()),
            noShowsTrend = calculateTrend(currNoShows.toDouble(), prevNoShows.toDouble()),
            rejectedTrend = calculateTrend(currRejected.toDouble(), prevRejected.toDouble())
        )
    }

    private fun calculateTrend(current: Double, previous: Double?): Pair<TrendType, String> {

        if (previous == null || previous == 0.0) {
            return Pair(TrendType.NEUTRAL, "")
        }

        val percentageChange = ((current - previous) / previous) * 100


        if (percentageChange.absoluteValue < 0.5) {
            return Pair(TrendType.NEUTRAL, "")
        }

        val trend = when {
            percentageChange > 0.5 -> TrendType.UP
            percentageChange < -0.5 -> TrendType.DOWN
            else -> TrendType.NEUTRAL
        }

        val trendValue = formatPercentageChange(percentageChange)

        return Pair(trend, trendValue)
    }

    private fun formatPercentageChange(change: Double): String {
        val absChange = change.absoluteValue
        val prefix = if (change >= 0) "+" else ""

        return when {
            absChange >= 100 -> "$prefix${change.toInt()}%"
            absChange >= 10 -> "$prefix${change.toInt()}%"
            else -> {
                val rounded = (change * 10).toInt() / 10.0
                if (rounded == rounded.toInt().toDouble()) {
                    "$prefix${rounded.toInt()}%"
                } else {
                    "$prefix${"%.1f".format(rounded)}%"
                }
            }
        }
    }
}