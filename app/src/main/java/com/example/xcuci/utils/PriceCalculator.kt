package com.example.xcuci.utils

import android.content.Context
import android.util.Log

class PriceCalculator {
    companion object {
        private const val PREFS_NAME = "price_settings"

        // Keys untuk SharedPreferences
        private const val KEY_CUCI_DAN_SETRIKA = "cuci_dan_setrika"
        private const val KEY_SETRIKA = "setrika"
        private const val KEY_CUCI = "cuci"

        private val handukPrices = mapOf(
            "S" to mapOf(0 to 5000, 1 to 7000, 2 to 8000, 3 to 9000, 4 to 10000),
            "M" to mapOf(0 to 6000, 1 to 8000, 2 to 9000, 3 to 10000, 4 to 11000),
            "L" to mapOf(0 to 7000, 1 to 8000, 2 to 9000, 3 to 10000, 4 to 11000),
            "XL" to mapOf(0 to 8000, 1 to 9000, 2 to 10000, 3 to 11000, 4 to 12000)
        )

        fun calculateBasePrice(weight: Double, pricePerKg: Double): Double {
            return weight * pricePerKg
        }

        fun getHandukPrice(size: String, daysDifference: Int): Int {
            val adjustedDays = if (daysDifference > 4) 4 else daysDifference
            return handukPrices[size]?.get(adjustedDays) ?: 0
        }

        fun calculateHandukPrice(countHanduk: Int, size: String, daysDifference: Int): Int {
            return if (countHanduk > 0 && size.isNotEmpty()) {
                countHanduk * getHandukPrice(size, daysDifference)
            } else {
                0
            }
        }

        fun getPricePerKgByDays(daysDifference: Int): Int {
            return when (daysDifference) {
                0 -> 20000
                1 -> 15000
                2 -> 12000
                3 -> 10000
                4 -> 8000
                5 -> 6000
                else -> 6000
            }
        }

        fun getServiceTypeText(daysDifference: Int): String {
            return when (daysDifference) {
                0 -> "Express (Hari Ini)"
                1 -> "Besok"
                else -> "${daysDifference + 1} Hari Lagi"
            }
        }

        // Harga default untuk setiap service type
        private val defaultCuciDanSetrikaPrices = mapOf(
            0 to 20000, // Express - hari ini
            1 to 15000, // Next day - besok
            2 to 12000, // 2 hari
            3 to 10000, // 3 hari
            4 to 8000,  // 4 hari
            5 to 6000   // 5 hari
        )

        private val defaultSetrikaPrices = mapOf(
            0 to 17000, // Express - hari ini
            1 to 13000, // Next day - besok
            2 to 10000, // 2 hari
            3 to 8000,  // 3 hari
            4 to 6000   // 4 hari
        )

        private val defaultCuciPrices = mapOf(
            0 to 17000, // Express - hari ini
            1 to 13000, // Next day - besok
            2 to 10000, // 2 hari
            3 to 8000,  // 3 hari
            4 to 6000   // 4 hari
        )

        // Method untuk reset semua harga ke default
        fun resetToDefault(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            editor.apply()
            Log.d("PriceCalculator", "All prices reset to default")
        }

        // Method untuk mendapatkan harga berdasarkan service type dari SharedPreferences
        fun getPricePerKgByServiceType(context: Context, daysDifference: Int, serviceType: String): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            return when (serviceType) {
                "cuci_dan_setrika" -> getPriceForCuciDanSetrika(context, daysDifference)
                "setrika" -> getPriceForSetrika(context, daysDifference)
                "cuci" -> getPriceForCuci(context, daysDifference)
                else -> getPriceForCuciDanSetrika(context, daysDifference) // Default
            }
        }

        // Method untuk menyimpan harga service type
        fun saveServiceTypePrice(context: Context, serviceType: String, days: Int, price: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()

            val key = when (serviceType) {
                "cuci_dan_setrika" -> "${KEY_CUCI_DAN_SETRIKA}_$days"
                "setrika" -> "${KEY_SETRIKA}_$days"
                "cuci" -> "${KEY_CUCI}_$days"
                else -> "${KEY_CUCI_DAN_SETRIKA}_$days"
            }

            editor.putInt(key, price)
            editor.apply()

            Log.d("PriceCalculator", "Saved $serviceType day $days: $price")
        }

