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
import com.example.xcuci.data.model.OrderRequest
import com.example.xcuci.data.repository.OrderRepository
import com.example.xcuci.databinding.FragmentAddOrderBinding
import com.example.xcuci.ui.adapter.CustomDialogAdapter
import com.example.xcuci.ui.adapter.CustomDropdownAdapter
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

    // Harga per item
    private val hargaKaos = 0 // atau sesuaikan dengan kebutuhan
    private val hargaCelana = 0 // atau sesuaikan dengan kebutuhan
    private val hargaHanduk = 3000 // Harga tambahan per handuk

    // Ukuran dan harga handuk
    private var selectedHandukSize: String = ""
    private val handukPrices = mapOf(
        // S: 1-5 hari (5000, 7000, 8000, 9000, 10000)
        "S" to mapOf(0 to 5000, 1 to 7000, 2 to 8000, 3 to 9000, 4 to 10000),

        // M: 1-5 hari (6000, 8000, 9000, 10000, 11000)
        "M" to mapOf(0 to 6000, 1 to 8000, 2 to 9000, 3 to 10000, 4 to 11000),

        // L: 1-5 hari (7000, 8000, 9000, 10000, 11000)
        "L" to mapOf(0 to 7000, 1 to 8000, 2 to 9000, 3 to 10000, 4 to 11000),

        // XL: 1-5 hari (8000, 9000, 10000, 11000, 12000)
        "XL" to mapOf(0 to 8000, 1 to 9000, 2 to 10000, 3 to 11000, 4 to 12000)
    )
    // Daftar ukuran handuk
    private val handukSizes = listOf("S", "M", "L", "XL")

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
        setupHandukSizeSelection() // Setup tetap dipanggil, tapi visibility diatur oleh counter
        calculateTotalPrice()
        updateTotalPcs()

        // Load recent customers saat pertama kali buka
        loadRecentCustomers()

        // Pastikan ukuran handuk disembunyikan di awal
        toggleHandukSizeVisibility(false)
    }

    private val handukSizesData = listOf(
        HandukSize("S", "Kecil"),
        HandukSize("M", "Sedang"),
        HandukSize("L", "Besar"),
        HandukSize("XL", "Extra Large")
    )

    private fun setupHandukSizeSelection() {

        // Setup click listener untuk custom dialog
        binding.etHandukSize.setOnClickListener {
            showCustomHandukSizeDialog()
        }

        binding.tilHandukSize.setEndIconOnClickListener {
            showCustomHandukSizeDialog()
        }
    }

    // TAMBAHKAN FUNCTION INI - Custom dialog yang lebih menarik
    private fun showCustomHandukSizeDialog() {
        if (countHanduk == 0) {
            Toast.makeText(requireContext(), "Tambahkan handuk terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Inflate custom dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_header, null)
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CustomDialogAdapter(handukSizesData, selectedHandukSize) { selectedSize ->
                // Handle item selection
                selectedHandukSize = selectedSize.size
                binding.etHandukSize.setText(selectedSize.size)
                updateHandukPriceInfo()
                calculateTotalPrice()
//                dialog.dismiss()
            }

            // Add divider
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_custom)!!)
            })
        }

        // Create custom dialog
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
            .setCustomTitle(dialogView)
            .setView(recyclerView)
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        // Setup close button
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Custom dialog window
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_custom_dialog)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.show()

        // Custom button styling
        val negativeButton = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
        negativeButton?.setTextColor(resources.getColor(R.color.grey_600, null))
    }

    // MODIFIKASI FUNCTION INI - Show handuk size selection dialog
    private fun showHandukSizeSelectionDialog() {
        if (countHanduk == 0) {
            Toast.makeText(requireContext(), "Tambahkan handuk terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val sizeDescriptions = mapOf(
            "S" to "Kecil (Rp 5.000 - 10.000)",
            "M" to "Sedang (Rp 6.000 - 11.000)",
            "L" to "Besar (Rp 7.000 - 11.000)",
            "XL" to "Extra Large (Rp 8.000 - 12.000)"
        )

        val sizeNames = handukSizes.map {
            "$it - ${sizeDescriptions[it]}"
        }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pilih Ukuran Handuk")
            .setItems(sizeNames) { _, which ->
                val selectedSize = handukSizes[which]
                selectedHandukSize = selectedSize
                binding.etHandukSize.setText(selectedSize)
                updateHandukPriceInfo()
                calculateTotalPrice()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // MODIFIKASI FUNCTION INI - Update handuk price info
    private fun updateHandukPriceInfo() {
        if (countHanduk > 0 && selectedHandukSize.isNotEmpty()) {
            val daysDifference = selectedDateCalendar?.let { calculateDaysDifference(it) } ?: 0
            val price = getHandukPrice(selectedHandukSize, daysDifference)

            val priceRange = when (selectedHandukSize) {
                "S" -> "Rp 2.000 - 1.200"
                "M" -> "Rp 3.000 - 1.800"
                "L" -> "Rp 4.000 - 2.400"
                "XL" -> "Rp 5.000 - 3.000"
                else -> "-"
            }

            binding.tvHandukPriceInfo.text = "Harga $selectedHandukSize: Rp ${numberFormat.format(price)}/pcs ($priceRange)"
            binding.tvHandukPriceInfo.visibility = View.VISIBLE
        } else if (countHanduk > 0) {
            binding.tvHandukPriceInfo.text = "Pilih ukuran handuk terlebih dahulu"
            binding.tvHandukPriceInfo.visibility = View.VISIBLE
        } else {
            binding.tvHandukPriceInfo.visibility = View.GONE
        }
    }

    // TAMBAHKAN FUNCTION INI - Dapatkan harga handuk berdasarkan ukuran dan hari
    private fun getHandukPrice(size: String, daysDifference: Int): Int {
        val adjustedDays = if (daysDifference > 4) 4 else daysDifference
        return handukPrices[size]?.get(adjustedDays) ?: 0
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

        // Handuk Counter - MODIFIKASI DENGAN TOGGLE VISIBILITY
        binding.btnPlusHanduk.setOnClickListener {
            countHanduk++
            updateHandukCounter()
            updateTotalPcs()

            // Tampilkan pilihan ukuran jika handuk > 0
            if (countHanduk == 1) {
                toggleHandukSizeVisibility(true)
            }

            calculateTotalPrice()
            showHandukPriceInfo()
        }

        binding.btnMinusHanduk.setOnClickListener {
            if (countHanduk > 0) {
                countHanduk--
                updateHandukCounter()
                updateTotalPcs()
                // Sembunyikan pilihan ukuran jika handuk = 0
                if (countHanduk == 0) {
                    toggleHandukSizeVisibility(false)
                }
                calculateTotalPrice() // Tambahkan ini
                showHandukPriceInfo() // Tampilkan info harga handuk
            }
        }
    }

    // MODIFIKASI FUNCTION INI - Show handuk price info
    private fun showHandukPriceInfo() {
        if (countHanduk > 0 && selectedHandukSize.isNotEmpty()) {
            val daysDifference = selectedDateCalendar?.let { calculateDaysDifference(it) } ?: 0
            val handukPricePerPiece = getHandukPrice(selectedHandukSize, daysDifference)
            val handukTotalPrice = countHanduk * handukPricePerPiece

            Toast.makeText(
                requireContext(),
                "Handuk $selectedHandukSize: $countHanduk × Rp $handukPricePerPiece = Rp ${numberFormat.format(handukTotalPrice)}",
                Toast.LENGTH_SHORT
            ).show()
        } else if (countHanduk > 0) {
            Toast.makeText(
                requireContext(),
                "Pilih ukuran handuk terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateHandukPriceDisplay(handukPrice: Long) {
        // Jika ada TextView khusus untuk harga handuk
        // binding.tvHandukPrice.text = "Rp ${numberFormat.format(handukPrice)}"

        // Atau tampilkan di helper text
        binding.tilWeight.helperText = "Harga handuk: Rp ${numberFormat.format(handukPrice)}"
    }

    private fun updateKaosCounter() {
        binding.tvCountKaos.text = countKaos.toString()
    }

    private fun updateCelanaCounter() {
        binding.tvCountCelana.text = countCelana.toString()
    }

    private fun updateHandukCounter() {
        binding.tvCountHanduk.text = countHanduk.toString()
        // Update visibility berdasarkan jumlah handuk
        if (countHanduk > 0 && binding.containerHandukSize.visibility != View.VISIBLE) {
            toggleHandukSizeVisibility(true)
        } else if (countHanduk == 0 && binding.containerHandukSize.visibility == View.VISIBLE) {
            toggleHandukSizeVisibility(false)
        }
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
            0 -> 15000
            1 -> 12000
            2 -> 10000
            3 -> 8000
            4 -> 7000
            else -> 6000
        }

        binding.etPricePerKg.setText(pricePerKg.toString())

        val serviceType = when (daysDifference) {
            0 -> "Express (Hari Ini)"
            1 -> "Besok"
            else -> "${daysDifference + 1} Hari Lagi"
        }
        // Update info harga handuk juga
        updateHandukPriceInfo()
        calculateTotalPrice()

        binding.tilPricePerKg.helperText = "Layanan: $serviceType"
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        return monthNames[month]
    }

    // MODIFIKASI FUNCTION INI - Calculate total price dengan harga handuk dinamis
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

            // Hitung harga berdasarkan berat
            val basePrice = weight * pricePerKg

            // Hitung harga handuk berdasarkan ukuran dan hari (hanya jika ada handuk dan ukuran dipilih)
            var handukAdditionalPrice = 0
            if (countHanduk > 0 && selectedHandukSize.isNotEmpty()) {
                val daysDifference = selectedDateCalendar?.let { calculateDaysDifference(it) } ?: 0
                val handukPricePerPiece = getHandukPrice(selectedHandukSize, daysDifference)
                handukAdditionalPrice = countHanduk * handukPricePerPiece // Convert Int to Long
            }

            // Total harga
            val totalPrice = basePrice + handukAdditionalPrice

            binding.tvTotalPrice.text = "Rp ${numberFormat.format(totalPrice)}"

            // Tampilkan breakdown harga
            updatePriceBreakdown(basePrice, handukAdditionalPrice)

        } catch (e: NumberFormatException) {
            binding.tvTotalPrice.text = "Rp 0"
        } catch (e: Exception) {
            binding.tvTotalPrice.text = "Rp 0"
        }
    }

    // TAMBAHKAN FUNCTION INI - Update breakdown harga
    private fun updatePriceBreakdown(basePrice: Double, handukAdditionalPrice: Int) {
        val daysDifference = selectedDateCalendar?.let { calculateDaysDifference(it) } ?: 0
        val serviceType = getServiceTypeText()

        if (countHanduk > 0 && selectedHandukSize.isNotEmpty()) {
            val handukPricePerPiece = getHandukPrice(selectedHandukSize, daysDifference)
            binding.tilPricePerKg.helperText =
                "Berat: ${numberFormat.format(basePrice)} + " +
                        "Handuk ($selectedHandukSize): ${numberFormat.format(handukAdditionalPrice)} " +
                        "(${countHanduk} × Rp ${numberFormat.format(handukPricePerPiece)})"
        } else {
            binding.tilPricePerKg.helperText = "Layanan: $serviceType"
        }
    }

    private fun getServiceTypeText(): String {
        val daysDifference = selectedDateCalendar?.let { calculateDaysDifference(it) } ?: 0
        return when (daysDifference) {
            0 -> "Express (Hari Ini)"
            1 -> "Besok"
            else -> "${daysDifference + 1} Hari Lagi"
        }
    }

    // TAMBAHKAN FUNCTION INI - Toggle visibility ukuran handuk
    private fun toggleHandukSizeVisibility(show: Boolean) {
        if (show) {
            binding.containerHandukSize.visibility = View.VISIBLE
            binding.tvHandukPriceInfo.visibility = View.VISIBLE
        } else {
            binding.containerHandukSize.visibility = View.GONE
            binding.tvHandukPriceInfo.visibility = View.GONE
            // Reset pilihan ukuran ketika disembunyikan
            selectedHandukSize = ""
            binding.etHandukSize.setText("")
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveOrder.setOnClickListener {
            if (validateInput()) {
                createOrder()
            }
        }

        // MODIFIKASI BACK BUTTON - Simple dan clean
        binding.toolbar.setNavigationOnClickListener {
            // Biarkan Activity handle back navigation
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    // MODIFIKASI FUNCTION INI - Validasi input
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

            // VALIDASI UKURAN HANDUK JIKA ADA HANDUK
            if (countHanduk > 0 && selectedHandukSize.isEmpty()) {
                Toast.makeText(requireContext(), "Pilih ukuran handuk terlebih dahulu", Toast.LENGTH_SHORT).show()
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

        // Hitung harga handuk
        var handukAdditionalPrice = 0
        if (countHanduk > 0 && selectedHandukSize.isNotEmpty()) {
            val handukPricePerPiece = getHandukPrice(selectedHandukSize, daysDifference)
            handukAdditionalPrice = countHanduk * handukPricePerPiece
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
            handukSize = selectedHandukSize, // Tambahkan ukuran handuk
            handukPrice = handukAdditionalPrice, // Tambahkan harga handuk
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

    // MODIFIKASI FUNCTION INI - Set input enabled
    private fun setInputEnabled(enabled: Boolean) {
        with(binding) {
            etCustomerName.isEnabled = enabled
            etPhone.isEnabled = enabled
            etAddress.isEnabled = enabled
            etWeight.isEnabled = enabled
            etPricePerKg.isEnabled = enabled
            etCompletionDate.isEnabled = enabled
            etHandukSize.isEnabled = enabled // Tambahkan ini
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