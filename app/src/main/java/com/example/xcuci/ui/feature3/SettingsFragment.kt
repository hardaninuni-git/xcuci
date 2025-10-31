package com.example.xcuci.ui.feature3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.xcuci.databinding.FragmentSettingsBinding
import com.example.xcuci.utils.PreferenceManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferenceManager: PreferenceManager

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
    }

    private fun setupSettings() {
        // Pindahkan logic settings dari SettingsActivity ke sini
        // ... (logic settings yang sudah kita buat sebelumnya)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}