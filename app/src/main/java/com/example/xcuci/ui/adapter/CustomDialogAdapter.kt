package com.example.xcuci.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xcuci.R
import com.example.xcuci.data.model.HandukSize
import com.example.xcuci.databinding.ItemCustomDialogBinding

class CustomDialogAdapter(
    private val sizes: List<HandukSize>,
    private var selectedSize: String = "",
    private val onItemClick: (HandukSize) -> Unit
) : RecyclerView.Adapter<CustomDialogAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val binding = ItemCustomDialogBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomDialogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val size = sizes[position]

        with(holder.binding) {
            // Set data
            tvSize.text = size.size
            tvDescription.text = size.description

            // Set price range
            tvPriceRange.text = getPriceRange(size.size)

            // Highlight selection
            ivCheck.visibility = if (size.size == selectedSize) View.VISIBLE else View.GONE
            containerItem.isSelected = size.size == selectedSize

            // Handle click
            containerItem.setOnClickListener {
                selectedSize = size.size // Update selected size
                onItemClick(size)
                notifyDataSetChanged() // Refresh untuk update visual selection
            }
        }
    }

    private fun getPriceRange(size: String): String {
        return when (size) {
            "S" -> "Rp 5.000 - 10.000"
            "M" -> "Rp 6.000 - 11.000"
            "L" -> "Rp 7.000 - 11.000"
            "XL" -> "Rp 8.000 - 12.000"
            else -> ""
        }
    }

    override fun getItemCount(): Int = sizes.size
}