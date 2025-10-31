package com.example.xcuci.data.model

data class Order(
    val id: Int,
    val customerName: String,
    val phone: String,
    val address: String,
    val weight: Double,
    val pricePerKg: Double,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val completionDate: String?,
    // TAMBAHKAN FIELD UNTUK DETAIL ITEM
    val kaosQty: Int,
    val celanaQty: Int,
    val handukQty: Int,
    val totalPcs: Int
)