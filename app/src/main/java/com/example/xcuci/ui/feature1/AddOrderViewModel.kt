package com.example.xcuci.ui.feature1

import androidx.lifecycle.ViewModel
import com.example.xcuci.data.model.OrderRequest
import com.example.xcuci.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AddOrderViewModel constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddOrderUiState>(AddOrderUiState.Idle)
    val uiState: StateFlow<AddOrderUiState> = _uiState

    suspend fun createOrder(orderRequest: OrderRequest): Boolean {
        return try {
            _uiState.value = AddOrderUiState.Loading
            val result = orderRepository.createOrder(orderRequest) { result ->
                result.isSuccess
            }
            _uiState.value = AddOrderUiState.Success
            true
        } catch (e: Exception) {
            _uiState.value = AddOrderUiState.Error(e.message ?: "Unknown error")
            false
        }
    }
}

sealed class AddOrderUiState {
    object Idle : AddOrderUiState()
    object Loading : AddOrderUiState()
    object Success : AddOrderUiState()
    data class Error(val message: String) : AddOrderUiState()
}