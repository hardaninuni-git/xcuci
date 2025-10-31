package com.example.xcuci.data.model

data class Customer(
    val name: String,
    val phone: String,
    val address: String,
    val totalOrders: Int = 0,
    val createdAt: String? = null
)