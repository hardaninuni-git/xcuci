package com.example.xcuci.ui.feature4

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.xcuci.App
import com.example.xcuci.data.model.Order
import com.example.xcuci.databinding.FragmentHistoryOrderBinding
import com.example.xcuci.ui.adapter.OrderHistoryAdapter
import com.example.xcuci.utils.LoadingUtils
import com.google.android.material.tabs.TabLayoutMediator
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.filter

class HistoryOrderFragment : Fragment() {

    private var _binding: FragmentHistoryOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPagerAdapter: OrderHistoryAdapter // GUNAKAN OrderHistoryAdapter
    private var allOrders: List<Order> = emptyList()
    private var filteredOrders: List<Order> = emptyList()

    private lateinit var searchBar: MaterialSearchBar
    private val searchSuggestions = mutableListOf<String>()
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup Lottie animation menggunakan LoadingUtils
        LoadingUtils.setupLottieAnimation(binding.lottieProgress)
        setupTabs()
        setupModernSearch()
        // Delay loadOrders sampai ViewPager benar-benar siap
        binding.viewPager.postDelayed({
            loadOrders()
        }, 300) // 300ms delay
    }

    private fun setupTabs() {
        viewPagerAdapter = OrderHistoryAdapter(requireActivity()) // GUNAKAN OrderHistoryAdapter
        binding.viewPager.adapter = viewPagerAdapter

        // SET OFFSCREEN PAGE LIMIT untuk preload semua tab
        binding.viewPager.offscreenPageLimit = viewPagerAdapter.itemCount

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewPagerAdapter.getTabTitle(position)
        }.attach()

        // ⭐ INI YANG PENTING: Set default tab ke Selesai (position 1)
//        binding.viewPager.setCurrentItem(1, false)
    }

    private fun setupModernSearch(){
        searchBar = binding.searchBar

        searchBar.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {
                if (!enabled){
                    filterOrders("")
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                text?.toString()?.let { query ->
                    performSearch(query)
                }
            }

            override fun onButtonClicked(buttonCode: Int) {
                when (buttonCode) {
                    MaterialSearchBar.BUTTON_NAVIGATION -> {
                        requireActivity().onBackPressed()
                    }
                    MaterialSearchBar.BUTTON_SPEECH -> {
                        // Optional: Implement voice search
                        showVoiceSearchNotImplemented()
                    }
                }
            }
        })

        searchBar.addTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                // Real-time search dengan debounce
                s?.toString()?.let { query ->
                    performRealTimeSearch(query)
                }
            }

        })
    }

    private fun performRealTimeSearch(query: String) {
        // Cancel previous search job
        searchJob?.cancel()

        // Debounce search (300ms delay)
        searchJob = lifecycleScope.launch {
            delay(300) // Tunggu 300ms setelah user berhenti mengetik
            if (isAdded && searchBar.text.toString() == query) {
                filterOrders(query)

                // Update suggestions based on current results
                updateSearchSuggestions(query)
            }
        }
    }

    private fun performSearch(query: String) {
        filterOrders(query)
        addToSearchHistory(query)
        hideKeyboard()
    }

    private fun addToSearchHistory(query: String) {
        if (query.isNotEmpty() && !searchSuggestions.contains(query)) {
            searchSuggestions.add(0, query)
            // Keep only last 5 suggestions
            if (searchSuggestions.size > 5) {
                searchSuggestions.removeAt(searchSuggestions.lastIndex)
            }
            searchBar.setLastSuggestions(searchSuggestions)
        }
    }

    private fun updateSearchSuggestions(query: String) {
        if (query.length >= 2) { // Hanya buat suggestions untuk query panjang >= 2 karakter
            val newSuggestions = allOrders
                .filter { order ->
                    order.customerName.contains(query, true) ||
                            order.phone.contains(query, true)
                }
                .take(3) // Ambil maksimal 3 suggestions
                .map { order ->
                    "${order.customerName} - ${order.phone}"
                }

            // Update suggestions jika ada hasil
            if (newSuggestions.isNotEmpty()) {
                searchBar.setLastSuggestions(newSuggestions)
            }
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchBar.windowToken, 0)
    }

    private fun showVoiceSearchNotImplemented() {
        Toast.makeText(requireContext(), "Fitur pencarian suara belum tersedia", Toast.LENGTH_SHORT).show()
    }

    private fun loadOrders() {
        Log.d("XBZ", "loadOrders: START")

        // GUNAKAN METHOD KHUSUS UNTUK HISTORY
        LoadingUtils.showLoadingForHistory(
            lottieLoading = binding.lottieProgress,
            binding.emptyState,
            binding.viewPager
        )

        val app = requireActivity().application as App
        val orderRepository = app.orderRepository

        orderRepository.getOrders { orders ->
            requireActivity().runOnUiThread {
                allOrders = orders
                filteredOrders = allOrders
                Log.d("XBZ", "updateAllTabs loadOrders - Data ready: ${orders.size} orders")
                updateAllTabs()
                checkEmptyState()
                // SEMBUNYIKAN LOADING
                LoadingUtils.hideLoadingForHistory(binding.lottieProgress)

                Toast.makeText(
                    requireContext(),
                    "Memuat ${orders.size} order",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun filterOrders(query: String) {
        filteredOrders = if (query.isEmpty()) {
            allOrders
        } else {
            allOrders.filter { order ->
                order.customerName.contains(query, true) ||
                        order.phone.contains(query, true) ||
                        order.id.toString().contains(query)
            }
        }
        Log.d("XBZ","updateAllTabs filterOrders")
        updateAllTabs()
        checkEmptyState()
    }

    private fun updateAllTabs() {
        Log.d("XBZ", "updateAllTabs")
        for (i in 0 until viewPagerAdapter.itemCount) {
            val fragment = viewPagerAdapter.getFragment(i)
            if (fragment is OrderTabFragment) {
                Log.d("XBZ","DEBUG: Updating tab $i (${getStatusForPosition(i)})")
                fragment.updateOrders(filteredOrders)
            }
        }
    }

    private fun getStatusForPosition(position: Int): String {
        return when (position) {
            0 -> "processing"
            1 -> "completed"
            2 -> "cancelled"
            else -> ""
        }
    }

    private fun checkEmptyState() {
        if (filteredOrders.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.viewPager.visibility = View.GONE
            binding.tabLayout.visibility = View.VISIBLE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.viewPager.visibility = View.VISIBLE
            binding.tabLayout.visibility = View.VISIBLE
        }
    }

    fun refreshData() {
        loadOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}