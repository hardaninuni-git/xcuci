package com.example.xcuci.utils

class PriceCalculator {
    companion object {
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
    }
}