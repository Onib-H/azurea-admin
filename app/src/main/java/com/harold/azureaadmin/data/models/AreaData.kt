package com.harold.azureaadmin.data.models

data class Area(
    val id: Int = 0,
    val area_name: String = "",
    val description: String = "",
    val images: List<AreaImage> = emptyList(),
    val status: String = "available",
    val capacity: Int = 0,
    val price_per_hour: String = "₱0.00",
    val discounted_price: String? = null,
    val discount_percent: Int = 0,
    val senior_discounted_price: Double = 0.0,
    val average_rating: Double = 0.0,
    val reviews: List<Review> = emptyList(),
    val price_per_hour_numeric: Double = 0.0,
    val discounted_price_numeric: Double? = null
)

data class Review(
    val id: Int,
    val rating: Int,
    val user_id: Int,
    val booking: Int,
    val review_text: String,
    val created_at: String,
    val room_id: Int?,            // nullable
    val area_id: Int?,
    val user_profile_image: String?,
    val formatted_date: String,
    val user_name: String,
    val booking_details: BookingDetails?
)


data class AreaImage(
    val id: Int,
    val area_image: String = ""
)

data class AreaResponse(
    val data: List<Area>
)


data class AreaResponseSingle(
    val message: String,
    val data: Area
)

data class AreaDetailResponse(
    val data: AreaDetail
)

data class AreaDetail(
    val id: Int,
    val area_name: String,
    val description: String,
    val images: List<AreaImage>,
    val status: String,
    val capacity: Int,
    val price_per_hour: String,
    val discounted_price: String,
    val discount_percent: Int,
    val senior_discounted_price: Double,
    val average_rating: Double,
    val price_per_hour_numeric: Double,
    val discounted_price_numeric: Double
)