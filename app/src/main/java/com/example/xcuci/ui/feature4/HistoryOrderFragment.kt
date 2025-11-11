package com.example.xcuci.ui.feature4

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.xcuci.App
import com.example.xcuci.data.local.database.AppDatabase
import com.example.xcuci.data.model.Order
import com.example.xcuci.data.repository.OrderRepository
import com.example.xcuci.data.repository.RetrofitClient
import com.example.xcuci.databinding.FragmentHistoryOrderBinding
import com.example.xcuci.ui.adapter.OrderHistoryAdapter
import com.example.xcuci.ui.feature2.OrderDetailActivity
import com.example.xcuci.utils.LoadingUtils
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import kotlin.collections.filter

class HistoryOrderFragment : Fragment() {

    private var _binding: FragmentHistoryOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPagerAdapter: OrderHistoryAdapter // GUNAKAN OrderHistoryAdapter
    private var allOrders: List<Order> = emptyList()
    private var filteredOrders: List<Order> = emptyList()

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
        setupSearch()
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
        binding.viewPager.setCurrentItem(1, false)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterOrders(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadOrders() {
//        LoadingUtils.showLoading(binding.lottieProgress, recyclerView = null)
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
//                LoadingUtils.hideLoading(binding.lottieProgress, recyclerView = null)
                // SEMBUNYIKAN LOADING
                LoadingUtils.hideLoadingForHistory(binding.lottieProgress)

                // Tampilkan search kembali (ViewPager & TabLayout akan ditampilkan di checkEmptyState)
                binding.tilSearch.visibility = View.VISIBLE

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
            binding.tabLayout.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.viewPager.visibility = View.VISIBLE
            binding.tabLayout.visibility = View.VISIBLE
        }
    }

    fun openOrderDetail(orderId: Int) {
        OrderDetailActivity.start(requireContext() as AppCompatActivity, orderId)
    }

    fun refreshData() {
        loadOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}