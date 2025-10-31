package com.example.xcuci

import android.app.Application
import com.example.xcuci.data.local.database.AppDatabase
import com.example.xcuci.data.repository.CustomerRepository
import com.example.xcuci.data.repository.OrderRepository
import com.example.xcuci.data.repository.RetrofitClient

class App : Application() {

    companion object {
        private var instance: App? = null

        fun getInstance(): App = instance!!
    }

    lateinit var orderRepository: OrderRepository
    lateinit var customerRepository: CustomerRepository

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize repositories
        val database = AppDatabase.getDatabase(this)
        orderRepository = OrderRepository(
            database.orderDao(),
            RetrofitClient.apiService
        )
        customerRepository = CustomerRepository(database.customerDao())
    }
}