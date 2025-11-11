package com.example.xcuci

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.xcuci.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.xcuci.ui.feature1.AddOrderFragment
import com.example.xcuci.ui.feature1.OrdersFragment
import com.example.xcuci.ui.feature3.SettingsFragment
import com.example.xcuci.ui.feature4.HistoryOrderFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragmentTag: String = ORDERS_FRAGMENT_TAG

    companion object {
        private const val ORDERS_FRAGMENT_TAG = "orders_fragment"
        private const val CUSTOMERS_FRAGMENT_TAG = "customers_fragment"
        private const val REPORTS_FRAGMENT_TAG = "reports_fragment"
        private const val HISTORY_FRAGMENT_TAG = "history_fragment"
        private const val ADD_ORDER_FRAGMENT_TAG = "add_order_fragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupFab()

        // Load default fragment (OrdersFragment)
        if (savedInstanceState == null) {
            showFragment(OrdersFragment(), ORDERS_FRAGMENT_TAG, addToBackStack = false)
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_orders -> {
                    if (currentFragmentTag != ORDERS_FRAGMENT_TAG) {
                        showFragment(OrdersFragment(), ORDERS_FRAGMENT_TAG, addToBackStack = false)
                    }
                    true
                }
                R.id.nav_customers -> {
                    if (currentFragmentTag != CUSTOMERS_FRAGMENT_TAG) {
                        showFragment(AddOrderFragment(), CUSTOMERS_FRAGMENT_TAG, addToBackStack = false)
                    }
                    true
                }
                R.id.nav_history -> {
                    if (currentFragmentTag != HISTORY_FRAGMENT_TAG) {
                        showFragment(HistoryOrderFragment(), HISTORY_FRAGMENT_TAG, addToBackStack = false)
                    }
                    true
                }
                R.id.nav_reports -> {
                    if (currentFragmentTag != REPORTS_FRAGMENT_TAG) {
                        showFragment(SettingsFragment(), REPORTS_FRAGMENT_TAG, addToBackStack = false)
                    }
                    true
                }
                else -> false
            }
        }

        // Set selected item
        binding.bottomNavigation.selectedItemId = R.id.nav_orders
    }

    private fun showFragment(fragment: Fragment, tag: String, addToBackStack: Boolean = true) {
        currentFragmentTag = tag

        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)

        if (addToBackStack) {
            transaction.addToBackStack(tag)
        }

        transaction.commit()
    }

    private fun setupFab() {
        binding.fabAddOrder.setOnClickListener {
            // Navigate to AddOrderFragment dengan back stack
            showAddOrderFragment()
        }
    }

    private fun showAddOrderFragment() {
        val fragment = AddOrderFragment()
        showFragment(fragment, ADD_ORDER_FRAGMENT_TAG, addToBackStack = true)

        // Update bottom navigation selection jika perlu
        binding.bottomNavigation.selectedItemId = R.id.nav_customers
    }

    override fun onBackPressed() {
        // Cek jika ada fragment di back stack
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Handle back stack navigation
            handleBackStackNavigation()
        } else {
            // Double back press to exit
            handleExitApp()
        }
    }

    private fun handleBackStackNavigation() {
        // Pop back stack
        supportFragmentManager.popBackStack()

        // Update current fragment tag berdasarkan fragment yang aktif
        updateCurrentFragmentTag()
    }

    private fun updateCurrentFragmentTag() {
        // Cari fragment yang sedang aktif
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        currentFragment?.let { fragment ->
            when (fragment) {
                is OrdersFragment -> currentFragmentTag = ORDERS_FRAGMENT_TAG
                is AddOrderFragment -> currentFragmentTag = CUSTOMERS_FRAGMENT_TAG
                is HistoryOrderFragment -> currentFragmentTag = HISTORY_FRAGMENT_TAG
                is SettingsFragment -> currentFragmentTag = REPORTS_FRAGMENT_TAG
            }

            // Update bottom navigation selection
            updateBottomNavigationSelection()
        }
    }

    private fun updateBottomNavigationSelection() {
        when (currentFragmentTag) {
            ORDERS_FRAGMENT_TAG -> binding.bottomNavigation.selectedItemId = R.id.nav_orders
            CUSTOMERS_FRAGMENT_TAG -> binding.bottomNavigation.selectedItemId = R.id.nav_customers
            HISTORY_FRAGMENT_TAG -> binding.bottomNavigation.selectedItemId = R.id.nav_history
            REPORTS_FRAGMENT_TAG -> binding.bottomNavigation.selectedItemId = R.id.nav_reports
        }
    }

    private fun handleExitApp() {
        if (isTaskRoot) {
            // Ini adalah root activity, gunakan double back press to exit
            if (System.currentTimeMillis() - backPressedTime > 2000) {
                Toast.makeText(this, "Tekan kembali sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
                backPressedTime = System.currentTimeMillis()
            } else {
                super.onBackPressed()
            }
        } else {
            // Bukan root activity, langsung back press normal
            super.onBackPressed()
        }
    }

    private var backPressedTime: Long = 0
}