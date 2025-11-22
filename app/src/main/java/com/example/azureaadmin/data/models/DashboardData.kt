package com.example.azureaadmin.data.models

data class AdminStatsResponse(
    val total_rooms: Int,
    val available_rooms: Int,
    val occupied_rooms: Int,
    val maintenance_rooms: Int,
    val active_bookings: Int,
    val pending_bookings: Int,
    val unpaid_bookings: Int,
    val checked_in_count: Int,
    val total_bookings: Int,
    val upcoming_reservations: Int,
    val revenue: Double,
    val room_revenue: Double,
    val venue_revenue: Double,
    val formatted_revenue: String,
    val formatted_room_revenue: String,
    val formatted_venue_revenue: String,
    val month: Int,
    val year: Int
)


data class BookingStatusResponse(
    val pending: Int,
    val reserved: Int,
    val checked_in: Int,
    val checked_out: Int,
    val cancelled: Int,
    val no_show: Int,
    val rejected: Int
)

data class BookingStatusCounts(
    val label: String,
    val count: Int,
    val percentage: Float = 0f
)


data class DailyRevenueResponse(
    val data: List<Double>,
    val month: Int,
    val year: Int,
    val days_in_month: Int
)

data class DailyBookingsResponse(
    val data: List<Int>,
    val month: Int,
    val year: Int,
    val days_in_month: Int
)

data class DailyCancellationsResponse(
    val data: List<Int>,
    val month: Int,
    val year: Int,
    val days_in_month: Int
)

data class DailyCheckInCheckoutResponse(
    val checkins: List<Int>,
    val checkouts: List<Int>,
    val month: Int,
    val year: Int,
    val days_in_month: Int
)

data class DailyNoShowRejectedResponse(
    val no_shows: List<Int>,
    val rejected: List<Int>,
    val month: Int,
    val year: Int,
    val days_in_month: Int
)

data class PropertyRevenueResponse(
    val room_names: List<String>,
    val revenue_data: List<Double>,
    val month: Int,
    val year: Int
)

data class PropertyBookingResponse(
    val room_names: List<String>,
    val booking_counts: List<Int>,
    val month: Int,
    val year: Int
)


data class ReportData(
    val period: String,
    val stats: Stats,
    val bookingStatusCounts: BookingStatus,
    val areaNames: List<String>,
    val areaRevenueValues: List<Double>,
    val areaBookingValues: List<Int>,
    val roomNames: List<String>,
    val roomRevenueValues: List<Double>,
    val roomBookingValues: List<Int>
)

data class Stats(
    val activeBookings: Int,
    val pendingBookings: Int,
    val totalBookings: Int,
    val revenue: Double,
    val formattedRevenue: String,
    val roomRevenue: Double,
    val venueRevenue: Double,
    val totalRooms: Int,
    val availableRooms: Int,
    val occupiedRooms: Int,
    val maintenanceRooms: Int,
    val checkedInCount: Int
)

data class BookingStatus(
    val reserved: Int,
    val checked_out: Int,
    val cancelled: Int,
    val no_show: Int,
    val rejected: Int
)



