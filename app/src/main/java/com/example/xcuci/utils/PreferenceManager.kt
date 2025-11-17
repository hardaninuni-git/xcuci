package com.example.xcuci.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    companion object {
        private const val PREF_NAME = "XcuciPreferences"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME = "app_theme"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_AUTO_SYNC = "auto_sync_enabled"
        private const val KEY_DEFAULT_PRICE = "default_price_per_kg"
        private const val KEY_PRINTER_NAME = "printer_name"
        private const val KEY_PRINTER_ADDRESS = "printer_address"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_SHOP_ADDRESS = "shop_address"
        private const val KEY_SHOP_PHONE = "shop_phone"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ===== BOOLEAN METHODS =====
    fun saveBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    // ===== STRING METHODS =====
    fun saveString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    // ===== INT METHODS =====
    fun saveInt(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    // ===== LONG METHODS =====
    fun saveLong(key: String, value: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    // ===== FLOAT METHODS =====
    fun saveFloat(key: String, value: Float) {
        val editor = sharedPreferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    // ===== SPECIFIC PREFERENCE METHODS =====

    // First Launch
    fun isFirstLaunch(): Boolean {
        return getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunch(completed: Boolean) {
        saveBoolean(KEY_FIRST_LAUNCH, completed)
    }

    // Language Preferences
    fun getAppLanguage(): String {
        return getString(KEY_LANGUAGE, "id") // Default: Indonesian
    }

    fun setAppLanguage(languageCode: String) {
        saveString(KEY_LANGUAGE, languageCode)
    }

    // Theme Preferences
    fun getAppTheme(): String {
        return getString(KEY_THEME, "light") // Default: Light theme
    }

    fun setAppTheme(theme: String) {
        saveString(KEY_THEME, theme)
    }

    // Notifications
    fun isNotificationsEnabled(): Boolean {
        return getBoolean(KEY_NOTIFICATIONS, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        saveBoolean(KEY_NOTIFICATIONS, enabled)
    }

    // Auto Sync
    fun isAutoSyncEnabled(): Boolean {
        return getBoolean(KEY_AUTO_SYNC, true)
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        saveBoolean(KEY_AUTO_SYNC, enabled)
    }

    // Default Price
    fun getDefaultPricePerKg(): Int {
        return getInt(KEY_DEFAULT_PRICE, 5000) // Default: Rp 5,000
    }

    fun setDefaultPricePerKg(price: Int) {
        saveInt(KEY_DEFAULT_PRICE, price)
    }

    // Printer Settings
    fun getPrinterName(): String {
        return getString(KEY_PRINTER_NAME, "")
    }

    fun setPrinterName(name: String) {
        saveString(KEY_PRINTER_NAME, name)
    }

    fun getPrinterAddress(): String {
        return getString(KEY_PRINTER_ADDRESS, "")
    }

    fun setPrinterAddress(address: String) {
        saveString(KEY_PRINTER_ADDRESS, address)
    }

    // Shop Information
    fun getShopName(): String {
        return getString(KEY_SHOP_NAME, "XCUCI Laundry")
    }

    fun setShopName(name: String) {
        saveString(KEY_SHOP_NAME, name)
    }

    fun getShopAddress(): String {
        return getString(KEY_SHOP_ADDRESS, "")
    }

    fun setShopAddress(address: String) {
        saveString(KEY_SHOP_ADDRESS, address)
    }

    fun getShopPhone(): String {
        return getString(KEY_SHOP_PHONE, "")
    }

    fun setShopPhone(phone: String) {
        saveString(KEY_SHOP_PHONE, phone)
    }

    // User Information
    fun getUserName(): String {
        return getString(KEY_USER_NAME, "")
    }

    fun setUserName(name: String) {
        saveString(KEY_USER_NAME, name)
    }

    // ===== UTILITY METHODS =====

    fun clearAllPreferences() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun removeKey(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }

    fun containsKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun getAllPreferences(): Map<String, *> {
        return sharedPreferences.all
    }

    // ===== ORDER-RELATED PREFERENCES =====

    fun getLastOrderId(): Int {
        return getInt("last_order_id", 0)
    }

    fun setLastOrderId(orderId: Int) {
        saveInt("last_order_id", orderId)
    }

    fun getOrderCounter(): Int {
        return getInt("order_counter", 0)
    }

    fun incrementOrderCounter() {
        val current = getOrderCounter()
        saveInt("order_counter", current + 1)
    }

    // ===== BACKUP & SYNC PREFERENCES =====

    fun getLastSyncTime(): Long {
        return getLong("last_sync_time", 0)
    }

    fun setLastSyncTime(timestamp: Long) {
        saveLong("last_sync_time", timestamp)
    }

    fun isBackupEnabled(): Boolean {
        return getBoolean("auto_backup_enabled", false)
    }

    fun setBackupEnabled(enabled: Boolean) {
        saveBoolean("auto_backup_enabled", enabled)
    }

    // ===== UI PREFERENCES =====

    fun getListViewType(): String {
        return getString("list_view_type", "grid") // grid or list
    }

    fun setListViewType(type: String) {
        saveString("list_view_type", type)
    }

    fun getSortOrder(): String {
        return getString("sort_order", "date_desc") // date_asc, date_desc, name_asc, name_desc
    }

    fun setSortOrder(order: String) {
        saveString("sort_order", order)
    }
}