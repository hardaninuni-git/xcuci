package com.example.xcuci.data.remote.response

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)