package com.example.azureaadmin.data.models

// Main response wrapper
data class UsersResponse(
    val users: List<User>,
    val pagination: Pagination
)

data class Pagination(
    val total_pages: Int,
    val current_page: Int,
    val total_items: Int,
    val page_size: Int
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val role: String,
    val profile_image: String,
    val valid_id_type: String?,
    val valid_id_type_display: String?,
    val valid_id_front: String?,
    val valid_id_back: String?,
    val is_verified: String?,
    val valid_id_rejection_reason: String?,
    val last_booking_date: String?,
    val is_senior_or_pwd: Boolean
)

data class ApproveIdRequest(
    val is_senior_or_pwd: Boolean
)

data class ApproveIdResponse(
    val message: String,
    val user: User
)

data class RejectIdRequest(
    val reason: String
)

data class RejectIdResponse(
    val message: String,
    val user_id: Int,
    val is_verified: String,
    val valid_id_rejection_reason: String
)