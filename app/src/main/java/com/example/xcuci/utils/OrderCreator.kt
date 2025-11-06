package com.example.xcuci.utils

import androidx.lifecycle.LifecycleCoroutineScope
import com.example.xcuci.data.model.Order
import com.example.xcuci.data.model.OrderRequest
import com.example.xcuci.data.repository.OrderRepository
import kotlinx.coroutines.launch

// manager/OrderCreator.kt
class OrderCreator(
    private val orderRepository: OrderRepository,
    private val lifecycleScope: LifecycleCoroutineScope
) {

    fun createOrder(
        orderRequest: OrderRequest,
        onLoading: (Boolean) -> Unit = {},
        onSuccess: (Order?) -> Unit,
        onError: (String) -> Unit
    ) {
        onLoading(true)

        orderRepository.createOrder(orderRequest) { result ->
            lifecycleScope.launch {
                onLoading(false)

                // Handle result based on your repository implementation
                when {
                    result.isSuccess -> {
                        val order = result.getOrNull()
                        onSuccess(order)
                    }
                    else -> {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Gagal membuat order"
                        onError(errorMessage)
                    }
                }
            }
        }
    }
}