package com.example.xcuci.utils

import android.view.View
import com.airbnb.lottie.LottieAnimationView
import androidx.recyclerview.widget.RecyclerView

object LoadingUtils {

    private var isLoading = false

    fun showLoading(
        lottieLoading: LottieAnimationView,
        recyclerView: RecyclerView? = null
    ) {
        isLoading = true
        lottieLoading.visibility = View.VISIBLE
        lottieLoading.playAnimation()
        recyclerView?.visibility = View.GONE
    }

    fun hideLoading(
        lottieLoading: LottieAnimationView,
        recyclerView: RecyclerView? = null
    ) {
        isLoading = false
        lottieLoading.visibility = View.GONE
        lottieLoading.pauseAnimation()
        recyclerView?.visibility = View.VISIBLE
    }

    // Untuk loading tanpa RecyclerView (seperti di AddOrderActivity)
    fun showLoading(lottieLoading: LottieAnimationView) {
        isLoading = true
        lottieLoading.visibility = View.VISIBLE
        lottieLoading.playAnimation()
    }

    fun hideLoading(lottieLoading: LottieAnimationView) {
        isLoading = false
        lottieLoading.visibility = View.GONE
        lottieLoading.pauseAnimation()
    }

    fun isLoading(): Boolean {
        return isLoading
    }
}