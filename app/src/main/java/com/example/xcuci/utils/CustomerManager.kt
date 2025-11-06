package com.example.xcuci.utils

import androidx.lifecycle.LifecycleCoroutineScope
import com.example.xcuci.data.model.Customer
import com.example.xcuci.data.repository.CustomerRepository
import kotlinx.coroutines.launch

class CustomerManager(
    private val customerRepository: CustomerRepository,
    private val lifecycleScope: LifecycleCoroutineScope
) {

    suspend fun loadRecentCustomers(limit: Int = 10): List<Customer> {
        return try {
            customerRepository.getRecentCustomers(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchCustomers(query: String): List<Customer> {
        return try {
            customerRepository.searchCustomers(query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCustomerByPhone(phone: String): Customer? {
        return try {
            customerRepository.getCustomerByPhone(phone)
        } catch (e: Exception) {
            null
        }
    }

    fun loadRecentCustomersAsync(limit: Int = 10, onResult: (List<Customer>) -> Unit) {
        lifecycleScope.launch {
            val customers = loadRecentCustomers(limit)
            onResult(customers)
        }
    }

    fun searchCustomersAsync(query: String, onResult: (List<Customer>) -> Unit) {
        lifecycleScope.launch {
            val customers = searchCustomers(query)
            onResult(customers)
        }
    }
}