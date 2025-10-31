package com.example.xcuci.ui.feature4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class PlaceholderFragment : Fragment() {

    companion object {
        private const val ARG_TITLE = "title"

        fun newInstance(title: String): PlaceholderFragment {
            val fragment = PlaceholderFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(requireContext()).apply {
            text = "${arguments?.getString(ARG_TITLE) ?: "Fragment"} - Coming Soon"
            textSize = 18f
            gravity = android.view.Gravity.CENTER
            setPadding(50, 50, 50, 50)
        }
    }
}