package com.example.xcuci.ui.feature1

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.Fragment
import com.example.xcuci.data.local.database.AppDatabase
import com.example.xcuci.data.model.Order
import com.example.xcuci.data.repository.OrderRepository
import com.example.xcuci.data.repository.RetrofitClient
import com.example.xcuci.databinding.FragmentOrdersBinding
import com.example.xcuci.ui.adapter.OrderAdapter
import com.example.xcuci.ui.feature2.OrderDetailActivity

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: OrderAdapter
    private val orderList = mutableListOf<Order>()
    private lateinit var orderRepository: OrderRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize repository
        orderRepository = OrderRepository(
            AppDatabase.getDatabase(requireContext()).orderDao(),
            RetrofitClient.apiService
        )

        setupRecyclerView()
        setupLottieAnimation()
        loadOrders()

        binding.swipeRefresh.setOnRefreshListener {
            loadOrders()
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(orderList) { order ->
            val activity = requireActivity() as AppCompatActivity
            OrderDetailActivity.start(activity, order.id)
        }

        binding.recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun setupLottieAnimation() {
        binding.lottieProgress.setAnimation(com.example.xcuci.R.raw.loading_animation)
        binding.lottieProgress.loop(true)
    }

    private fun showLoading() {
        binding.lottieProgress.visibility = View.VISIBLE
        binding.lottieProgress.playAnimation()
        binding.recyclerViewOrders.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.lottieProgress.visibility = View.GONE
        binding.lottieProgress.pauseAnimation()
        binding.recyclerViewOrders.visibility = View.VISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadOrders() {
        binding.swipeRefresh.isRefreshing = true
        showLoading()

        orderRepository.getOrders { orders ->
            activity?.runOnUiThread {
                binding.swipeRefresh.isRefreshing = false
                hideLoading()
                // FILTER: Hanya ambil orders dengan status "pending"
                val pendingOrders = orders.filter { order ->
                    order.status.equals("pending", ignoreCase = true)
                }

                orderList.clear()
                orderList.addAll(pendingOrders) // GUNAKAN pendingOrders untuk menampilkan status pending
//                orderList.addAll(orders)
                orderAdapter.notifyDataSetChanged()

                if (orders.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada data order", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "${orders.size} order dimuat", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data ketika fragment kembali visible
        loadOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}