package com.example.xcuci.utils

import android.view.View
import com.example.xcuci.databinding.FragmentAddOrderBinding
import java.text.NumberFormat
import java.util.Locale

class UIStateManager(private val binding: FragmentAddOrderBinding) {

    fun setLoading(loading: Boolean) {
        if (loading) {
            LoadingUtils.showLoading(binding.lottieProgress)
            binding.progressOverlay.visibility = View.VISIBLE
        } else {
            LoadingUtils.hideLoading(binding.lottieProgress)
            binding.progressOverlay.visibility = View.GONE
        }
    }

    fun setInputEnabled(enabled: Boolean) {
        with(binding) {
            etCustomerName.isEnabled = enabled
            etPhone.isEnabled = enabled
            etAddress.isEnabled = enabled
            etWeight.isEnabled = enabled
            etPricePerKg.isEnabled = enabled
            etCompletionDate.isEnabled = enabled
            etHandukSize.isEnabled = enabled
            btnSaveOrder.isEnabled = enabled

            btnPlusKaos.isEnabled = enabled
            btnMinusKaos.isEnabled = enabled
            btnPlusCelana.isEnabled = enabled
            btnMinusCelana.isEnabled = enabled
            btnPlusHanduk.isEnabled = enabled
            btnMinusHanduk.isEnabled = enabled
        }
    }

    fun updateCounterDisplays(kaos: Int, celana: Int, handuk: Int, totalPcs: Int) {
        binding.tvCountKaos.text = kaos.toString()
        binding.tvCountCelana.text = celana.toString()
        binding.tvCountHanduk.text = handuk.toString()
        binding.tvTotalPcs.text = "$totalPcs pcs"
    }

    fun toggleHandukSizeVisibility(show: Boolean) {
        if (show) {
            binding.containerHandukSize.visibility = View.VISIBLE
            binding.tvHandukPriceInfo.visibility = View.VISIBLE
        } else {
            binding.containerHandukSize.visibility = View.GONE
            binding.tvHandukPriceInfo.visibility = View.GONE
        }
    }

    fun updateHandukPriceInfo(countHanduk: Int, selectedSize: String, daysDifference: Int) {
        if (countHanduk > 0 && selectedSize.isNotEmpty()) {
            val price = PriceCalculator.getHandukPrice(selectedSize, daysDifference)
            val priceRange = when (selectedSize) {
                "S" -> "Rp 2.000 - 1.200"
                "M" -> "Rp 3.000 - 1.800"
                "L" -> "Rp 4.000 - 2.400"
                "XL" -> "Rp 5.000 - 3.000"
                else -> "-"
            }
            binding.tvHandukPriceInfo.text = "Harga $selectedSize: Rp ${NumberFormat.getNumberInstance(
                Locale("id", "ID")
            ).format(price)}/pcs ($priceRange)"
            binding.tvHandukPriceInfo.visibility = View.VISIBLE
        } else if (countHanduk > 0) {
            binding.tvHandukPriceInfo.text = "Pilih ukuran handuk terlebih dahulu"
            binding.tvHandukPriceInfo.visibility = View.VISIBLE
        } else {
            binding.tvHandukPriceInfo.visibility = View.GONE
        }
    }
}