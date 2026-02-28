package com.blank.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object TimeUtil {

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val isoDateFormatWithMs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun fromIsoString(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        
        return try {
            val date = parseDate(isoString)
            fromTimestamp(date?.time ?: return "")
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseDate(isoString: String): Date? {
        return try {
            isoDateFormat.parse(isoString)
        } catch (e: Exception) {
            try {
                isoDateFormatWithMs.parse(isoString)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun fromTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "${minutes}m ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "${hours}h ago"
            }
            diff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                "${days}d ago"
            }
            diff < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7
                "${weeks}w ago"
            }
            diff < TimeUnit.DAYS.toMillis(365) -> {
                val months = TimeUnit.MILLISECONDS.toDays(diff) / 30
                "${months}mo ago"
            }
            else -> {
                val years = TimeUnit.MILLISECONDS.toDays(diff) / 365
                "${years}y ago"
            }
        }
    }

    fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        
        return try {
            val date = parseDate(isoString) ?: return ""
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDateTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        
        return try {
            val date = parseDate(isoString) ?: return ""
            SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            ""
        }
    }
}
