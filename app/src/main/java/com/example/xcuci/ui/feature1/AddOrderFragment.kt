package com.example.xcuci.ui.feature1

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xcuci.App
import com.example.xcuci.R
import com.example.xcuci.data.model.Customer
import com.example.xcuci.data.model.HandukSize
import com.example.xcuci.data.model.Order
import com.example.xcuci.data.model.OrderRequest
import com.example.xcuci.databinding.FragmentAddOrderBinding
import com.example.xcuci.ui.adapter.CustomDialogAdapter
import com.example.xcuci.ui.adapter.CustomerDialogAdapter
import com.example.xcuci.utils.CounterManager
import com.example.xcuci.utils.CustomerInputHelper
import com.example.xcuci.utils.CustomerManager
import com.example.xcuci.utils.DateHelper
import com.example.xcuci.utils.HandukSizeManager
import com.example.xcuci.utils.OrderCreator
import com.example.xcuci.utils.OrderValidator
import com.example.xcuci.utils.PriceCalculator
import com.example.xcuci.utils.UIStateManager
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class AddOrderFragment : Fragment(), CustomerInputHelper.CustomerInputListener {

    private var _binding: FragmentAddOrderBinding? = null
    private val binding get() = _binding!!

    // Managers
    private lateinit var customerInputHelper: CustomerInputHelper
    private val counterManager = CounterManager()
    private val handukSizeManager = HandukSizeManager()
    private lateinit var orderValidator: OrderValidator
    private lateinit var uiStateManager: UIStateManager
    private lateinit var customerManager: CustomerManager
    private lateinit var orderCreator: OrderCreator

    // State variables
    private var completionDate: String? = null
    private var selectedDateCalendar: Calendar? = null
    private var customerList: List<Customer> = emptyList()
    private val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeManagers()
        setupUI()
        loadRecentCustomers()
        toggleHandukSizeVisibility(false)
    }

    private fun initializeManagers() {
        val app = requireActivity().application as App

        orderValidator = OrderValidator(requireContext())
        uiStateManager = UIStateManager(binding)
        customerManager = CustomerManager(app.customerRepository, viewLifecycleOwner.lifecycleScope)
        orderCreator = OrderCreator(app.orderRepository, viewLifecycleOwner.lifecycleScope)

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
    }

    private fun setupUI() {
        setupToolbar()
        setupClickListeners()
        setupDatePicker()
        setupCounterListeners()
        setupLottieAnimation()
        setupCustomerSelection()
        setupHandukSizeSelection()
        calculateTotalPrice()
        updateTotalPcs()
    }

    //region Setup Methods
    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.title = "Tambah Order Laundry"
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveOrder.setOnClickListener {
            if (validateInput()) {
                createOrder()
            }
        }
    }

    private fun setupDatePicker() {
        binding.etCompletionDate.setOnClickListener { showDatePickerDialog() }
        binding.tilCompletionDate.setEndIconOnClickListener { showDatePickerDialog() }

        binding.etCompletionDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilCompletionDate.error = null
            }
        })
    }

    private fun setupCounterListeners() {
        binding.btnPlusKaos.setOnClickListener {
            counterManager.incrementKaos()
            updateKaosCounter()
            updateTotalPcs()
        }

        binding.btnMinusKaos.setOnClickListener {
            counterManager.decrementKaos()
            updateKaosCounter()
            updateTotalPcs()
        }

        binding.btnPlusCelana.setOnClickListener {
            counterManager.incrementCelana()
            updateCelanaCounter()
            updateTotalPcs()
        }

        binding.btnMinusCelana.setOnClickListener {
            counterManager.decrementCelana()
            updateCelanaCounter()
            updateTotalPcs()
        }

        binding.btnPlusHanduk.setOnClickListener {
            counterManager.incrementHanduk()
            updateHandukCounter()
            updateTotalPcs()

            if (counterManager.countHanduk == 1) {
                toggleHandukSizeVisibility(true)
            }
            calculateTotalPrice()
            showHandukPriceInfo()
        }

        binding.btnMinusHanduk.setOnClickListener {
            counterManager.decrementHanduk()
            updateHandukCounter()
            updateTotalPcs()

            if (counterManager.countHanduk == 0) {
                toggleHandukSizeVisibility(false)
            }
            calculateTotalPrice()
            showHandukPriceInfo()
        }
    }

    private fun setupLottieAnimation() {
        binding.lottieProgress.setAnimation(R.raw.loading_animation)
        binding.lottieProgress.loop(true)
    }

    private fun setupCustomerSelection() {
        binding.tilCustomerName.setEndIconMode(com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM)
        binding.tilCustomerName.setEndIconDrawable(R.drawable.ic_arrow_drop_down)

        setupManualDropdown()
        binding.tilCustomerName.setEndIconOnClickListener { showCustomerSelectionDialog() }

        customerInputHelper.setupAllInputs(
            customerNameEditText = binding.etCustomerName,
            phoneEditText = binding.etPhone
        )
    }

    private fun setupManualDropdown() {
        binding.etCustomerName.setOnClickListener {
            if (customerList.isNotEmpty()) {
                Log.d("XBZ", "setupManualDropdown Isi")
                showCustomerSelectionDialog()
            } else {
                Log.d("XBZ", "setupManualDropdown Kosong")
                loadRecentCustomers()
            }
        }

        binding.etCustomerName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && customerList.isNotEmpty()) {
                binding.etCustomerName.postDelayed({ showCustomerSelectionDialog() }, 100)
            }
        }
    }

    private fun setupHandukSizeSelection() {
        binding.etHandukSize.setOnClickListener { showCustomHandukSizeDialog() }
        binding.tilHandukSize.setEndIconOnClickListener { showCustomHandukSizeDialog() }
    }
    //endregion

    //region Counter Methods
    private fun updateKaosCounter() {
        binding.tvCountKaos.text = counterManager.countKaos.toString()
    }

    private fun updateCelanaCounter() {
        binding.tvCountCelana.text = counterManager.countCelana.toString()
    }

    private fun updateHandukCounter() {
        binding.tvCountHanduk.text = counterManager.countHanduk.toString()
        if (counterManager.countHanduk > 0 && binding.containerHandukSize.visibility != View.VISIBLE) {
            toggleHandukSizeVisibility(true)
        } else if (counterManager.countHanduk == 0 && binding.containerHandukSize.visibility == View.VISIBLE) {
            toggleHandukSizeVisibility(false)
        }
    }

    private fun updateTotalPcs() {
        binding.tvTotalPcs.text = "${counterManager.getTotalPcs()} pcs"
    }

    private fun toggleHandukSizeVisibility(show: Boolean) {
        uiStateManager.toggleHandukSizeVisibility(show)
        if (!show) {
            handukSizeManager.resetSelection()
            binding.etHandukSize.setText("")
        }
    }
    //endregion

    //region Price Calculation
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
            val daysDifference = selectedDateCalendar?.let { DateHelper.calculateDaysDifference(it) } ?: 0

            val basePrice = PriceCalculator.calculateBasePrice(weight, pricePerKg)
            val handukAdditionalPrice = PriceCalculator.calculateHandukPrice(
                counterManager.countHanduk,
                handukSizeManager.selectedHandukSize,
                daysDifference
            )

            val totalPrice = basePrice + handukAdditionalPrice
            binding.tvTotalPrice.text = "Rp ${numberFormat.format(totalPrice)}"
            updatePriceBreakdown(basePrice, handukAdditionalPrice, daysDifference)

        } catch (e: Exception) {
            binding.tvTotalPrice.text = "Rp 0"
        }
    }

    private fun updatePriceBreakdown(basePrice: Double, handukAdditionalPrice: Int, daysDifference: Int) {
        val serviceType = PriceCalculator.getServiceTypeText(daysDifference)

        if (counterManager.countHanduk > 0 && handukSizeManager.selectedHandukSize.isNotEmpty()) {
            val handukPricePerPiece = PriceCalculator.getHandukPrice(handukSizeManager.selectedHandukSize, daysDifference)
            binding.tilPricePerKg.helperText =
                "Berat: ${numberFormat.format(basePrice)} + " +
                        "Handuk (${handukSizeManager.selectedHandukSize}): ${numberFormat.format(handukAdditionalPrice)} " +
                        "(${counterManager.countHanduk} × Rp ${numberFormat.format(handukPricePerPiece)})"
        } else {
            binding.tilPricePerKg.helperText = "Layanan: $serviceType"
        }
    }

    private fun showHandukPriceInfo() {
        if (counterManager.countHanduk > 0 && handukSizeManager.selectedHandukSize.isNotEmpty()) {
            val daysDifference = selectedDateCalendar?.let { DateHelper.calculateDaysDifference(it) } ?: 0
            val handukPricePerPiece = PriceCalculator.getHandukPrice(handukSizeManager.selectedHandukSize, daysDifference)
            val handukTotalPrice = counterManager.countHanduk * handukPricePerPiece

            Toast.makeText(
                requireContext(),
                "Handuk ${handukSizeManager.selectedHandukSize}: ${counterManager.countHanduk} × Rp $handukPricePerPiece = Rp ${numberFormat.format(handukTotalPrice)}",
                Toast.LENGTH_SHORT
            ).show()
        } else if (counterManager.countHanduk > 0) {
            Toast.makeText(requireContext(), "Pilih ukuran handuk terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }
    //endregion

    //region Date Picker
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                handleDateSelection(selectedYear, selectedMonth, selectedDay)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        val maxDateCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 4) }
        datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis
        datePickerDialog.show()
    }

    private fun handleDateSelection(year: Int, month: Int, day: Int) {
        val selectedCalendar = Calendar.getInstance().apply {
            set(year, month, day)
        }

        val daysDifference = DateHelper.calculateDaysDifference(selectedCalendar)

        if (daysDifference < 0) {
            Toast.makeText(requireContext(), "Tanggal tidak boleh sebelum hari ini", Toast.LENGTH_SHORT).show()
            return
        }

        if (daysDifference > 4) {
            Toast.makeText(requireContext(), "Maksimal 5 hari dari hari ini", Toast.LENGTH_SHORT).show()
            return
        }

        selectedDateCalendar = selectedCalendar
        completionDate = DateHelper.formatDatabaseDate(year, month, day)

        val displayDate = DateHelper.formatDisplayDate(day, month, year)
        binding.etCompletionDate.setText(displayDate)

        updatePriceBasedOnDate(daysDifference)
        calculateTotalPrice()
    }

    private fun updatePriceBasedOnDate(daysDifference: Int) {
        val pricePerKg = PriceCalculator.getPricePerKgByDays(daysDifference)
        binding.etPricePerKg.setText(pricePerKg.toString())

        val serviceType = PriceCalculator.getServiceTypeText(daysDifference)
        updateHandukPriceInfo()
        calculateTotalPrice()

        binding.tilPricePerKg.helperText = "Layanan: $serviceType"
    }

    private fun updateHandukPriceInfo() {
        val daysDifference = selectedDateCalendar?.let { DateHelper.calculateDaysDifference(it) } ?: 0
        uiStateManager.updateHandukPriceInfo(
            counterManager.countHanduk,
            handukSizeManager.selectedHandukSize,
            daysDifference
        )
    }
    //endregion

    //region Customer Management
    override fun onLoadRecentCustomers() {
        loadRecentCustomers()
    }

    override fun onSearchCustomers(query: String) {
        customerManager.searchCustomersAsync(query) { customers ->
            customerList = customers
        }
    }

    override fun onAutoFillCustomer(customer: Customer) {
        autoFillCustomerData(customer)
    }

    override suspend fun onGetCustomerByPhone(phone: String): Customer? {
        return customerManager.getCustomerByPhone(phone)
    }

    private fun loadRecentCustomers() {
        println("DEBUG: Loading recent customers...")
        Log.d("XBZ", "loadRecentCustomers")
        customerManager.loadRecentCustomersAsync { customers ->
            println("DEBUG: Customers loaded: ${customers.size}")
            Log.d("XBZ", "loadRecentCustomersAsync ${customers.size}")
            customerList = customers


            // Debug: Print customer names
            customers.forEachIndexed { index, customer ->
                Log.d("XBZ", "customers.forEachIndexed $index: ${customer.name} - ${customer.phone}")
            }
        }
    }

    // Modifikasi dialog customer dengan search
    private fun showCustomerSelectionDialog() {
        // Selalu load data terbaru saat dialog dibuka
        Log.d("XBZ","showCustomerSelectionDialog")
        loadRecentCustomersWithCallback { success ->
            if (success && customerList.isNotEmpty()) {
                Toast.makeText(requireContext(), "Ada data pelanggan", Toast.LENGTH_SHORT).show()
                showCustomerDialog()
            } else {
                Toast.makeText(requireContext(), "Tidak ada data pelanggan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRecentCustomersWithCallback(onComplete: (Boolean) -> Unit) {
        customerManager.loadRecentCustomersAsync { customers ->
            customerList = customers

            requireActivity().runOnUiThread {
                onComplete(customers.isNotEmpty())
            }
        }
    }

    private fun showCustomerDialog() {
        println("DEBUG: ===== START showCustomerDialog() =====")

        // Inflate custom dialog layout dengan search
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_customer_with_search, null)

        // Setup search functionality
        val etSearch = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etSearch)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)

        // DEBUG 1: Print customerList detail
        println("DEBUG: customerList size in dialog: ${customerList.size}")
        customerList.forEachIndexed { index, customer ->
            println("DEBUG:   [$index] ${customer.name} - ${customer.phone} (${customer.totalOrders} orders)")
        }

        // PERBAIKAN 1: Update title dengan data terbaru
        tvTitle.text = "Pilih Pelanggan (${customerList.size})"

        // Create dialog
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
            .setView(dialogView)
            .setNegativeButton("Pelanggan Baru") { d, _ ->
                clearCustomerFields()
                d.dismiss()
            }
            .create()

        val recyclerViewCustomer = dialogView.findViewById<RecyclerView>(R.id.recyclerViewCustomer)

        // DEBUG 2: Print sebelum membuat adapter
        println("DEBUG: Creating adapter with ${customerList.size} customers")

        // PERBAIKAN 2: Pastikan adapter menggunakan data terbaru
        val adapter = CustomerDialogAdapter(ArrayList(customerList)) { selectedCustomer ->
            println("DEBUG: Customer selected: ${selectedCustomer.name}")
            handleCustomerSelection(selectedCustomer)
            dialog.dismiss()
        }

        // DEBUG 3: Print setelah adapter dibuat
        println("DEBUG: Adapter created with itemCount: ${adapter.itemCount}")

        recyclerViewCustomer.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_custom)!!)
            })

            // DEBUG: Set background sementara untuk test visibility
            setBackgroundColor(ContextCompat.getColor(context, R.color.grey_100))
        }

        // DEBUG 4: Print RecyclerView setup
        println("DEBUG: RecyclerView setup completed")
        println("DEBUG: RecyclerView: $recyclerViewCustomer")
        println("DEBUG: RecyclerView layoutManager: ${recyclerViewCustomer.layoutManager}")
        println("DEBUG: RecyclerView adapter: ${recyclerViewCustomer.adapter}")

        // PERBAIKAN 3: Setup search yang benar dengan filter di adapter
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                println("DEBUG: Search query: '$query'")
                println("DEBUG: Before filter - adapter itemCount: ${adapter.itemCount}")

                // Langsung panggil filter - adapter sudah diinisialisasi
                adapter.filter(query)

                println("DEBUG: After filter - adapter itemCount: ${adapter.itemCount}")
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // PERBAIKAN 4: Setup close button
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)
        btnClose.setOnClickListener {
            println("DEBUG: Dialog closed by close button")
            dialog.dismiss()
        }

        // PERBAIKAN 5: Setup dialog window
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_custom_dialog)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )

        dialog.show()

        // DEBUG 5: Print setelah dialog show
        println("DEBUG: Dialog shown successfully")

        // PERBAIKAN 6: Tampilkan keyboard otomatis setelah dialog show
        etSearch.postDelayed({
            println("DEBUG: Showing keyboard...")
            etSearch.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT)
        }, 300)

        // PERBAIKAN 7: Setup negative button color
        val negativeButton = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        negativeButton?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        // DEBUG 6: Final check
        println("DEBUG: Final check - customerList size: ${customerList.size}")
        println("DEBUG: Final check - adapter item count: ${adapter.itemCount}")

        // DEBUG 7: Check RecyclerView visibility setelah dialog tampil
        recyclerViewCustomer.post {
            println("DEBUG: RecyclerView final status:")
            println("DEBUG:   - height: ${recyclerViewCustomer.height}")
            println("DEBUG:   - width: ${recyclerViewCustomer.width}")
            println("DEBUG:   - visibility: ${recyclerViewCustomer.visibility}")
            println("DEBUG:   - child count: ${recyclerViewCustomer.childCount}")
            println("DEBUG:   - isShown: ${recyclerViewCustomer.isShown}")
        }

        println("DEBUG: ===== END showCustomerDialog() =====")
    }

    private fun handleCustomerSelection(customer: Customer) {
        autoFillCustomerData(customer)

        // Optional: Tampilkan toast konfirmasi
        Toast.makeText(
            requireContext(),
            "Pelanggan dipilih: ${customer.name}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun clearCustomerFields() {
        binding.etCustomerName.setText("")
        binding.etPhone.setText("")
        binding.etAddress.setText("")

        // Focus ke field nama untuk input baru
        binding.etCustomerName.requestFocus()

        Toast.makeText(requireContext(), "Silakan input data pelanggan baru", Toast.LENGTH_SHORT).show()
    }

    private fun autoFillCustomerData(customer: Customer) {
        binding.etCustomerName.setText(customer.name)
        binding.etPhone.setText(customer.phone)
        binding.etAddress.setText(customer.address)
    }

    //region Handuk Size Selection
    private fun showCustomHandukSizeDialog() {
        if (counterManager.countHanduk == 0) {
            Toast.makeText(requireContext(), "Tambahkan handuk terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_header, null)
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CustomDialogAdapter(
                handukSizeManager.getHandukSizes(),
                handukSizeManager.selectedHandukSize
            ) { selectedSize ->
                handleHandukSizeSelection(selectedSize)
            }

            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_custom)!!)
            })
        }

        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
            .setCustomTitle(dialogView)
            .setView(recyclerView)
            .setNegativeButton("Batal") { d, _ -> d.dismiss() }
            .create()

        dialogView.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_custom_dialog)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.show()

        val negativeButton = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        negativeButton?.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_600))
    }

    private fun handleHandukSizeSelection(selectedSize: HandukSize) {
        handukSizeManager.selectedHandukSize = selectedSize.size
        binding.etHandukSize.setText(selectedSize.size)
        updateHandukPriceInfo()
        calculateTotalPrice()
    }
    //endregion

    //region Order Creation
    private fun validateInput(): Boolean {
        return when (val result = orderValidator.validateInput(
            customerName = binding.etCustomerName.text.toString(),
            phone = binding.etPhone.text.toString(),
            address = binding.etAddress.text.toString(),
            weight = binding.etWeight.text.toString(),
            completionDate = binding.etCompletionDate.text.toString(),
            pricePerKg = binding.etPricePerKg.text.toString(),
            countHanduk = counterManager.countHanduk,
            selectedHandukSize = handukSizeManager.selectedHandukSize,
            totalPcs = counterManager.getTotalPcs(),
            selectedDateCalendar = selectedDateCalendar
        )) {
            is OrderValidator.ValidationResult.Success -> true
            is OrderValidator.ValidationResult.Error -> {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    private fun createOrder() {
        val customerName = binding.etCustomerName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val weight = binding.etWeight.text.toString().toDouble()
        val pricePerKg = binding.etPricePerKg.text.toString().toDouble()
        val totalPcs = counterManager.getTotalPcs()

        val daysDifference = selectedDateCalendar?.let { DateHelper.calculateDaysDifference(it) } ?: 0
        val serviceType = when (daysDifference) {
            0 -> "express"
            1 -> "next_day"
            else -> "regular"
        }

        val handukAdditionalPrice = PriceCalculator.calculateHandukPrice(
            counterManager.countHanduk,
            handukSizeManager.selectedHandukSize,
            daysDifference
        )

        val orderRequest = OrderRequest(
            customerName = customerName,
            phone = phone,
            address = address,
            weight = weight,
            pricePerKg = pricePerKg,
            completionDate = completionDate,
            serviceType = serviceType,
            kaosQty = counterManager.countKaos,
            celanaQty = counterManager.countCelana,
            handukQty = counterManager.countHanduk,
            handukSize = handukSizeManager.selectedHandukSize,
            handukPrice = handukAdditionalPrice,
            totalPcs = totalPcs
        )

        uiStateManager.setInputEnabled(false)

        orderCreator.createOrder(
            orderRequest = orderRequest,
            onLoading = { loading ->
                uiStateManager.setLoading(loading)
            },
            onSuccess = { order ->
                handleOrderSuccess(order)
            },
            onError = { errorMessage ->
                handleOrderError(errorMessage)
            }
        )
    }

    private fun handleOrderSuccess(order: Order?) {
        uiStateManager.setInputEnabled(true)
        val message = "Order berhasil dibuat${if (order?.id ?: 0 < 0) " (Offline)" else ""}"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun handleOrderError(errorMessage: String) {
        uiStateManager.setInputEnabled(true)
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }
    //endregion

    override fun onDestroyView() {
        super.onDestroyView()
        customerInputHelper.cleanup()
        _binding = null
    }
}