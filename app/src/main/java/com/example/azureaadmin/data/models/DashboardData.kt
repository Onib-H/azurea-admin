package com.example.azureaadmin.data.models

import androidx.compose.ui.graphics.Color

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



//data class DashboardStat(
//    val label: String,
//    val value: String,
//    val colors: List<Color>,
//    val icon: androidx.compose.ui.graphics.vector.ImageVector
//)

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

