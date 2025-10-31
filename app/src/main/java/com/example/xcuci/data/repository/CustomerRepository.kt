package com.example.xcuci.data.repository

import com.example.xcuci.data.local.dao.CustomerDao
import com.example.xcuci.data.model.Customer

class CustomerRepository(private val customerDao: CustomerDao) {

    suspend fun getAllCustomers(): List<Customer> {
        return customerDao.getAllCustomers()
    }

    suspend fun searchCustomers(query: String): List<Customer> {
        return if (query.isBlank()) {
            customerDao.getRecentCustomers(10)
        } else {
            customerDao.searchCustomers("%$query%")
        }
    }

    suspend fun getRecentCustomers(limit: Int = 10): List<Customer> {
        return customerDao.getRecentCustomers(limit)
    }

    suspend fun getCustomerByPhone(phone: String): Customer? {
        return customerDao.getCustomerByPhone(phone)
    }
}