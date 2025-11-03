package com.example.azureaadmin.utils

import android.net.Uri
import com.example.azureaadmin.data.models.ValidationResult

fun validateRoomInputs(
    inputs: Map<String, String>,
    selectedAmenities: Set<String>,
    selectedImageUris: List<Uri>
): ValidationResult {
    val errors = mutableMapOf<String, String>()

    val isRoom = inputs.containsKey("Room Type") // determines whether it's a Room or Area

    // Name validation
    if (inputs["Name"].isNullOrBlank()) {
        errors["Name"] = "${if (isRoom) "Room" else "Area"} name is required"
    }

    // Room Type validation (for rooms only)
    if (isRoom && inputs["Room Type"].isNullOrBlank()) {
        errors["Room Type"] = "Room type is required"
    }

    // Bed Type validation (for rooms only)
    if (isRoom && inputs["Bed Type"].isNullOrBlank()) {
        errors["Bed Type"] = "Bed type is required"
    }

    // Capacity / Max Guests validation
    val capacity = inputs["Capacity"]?.toIntOrNull()
    if (capacity == null || capacity <= 0) {
        errors["Capacity"] = "Valid ${if (isRoom) "max guests" else "capacity"} is required"
    } else {
        if (isRoom && (capacity !in 1..10)) {
            errors["Capacity"] = "Max guests must be between 1 and 10"
        } else if (!isRoom && (capacity !in 1..500)) {
            errors["Capacity"] = "Capacity must be between 1 and 500"
        }
    }

    // Price validation
    val price = inputs["Price"]?.toDoubleOrNull()
    if (price == null || price < 0) {
        errors["Price"] = "Valid price is required"
    } else {
        if (isRoom && price !in 1.0..50000.0) {
            errors["Price"] = "Room price must be between ₱1 and ₱50,000"
        } else if (!isRoom && price !in 1.0..10000.0) {
            errors["Price"] = "Area price must be between ₱1 and ₱10,000"
        }
    }

    // Discount validation (optional but must be valid if provided)
    val discount = inputs["Discount"]?.toIntOrNull()
    if (!inputs["Discount"].isNullOrBlank() && (discount == null || discount < 0 || discount > 100)) {
        errors["Discount"] = "Discount must be between 0 and 99"
    }

    // Image validation
    if (selectedImageUris.isEmpty()) {
        errors["Images"] = "At least one image is required"
    }

    return ValidationResult(errors.isEmpty(), errors)
}