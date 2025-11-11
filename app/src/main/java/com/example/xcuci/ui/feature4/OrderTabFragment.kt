package com.example.xcuci.ui.feature4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xcuci.data.model.Order
import com.example.xcuci.databinding.FragmentOrderTabBinding
import com.example.xcuci.ui.adapter.OrderHistoryAdapter
import com.example.xcuci.ui.adapter.OrderListAdapter
import com.example.xcuci.ui.feature2.OrderDetailActivity

class OrderTabFragment : Fragment() {

    private var _binding: FragmentOrderTabBinding? = null
    private val binding get() = _binding!!

    private var status: String = ""
    private lateinit var adapter: OrderListAdapter // GUNAKAN OrderListAdapter
    private var orders: List<Order> = emptyList()

    companion object {
        private const val ARG_STATUS = "status"

        fun newInstance(status: String): OrderTabFragment {
            val fragment = OrderTabFragment()
            val args = Bundle()
            args.putString(ARG_STATUS, status)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            status = it.getString(ARG_STATUS) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // INISIALISASI OrderListAdapter
        adapter = OrderListAdapter(emptyList()) { order ->
//            (parentFragment as? HistoryOrderFragment)?.openOrderDetail(order.id)
            // Langsung buka OrderDetailActivity tanpa melalui HistoryOrderFragment
            OrderDetailActivity.start(requireContext() as androidx.appcompat.app.AppCompatActivity, order.id)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@OrderTabFragment.adapter
        }
    }

    fun updateOrders(allOrders: List<Order>) {
        // Filter orders berdasarkan status tab ini
        val filteredOrders = allOrders.filter {
            it.status.equals(status, ignoreCase = true)
        }

        orders = filteredOrders

        // UPDATE ADAPTER
        adapter.updateData(filteredOrders)

        // Tampilkan empty state jika tidak ada data
        if (filteredOrders.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    fun refreshData(allOrders: List<Order>) {
        updateOrders(allOrders)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}