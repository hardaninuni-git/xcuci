package com.example.xcuci.data.repository

import android.util.Log
import com.example.xcuci.data.local.dao.OrderDao
import com.example.xcuci.data.local.entity.OrderEntity
import com.example.xcuci.data.model.Order
import com.example.xcuci.data.model.OrderRequest
import com.example.xcuci.data.remote.response.ApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class OrderRepository(
    private val orderDao: OrderDao,
    private val apiService: ApiService
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // GET ALL ORDERS - Compatible dengan MainActivity.loadOrders()
    fun getOrders(callback: (List<Order>) -> Unit) {
        // Coba ambil dari server dulu
        apiService.getOrders().enqueue(object : Callback<ApiResponse<List<Order>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Order>>>,
                response: Response<ApiResponse<List<Order>>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val serverOrders = response.body()?.data ?: emptyList()

                    // Simpan ke local database menggunakan coroutine
                    coroutineScope.launch {
                        orderDao.insertAllOrders(serverOrders.map { it.toEntity(isSynced = true) })

                        // Kembalikan data ke callback
                        callback(serverOrders)
                    }

                } else {
                    // Jika server gagal, ambil dari local database
                    Log.d("XBZ","ambil dari local database")
                    getOrdersFromLocal(callback)
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Order>>>, t: Throwable) {
                // Jika network error, ambil dari local database
                getOrdersFromLocal(callback)
            }
        })
    }

    // GET ORDER BY ID - Untuk OrderDetailActivity
    fun getOrder(id: Int, callback: (Order?) -> Unit) {
        // Cari di local database dulu (sesuai dengan OrderDetailActivity)
        getOrderFromLocal(id, callback)
    }

    // CREATE ORDER
    fun createOrder(orderRequest: OrderRequest, callback: (Result<Order>) -> Unit) {
        apiService.createOrder(orderRequest).enqueue(object : Callback<ApiResponse<Order>> {
            override fun onResponse(
                call: Call<ApiResponse<Order>>,
                response: Response<ApiResponse<Order>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val order = response.body()?.data!!

                    // Simpan ke local database menggunakan coroutine
                    coroutineScope.launch {
                        orderDao.insertOrder(order.toEntity(isSynced = true))
                        callback(Result.success(order))
                    }

                } else {
                    // Jika server gagal, buat offline order
                    createOrderOffline(orderRequest, callback)
                }
            }

            override fun onFailure(call: Call<ApiResponse<Order>>, t: Throwable) {
                // Jika network error, buat offline order
                createOrderOffline(orderRequest, callback)
            }
        })
    }

    // UPDATE ORDER - Versi baru untuk OrderDetailActivity
    fun updateOrder(order: Order, callback: (Result<Boolean>) -> Unit) {
        // Update di local database dulu
        coroutineScope.launch {
            orderDao.updateOrder(order.toEntity(isSynced = false))
        }

        // Coba update ke server
        val orderRequest = OrderRequest(
            customerName = order.customerName,
            phone = order.phone,
            address = order.address,
            weight = order.weight,
            pricePerKg = order.pricePerKg,
            completionDate = order.completionDate,
            status = order.status,
            // TAMBAHKAN FIELD BARU
            kaosQty = order.kaosQty,
            celanaQty = order.celanaQty,
            handukQty = order.handukQty,
            totalPcs = order.totalPcs
        )

        apiService.updateOrder(order.id, orderRequest).enqueue(object : Callback<ApiResponse<Order>> {
            override fun onResponse(
                call: Call<ApiResponse<Order>>,
                response: Response<ApiResponse<Order>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    // Mark as synced jika berhasil
                    coroutineScope.launch {
                        orderDao.markAsSynced(order.id)
                        callback(Result.success(true))
                    }
                } else {
                    callback(Result.success(false)) // Berhasil update lokal, tapi gagal sync
                }
            }

            override fun onFailure(call: Call<ApiResponse<Order>>, t: Throwable) {
                callback(Result.success(false)) // Berhasil update lokal, tapi gagal sync
            }
        })
    }

    // UPDATE ORDER - Versi lama (tetap dipertahankan untuk kompatibilitas)
    fun updateOrder(id: Int, orderRequest: OrderRequest, callback: (Result<Boolean>) -> Unit) {
        apiService.updateOrder(id, orderRequest).enqueue(object : Callback<ApiResponse<Order>> {
            override fun onResponse(
                call: Call<ApiResponse<Order>>,
                response: Response<ApiResponse<Order>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedOrder = response.body()?.data!!

                    // Update local database menggunakan coroutine
                    coroutineScope.launch {
                        orderDao.insertOrder(updatedOrder.toEntity(isSynced = true))
                        callback(Result.success(true))
                    }

                } else {
                    callback(Result.failure(Exception("Gagal update order")))
                }
            }

            override fun onFailure(call: Call<ApiResponse<Order>>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    // DELETE ORDER
    fun deleteOrder(id: Int, callback: (Result<Boolean>) -> Unit) {
        // Hapus dari local database dulu
        coroutineScope.launch {
            orderDao.deleteOrderById(id)
        }

        // Coba hapus dari server
        apiService.deleteOrder(id).enqueue(object : Callback<ApiResponse<String>> {
            override fun onResponse(
                call: Call<ApiResponse<String>>,
                response: Response<ApiResponse<String>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    callback(Result.success(true))
                } else {
                    callback(Result.success(false)) // Berhasil hapus lokal, tapi gagal di server
                }
            }

            override fun onFailure(call: Call<ApiResponse<String>>, t: Throwable) {
                callback(Result.success(false)) // Berhasil hapus lokal, tapi gagal di server
            }
        })
    }

    // SAVE ORDER TO LOCAL - Untuk OrderDetailActivity
    fun saveOrderToLocal(order: Order) {
        coroutineScope.launch {
            orderDao.insertOrder(order.toEntity(isSynced = true))
        }
    }

    // PRIVATE HELPER METHODS
    private fun getOrdersFromLocal(callback: (List<Order>) -> Unit) {
        coroutineScope.launch {
            val localOrders = orderDao.getAllOrdersSync().map { it.toOrder() }
            callback(localOrders)
        }
    }

    private fun getOrderFromLocal(id: Int, callback: (Order?) -> Unit) {
        coroutineScope.launch {
            val localOrder = orderDao.getOrderById(id)?.toOrder()
            callback(localOrder)
        }
    }

    private fun createOrderOffline(orderRequest: OrderRequest, callback: (Result<Order>) -> Unit) {
        coroutineScope.launch {
            val localOrder = Order(
                id = generateLocalId(),
                customerName = orderRequest.customerName,
                phone = orderRequest.phone,
                address = orderRequest.address,
                weight = orderRequest.weight,
                pricePerKg = orderRequest.pricePerKg,
                totalPrice = orderRequest.weight * orderRequest.pricePerKg,
                status = "pending",
                createdAt = getCurrentDateTime(),
                updatedAt = getCurrentDateTime(),
                completionDate = orderRequest.completionDate,
                // TAMBAHKAN FIELD BARU - INISIALISASI DENGAN NILAI 0
                kaosQty = orderRequest.kaosQty,
                celanaQty = orderRequest.celanaQty,
                handukQty = orderRequest.handukQty,
                totalPcs = orderRequest.totalPcs
            )

            orderDao.insertOrder(localOrder.toEntity(isSynced = false))
            callback(Result.success(localOrder))
        }
    }

    // MANUAL SYNC METHOD
    fun syncOrders(callback: (Boolean) -> Unit) {
        apiService.getOrders().enqueue(object : Callback<ApiResponse<List<Order>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Order>>>,
                response: Response<ApiResponse<List<Order>>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val serverOrders = response.body()?.data ?: emptyList()

                    coroutineScope.launch {
                        // Clear existing data dan simpan yang baru
                        orderDao.insertAllOrders(serverOrders.map { it.toEntity(isSynced = true) })
                        callback(true)
                    }

                } else {
                    callback(false)
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Order>>>, t: Throwable) {
                callback(false)
            }
        })
    }

    private fun generateLocalId(): Int {
        return -System.currentTimeMillis().toInt()
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
    }
}

