package com.example.xcuci.data.model

data class OrderRequest(
    val customerName: String,
    val phone: String,
    val address: String,
    val weight: Double,
    val pricePerKg: Double,
    val status: String? = null, // Tambahkan field status
    val completionDate: String?,
    val serviceType: String? = null,
    // TAMBAHKAN FIELD BARU - HAPUS DEFAULT VALUES
    val kaosQty: Int, // HAPUS = 0
    val celanaQty: Int, // HAPUS = 0
    val handukQty: Int, // HAPUS = 0
    val totalPcs: Int // HAPUS = 0
)