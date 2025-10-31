package com.example.xcuci.data.local.dao

import androidx.room.*
import com.example.xcuci.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    fun getAllOrders(): Flow<List<OrderEntity>> // Untuk future enhancement

    // Method synchronous untuk kompatibilitas dengan callback pattern
    @Query("SELECT * FROM orders ORDER BY created_at DESC")
    fun getAllOrdersSync(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Int): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Int)

    @Query("SELECT * FROM orders WHERE is_synced = 0")
    suspend fun getUnsyncedOrders(): List<OrderEntity>

    @Query("UPDATE orders SET is_synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}