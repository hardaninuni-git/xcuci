package com.example.xcuci.utils

import com.example.xcuci.data.model.HandukSize

class HandukSizeManager {
    var selectedHandukSize: String = ""

    private val handukSizesData = listOf(
        HandukSize("S", "Kecil"),
        HandukSize("M", "Sedang"),
        HandukSize("L", "Besar"),
        HandukSize("XL", "Extra Large")
    )

    fun getHandukSizes(): List<HandukSize> = handukSizesData

    fun getPriceRange(size: String): String {
        return when (size) {
            "S" -> "Rp 5.000 - 10.000"
            "M" -> "Rp 6.000 - 11.000"
            "L" -> "Rp 7.000 - 11.000"
            "XL" -> "Rp 8.000 - 12.000"
            else -> ""
        }
    }

    fun shouldShowSizeSelection(countHanduk: Int): Boolean {
        return countHanduk > 0
    }

    fun resetSelection() {
        selectedHandukSize = ""
    }
}