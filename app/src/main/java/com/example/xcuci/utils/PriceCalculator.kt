package com.example.xcuci.utils

class PriceCalculator {
    companion object {
        private const val PREFS_NAME = "price_settings"

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

        // Method untuk mendapatkan harga berdasarkan tipe layanan dan hari
        fun getPricePerKgByServiceType(daysDifference: Int, serviceType: String): Int {
            return when (serviceType) {
                "cuci_dan_setrika" -> getPriceForCuciDanSetrika(daysDifference)
                "setrika" -> getPriceForSetrika(daysDifference)
                "cuci" -> getPriceForCuci(daysDifference)
                else -> getPriceForCuciDanSetrika(daysDifference) // Default
            }
        }

        // Harga untuk Cuci dan Setrika (5 hari)
        private fun getPriceForCuciDanSetrika(daysDifference: Int): Int {
            return when (daysDifference) {
                0 -> 20000 // Express - hari ini
                1 -> 15000 // Next day - besok
                2 -> 12000 // 2 hari
                3 -> 10000 // 3 hari
                4 -> 8000  // 4 hari
                5 -> 6000  // 5 hari
                else -> 6000
            }
        }

        // Harga untuk Setrika (4 hari)
        private fun getPriceForSetrika(daysDifference: Int): Int {
            return when (daysDifference) {
                0 -> 17000 // Express - hari ini
                1 -> 13000 // Next day - besok
                2 -> 10000 // 2 hari
                3 -> 8000  // 3 hari
                4 -> 6000  // 4 hari
                else -> 6000
            }
        }

        // Harga untuk Cuci (4 hari)
        private fun getPriceForCuci(daysDifference: Int): Int {
            return when (daysDifference) {
                0 -> 17000 // Express - hari ini
                1 -> 13000 // Next day - besok
                2 -> 10000 // 2 hari
                3 -> 8000  // 3 hari
                4 -> 6000  // 4 hari
                else -> 6000
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
    }
}