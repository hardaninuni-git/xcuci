package com.example.xcuci.data.repository

import com.example.xcuci.data.model.Order
import com.example.xcuci.data.model.OrderRequest
import com.example.xcuci.data.remote.response.ApiResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("orders")
    fun getOrders(): Call<ApiResponse<List<Order>>>

    @GET("orders/{id}")
    fun getOrder(@Path("id") id: Int): Call<ApiResponse<Order>>

    @POST("orders")
    fun createOrder(@Body order: OrderRequest): Call<ApiResponse<Order>>

    @PUT("orders/{id}")
    fun updateOrder(@Path("id") id: Int, @Body order: OrderRequest): Call<ApiResponse<Order>>

    @DELETE("orders/{id}")
    fun deleteOrder(@Path("id") id: Int): Call<ApiResponse<String>>
}