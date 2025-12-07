    package com.harold.azureaadmin.data.models

    data class Room(
        val id: Int = 0,
        val room_name: String = "",
        val room_type: String = "",
        val images: List<RoomImage> = emptyList(),
        val bed_type: String = "",
        val status: String = "available",
        val room_price: String = "₱0.00",
        val discount_percent: Int = 0,
        val discounted_price: String? = null,
        val senior_discounted_price: Double = 0.0,
        val description: String = "",
        val max_guests: Int = 0,
        val amenities: List<Amenity> = emptyList(),
        val average_rating: Double = 0.0,
        val price_per_night: Double = 0.0,
        val discounted_price_numeric: Double? = null
    )

    data class RoomImage(
        val id: Int,
        val room_image: String = ""
    )

    data class RoomResponse(
        val data: List<Room>
    )

    data class RoomResponseSingle(
        val message: String,
        val data: Room
    )

    data class RoomDetail(
        val id: Int,
        val room_name: String,
        val room_type: String,
        val bed_type: String,
        val images: List<RoomImage>,
        val status: String,
        val room_price: String,
        val discount_percent: Int,
        val discounted_price: String?,
        val description: String,
        val max_guests: Int,
        val amenities: List<Amenity>,
        val price_per_night: Double,
        val discounted_price_numeric: Double?
    )

    data class RoomDetailResponse(
        val data: RoomDetail
    )