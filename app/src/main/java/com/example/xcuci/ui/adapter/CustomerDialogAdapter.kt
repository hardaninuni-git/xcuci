package com.example.xcuci.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xcuci.R
import com.example.xcuci.data.model.Customer

class CustomerDialogAdapter(
    private var customers: List<Customer>,
    private val onItemClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerDialogAdapter.ViewHolder>() {

    // PERBAIKAN: Gunakan mutable list untuk filtered data
    private var filteredCustomers: MutableList<Customer> = customers.toMutableList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.containerItem)
        val tvName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvCustomerPhone)
        val tvOrderCount: TextView = itemView.findViewById(R.id.tvOrderCount)
        val ivCheck: ImageView = itemView.findViewById(R.id.ivCheck)

        // DEBUG: Tambahkan ID untuk tracking
        init {
            Log.d("CustomerAdapter", "ViewHolder created for position: $adapterPosition")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("CustomerAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_dialog, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("CustomerAdapter", "onBindViewHolder called for position: $position")

        if (position < 0 || position >= filteredCustomers.size) {
            Log.e("CustomerAdapter", "Invalid position: $position, filteredCustomers size: ${filteredCustomers.size}")
            return
        }

        val customer = filteredCustomers[position]
        Log.d("CustomerAdapter", "Binding customer: ${customer.name} at position: $position")

        // Set data
        holder.tvName.text = customer.name
        holder.tvPhone.text = customer.phone
        holder.tvOrderCount.text = "${customer.totalOrders} order"

        // Hide checkmark untuk customer selection
        holder.ivCheck.visibility = View.GONE

        // Handle click
        holder.container.setOnClickListener {
            Log.d("CustomerAdapter", "Item clicked: ${customer.name}")
            onItemClick(customer)
        }

        // Add ripple effect
        holder.container.isClickable = true

        // DEBUG: Set background untuk test visibility
        if (position % 2 == 0) {
            holder.container.setBackgroundColor(0x10FF0000) // Light red
        } else {
            holder.container.setBackgroundColor(0x100000FF) // Light blue
        }
    }

    fun updateData(newCustomers: List<Customer>) {
        Log.d("CustomerAdapter", "updateData called with ${newCustomers.size} customers")
        this.customers = newCustomers
        this.filteredCustomers.clear()
        this.filteredCustomers.addAll(newCustomers)
        Log.d("CustomerAdapter", "Before notifyDataSetChanged, itemCount: $itemCount")
        notifyDataSetChanged()
        Log.d("CustomerAdapter", "After notifyDataSetChanged, itemCount: $itemCount")
    }

    fun filter(query: String) {
        Log.d("CustomerAdapter", "filter called with query: '$query'")
        Log.d("CustomerAdapter", "Before filter, customers size: ${customers.size}")

        filteredCustomers.clear()

        if (query.isEmpty()) {
            filteredCustomers.addAll(customers)
        } else {
            val filtered = customers.filter {
                it.name.contains(query, true) || it.phone.contains(query, true)
            }
            filteredCustomers.addAll(filtered)
        }

        Log.d("CustomerAdapter", "After filter, filteredCustomers size: ${filteredCustomers.size}")
        Log.d("CustomerAdapter", "Before notifyDataSetChanged")
        notifyDataSetChanged()
        Log.d("CustomerAdapter", "After notifyDataSetChanged, itemCount: $itemCount")
    }

    override fun getItemCount(): Int {
        val count = filteredCustomers.size
        Log.d("CustomerAdapter", "getItemCount called: $count")
        return count
    }
}