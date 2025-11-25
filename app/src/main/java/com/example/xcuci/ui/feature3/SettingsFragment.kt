package com.example.xcuci.ui.feature3

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.xcuci.R
import com.example.xcuci.databinding.DialogPriceSettingsBinding
import com.example.xcuci.databinding.FragmentSettingsBinding
import com.example.xcuci.utils.PreferenceManager
import com.example.xcuci.utils.PriceCalculator
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager

    companion object {
        private const val LANGUAGE_PREF_KEY = "app_language"
        private const val LANGUAGE_ID = "id"
        private const val LANGUAGE_EN = "en"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        setupSettings()
        setupClickListeners()
    }

    private fun setupSettings() {
        updateUI()
    }

    private fun updateUI() {
        binding.tvLanguageTitle.text = getString(R.string.settings_language)
        binding.tvPriceTitle.text = getString(R.string.settings_default_price)
    }

    private fun setupClickListeners() {
        binding.cardDefaultPrice.setOnClickListener {
            showPriceSettingsDialog()
        }
        binding.cardBahasa.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    private fun showPriceSettingsDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_price_settings, null)
        val dialogBinding = com.example.xcuci.databinding.DialogPriceSettingsBinding.bind(dialogView)
        setupPriceDialog(dialogBinding)
        AlertDialog.Builder(requireContext())
            .setTitle("Pengaturan Harga Layanan")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, _ ->
                savePriceSettings(dialogBinding)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .setNeutralButton("Reset Default") { dialog, _ ->
                resetPriceToDefault()
                setupPriceDialog(dialogBinding) // Refresh dialog dengan harga default
                Toast.makeText(requireContext(), "Harga direset ke default", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setupPriceDialog(dialogBinding: DialogPriceSettingsBinding) {
        val context = requireContext()
        // Setup tab layout
        dialogBinding.tabLayout.addTab(dialogBinding.tabLayout.newTab().setText("Cuci & Setrika"))
        dialogBinding.tabLayout.addTab(dialogBinding.tabLayout.newTab().setText("Setrika Saja"))
        dialogBinding.tabLayout.addTab(dialogBinding.tabLayout.newTab().setText("Cuci Saja"))
        // Load initial data (Cuci & Setrika)
        loadCuciDanSetrikaPrices(dialogBinding)

        dialogBinding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                when (tab.position) {
                    0 -> loadCuciDanSetrikaPrices(dialogBinding)
                    1 -> loadSetrikaPrices(dialogBinding)
                    2 -> loadCuciPrices(dialogBinding)
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun loadCuciDanSetrikaPrices(dialogBinding: com.example.xcuci.databinding.DialogPriceSettingsBinding) {
        val context = requireContext()
        val prices = PriceCalculator.getAllServiceTypePrices(context, "cuci_dan_setrika")

        dialogBinding.serviceTitle.text = "Harga Cuci & Setrika (5 Hari)"

        // Show all 6 days
        dialogBinding.day5Container.visibility = View.VISIBLE

        // Set prices
        dialogBinding.etDay0.setText(prices[0].toString())
        dialogBinding.etDay1.setText(prices[1].toString())
        dialogBinding.etDay2.setText(prices[2].toString())
        dialogBinding.etDay3.setText(prices[3].toString())
        dialogBinding.etDay4.setText(prices[4].toString())
        dialogBinding.etDay5.setText(prices[5].toString())
    }

    private fun loadSetrikaPrices(dialogBinding: com.example.xcuci.databinding.DialogPriceSettingsBinding) {
        val context = requireContext()
        val prices = PriceCalculator.getAllServiceTypePrices(context, "setrika")

        dialogBinding.serviceTitle.text = "Harga Setrika Saja (4 Hari)"

        // Hide day 5
        dialogBinding.day5Container.visibility = View.GONE

        // Set prices
        dialogBinding.etDay0.setText(prices[0].toString())
        dialogBinding.etDay1.setText(prices[1].toString())
        dialogBinding.etDay2.setText(prices[2].toString())
        dialogBinding.etDay3.setText(prices[3].toString())
        dialogBinding.etDay4.setText(prices[4].toString())
    }

    private fun loadCuciPrices(dialogBinding: com.example.xcuci.databinding.DialogPriceSettingsBinding) {
        val context = requireContext()
        val prices = PriceCalculator.getAllServiceTypePrices(context, "cuci")

        dialogBinding.serviceTitle.text = "Harga Cuci Saja (4 Hari)"

        // Hide day 5
        dialogBinding.day5Container.visibility = View.GONE

        // Set prices
        dialogBinding.etDay0.setText(prices[0].toString())
        dialogBinding.etDay1.setText(prices[1].toString())
        dialogBinding.etDay2.setText(prices[2].toString())
        dialogBinding.etDay3.setText(prices[3].toString())
        dialogBinding.etDay4.setText(prices[4].toString())
    }

    private fun savePriceSettings(dialogBinding: com.example.xcuci.databinding.DialogPriceSettingsBinding) {
        val context = requireContext()
        val selectedTab = dialogBinding.tabLayout.selectedTabPosition

        try {
            when (selectedTab) {
                0 -> saveCuciDanSetrikaPrices(dialogBinding)
                1 -> saveSetrikaPrices(dialogBinding)
                2 -> saveCuciPrices(dialogBinding)
            }
//            updateCurrentPriceDisplay()
            Toast.makeText(requireContext(), "Harga berhasil disimpan", Toast.LENGTH_SHORT).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Pastikan semua harga diisi dengan angka", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCuciDanSetrikaPrices(dialogBinding: com.example.xcuci.databinding.DialogPriceSettingsBinding) {
        val context = requireContext()

        for (day in 0..5) {
            val price = when (day) {
                0 -> dialogBinding.etDay0.text.toString().toInt()
                1 -> dialogBinding.etDay1.text.toString().toInt()
                2 -> dialogBinding.etDay2.text.toString().toInt()
                3 -> dialogBinding.etDay3.text.toString().toInt()
                4 -> dialogBinding.etDay4.text.toString().toInt()
                5 -> dialogBinding.etDay5.text.toString().toInt()
                else -> 0
            }
            PriceCalculator.saveServiceTypePrice(context, "cuci_dan_setrika", day, price)
        }
    }

    private fun saveSetrikaPrices(dialogBinding: com.example.xcuci.databinding.DialogPriceSettingsBinding) {
        val context = requireContext()

        for (day in 0..4) {
            val price = when (day) {
                0 -> dialogBinding.etDay0.text.toString().toInt()
                1 -> dialogBinding.etDay1.text.toString().toInt()
                2 -> dialogBinding.etDay2.text.toString().toInt()
                3 -> dialogBinding.etDay3.text.toString().toInt()
                4 -> dialogBinding.etDay4.text.toString().toInt()
                else -> 0
            }
            PriceCalculator.saveServiceTypePrice(context, "setrika", day, price)
        }
    }

    private fun saveCuciPrices(dialogBinding: com.example.xcuci.databinding.DialogPriceSettingsBinding) {
        val context = requireContext()

        for (day in 0..4) {
            val price = when (day) {
                0 -> dialogBinding.etDay0.text.toString().toInt()
                1 -> dialogBinding.etDay1.text.toString().toInt()
                2 -> dialogBinding.etDay2.text.toString().toInt()
                3 -> dialogBinding.etDay3.text.toString().toInt()
                4 -> dialogBinding.etDay4.text.toString().toInt()
                else -> 0
            }
            PriceCalculator.saveServiceTypePrice(context, "cuci", day, price)
        }
    }

    private fun resetPriceToDefault() {
        PriceCalculator.resetToDefault(requireContext())
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            getString(R.string.language_indonesian),
            getString(R.string.language_english)
        )

        val currentLanguage = preferenceManager.getAppLanguage()
        val checkedItem = when (currentLanguage) {
            "id" -> 0
            "en" -> 1
            else -> 0
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_language_title))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val newLanguage = when (which) {
                    0 -> "id"
                    1 -> "en"
                    else -> "id"
                }

                if (newLanguage != currentLanguage) {
                    setAppLanguage(newLanguage)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setAppLanguage(languageCode: String) {
        // Save language preference
        preferenceManager.saveString(LANGUAGE_PREF_KEY, languageCode)

        // Update app locale
        updateLocale(requireContext(), languageCode)

        // Update display
        updateCurrentLanguageDisplay()

        // Show restart message
        showRestartAppDialog()
    }

    private fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)

        // For API 17+
        context.createConfigurationContext(configuration)

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun updateCurrentLanguageDisplay() {
        val currentLanguage = preferenceManager.getString(LANGUAGE_PREF_KEY, LANGUAGE_ID)
        val languageText = when (currentLanguage) {
            LANGUAGE_ID -> "Bahasa Indonesia"
            LANGUAGE_EN -> "English"
            else -> "Bahasa Indonesia"
        }

        // Update text in the language card if you have a TextView for it
        // If you don't have one, you can add it to the layout or just keep as is
    }

    private fun showRestartAppDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Perubahan Bahasa")
            .setMessage("Aplikasi perlu dimulai ulang untuk menerapkan perubahan bahasa. Restart sekarang?\n\nLanguage changed. App needs to restart to apply language changes. Restart now?")
            .setPositiveButton("Ya / Yes") { _, _ ->
                restartApp()
            }
            .setNegativeButton("Nanti / Later", null)
            .show()
    }

    private fun restartApp() {
        val intent = requireActivity().intent
        requireActivity().finish()
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}