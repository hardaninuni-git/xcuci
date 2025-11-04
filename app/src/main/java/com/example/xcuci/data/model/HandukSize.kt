package com.example.xcuci.data.model

data class HandukSize(
    val size: String,
    val description: String
) {
    override fun toString(): String {
        return size
    }
}