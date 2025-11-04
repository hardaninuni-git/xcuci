package com.example.xcuci.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.xcuci.R
import com.example.xcuci.data.model.HandukSize

class CustomDropdownAdapter()
//    context: Context,
//    private val sizes: List<HandukSize>,
//    private val selectedSize: String = ""
//) : ArrayAdapter<HandukSize>(context, R.layout.item_custom_dropdown, sizes) {
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        return createView(position, convertView, parent, false)
//    }
//
//    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
//        return createView(position, convertView, parent, true)
//    }
//
//    private fun createView(position: Int, convertView: View?, parent: ViewGroup, isDropdown: Boolean): View {
//        val view = convertView ?: LayoutInflater.from(context)
//            .inflate(R.layout.item_custom_dropdown, parent, false)
//
//        val size = getItem(position)
//        val tvItemText = view.findViewById<TextView>(R.id.tvItemText)
//        val ivCheck = view.findViewById<ImageView>(R.id.ivCheck)
//
//        size?.let {
//            val priceRange = when (it.size) {
//                "S" -> "Rp 5.000 - 10.000"
//                "M" -> "Rp 6.000 - 11.000"
//                "L" -> "Rp 7.000 - 11.000"
//                "XL" -> "Rp 8.000 - 12.000"
//                else -> ""
//            }
//
//            tvItemText.text = "${it.size} - ${it.description} ($priceRange)"
//
//            // Tampilkan checkmark jika ini adalah item yang dipilih
//            if (isDropdown) {
//                ivCheck.visibility = if (it.size == selectedSize) View.VISIBLE else View.GONE
//            } else {
//                ivCheck.visibility = View.GONE
//            }
//        }
//
//        return view
//    }
//}