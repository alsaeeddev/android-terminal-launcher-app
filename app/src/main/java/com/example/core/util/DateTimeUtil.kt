package com.example.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtil {

    fun formatCurrentTime(pattern: String = "HH:mm:ss"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatCurrentDate(pattern: String = "EEE, MMM d, yyyy"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatTimestamp(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Generates a classic UNIX "cal" ASCII calendar for the given month/year or current month
     */
    fun generateAsciiCalendar(calendar: Calendar = Calendar.getInstance()): List<String> {
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
        val lines = mutableListOf<String>()

        // Header centered over 20 chars
        val padding = ((20 - monthName.length) / 2).coerceAtLeast(0)
        lines.add(" ".repeat(padding) + monthName)
        lines.add("Su Mo Tu We Th Fr Sa")

        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 7 = Saturday
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = Calendar.getInstance().let {
            if (it.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                it.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
            ) it.get(Calendar.DAY_OF_MONTH) else -1
        }

        val row = StringBuilder()
        for (i in 1 until firstDayOfWeek) {
            row.append("   ")
        }

        for (day in 1..maxDays) {
            val dayStr = if (day == today) {
                String.format(Locale.US, "%2d*", day)
            } else {
                String.format(Locale.US, "%2d ", day)
            }
            row.append(dayStr)
            if ((firstDayOfWeek - 1 + day) % 7 == 0 || day == maxDays) {
                lines.add(row.toString().trimEnd())
                row.clear()
            }
        }

        return lines
    }
}
