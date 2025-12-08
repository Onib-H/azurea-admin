package com.harold.azureaadmin.utils

import android.net.Uri
import com.harold.azureaadmin.data.models.ValidationResult

fun validateRoomInputs(
    inputs: Map<String, String>,
    selectedAmenities: Set<String>,
    selectedImageUris: List<Uri>,
    availableAmenities: List<String>
): ValidationResult {

    val errors = mutableMapOf<String, String>()
    val isRoom = inputs.containsKey("Room Type")

    // Name validation
    val name = inputs["Name"]?.trim()
    if (name.isNullOrBlank()) {
        errors["Name"] = "Name is required"
    } else if (name.length < 3) {
        errors["Name"] = "Name must be at least 3 characters"
    } else if (name.length > 50) {
        errors["Name"] = "Name must not exceed 50 characters"
    }

    // Room Type validation
    if (isRoom && inputs["Room Type"].isNullOrBlank()) {
        errors["Room Type"] = "Room type is required"
    }

    // Bed Type validation
    if (isRoom && inputs["Bed Type"].isNullOrBlank()) {
        errors["Bed Type"] = "Bed type is required"
    }

    // Capacity / Max Guests
    val capacityStr = inputs["Capacity"]?.trim()
    if (capacityStr.isNullOrBlank()) {
        errors["Capacity"] = "Capacity is required"
    } else {
        val capacity = capacityStr.toIntOrNull()
        if (capacity == null) {
            errors["Capacity"] = "Must be a valid number"
        } else if (capacity <= 0) {
            errors["Capacity"] = "Must be greater than 0"
        } else if (isRoom && capacity > 10) {
            errors["Capacity"] = "Max guests cannot exceed 10"
        } else if (!isRoom && capacity > 500) {
            errors["Capacity"] = "Capacity cannot exceed 500"
        }
    }

    // Price validation
    val priceStr = inputs["Price"]?.trim()
    if (priceStr.isNullOrBlank()) {
        errors["Price"] = "Price is required"
    } else {
        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            errors["Price"] = "Must be a valid amount"
        } else if (price <= 0) {
            errors["Price"] = "Must be greater than 0"
        } else if (price > 1000000.0) {
            errors["Price"] = "Cannot exceed ₱1,000,000"
        }
    }

    // Description (optional)
    val description = inputs["Description"]?.trim()
    if (!description.isNullOrBlank()) {
        if (description.length < 10) {
            errors["Description"] = "Must be at least 10 characters"
        } else if (description.length > 500) {
            errors["Description"] = "Must not exceed 500 characters"
        }
    }

    // Discount (optional)
    val discountStr = inputs["Discount"]?.trim()
    if (!discountStr.isNullOrBlank()) {
        val discount = discountStr.toIntOrNull()
        if (discount == null) {
            errors["Discount"] = "Must be a valid number"
        } else if (discount < 0) {
            errors["Discount"] = "Cannot be negative"
        } else if (discount > 99) {
            errors["Discount"] = "Cannot exceed 99%"
        }
    }

    // Image validation
    if (selectedImageUris.isEmpty()) {
        errors["Images"] = "At least 1 image is required"
    } else if (selectedImageUris.size > 10) {
        errors["Images"] = "Maximum 10 images allowed"
    }

    // Amenities validation (for rooms only)
    if (isRoom) {
        if (availableAmenities.isEmpty()) {
            errors["Amenities"] = "Create an amenity first"
        } else if (selectedAmenities.isEmpty()) {
            errors["Amenities"] = "Select at least 1 amenity"
        }
    }

    return ValidationResult(errors.isEmpty(), errors)
}
