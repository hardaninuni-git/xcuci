package com.example.xcuci.data.repository

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Body

interface DebugService {

    // Test root endpoint
    @GET(".")
    fun getRootRaw(): Call<String>

    // Test orders endpoint
    @GET("orders")
    fun getOrdersRaw(): Call<String>

    // Test single order endpoint
    @GET("orders/{id}")
    fun getOrderRaw(@Path("id") id: Int): Call<String>

    // Test create order (dengan raw string)
    @POST("orders")
    fun createOrderRaw(@Body body: String): Call<String>

    // Test lainnya sesuai kebutuhan
    @GET("index.php")
    fun getIndexRaw(): Call<String>
}