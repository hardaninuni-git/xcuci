package com.example.xcuci.ui.feature3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.xcuci.R
import com.example.xcuci.databinding.ActivitySettingsBinding
import com.example.xcuci.utils.PreferenceManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        setupToolbar()
        setupClickListeners()
        loadCurrentSettings()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Pengaturan"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupClickListeners() {
        // Theme Settings
        binding.cardTheme.setOnClickListener {
            showThemeDialog()
        }

        // Printer Settings
        binding.cardPrinter.setOnClickListener {
            openPrinterSettings()
        }

        // Notification Settings
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setNotificationsEnabled(isChecked)
            showToast("Notifikasi ${if (isChecked) "diaktifkan" else "dimatikan"}")
        }

        // Auto Sync
        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            preferenceManager.setAutoSyncEnabled(isChecked)
            showToast("Auto sync ${if (isChecked) "diaktifkan" else "dimatikan"}")
        }

        // Default Price
        binding.cardDefaultPrice.setOnClickListener {
            showDefaultPriceDialog()
        }

        // Data Management
        binding.cardDataManagement.setOnClickListener {
            showDataManagementDialog()
        }

        // About
        binding.cardAbout.setOnClickListener {
            showAboutDialog()
        }

        // Help & Support
        binding.cardHelp.setOnClickListener {
            openHelpAndSupport()
        }
    }

    private fun loadCurrentSettings() {
        // Load current theme
        val currentTheme = preferenceManager.getTheme()
        binding.tvCurrentTheme.text = when (currentTheme) {
            "light" -> "Mode Terang"
            "dark" -> "Mode Gelap"
            else -> "Mengikuti Sistem"
        }

        // Load other settings
        binding.switchNotifications.isChecked = preferenceManager.isNotificationsEnabled()
        binding.switchAutoSync.isChecked = preferenceManager.isAutoSyncEnabled()

        // Load default price if exists
        val defaultPrice = preferenceManager.getDefaultPricePerKg()
        if (defaultPrice > 0) {
            binding.tvDefaultPriceValue.text = "Rp ${String.format("%,d", defaultPrice)}/kg"
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Mode Terang", "Mode Gelap", "Mengikuti Sistem")
        val themeValues = arrayOf("light", "dark", "system")

        val currentTheme = preferenceManager.getTheme()
        var checkedItem = when (currentTheme) {
            "light" -> 0
            "dark" -> 1
            else -> 2
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Pilih Tema")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                checkedItem = which
            }
            .setPositiveButton("Simpan") { dialog, which ->
                val selectedTheme = themeValues[checkedItem]
                preferenceManager.setTheme(selectedTheme)
                applyTheme(selectedTheme)
                binding.tvCurrentTheme.text = themes[checkedItem]
                showToast("Tema berhasil diubah")
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun openPrinterSettings() {
        // Implement printer settings logic here
        // You can reuse your existing printer connection logic
        Toast.makeText(this, "Pengaturan Printer - Coming Soon", Toast.LENGTH_SHORT).show()
    }

    private fun showDefaultPriceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_default_price, null)
        val etDefaultPrice = dialogView.findViewById<android.widget.EditText>(R.id.etDefaultPrice)

        // Set current default price if exists
        val currentPrice = preferenceManager.getDefaultPricePerKg()
        if (currentPrice > 0) {
            etDefaultPrice.setText(currentPrice.toString())
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Harga Default per Kg")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, which ->
                val priceText = etDefaultPrice.text.toString()
                if (priceText.isNotEmpty()) {
                    val price = priceText.toIntOrNull() ?: 0
                    if (price > 0) {
                        preferenceManager.setDefaultPricePerKg(price)
                        binding.tvDefaultPriceValue.text = "Rp ${String.format("%,d", price)}/kg"
                        showToast("Harga default disimpan")
                    } else {
                        showToast("Harga harus lebih dari 0")
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDataManagementDialog() {
        val options = arrayOf("Ekspor Data", "Hapus Semua Data", "Backup ke Cloud")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Kelola Data")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> exportData()
                    1 -> showDeleteConfirmation()
                    2 -> backupToCloud()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun exportData() {
        Toast.makeText(this, "Fitur ekspor data akan segera hadir", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hapus Semua Data")
            .setMessage("Apakah Anda yakin ingin menghapus semua data? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { dialog, which ->
                deleteAllData()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteAllData() {
        // Implement delete all data logic
        Toast.makeText(this, "Fitur hapus data akan segera hadir", Toast.LENGTH_SHORT).show()
    }

    private fun backupToCloud() {
        Toast.makeText(this, "Fitur backup cloud akan segera hadir", Toast.LENGTH_SHORT).show()
    }

    private fun showAboutDialog() {
        val appVersion = packageManager.getPackageInfo(packageName, 0).versionName

        val aboutMessage = """
            XCuci Laundry Manager
            Version: $appVersion
            
            Aplikasi manajemen laundry yang memudahkan Anda mengelola order, pelanggan, dan laporan bisnis laundry.
            
            Fitur:
            • Management Order
            • Cetak Struk
            • Tracking Status
            • Laporan Keuangan
            • Multi-device Sync
            
            © 2024 XCuci Team
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Tentang Aplikasi")
            .setMessage(aboutMessage)
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun openHelpAndSupport() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@xcuci.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi XCuci")
            putExtra(Intent.EXTRA_TEXT, "Halo Team XCuci,\n\nSaya butuh bantuan mengenai:\n\n")
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Pilih aplikasi email"))
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak ada aplikasi email yang terinstall", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun start(activity: android.app.Activity) {
            val intent = Intent(activity, SettingsActivity::class.java)
            activity.startActivity(intent)
        }
    }
}