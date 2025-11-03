package com.example.azureaadmin.data.models

data class MessageResponse(
    val message: String
)

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
)



