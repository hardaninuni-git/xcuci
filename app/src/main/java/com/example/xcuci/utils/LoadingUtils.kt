package com.example.xcuci.utils

import android.util.Log
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import androidx.recyclerview.widget.RecyclerView

object LoadingUtils {

    private var isLoading = false

    /**
     * Setup Lottie animation - bisa dipanggil dari class mana saja
     */
    fun setupLottieAnimation(lottieLoading: LottieAnimationView, animationResId: Int = com.example.xcuci.R.raw.loading_animation) {
        try {
            lottieLoading.setAnimation(animationResId)
            lottieLoading.loop(true)
            Log.d("LoadingUtils", "Lottie animation setup successfully")
        } catch (e: Exception) {
            Log.e("LoadingUtils", "Failed to setup Lottie animation", e)
        }
    }

    /**
     * Untuk loading dengan RecyclerView dan views lainnya
     */
    fun showLoading(
        lottieLoading: LottieAnimationView,
        recyclerView: RecyclerView? = null,
        otherViews: List<View>? = null
    ) {
        isLoading = true

        // Setup animation jika belum disetup
        if (lottieLoading.animation == null) {
            setupLottieAnimation(lottieLoading)
        }

        lottieLoading.visibility = View.VISIBLE
        lottieLoading.playAnimation()

        recyclerView?.visibility = View.GONE
        otherViews?.forEach { it.visibility = View.GONE }

        Log.d("LoadingUtils", "Loading shown")
    }

    /**
     * Untuk loading sederhana tanpa RecyclerView
     */
    fun showLoadingSimple(lottieLoading: LottieAnimationView) {
        isLoading = true

        // Setup animation jika belum disetup
        if (lottieLoading.animation == null) {
            setupLottieAnimation(lottieLoading)
        }

        lottieLoading.visibility = View.VISIBLE
        lottieLoading.playAnimation()

        Log.d("LoadingUtils", "Simple loading shown")
    }

    fun hideLoading(
        lottieLoading: LottieAnimationView,
        recyclerView: RecyclerView? = null,
        otherViews: List<View>? = null
    ) {
        isLoading = false
        lottieLoading.visibility = View.GONE
        lottieLoading.pauseAnimation()

        recyclerView?.visibility = View.VISIBLE
        otherViews?.forEach { it.visibility = View.VISIBLE }

        Log.d("LoadingUtils", "Loading hidden with views")
    }

    fun hideLoadingSimple(lottieLoading: LottieAnimationView) {
        isLoading = false
        lottieLoading.visibility = View.GONE
        lottieLoading.pauseAnimation()

        Log.d("LoadingUtils", "Simple loading hidden")
    }

    /**
     * Method khusus untuk HistoryOrderFragment dengan ViewPager2
     */
    fun showLoadingForHistory(
        lottieLoading: LottieAnimationView,
        vararg viewsToHide: View
    ) {
        isLoading = true

        // Setup animation jika belum disetup
        if (lottieLoading.animation == null) {
            setupLottieAnimation(lottieLoading)
        }

        lottieLoading.visibility = View.VISIBLE
        lottieLoading.playAnimation()

        // Sembunyikan semua views yang diberikan
        viewsToHide.forEach { it.visibility = View.GONE }

        Log.d("LoadingUtils", "Loading shown for History - ${viewsToHide.size} views hidden")
    }

    fun hideLoadingForHistory(
        lottieLoading: LottieAnimationView,
        vararg viewsToShow: View
    ) {
        isLoading = false
        lottieLoading.visibility = View.GONE
        lottieLoading.pauseAnimation()

        // Tampilkan semua views yang diberikan
        viewsToShow.forEach { it.visibility = View.VISIBLE }

        Log.d("LoadingUtils", "Loading hidden for History - ${viewsToShow.size} views shown")
    }

    fun isLoading(): Boolean {
        return isLoading
    }
}