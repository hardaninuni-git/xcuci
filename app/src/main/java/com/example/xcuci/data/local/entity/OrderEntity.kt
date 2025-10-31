package com.example.xcuci.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "customer_name")
    val customerName: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "weight")
    val weight: Double,

    @ColumnInfo(name = "price_per_kg")
    val pricePerKg: Double,

    @ColumnInfo(name = "total_price")
    val totalPrice: Double,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String,

    @ColumnInfo(name = "completion_date")
    val completionDate: String?, // Tambahkan

    @ColumnInfo(name = "service_type")
    val serviceType: String? = null, // Tambahkan

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false, // TAMBAHKAN KOMA DI SINI

    // TAMBAHKAN FIELD UNTUK DETAIL ITEM
    @ColumnInfo(name = "kaos_qty")
    val kaosQty: Int,

    @ColumnInfo(name = "celana_qty")
    val celanaQty: Int,

    @ColumnInfo(name = "handuk_qty")
    val handukQty: Int,

    @ColumnInfo(name = "total_pcs")
    val totalPcs: Int
)