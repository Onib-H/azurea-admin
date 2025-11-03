package com.example.azureaadmin.data.models


data class Amenity(
    val id: Int,
    val description: String = ""
)

data class AmenityRequest(
    val description: String
)

data class AmenityResponse(
    val data: List<Amenity>
)



