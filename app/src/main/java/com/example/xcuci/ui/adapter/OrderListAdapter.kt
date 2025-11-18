package com.example.xcuci.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.xcuci.data.model.Order
import com.example.xcuci.databinding.ItemOrderBinding
import com.example.xcuci.utils.formatCompletionDate
import java.text.NumberFormat
import java.util.Locale

class OrderListAdapter(
    private var orders: List<Order>,
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderListAdapter.OrderViewHolder>() {

    private val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))

    fun updateData(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(orders[adapterPosition])
                }
            }
        }

        fun bind(order: Order) {
            with(binding) {
                // Customer Info
                tvCustomerName.text = order.customerName
                tvPhone.text = order.phone
                tvAddress.text = order.address

                // Order Details
                tvWeight.text = "Berat: ${order.weight} kg"
                tvPricePerKg.text = "Harga: Rp ${numberFormat.format(order.pricePerKg)}/kg"
                tvTotalPrice.text = "Total: Rp ${numberFormat.format(order.totalPrice)}"

                // Status
                tvStatus.text = getStatusText(order.status)
                setStatusStyle(order.status)

                // Date
                tvDate.text = "Tanggal: ${formatDate(order.createdAt)}"
                tvCompletionDate.text = "Selesai: ${order.completionDate.formatCompletionDate()}"
                tvTipeLayanan.text = "${order.layananType}"

                // Item details
                displayItemDetails(order)
            }
        }

        private fun displayItemDetails(order: Order) {
            with(binding) {
                // Sembunyikan semua item detail terlebih dahulu
                layoutKaosDetail.visibility = View.GONE
                layoutCelanaDetail.visibility = View.GONE
                layoutHandukDetail.visibility = View.GONE
                layoutTotalPcs.visibility = View.GONE

                val hasItemDetails = order.kaosQty > 0 || order.celanaQty > 0 || order.handukQty > 0

                if (hasItemDetails) {
                    if (order.kaosQty > 0) {
                        layoutKaosDetail.visibility = View.GONE
                        tvKaosQty.text = "${order.kaosQty} pcs"
                    }
                    if (order.celanaQty > 0) {
                        layoutCelanaDetail.visibility = View.VISIBLE
                        tvCelanaQty.text = "${order.celanaQty} pcs"
                    }
                    if (order.handukQty > 0) {
                        layoutHandukDetail.visibility = View.VISIBLE
                        tvHandukQty.text = "${order.handukQty} pcs"
                    }
                    val totalPcs = order.kaosQty + order.celanaQty + order.handukQty
                    if (totalPcs > 0) {
                        layoutTotalPcs.visibility = View.VISIBLE
                        tvTotalPcs.text = "Total: $totalPcs pcs"
                    }
                }
            }
        }

        private fun getStatusText(status: String): String {
            return when (status.lowercase()) {
                "pending" -> "Menunggu"
                "processing" -> "Diproses"
                "completed" -> "Selesai"
                "cancelled" -> "Dibatalkan"
                else -> status
            }
        }

        private fun setStatusStyle(status: String) {
            val context = binding.root.context
            with(binding) {
                when (status.lowercase()) {
                    "pending" -> {
                        tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                        tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    }
                    "processing" -> {
                        tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light))
                        tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                    "completed" -> {
                        tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light))
                        tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                    "cancelled" -> {
                        tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
                        tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                }
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                if (dateString.contains(" ")) {
                    dateString.split(" ")[0]
                } else {
                    dateString
                }
            } catch (e: Exception) {
                dateString
            }
        }
    }
}