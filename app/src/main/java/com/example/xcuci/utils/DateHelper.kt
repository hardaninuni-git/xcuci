package com.example.xcuci.utils

import java.util.Calendar
import java.util.Locale

class DateHelper {
    companion object {
        fun calculateDaysDifference(selectedDate: Calendar): Int {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val selected = selectedDate.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diff = selected.timeInMillis - today.timeInMillis
            return (diff / (24 * 60 * 60 * 1000)).toInt()
        }

        fun getMonthName(month: Int): String {
            val monthNames = arrayOf(
                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )
            return monthNames[month]
        }

        fun formatDatabaseDate(year: Int, month: Int, day: Int): String {
            return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
        }

        fun formatDisplayDate(day: Int, month: Int, year: Int): String {
            return String.format(
                Locale("id", "ID"),
                "%02d %s %04d",
                day,
                getMonthName(month),
                year
            )
        }
    }
}