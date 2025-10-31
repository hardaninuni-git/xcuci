package com.example.xcuci

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xcuci.data.model.Order
import com.example.xcuci.data.repository.RetrofitClient
import com.example.xcuci.databinding.ActivityMainBinding
import com.example.xcuci.ui.adapter.OrderAdapter
import com.example.xcuci.ui.feature1.AddOrderActivity
import com.example.xcuci.ui.feature2.OrderDetailActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.xcuci.data.local.database.AppDatabase
import com.example.xcuci.data.repository.OrderRepository
import com.example.xcuci.ui.feature1.AddOrderFragment
import com.example.xcuci.ui.feature1.OrdersFragment
import com.example.xcuci.ui.feature3.SettingsActivity
import com.example.xcuci.ui.feature3.SettingsFragment
import com.example.xcuci.ui.feature4.PlaceholderFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupFab()

        // Load default fragment (OrdersFragment)
        if (savedInstanceState == null) {
            showFragment(OrdersFragment())
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_orders -> {
                    showFragment(OrdersFragment())
                    true
                }
                R.id.nav_customers -> {
                    showFragment(AddOrderFragment())
                    true
                }
                R.id.nav_reports -> {
                    showFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        // Set selected item
        binding.bottomNavigation.selectedItemId = R.id.nav_orders
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showPlaceholderFragment(title: String) {
        val fragment = PlaceholderFragment.newInstance(title)
        showFragment(fragment)
        Toast.makeText(this, "$title - Coming Soon", Toast.LENGTH_SHORT).show()
    }

    private fun setupFab() {
        binding.fabAddOrder.setOnClickListener {
            // Navigate to AddOrderFragment
            showAddOrderFragment()
        }
    }

    private fun showAddOrderFragment() {
        val fragment = AddOrderFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("add_order")
            .commit()
    }
}