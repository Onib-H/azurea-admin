package com.example.azureaadmin.data.models

data class BookingResponse(
    val data: List<BookingData>,
    val pagination: Pagination
)


data class BookingData(
    val id: Int,
    val user: User,
    val room: Int?,
    val room_details: RoomDetails?,
    val area: Int?,
    val area_details: AreaDetails?,
    val check_in_date: String,
    val check_out_date: String,
    val status: String,
    val special_request: String?,
    val cancellation_date: String?,
    val cancellation_reason: String?,
    val time_of_arrival: String?,
    val is_venue_booking: Boolean,
    val total_price: Double,
    val number_of_guests: Int,
    val created_at: String,
    val updated_at: String,
    val payment_method: String,
    val payment_proof: String?,
    val payment_date: String?,
    val down_payment: Double?,
    val phone_number: String,
    val total_amount: Double,
    val original_price: Double,
    val discount_percent: Int,
    val discounted_price: Double
)

data class BookingDetailsResponse(
    val data: BookingDetails
)

data class BookingDetails(
    val id: Int,
    val user: User,
//    val room: Int?,
    val room_details: RoomDetails?,
    val area: Int?,
    val area_details: AreaDetails?,
    val check_in_date: String,
    val check_out_date: String,
    val status: String,
    val special_request: String?,
    val cancellation_date: String?,
    val cancellation_reason: String?,
    val time_of_arrival: String?,
    val is_venue_booking: Boolean,
    val total_price: Double,
    val number_of_guests: Int,
    val created_at: String,
    val updated_at: String,
    val payment_method: String,
    val payment_proof: String?,
    val payment_date: String?,
    val down_payment: Double?,
    val phone_number: String,
    val total_amount: Double,
    val original_price: Double,
    val discount_percent: Int,
    val discounted_price: Double
)

data class UpdateBookingStatusRequest(
    val status: String,
    val down_payment: Double? = null,
    val reason: String? = null,
    val set_available: Boolean? = null
)

data class UpdateBookingStatusResponse(
    val message: String,
    val data: BookingDetails
)


data class RoomDetails(
    val id: Int,
    val room_name: String,
    val room_type: String,
    val images: List<RoomImage>,
    val bed_type: String,
    val status: String,
    val room_price: String,
    val discount_percent: Int,
    val discounted_price: String,
    val senior_discounted_price: Double,
    val description: String,
    val max_guests: Int,
    val amenities: List<Amenity>,
    val average_rating: Double,
    val price_per_night: Double,
    val discounted_price_numeric: Double
)

data class AreaDetails(
    val id: Int,
    val area_name: String,
    val description: String,
    val images: List<AreaImage>,
    val status: String,
    val capacity: Int,
    val price_per_hour: String,
    val discounted_price: String?,
    val discount_percent: Int,
    val senior_discounted_price: Double,
    val average_rating: Double,
    val price_per_hour_numeric: Double,
    val discounted_price_numeric: Double?
)

data class RecordPaymentRequest(
    val amount: Double,
    val transaction_type: String = "full_payment"
)

data class RecordPaymentResponse(
    val message: String,
    val transaction_id: Int,
    val booking_id: Int,
    val amount: Double
)