        // Harga untuk Cuci dan Setrika (5 hari)
        private fun getPriceForCuciDanSetrika(context: Context, daysDifference: Int): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            return when (daysDifference) {
                0 -> prefs.getInt("${KEY_CUCI_DAN_SETRIKA}_0", defaultCuciDanSetrikaPrices[0] ?: 20000)
                1 -> prefs.getInt("${KEY_CUCI_DAN_SETRIKA}_1", defaultCuciDanSetrikaPrices[1] ?: 15000)
                2 -> prefs.getInt("${KEY_CUCI_DAN_SETRIKA}_2", defaultCuciDanSetrikaPrices[2] ?: 12000)
                3 -> prefs.getInt("${KEY_CUCI_DAN_SETRIKA}_3", defaultCuciDanSetrikaPrices[3] ?: 10000)
                4 -> prefs.getInt("${KEY_CUCI_DAN_SETRIKA}_4", defaultCuciDanSetrikaPrices[4] ?: 8000)
                5 -> prefs.getInt("${KEY_CUCI_DAN_SETRIKA}_5", defaultCuciDanSetrikaPrices[5] ?: 6000)
                else -> prefs.getInt("${KEY_CUCI_DAN_SETRIKA}_5", defaultCuciDanSetrikaPrices[5] ?: 6000)
            }
        }

        // Harga untuk Setrika (4 hari)
        private fun getPriceForSetrika(context: Context, daysDifference: Int): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            return when (daysDifference) {
                0 -> prefs.getInt("${KEY_SETRIKA}_0", defaultSetrikaPrices[0] ?: 17000)
                1 -> prefs.getInt("${KEY_SETRIKA}_1", defaultSetrikaPrices[1] ?: 13000)
                2 -> prefs.getInt("${KEY_SETRIKA}_2", defaultSetrikaPrices[2] ?: 10000)
                3 -> prefs.getInt("${KEY_SETRIKA}_3", defaultSetrikaPrices[3] ?: 8000)
                4 -> prefs.getInt("${KEY_SETRIKA}_4", defaultSetrikaPrices[4] ?: 6000)
                else -> prefs.getInt("${KEY_SETRIKA}_4", defaultSetrikaPrices[4] ?: 6000)
            }
        }

        // Harga untuk Cuci (4 hari)
        private fun getPriceForCuci(context: Context, daysDifference: Int): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            return when (daysDifference) {
                0 -> prefs.getInt("${KEY_CUCI}_0", defaultCuciPrices[0] ?: 17000)
                1 -> prefs.getInt("${KEY_CUCI}_1", defaultCuciPrices[1] ?: 13000)
                2 -> prefs.getInt("${KEY_CUCI}_2", defaultCuciPrices[2] ?: 10000)
                3 -> prefs.getInt("${KEY_CUCI}_3", defaultCuciPrices[3] ?: 8000)
                4 -> prefs.getInt("${KEY_CUCI}_4", defaultCuciPrices[4] ?: 6000)
                else -> prefs.getInt("${KEY_CUCI}_4", defaultCuciPrices[4] ?: 6000)
            }
        }

        // Method untuk mendapatkan maksimal hari berdasarkan tipe layanan
        fun getMaxDaysByServiceType(serviceType: String): Int {
            return when (serviceType) {
                "cuci_dan_setrika" -> 5
                "setrika" -> 4
                "cuci" -> 4
                else -> 5 // Default
            }
        }

        // Method untuk mendapatkan semua harga suatu service type (untuk display di settings)
        fun getAllServiceTypePrices(context: Context, serviceType: String): Map<Int, Int> {
            val result = mutableMapOf<Int, Int>()

            val maxDays = getMaxDaysByServiceType(serviceType)
            for (day in 0..maxDays) {
                result[day] = getPricePerKgByServiceType(context, day, serviceType)
            }

            return result
        }
    }
}