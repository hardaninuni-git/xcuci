package com.example.xcuci.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("XCuciPrefs", Context.MODE_PRIVATE)

    // Theme
    fun setTheme(theme: String) {
        sharedPreferences.edit().putString("app_theme", theme).apply()
    }

    fun getTheme(): String {
        return sharedPreferences.getString("app_theme", "system") ?: "system"
    }

    // Notifications
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }

    // Auto Sync
    fun setAutoSyncEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("auto_sync_enabled", enabled).apply()
    }

    fun isAutoSyncEnabled(): Boolean {
        return sharedPreferences.getBoolean("auto_sync_enabled", true)
    }

    // Default Price
    fun setDefaultPricePerKg(price: Int) {
        sharedPreferences.edit().putInt("default_price_per_kg", price).apply()
    }

    fun getDefaultPricePerKg(): Int {
        return sharedPreferences.getInt("default_price_per_kg", 0)
    }

    // Printer Settings
    fun setPrinterName(printerName: String) {
        sharedPreferences.edit().putString("printer_name", printerName).apply()
    }

    fun getPrinterName(): String? {
        return sharedPreferences.getString("printer_name", null)
    }

    // Clear all preferences
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}