// Extension functions untuk konversi
private fun OrderEntity.toOrder(): Order {
    return Order(
        id = this.id,
        customerName = this.customerName,
        phone = this.phone,
        address = this.address,
        weight = this.weight,
        pricePerKg = this.pricePerKg,
        totalPrice = this.totalPrice,
        status = this.status,
        completionDate = this.completionDate, // TAMBAHKAN INI
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        // TAMBAHKAN FIELD BARU
        kaosQty = this.kaosQty,
        celanaQty = this.celanaQty,
        handukQty = this.handukQty,
        totalPcs = this.totalPcs

    )
}

private fun Order.toEntity(isSynced: Boolean = true): OrderEntity {
    return OrderEntity(
        id = this.id,
        customerName = this.customerName,
        phone = this.phone,
        address = this.address,
        weight = this.weight,
        pricePerKg = this.pricePerKg,
        totalPrice = this.totalPrice,
        status = this.status,
        completionDate = this.completionDate, // TAMBAHKAN INI
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        isSynced = isSynced,
        // TAMBAHKAN FIELD BARU
        kaosQty = this.kaosQty,
        celanaQty = this.celanaQty,
        handukQty = this.handukQty,
        totalPcs = this.totalPcs
    )
}

// Extension function untuk cek apakah order offline
fun Order.isOfflineOrder(): Boolean = this.id < 0