package com.example.azureaadmin.data.models

data class AdminLoginRequest(
    val email: String,
    val password: String
)

data class AdminLoginResponse(
    val message: String,
    val user: AdminUser?,
    val access_token: String?,
    val refresh_token: String?
)

data class AdminUser(
    val id: Int,
    val email: String,
    val username: String,
    val first_name: String,
    val last_name: String,
    val role: String,
    val profile_image: String?
)

data class UserAuthResponse(
    val isAuthenticated: Boolean,
    val role: String?,
    val user: UserInfo?
)

data class UserInfo(
    val id: Int,
    val email: String,
    val username: String,
    val first_name: String,
    val last_name: String,
    val profile_image: String,
    val is_verified: Boolean,
    val last_booking_date: String?,
    val is_senior_or_pwd: Boolean
)