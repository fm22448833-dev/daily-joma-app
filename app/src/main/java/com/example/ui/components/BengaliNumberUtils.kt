package com.example.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BengaliNumberUtils {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBangla(number: Number): String {
        val str = if (number is Double && number % 1.0 == 0.0) {
            number.toInt().toString()
        } else {
            number.toString()
        }
        return toBanglaDigits(str)
    }

    fun toBanglaDigits(input: String): String {
        val sb = StringBuilder()
        for (char in input) {
            if (char in '0'..'9') {
                sb.append(banglaDigits[char - '0'])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    fun formatTaka(amount: Double): String {
        val formatted = if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%,d", amount.toLong())
        } else {
            String.format(Locale.US, "%,.2f", amount)
        }
        return "৳ " + toBanglaDigits(formatted)
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateToDisplay(dateString: String): String {
        return try {
            val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdfInput.parse(dateString)
            if (date != null) {
                val sdfOutput = SimpleDateFormat("dd MMM, yyyy", Locale("bn", "BD"))
                toBanglaDigits(sdfOutput.format(date))
            } else {
                toBanglaDigits(dateString)
            }
        } catch (e: Exception) {
            toBanglaDigits(dateString)
        }
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a, dd/MM/yy", Locale.getDefault())
        return toBanglaDigits(sdf.format(Date(timestamp)))
    }
}
