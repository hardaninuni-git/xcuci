package com.example.xcuci.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.xcuci.data.model.Customer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomerInputHelper(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val config: Config = Config()
) {

    private var searchJob: Job? = null
    private var phoneSearchJob: Job? = null

    data class Config(
        val customerNameDebounceMs: Long = 300,
        val phoneDebounceMs: Long = 500,
        val minPhoneLength: Int = 10,
        val enablePhoneAutoFill: Boolean = true
    )

    interface CustomerInputListener {
        fun onLoadRecentCustomers()
        fun onSearchCustomers(query: String)
        fun onAutoFillCustomer(customer: Customer)
        suspend fun onGetCustomerByPhone(phone: String): Customer?
    }

    private var listener: CustomerInputListener? = null

    fun setListener(listener: CustomerInputListener) {
        this.listener = listener
    }

    fun setupCustomerNameInput(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                handleCustomerNameInput(s?.toString())
            }
        })
    }

    fun setupPhoneInput(editText: EditText) {
        if (!config.enablePhoneAutoFill) return

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                handlePhoneInput(s?.toString())
            }
        })
    }

    private fun handleCustomerNameInput(input: String?) {
        searchJob?.cancel()

        searchJob = lifecycleScope.launch {
            delay(config.customerNameDebounceMs)

            when {
                input.isNullOrEmpty() -> listener?.onLoadRecentCustomers()
                else -> listener?.onSearchCustomers(input)
            }
        }
    }

    private fun handlePhoneInput(input: String?) {
        if (!config.enablePhoneAutoFill) return

        phoneSearchJob?.cancel()

        val phone = input?.trim()
        if (!phone.isNullOrEmpty() && phone.length >= config.minPhoneLength) {
            phoneSearchJob = lifecycleScope.launch {
                delay(config.phoneDebounceMs)
                val customer = listener?.onGetCustomerByPhone(phone)
                customer?.let { listener?.onAutoFillCustomer(it) }
            }
        }
    }

    fun setupAllInputs(
        customerNameEditText: EditText,
        phoneEditText: EditText? = null
    ) {
        setupCustomerNameInput(customerNameEditText)
        phoneEditText?.let { setupPhoneInput(it) }
    }

    fun cleanup() {
        searchJob?.cancel()
        phoneSearchJob?.cancel()
        listener = null
    }
}