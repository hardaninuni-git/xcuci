package com.example.xcuci.ui.feature3

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.xcuci.R
import com.example.xcuci.databinding.FragmentSettingsBinding
import com.example.xcuci.utils.PreferenceManager
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
        // Pindahkan logic settings dari SettingsActivity ke sini
        // ... (logic settings yang sudah kita buat sebelumnya)
        updateUI()
    }

    private fun updateUI() {
        binding.tvLanguageTitle.text = getString(R.string.settings_language)
    }

    private fun setupClickListeners() {
        binding.cardBahasa.setOnClickListener {
            showLanguageSelectionDialog()
        }
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