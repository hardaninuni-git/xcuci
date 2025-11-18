package com.example.xcuci.ui.feature1

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import com.example.xcuci.R
import com.example.xcuci.data.model.OrderRequest
import com.example.xcuci.databinding.ActivityAddOrderBinding
import java.text.NumberFormat
import androidx.appcompat.app.AppCompatDelegate
import com.example.xcuci.data.repository.OrderRepository
import com.example.xcuci.utils.LoadingUtils
import java.util.Calendar // TAMBAHKAN INI
import android.app.DatePickerDialog // TAMBAHKAN INI
import com.example.xcuci.App

class AddOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddOrderBinding
    private lateinit var orderRepository: OrderRepository
    private val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))
    private var completionDate: String? = null
    private var selectedDateCalendar: Calendar? = null

    // Counter variables
    private var countKaos = 0
    private var countCelana = 0
    private var countHanduk = 0
    private var selectedServiceType: String = "cuci_dan_setrika" // ✅ DEFAULT: Cuci dan Setrika

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderRepository = (application as App).orderRepository

        setupToolbar()
        setupTextWatchers()
        setupClickListeners()
        setupDatePicker()
        setupCounterListeners() // TAMBAHKAN INI
        setupLottieAnimation()
        calculateTotalPrice()
        updateTotalPcs() // TAMBAHKAN INI
    }

    // TAMBAHKAN FUNCTION INI - Setup counter listeners
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

    // TAMBAHKAN FUNCTION INI - Update counter displays
    private fun updateKaosCounter() {
        binding.tvCountKaos.text = countKaos.toString()
    }

    private fun updateCelanaCounter() {
        binding.tvCountCelana.text = countCelana.toString()
    }

    private fun updateHandukCounter() {
        binding.tvCountHanduk.text = countHanduk.toString()
    }

    // TAMBAHKAN FUNCTION INI - Calculate and update total pcs
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
        binding.etCompletionDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tilCompletionDate.error = null // Clear error ketika user memilih tanggal
            }
        })
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                // Hitung selisih hari dari hari ini
                val daysDifference = calculateDaysDifference(selectedCalendar)

                if (daysDifference < 0) {
                    Toast.makeText(this, "Tanggal tidak boleh sebelum hari ini", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                if (daysDifference > 4) {
                    Toast.makeText(this, "Maksimal 5 hari dari hari ini", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                // Simpan tanggal yang dipilih
                selectedDateCalendar = selectedCalendar
                completionDate = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    selectedYear,
                    selectedMonth + 1,
                    selectedDay
                )

                // Format tampilan: DD MMMM YYYY
                val displayDate = String.format(
                    Locale("id", "ID"),
                    "%02d %s %04d",
                    selectedDay,
                    getMonthName(selectedMonth),
                    selectedYear
                )

                binding.etCompletionDate.setText(displayDate)

                // Update harga berdasarkan tanggal
                updatePriceBasedOnDate(daysDifference)
                calculateTotalPrice()
            },
            year,
            month,
            day
        )

        // Set minimum date ke hari ini
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

        // Set maximum date 5 hari dari sekarang
        val maxDateCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 4) // 4 hari kedepan = total 5 hari (hari ini + 4)
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
            0 -> 10000 // Hari ini - Express
            1 -> 8000  // Besok
            2 -> 7000  // 2 hari lagi
            3 -> 6000  // 3 hari lagi
            4 -> 5000  // 4 hari lagi (hari ke-5)
            else -> 5000 // Default
        }

        binding.etPricePerKg.setText(pricePerKg.toString())

        // Tampilkan info harga
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
        binding.etWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateTotalPrice()
            }
        })

        // TextWatcher untuk harga per kg
        binding.etPricePerKg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateTotalPrice()
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSaveOrder.setOnClickListener {
            if (validateInput()) {
                createOrderDB()
            }
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

            // VALIDASI TANGGAL SELESAI WAJIB DIISI - TAMBAHKAN INI
            if (etCompletionDate.text.toString().trim().isEmpty()) {
                tilCompletionDate.error = "Tanggal selesai harus diisi"
                return false
            }

            // Validasi tanggal jika dipilih
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
            // TAMBAHKAN VALIDASI MINIMAL 1 ITEM
            val totalPcs = countKaos + countCelana + countHanduk
            if (totalPcs == 0) {
                Toast.makeText(this@AddOrderActivity, "Minimal pilih 1 item (Kaos, Celana, atau Handuk)", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun createOrderDB() {
        val customerName = binding.etCustomerName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val weight = binding.etWeight.text.toString().toDouble()
        val pricePerKg = binding.etPricePerKg.text.toString().toDouble()
        val totalPcs = countKaos + countCelana + countHanduk

        // Tentukan service type berdasarkan hari
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
            layananType = selectedServiceType, // ✅ TAMBAHKAN SERVICE TYPE
            // KIRIM NILAI 0 EXPLICITLY
            kaosQty = countKaos,
            celanaQty = countCelana,
            handukQty = countHanduk,
            totalPcs = countKaos + countCelana + countHanduk
        )

        // Show loading
        LoadingUtils.showLoading(binding.lottieProgress)
        binding.progressOverlay.visibility = View.VISIBLE
        binding.btnSaveOrder.isEnabled = false
        setInputEnabled(false)

        // Gunakan repository
        orderRepository.createOrder(orderRequest) { result ->
            runOnUiThread {
                // Hide loading
                LoadingUtils.hideLoading(binding.lottieProgress)
                binding.progressOverlay.visibility = View.GONE
                binding.btnSaveOrder.isEnabled = true
                setInputEnabled(true)

                when {
                    result.isSuccess -> {
                        val order = result.getOrNull()
                        Toast.makeText(
                            this@AddOrderActivity,
                            "Order berhasil dibuat${if (order?.id ?: 0 < 0) " (Offline)" else ""}",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    else -> {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Gagal membuat order"
                        Toast.makeText(
                            this@AddOrderActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
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

            // Enable/disable counter buttons
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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tambah Order Laundry"
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!LoadingUtils.isLoading()) {
            finish()
        }
        return !LoadingUtils.isLoading()
    }

    companion object {
        fun start(activity: AppCompatActivity) {
            val intent = android.content.Intent(activity, AddOrderActivity::class.java)
            activity.startActivity(intent)
        }
    }
}