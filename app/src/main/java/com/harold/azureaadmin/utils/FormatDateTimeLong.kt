package com.harold.azureaadmin.utils

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object FormatDateTimeLong {
    fun format(dateTime: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")
            OffsetDateTime.parse(dateTime, inputFormatter).format(outputFormatter)
        } catch (e: Exception) {
            dateTime
        }
    }
}

