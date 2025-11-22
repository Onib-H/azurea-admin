package com.example.azureaadmin.utils

import java.text.SimpleDateFormat
import java.util.Locale

object FormatTime {
    fun format(time: String?): String {
        // Input format (24-hour)
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        // Output format (12-hour with AM/PM)
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(time)
        return outputFormat.format(date!!)
    }
}
