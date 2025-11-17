package com.example.xcuci.utils

import android.content.Context
import java.util.Calendar

class OrderValidator(private val context: Context) {

    fun validateInput(
        customerName: String,
        phone: String,
        address: String,
        weight: String,
        completionDate: String,
        pricePerKg: String,
        countHanduk: Int,
        selectedHandukSize: String,
        totalPcs: Int,
        selectedDateCalendar: Calendar?
    ): ValidationResult {

        if (customerName.trim().isEmpty()) {
            return ValidationResult.Error("Nama pelanggan harus diisi")
        }

        if (phone.trim().isEmpty()) {
            return ValidationResult.Error("Nomor telepon harus diisi")
        }

        if (address.trim().isEmpty()) {
            return ValidationResult.Error("Alamat harus diisi")
        }

        if (weight.trim().isEmpty()) {
            return ValidationResult.Error("Berat harus diisi")
        }

        val weightValue = try {
            weight.toDouble()
        } catch (e: NumberFormatException) {
            return ValidationResult.Error("Berat harus angka yang valid")
        }

        if (weightValue <= 0) {
            return ValidationResult.Error("Berat harus lebih dari 0")
        }

        if (completionDate.trim().isEmpty()) {
            return ValidationResult.Error("Tanggal selesai harus diisi")
        }

        if (selectedDateCalendar != null) {
            val daysDifference = DateHelper.calculateDaysDifference(selectedDateCalendar)
            if (daysDifference < 0) {
                return ValidationResult.Error("Tanggal tidak boleh sebelum hari ini")
            }
            if (daysDifference > 5) {
                return ValidationResult.Error("Maksimal 5 hari dari hari ini")
            }
        }

        try {
            val price = pricePerKg.toDouble()
            if (price <= 0) {
                return ValidationResult.Error("Harga harus lebih dari 0")
            }
        } catch (e: NumberFormatException) {
            return ValidationResult.Error("Harga harus angka yang valid")
        }

        if (countHanduk > 0 && selectedHandukSize.isEmpty()) {
            return ValidationResult.Error("Pilih ukuran handuk terlebih dahulu")
        }

        if (totalPcs == 0) {
            return ValidationResult.Error("Minimal pilih 1 item (Kaos, Celana, atau Handuk)")
        }

        return ValidationResult.Success
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}