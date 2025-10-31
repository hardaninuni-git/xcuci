package com.example.xcuci.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.xcuci.data.model.Customer

@Dao
interface CustomerDao {

    @Query("""
        SELECT 
            customer_name as name,
            phone,
            address,
            COUNT(*) as totalOrders,
            MAX(created_at) as lastOrderDate
        FROM orders 
        GROUP BY customer_name, phone, address
        ORDER BY MAX(created_at) DESC
    """)
    suspend fun getAllCustomers(): List<Customer>

    @Query("""
        SELECT 
            customer_name as name,
            phone,
            address,
            COUNT(*) as totalOrders,
            MAX(created_at) as lastOrderDate
        FROM orders 
        WHERE customer_name LIKE :query OR phone LIKE :query
        GROUP BY customer_name, phone, address
        ORDER BY MAX(created_at) DESC
    """)
    suspend fun searchCustomers(query: String): List<Customer>

    @Query("""
        SELECT 
            customer_name as name,
            phone,
            address,
            COUNT(*) as totalOrders,
            MAX(created_at) as lastOrderDate
        FROM orders 
        GROUP BY customer_name, phone, address
        ORDER BY MAX(created_at) DESC
        LIMIT :limit
    """)
    suspend fun getRecentCustomers(limit: Int): List<Customer>

    @Query("""
        SELECT 
            customer_name as name,
            phone,
            address,
            COUNT(*) as totalOrders,
            MAX(created_at) as lastOrderDate
        FROM orders 
        WHERE phone = :phone
        GROUP BY customer_name, phone, address
    """)
    suspend fun getCustomerByPhone(phone: String): Customer?
}