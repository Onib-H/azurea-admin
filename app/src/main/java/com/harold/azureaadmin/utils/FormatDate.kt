package com.harold.azureaadmin.utils


import java.text.SimpleDateFormat
import java.util.Locale

object FormatDate {

    fun format(input: String?): String {
        if (input.isNullOrBlank()) return "N/A"

        return try {
            val inputFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            )
            val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

            val date = inputFormats.firstNotNullOfOrNull { format ->
                runCatching { format.parse(input) }.getOrNull()
            }

            date?.let { outputFormat.format(it) } ?: "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    }
}
