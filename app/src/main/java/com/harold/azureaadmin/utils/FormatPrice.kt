package com.harold.azureaadmin.utils


import java.text.NumberFormat
import java.util.Locale

object FormatPrice {


    /**
     * Format a value (Int, Double, or String) into PHP Peso currency.
     * Examples:
     *  - 1500 -> ₱1,500
     *  - "2000.5" -> ₱2,000.50
     *  - 99.0 -> ₱99
     */
    fun formatPrice(value: Any?): String {
        val amount = when (value) {
            is Int -> value.toDouble()
            is Double -> value
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        val formatted = formatter.format(amount)

        // Replace "PHP" with "₱"
        return formatted.replace("PHP", "₱")
    }

    /**
     * Format a value (Int, Double, or String) into number with commas.
     * Examples:
     *  - 10000 -> 10,000
     *  - "2500" -> 2,500
     *  - 99.99 -> 100 (rounded)
     */
    fun formatNumber(value: Any?): String {
        val number = when (value) {
            is Int -> value.toLong()
            is Double -> value.toLong() // drop decimals for "number"
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }

        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return formatter.format(number)
    }

    fun formatRevenue(value: String): String {
        // Remove peso sign, commas, and spaces but keep decimals
        val cleaned = value.replace(Regex("[₱,\\s]"), "")
        val num = cleaned.toDoubleOrNull() ?: return value

        return when {
            num >= 1_000_000_000 -> "₱" + String.format("%.2fB", num / 1_000_000_000)
            num >= 1_000_000 -> "₱" + String.format("%.2fM", num / 1_000_000)
            num >= 1_000 -> "₱" + String.format("%.1fK", num / 1_000)
            else -> "₱" + String.format("%.2f", num)
        }
    }

}


