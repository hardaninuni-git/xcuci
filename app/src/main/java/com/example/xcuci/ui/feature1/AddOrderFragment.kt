package com.example.xcuci.ui.feature1

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.xcuci.App
import com.example.xcuci.R
import com.example.xcuci.data.model.Customer
import com.example.xcuci.data.model.OrderRequest
import com.example.xcuci.data.repository.OrderRepository
import com.example.xcuci.databinding.FragmentAddOrderBinding
import com.example.xcuci.utils.CustomerInputHelper
import com.example.xcuci.utils.LoadingUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class AddOrderFragment : Fragment(), CustomerInputHelper.CustomerInputListener {

    private var _binding: FragmentAddOrderBinding? = null
    private val binding get() = _binding!!

    private lateinit var customerInputHelper: CustomerInputHelper
    private val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))
    private var completionDate: String? = null
    private var selectedDateCalendar: Calendar? = null

    // Counter variables
    private var countKaos = 0
    private var countCelana = 0
    private var countHanduk = 0

    // Customer list
    private var customerList: List<Customer> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize CustomerInputHelper
        customerInputHelper = CustomerInputHelper(
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            config = CustomerInputHelper.Config(
                customerNameDebounceMs = 300,
                phoneDebounceMs = 500,
                minPhoneLength = 10,
                enablePhoneAutoFill = true
            )
        )
        customerInputHelper.setListener(this)

        setupToolbar()
        setupClickListeners()
        setupDatePicker()
        setupCounterListeners()
        setupLottieAnimation()
        setupCustomerSelection()
        calculateTotalPrice()
        updateTotalPcs()

        // Load recent customers saat pertama kali buka
        loadRecentCustomers()
    }

    private fun setupCustomerSelection() {
        // Add dropdown icon to customer name field
        binding.tilCustomerName.setEndIconMode(com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM)
        binding.tilCustomerName.setEndIconDrawable(R.drawable.ic_arrow_drop_down)

        // Setup manual dropdown behavior
        setupManualDropdown()

        // Setup end icon click listener
        binding.tilCustomerName.setEndIconOnClickListener {
            showCustomerSelectionDialog()
        }

        // Setup customer input helper untuk handle text changes
        customerInputHelper.setupAllInputs(
            customerNameEditText = binding.etCustomerName,
            phoneEditText = binding.etPhone
        )
    }

    private fun setupManualDropdown() {
        // Setup click listener untuk show dropdown ketika field diklik
        binding.etCustomerName.setOnClickListener {
            if (customerList.isNotEmpty()) {
                showCustomerSelectionDialog()
            } else {
                loadRecentCustomers()
            }
        }

        // Setup focus listener untuk show dropdown ketika focus
        binding.etCustomerName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && customerList.isNotEmpty()) {
                // Tunda sedikit agar keyboard tidak muncul
                binding.etCustomerName.postDelayed({
                    showCustomerSelectionDialog()
                }, 100)
            }
        }
    }

    // Implementasi CustomerInputListener
    override fun onLoadRecentCustomers() {
        loadRecentCustomers()
    }

    override fun onSearchCustomers(query: String) {
        Log.d("XBZ", "onSearchCustomers query: $query")
        searchCustomers(query)
    }

    override fun onAutoFillCustomer(customer: Customer) {
        autoFillCustomerData(customer)
    }

    override suspend fun onGetCustomerByPhone(phone: String): Customer? {
        return (requireActivity().application as App).customerRepository.getCustomerByPhone(phone)
    }

    private fun setupCustomerDropdown() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())

        // Cast ke AutoCompleteTextView
        val autoCompleteTextView = binding.etCustomerName as? AutoCompleteTextView
        autoCompleteTextView?.setAdapter(adapter)

        autoCompleteTextView?.setOnItemClickListener { _, _, position, _ ->
            val selectedCustomer = customerList.getOrNull(position)
            selectedCustomer?.let { customer ->
                autoFillCustomerData(customer)
            }
        }
    }

    private fun showCustomerSelectionDialog() {
        if (customerList.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada data pelanggan", Toast.LENGTH_SHORT).show()
            return
        }

        val customerNames = customerList.map {
            "${it.name} - ${it.phone} (${it.totalOrders} order)"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pilih Pelanggan")
            .setItems(customerNames) { _, which ->
                val selectedCustomer = customerList[which]
                autoFillCustomerData(selectedCustomer)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun autoFillCustomerData(customer: Customer) {
        binding.etCustomerName.setText(customer.name)
        binding.etPhone.setText(customer.phone)
        binding.etAddress.setText(customer.address)

        // Hide keyboard
        hideKeyboard()
    }

    private fun loadRecentCustomers() {
        lifecycleScope.launch {
            try {
                customerList = (requireActivity().application as App).customerRepository.getRecentCustomers(10)
                updateCustomerDropdown()
            } catch (e: Exception) {
                Log.e("AddOrderFragment", "Error loading customers: ${e.message}")
            }
        }
    }

    private fun searchCustomers(query: String) {
        lifecycleScope.launch {
            try {
                customerList = (requireActivity().application as App).customerRepository.searchCustomers(query)
                updateCustomerDropdown()
            } catch (e: Exception) {
                Log.e("AddOrderFragment", "Error searching customers: ${e.message}")
            }
        }
    }

    private fun updateCustomerDropdown() {
        val customerNames = customerList.map {
            "${it.name} - ${it.phone} (${it.totalOrders} order)"
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            customerNames
        )

        // Cast ke AutoCompleteTextView
        val autoCompleteTextView = binding.etCustomerName as? AutoCompleteTextView
        autoCompleteTextView?.setAdapter(adapter)

        // Show dropdown jika ada hasil dan user sedang mengetik
        if (customerNames.isNotEmpty() && binding.etCustomerName.text?.isNotEmpty() == true) {
            autoCompleteTextView?.showDropDown()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.etCustomerName.windowToken, 0)
    }

    private fun setupCounterListeners() {
        // Kaos Counter
        binding.btnPlusKaos.setOnClickListener {
            countKaos++
            updateKaosCounter()
            updateTotalPcs()
        }

        binding.btnMinusKaos.setOnClickListener {
            if (countKaos > 0) {
                countKaos--
                updateKaosCounter()
                updateTotalPcs()
            }
        }

        // Celana Counter
        binding.btnPlusCelana.setOnClickListener {
            countCelana++
            updateCelanaCounter()
            updateTotalPcs()
        }

        binding.btnMinusCelana.setOnClickListener {
            if (countCelana > 0) {
                countCelana--
                updateCelanaCounter()
                updateTotalPcs()
            }
        }

        // Handuk Counter
        binding.btnPlusHanduk.setOnClickListener {
            countHanduk++
            updateHandukCounter()
            updateTotalPcs()
        }

        binding.btnMinusHanduk.setOnClickListener {
            if (countHanduk > 0) {
                countHanduk--
                updateHandukCounter()
                updateTotalPcs()
            }
        }
    }

    private fun updateKaosCounter() {
        binding.tvCountKaos.text = countKaos.toString()
    }

    private fun updateCelanaCounter() {
        binding.tvCountCelana.text = countCelana.toString()
    }

    private fun updateHandukCounter() {
        binding.tvCountHanduk.text = countHanduk.toString()
    }

    private fun updateTotalPcs() {
        val totalPcs = countKaos + countCelana + countHanduk
        binding.tvTotalPcs.text = "$totalPcs pcs"
    }

    private fun setupDatePicker() {
        // Set click listener untuk tanggal selesai
        binding.etCompletionDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Set end icon click listener
        binding.tilCompletionDate.setEndIconOnClickListener {
            showDatePickerDialog()
        }

        // Clear error ketika text berubah (tanggal dipilih)
        binding.etCompletionDate.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.tilCompletionDate.error = null
            }
        })
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                val daysDifference = calculateDaysDifference(selectedCalendar)

                if (daysDifference < 0) {
                    Toast.makeText(requireContext(), "Tanggal tidak boleh sebelum hari ini", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                if (daysDifference > 4) {
                    Toast.makeText(requireContext(), "Maksimal 5 hari dari hari ini", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                selectedDateCalendar = selectedCalendar
                completionDate = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )

                val displayDate = String.format(
                    Locale("id", "ID"),
                    "%02d %s %04d",
                    selectedDay,
                    getMonthName(selectedMonth),
                    selectedYear
                )

                binding.etCompletionDate.setText(displayDate)
                updatePriceBasedOnDate(daysDifference)
                calculateTotalPrice()
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        val maxDateCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 4)
        }
        datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis
        datePickerDialog.show()
    }

    private fun calculateDaysDifference(selectedDate: Calendar): Int {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val selected = selectedDate.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diff = selected.timeInMillis - today.timeInMillis
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun updatePriceBasedOnDate(daysDifference: Int) {
        val pricePerKg = when (daysDifference) {
            0 -> 10000
            1 -> 8000
            2 -> 7000
            3 -> 6000
            4 -> 5000
            else -> 5000
        }

        binding.etPricePerKg.setText(pricePerKg.toString())

        val serviceType = when (daysDifference) {
            0 -> "Express (Hari Ini)"
            1 -> "Besok"
            else -> "${daysDifference + 1} Hari Lagi"
        }

        binding.tilPricePerKg.helperText = "Layanan: $serviceType"
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        return monthNames[month]
    }

    private fun calculateTotalPrice() {
        try {
            val weightText = binding.etWeight.text.toString().trim()
            val priceText = binding.etPricePerKg.text.toString().trim()

            if (weightText.isEmpty() || priceText.isEmpty()) {
                binding.tvTotalPrice.text = "Rp 0"
                return
            }

            val weight = weightText.toDouble()
            val pricePerKg = priceText.toDouble()
            val totalPrice = weight * pricePerKg

            binding.tvTotalPrice.text = "Rp ${numberFormat.format(totalPrice)}"

        } catch (e: NumberFormatException) {
            binding.tvTotalPrice.text = "Rp 0"
        } catch (e: Exception) {
            binding.tvTotalPrice.text = "Rp 0"
        }
    }

    private fun setupTextWatchers() {
        // TextWatcher untuk berat
        binding.etWeight.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                calculateTotalPrice()
            }
        })

        // TextWatcher untuk harga per kg
        binding.etPricePerKg.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                calculateTotalPrice()
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSaveOrder.setOnClickListener {
            if (validateInput()) {
                createOrder()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun validateInput(): Boolean {
        with(binding) {
            if (etCustomerName.text.toString().trim().isEmpty()) {
                etCustomerName.error = "Nama pelanggan harus diisi"
                return false
            }
            if (etPhone.text.toString().trim().isEmpty()) {
                etPhone.error = "Nomor telepon harus diisi"
                return false
            }
            if (etAddress.text.toString().trim().isEmpty()) {
                etAddress.error = "Alamat harus diisi"
                return false
            }
            if (etWeight.text.toString().trim().isEmpty()) {
                etWeight.error = "Berat harus diisi"
                return false
            }

            try {
                val weight = etWeight.text.toString().toDouble()
                if (weight <= 0) {
                    etWeight.error = "Berat harus lebih dari 0"
                    return false
                }
            } catch (e: NumberFormatException) {
                etWeight.error = "Berat harus angka yang valid"
                return false
            }

            if (etCompletionDate.text.toString().trim().isEmpty()) {
                tilCompletionDate.error = "Tanggal selesai harus diisi"
                return false
            }

            if (completionDate != null) {
                val daysDifference = calculateDaysDifference(selectedDateCalendar!!)
                if (daysDifference < 0) {
                    tilCompletionDate.error = "Tanggal tidak boleh sebelum hari ini"
                    return false
                }
                if (daysDifference > 4) {
                    tilCompletionDate.error = "Maksimal 5 hari dari hari ini"
                    return false
                }
            }

            try {
                val price = etPricePerKg.text.toString().toDouble()
                if (price <= 0) {
                    etPricePerKg.error = "Harga harus lebih dari 0"
                    return false
                }
            } catch (e: NumberFormatException) {
                etPricePerKg.error = "Harga harus angka yang valid"
                return false
            }

            val totalPcs = countKaos + countCelana + countHanduk
            if (totalPcs == 0) {
                Toast.makeText(requireContext(), "Minimal pilih 1 item (Kaos, Celana, atau Handuk)", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun createOrder() {
        val customerName = binding.etCustomerName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val weight = binding.etWeight.text.toString().toDouble()
        val pricePerKg = binding.etPricePerKg.text.toString().toDouble()
        val totalPcs = countKaos + countCelana + countHanduk

        val daysDifference = selectedDateCalendar?.let { calculateDaysDifference(it) } ?: 0
        val serviceType = when (daysDifference) {
            0 -> "express"
            1 -> "next_day"
            else -> "regular"
        }

        val orderRequest = OrderRequest(
            customerName = customerName,
            phone = phone,
            address = address,
            weight = weight,
            pricePerKg = pricePerKg,
            completionDate = completionDate,
            serviceType = serviceType,
            kaosQty = countKaos,
            celanaQty = countCelana,
            handukQty = countHanduk,
            totalPcs = totalPcs
        )

        LoadingUtils.showLoading(binding.lottieProgress)
        binding.progressOverlay.visibility = View.VISIBLE
        binding.btnSaveOrder.isEnabled = false
        setInputEnabled(false)

        (requireActivity().application as App).orderRepository.createOrder(orderRequest) { result ->
            requireActivity().runOnUiThread {
                LoadingUtils.hideLoading(binding.lottieProgress)
                binding.progressOverlay.visibility = View.GONE
                binding.btnSaveOrder.isEnabled = true
                setInputEnabled(true)

                when {
                    result.isSuccess -> {
                        val order = result.getOrNull()
                        Toast.makeText(
                            requireContext(),
                            "Order berhasil dibuat${if (order?.id ?: 0 < 0) " (Offline)" else ""}",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    }
                    else -> {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Gagal membuat order"
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setInputEnabled(enabled: Boolean) {
        with(binding) {
            etCustomerName.isEnabled = enabled
            etPhone.isEnabled = enabled
            etAddress.isEnabled = enabled
            etWeight.isEnabled = enabled
            etPricePerKg.isEnabled = enabled
            etCompletionDate.isEnabled = enabled
            btnSaveOrder.isEnabled = enabled

            btnPlusKaos.isEnabled = enabled
            btnMinusKaos.isEnabled = enabled
            btnPlusCelana.isEnabled = enabled
            btnMinusCelana.isEnabled = enabled
            btnPlusHanduk.isEnabled = enabled
            btnMinusHanduk.isEnabled = enabled
        }
    }

    private fun setupLottieAnimation() {
        binding.lottieProgress.setAnimation(R.raw.loading_animation)
        binding.lottieProgress.loop(true)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.title = "Tambah Order Laundry"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        customerInputHelper.cleanup()
        _binding = null
    }
}