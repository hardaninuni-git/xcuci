package com.example.xcuci.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.xcuci.ui.feature4.OrderTabFragment

class OrderHistoryAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    private val fragments = mutableMapOf<Int, Fragment>()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> OrderTabFragment.newInstance("processing")
            1 -> OrderTabFragment.newInstance("completed")
            2 -> OrderTabFragment.newInstance("cancelled")
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
        fragments[position] = fragment
        return fragment
    }

    fun getTabTitle(position: Int): String {
        return when (position) {
            0 -> "Diproses"
            1 -> "Selesai"
            2 -> "Dibatalkan"
            else -> ""
        }
    }

    fun getFragment(position: Int): Fragment? {
        return fragments[position]
    }

    fun getAllFragments(): List<Fragment> {
        return fragments.values.toList()
    }
}