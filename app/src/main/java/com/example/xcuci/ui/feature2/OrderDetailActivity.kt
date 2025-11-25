package com.example.xcuci.ui.feature2

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anggastudio.printama.Printama
import com.anggastudio.printama.PrintamaUI
import com.example.xcuci.R
import com.example.xcuci.data.local.database.AppDatabase
import com.example.xcuci.data.model.Order
import com.example.xcuci.data.remote.response.ApiResponse
import com.example.xcuci.data.repository.OrderRepository
import com.example.xcuci.data.repository.RetrofitClient
import com.example.xcuci.data.repository.isOfflineOrder
import com.example.xcuci.databinding.ActivityOrderDetailBinding
import com.example.xcuci.utils.LoadingUtils
import com.example.xcuci.utils.formatCompletionDate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private var orderId: Int = -1
    private lateinit var orderRepository: OrderRepository
    private val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))
    private var currentOrder: Order? = null

    private val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 432

    // Activity result launcher for Bluetooth enable
    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Bluetooth enabled, now connect to printer
            showPrinterList()
        } else {
            Toast.makeText(this, "Bluetooth diperlukan untuk mencetak struk", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getIntExtra(EXTRA_ORDER_ID, -1)
        if (orderId == -1) {
            Toast.makeText(this, "Order ID tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize repository
        orderRepository = OrderRepository(
            AppDatabase.getDatabase(this).orderDao(),
            RetrofitClient.apiService
        )

        setupToolbar()
        setupLottieAnimation()
        setupClickListeners()
        loadOrderDetail()
        checkPrinterConnection()
    }

    override fun onResume() {
        super.onResume()
        checkPrinterConnection()
    }

    private fun setupLottieAnimation() {
        binding.lottieProgress.setAnimation(R.raw.loading_animation)
        binding.lottieProgress.loop(true)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Detail Order"

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.tvPhone.setOnClickListener {
            currentOrder?.let { order ->
                openWhatsApp(order.phone)
            }
        }

        binding.btnUpdateStatus.setOnClickListener {
            showStatusUpdateDialog()
        }

        binding.btnDeleteOrder.setOnClickListener {
            deleteOrder()
        }

        binding.btnPrintOrder.setOnClickListener {
            connectToPrinter(true)
        }

        // Connect printer button (optional, bisa ditambahkan di UI jika perlu)
        binding.btnPrintOrder.setOnLongClickListener {
            showPrinterSettings()
            true
        }

        binding.btnShare.setOnClickListener {
            shareOrderDetails()
        }
    }

    private fun openWhatsApp(phoneNumber: String) {
        try {
            // Bersihkan nomor telepon dari karakter non-digit
            val cleanNumber = phoneNumber.replace("[^0-9]".toRegex(), "")

            if (cleanNumber.isEmpty()) {
                Toast.makeText(this, "Nomor telepon tidak valid", Toast.LENGTH_SHORT).show()
                return
            }

            // Format nomor untuk WhatsApp (tanpa +62, cukup 62)
            val formattedNumber = if (cleanNumber.startsWith("0")) {
                "62${cleanNumber.substring(1)}"
            } else if (cleanNumber.startsWith("+62")) {
                cleanNumber.substring(1)
            } else if (cleanNumber.startsWith("62")) {
                cleanNumber
            } else {
                "62$cleanNumber"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://wa.me/$formattedNumber")
                setPackage("com.whatsapp")
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // WhatsApp tidak terinstall, buka browser
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("https://wa.me/$formattedNumber")
                }
                startActivity(webIntent)
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showPrinterSettings() {
        connectToPrinter(true)
    }

    private fun shareOrderDetails() {
        currentOrder?.let { order ->
            try {
                val shareContent = buildShareContent(order)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Detail Order Laundry - ORDER-#${order.id}")
                    putExtra(Intent.EXTRA_TEXT, shareContent)
                }

                // Create chooser dialog
                val shareChooser = Intent.createChooser(shareIntent, "Bagikan Detail Order")
                startActivity(shareChooser)

            }catch (e: Exception) {
                Toast.makeText(this, "Gagal membagikan detail order", Toast.LENGTH_SHORT).show()
                Log.e("OrderDetailActivity", "Error sharing order: ${e.message}")
            }
        } ?: run {
            Toast.makeText(this, "Data order tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildShareContent(order: Order): String {
        val statusText = getStatusText(order.status)
        val totalPcs = order.kaosQty + order.celanaQty + order.handukQty
        return """
        🧺 *DETAIL ORDER LAUNDRY* 🧺
        
        *No. Order:* ORDER-#${order.id}
        *Status:* $statusText
        *Tanggal:* ${order.createdAt.formatCompletionDate()}
        
        👤 *PELANGGAN*
        Nama: ${order.customerName}
        Telepon: ${order.phone}
        Alamat: ${order.address}
        
        📋 *DETAIL ORDER*
        Berat: ${order.weight} kg
        Harga/kg: Rp ${numberFormat.format(order.pricePerKg)}
        Total: Rp ${numberFormat.format(order.totalPrice)}
        Tipe Layanan: ${order.layananType}
        Total Item: $totalPcs pcs
        
        📅 *ESTIMASI SELESAI*
        ${order.completionDate.formatCompletionDate()}
        
        📞 *KONTAK LAUNDRY*
        YUMA LAUNDRY
        085694245178
        
        _*Terima kasih telah menggunakan layanan kami*_ 🙏
    """.trimIndent()
    }

    private fun connectToPrinter(isTriggered: Boolean) {
        if (!hasBluetoothPermissions()) {
            if (isTriggered) {
                if (shouldShowPermissionRationale()) {
                    // Permission is denied
                    AlertDialog.Builder(this)
                        .setTitle("Izin Bluetooth (Perangkat Sekitar) Ditolak")
                        .setMessage("Fitur cetak tidak akan aktif karena izin Bluetooth (Perangkat Sekitar) tidak diberikan. Silakan buka pengaturan aplikasi dan izinkan akses Perangkat Sekitar")
                        .setPositiveButton("Buka Pengaturan") { dialog, which ->
                            openDeviceSettings()
                        }
                        .setNegativeButton("Batal") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    // ask permission
                    requestBluetoothPermission()
                }
            }
        } else {
            // Permission has already been granted
            showPrinterList()
        }
    }

    private fun openDeviceSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = android.net.Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                PERMISSION_REQUEST_BLUETOOTH_CONNECT
            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(enableBtIntent)
        }
    }

    private fun checkPrinterConnection() {
        if (hasBluetoothPermissions()) {
            val deviceNameDisplay = Printama.getSavedPrinterName(this)
            if (deviceNameDisplay == null || deviceNameDisplay.isEmpty()) {
//                binding.btnPrintOrder.text = "Cetak Struk"
            } else {
//                binding.btnPrintOrder.text = "Cetak Struk"
            }
        } else {
//            binding.btnPrintOrder.text = "Cetak Struk"
        }
    }

    private fun showAfterConnectLayout() {
        // Update UI setelah terhubung ke printer
        checkPrinterConnection()
    }

    private fun hideAfterConnectLayout() {
        binding.btnPrintOrder.text = "Cetak Struk\n(Bluetooth)"
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun showPrinterList() {
        PrintamaUI.showPrinterList(this) { selectedDevice ->
            if (selectedDevice != null) {
                checkPrinterConnection()
                showAfterConnectLayout()
                val deviceNameDisplay = Printama.getSavedPrinterName(this)
                Toast.makeText(this, "Terhubung ke $deviceNameDisplay", Toast.LENGTH_SHORT).show()

                // Auto print setelah terhubung
                currentOrder?.let { showPrintDialog(it) }
            } else {
                hideAfterConnectLayout()
            }
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowPermissionRationale(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        return permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH_CONNECT) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                checkPrinterConnection()
                showPrinterList()
            } else {
                // Permission denied
                Toast.makeText(this, "Izin Bluetooth ditolak", Toast.LENGTH_SHORT).show()
                hideAfterConnectLayout()
            }
        }
    }

    // PRINT FUNCTIONALITY
    private fun showPrintDialog(order: Order) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.layout_receipt_preview, null)
        setupReceiptPreview(dialogView, order)

        AlertDialog.Builder(this)
            .setTitle("Preview Struk")
            .setView(dialogView)
            .setPositiveButton("Cetak Sekarang") { dialog, which ->
                printReceipt(order)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupReceiptPreview(dialogView: View, order: Order) {
        with(dialogView) {
            findViewById<TextView>(R.id.tvReceiptOrderId).text = "ORDER-#${order.id}"
            findViewById<TextView>(R.id.tvReceiptDate).text = order.createdAt.formatCompletionDate()
            findViewById<TextView>(R.id.tvReceiptCustomerName).text = order.customerName
            findViewById<TextView>(R.id.tvReceiptPhone).text = order.phone
            findViewById<TextView>(R.id.tvReceiptAddress).text = order.address
            findViewById<TextView>(R.id.tvReceiptWeight).text = "${order.weight} kg"
            findViewById<TextView>(R.id.tvReceiptPricePerKg).text = "Rp ${numberFormat.format(order.pricePerKg)}"
            findViewById<TextView>(R.id.tvReceiptTotalPrice).text = "Rp ${numberFormat.format(order.totalPrice)}"
            val totalPcs = order.kaosQty + order.celanaQty + order.handukQty
            findViewById<TextView>(R.id.tvReceiptLayanan).text = "${order.layananType}"
            findViewById<TextView>(R.id.tvReceiptKaos).text = "${order.kaosQty} pcs"
            findViewById<TextView>(R.id.tvReceiptCelana).text = "${order.celanaQty} pcs"
            findViewById<TextView>(R.id.tvReceiptHanduk).text = "${order.handukQty} pcs"
            findViewById<TextView>(R.id.tvReceiptTotalItem).text = "$totalPcs pcs"
            findViewById<TextView>(R.id.tvReceiptCompletionDate).text = order.completionDate.formatCompletionDate()
        }
    }

    private fun printReceipt(order: Order) {
        if (!hasBluetoothPermissions()) {
            Toast.makeText(this, "Izin Bluetooth diperlukan untuk mencetak", Toast.LENGTH_SHORT).show()
            return
        }

        val savedPrinter = Printama.getSavedPrinterName(this)
        if (savedPrinter.isNullOrEmpty()) {
            Toast.makeText(this, "Belum ada printer tersambung", Toast.LENGTH_SHORT).show()
            showPrinterList()
            return
        }

        LoadingUtils.showLoading(binding.lottieProgress)

        Printama.with(this).connect { printer ->
        try {
            // Print receipt content
            getReceiptContent(order).forEach { line ->
                printer.printTextln(line)
            }

            printer.addNewLine(3)
            printer.feedPaper()

            runOnUiThread {
                LoadingUtils.hideLoading(binding.lottieProgress)
                Toast.makeText(this, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            runOnUiThread {
                LoadingUtils.hideLoading(binding.lottieProgress)
                showPrintErrorDialog("Print gagal: ${e.message}")
            }
        }}
    }

    private fun getReceiptContent(order: Order): List<String> {
        return listOf(
            "YUMA LAUNDRY",
            "========================",
            "No. Order : ORDER-#${order.id}",
            "Tanggal   : ${order.createdAt.formatCompletionDate()}",
            "Status    : ${getStatusText(order.status)}",
            "------------------------",
            "PELANGGAN:",
            order.customerName,
            order.phone,
            order.address,
            "------------------------",
            "DETAIL ORDER:",
            "Berat     : ${order.weight} kg",
            "Harga/kg  : Rp ${numberFormat.format(order.pricePerKg)}",
            "Total     : Rp ${numberFormat.format(order.totalPrice)}",
            "------------------------",
            "DETAIL ORDER:",
            "Tipe Layanan: ${order.layananType}",
//            "Kaos      : ${order.kaosQty} pcs",
//            "Celana    : ${order.celanaQty} pcs",
//            "Handuk    : ${order.handukQty} pcs",
//            "Total Item: ${numberFormat.format(order.kaosQty + order.celanaQty + order.handukQty)} pcs",
            "Total Item: ${order.kaosQty} pcs",
            "Selesai   : ${order.completionDate.formatCompletionDate()}",
            "========================",
            "Terima kasih",
            "085694245178"
        )
    }

    private fun showPrintErrorDialog(error: String) {
        AlertDialog.Builder(this)
            .setTitle("Gagal Cetak")
            .setMessage("Error: $error\n\nPastikan:\n• Printer sudah terhubung via Bluetooth\n• Kertas printer tersedia\n• Printer dalam kondisi siap")
            .setPositiveButton("Coba Lagi") { dialog, which ->
                currentOrder?.let { printReceipt(it) }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // REST OF YOUR EXISTING CODE (loadOrderDetail, displayOrderDetail, etc.)
    // ... [Semua function yang sudah ada sebelumnya tetap dipertahankan]

    private fun loadOrderDetail() {
        LoadingUtils.showLoading(binding.lottieProgress)
        binding.lottieProgress.visibility = View.VISIBLE

        orderRepository.getOrder(orderId) { order ->
            runOnUiThread {
                LoadingUtils.hideLoading(binding.lottieProgress)
                binding.lottieProgress.visibility = View.GONE

                if (order != null) {
                    currentOrder = order
                    displayOrderDetail(order)
                } else {
                    loadOrderDetailFromServer()
                }
            }
        }
    }

    private fun loadOrderDetailFromServer() {
        LoadingUtils.showLoading(binding.lottieProgress)
        binding.lottieProgress.visibility = View.VISIBLE

        RetrofitClient.apiService.getOrder(orderId).enqueue(object : Callback<ApiResponse<Order>> {
            override fun onResponse(call: Call<ApiResponse<Order>>, response: Response<ApiResponse<Order>>) {
                LoadingUtils.hideLoading(binding.lottieProgress)
                binding.lottieProgress.visibility = View.GONE

                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { order ->
                        currentOrder = order
                        displayOrderDetail(order)
                        saveOrderToLocal(order)
                    }
                } else {
                    Toast.makeText(this@OrderDetailActivity, "Gagal memuat detail order", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<ApiResponse<Order>>, t: Throwable) {
                LoadingUtils.hideLoading(binding.lottieProgress)
                binding.lottieProgress.visibility = View.GONE
                Toast.makeText(this@OrderDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun saveOrderToLocal(order: Order) {
        Thread {
            orderRepository.saveOrderToLocal(order)
        }.start()
    }

    private fun displayOrderDetail(order: Order) {
        with(binding) {
            tvCustomerName.text = order.customerName
            tvPhone.text = order.phone
            tvAddress.text = order.address
            tvWeight.text = getString(R.string.format_weight, order.weight)
            tvPricePerKg.text = getString(R.string.format_price_per_kg, numberFormat.format(order.pricePerKg))
            tvTotalPrice.text = getString(R.string.format_total_price, numberFormat.format(order.totalPrice))
            tvStatus.text = getStatusText(order.status)
            setStatusStyle(order.status)
            tvCreatedAt.text = formatDateTime(order.createdAt)
            tvUpdatedAt.text = formatDateTime(order.updatedAt)
            tvCompletionDate.text = order.completionDate.formatCompletionDate()
            tvTipeLayanan.text = order.layananType
            tvOrderId.text = getString(R.string.format_order_id, order.id)

            // Tampilkan detail per item
            displayItemDetails(order)

            if (order.isOfflineOrder()) {
                tvStatus.text = "${getStatusText(order.status)} ⚡"
            }
            // PERBAIKAN: Sembunyikan tombol update status jika order sudah selesai
            updateStatusButtonVisibility(order.status)
        }
    }

    private fun updateStatusButtonVisibility(status: String) {
        val isComplete = status.equals("completed", ignoreCase = true)
        if (isComplete) {
            binding.llUpdateStatus.visibility = View.GONE
        } else {
            binding.llUpdateStatus.visibility = View.VISIBLE
        }
    }

    private fun displayItemDetails(order: Order) {
        with(binding) {
            // Sembunyikan semua item detail terlebih dahulu
            layoutKaosDetail.visibility = View.GONE
            layoutCelanaDetail.visibility = View.GONE
            layoutHandukDetail.visibility = View.GONE
            layoutTotalPcs.visibility = View.GONE

            val punyaItemDetails = order.kaosQty > 0 || order.celanaQty > 0 || order.handukQty > 0
            if (punyaItemDetails) {
                if (order.kaosQty > 0){
                    layoutKaosDetail.visibility = View.GONE
                    tvKaosQty.text = getString(R.string.item_qty, order.kaosQty)
                }
                if (order.celanaQty > 0){
                    layoutCelanaDetail.visibility = View.VISIBLE
                    tvCelanaQty.text = "${order.celanaQty} pcs"
                }
                if (order.handukQty > 0){
                    layoutHandukDetail.visibility = View.VISIBLE
                    tvHandukQty.text = "${order.handukQty} pcs"
                }
                val totalPcs = order.kaosQty + order.celanaQty + order.handukQty
                if (totalPcs > 0){
                    layoutTotalPcs.visibility = View.VISIBLE
                    tvTotalPcs.text = "Total: $totalPcs pcs"
                }
            } else {
                Log.d("XBZ", "Tidak ada item details - semua quantity 0")
            }

        }
    }

    private fun getStatusText(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "Menunggu"
            "processing" -> "Diproses"
            "completed" -> "Selesai"
            "cancelled" -> "Dibatalkan"
            else -> status
        }
    }

    private fun setStatusStyle(status: String) {
        with(binding) {
            when (status.lowercase()) {
                "pending" -> tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#FFA726"))
                "processing" -> tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#42A5F5"))
                "completed" -> tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#66BB6A"))
                "cancelled" -> tvStatus.setBackgroundColor(android.graphics.Color.parseColor("#EF5350"))
            }
        }
    }

    private fun formatDateTime(dateTimeString: String): String {
        return try {
            dateTimeString.replace(" ", " at ")
        } catch (e: Exception) {
            dateTimeString
        }
    }

    private fun showStatusUpdateDialog() {
        val statuses = arrayOf("Menunggu", "Diproses", "Selesai", "Dibatalkan")
        val actualStatuses = arrayOf("pending", "processing", "completed", "cancelled")

        AlertDialog.Builder(this)
            .setTitle("Update Status Order")
            .setItems(statuses) { dialog, which ->
                updateOrderStatus(actualStatuses[which])
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateOrderStatus(newStatus: String) {
        LoadingUtils.showLoading(binding.lottieProgress)

        currentOrder?.let { order ->
            val updatedOrder = order.copy(
                status = newStatus,
                updatedAt = getCurrentDateTime()
            )

            orderRepository.updateOrder(updatedOrder) { result ->
                runOnUiThread {
                    LoadingUtils.hideLoading(binding.lottieProgress)

                    when {
                        result.isSuccess -> {
                            val success = result.getOrNull()
                            if (success == true) {
                                Toast.makeText(this@OrderDetailActivity, "Status berhasil diupdate menjadi: ${getStatusText(newStatus)}", Toast.LENGTH_SHORT).show()
                                loadOrderDetail()
                            } else {
                                Toast.makeText(this@OrderDetailActivity, "Status berhasil diupdate lokal", Toast.LENGTH_SHORT).show()
                                loadOrderDetail()
                            }
                        }
                        else -> {
                            val errorMessage = result.exceptionOrNull()?.message ?: "Gagal update status"
                            Toast.makeText(this@OrderDetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } ?: run {
            LoadingUtils.hideLoading(binding.lottieProgress)
            Toast.makeText(this, "Data order tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteOrder() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Order")
            .setMessage("Apakah Anda yakin ingin menghapus order ini?")
            .setPositiveButton("Hapus") { dialog, which ->
                performDeleteOrder()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performDeleteOrder() {
        LoadingUtils.showLoading(binding.lottieProgress)

        orderRepository.deleteOrder(orderId) { result ->
            runOnUiThread {
                LoadingUtils.hideLoading(binding.lottieProgress)

                when {
                    result.isSuccess -> {
                        val success = result.getOrNull()
                        if (success == true) {
                            Toast.makeText(this@OrderDetailActivity, "Order berhasil dihapus", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@OrderDetailActivity, "Order berhasil dihapus dari lokal", Toast.LENGTH_SHORT).show()
                        }
                        finish()
                    }
                    else -> {
                        val errorMessage = result.exceptionOrNull()?.message ?: "Gagal menghapus order"
                        Toast.makeText(this@OrderDetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getCurrentDateTime(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(java.util.Date())
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"

        fun start(activity: AppCompatActivity, orderId: Int) {
            val intent = android.content.Intent(activity, OrderDetailActivity::class.java).apply {
                putExtra(EXTRA_ORDER_ID, orderId)
            }
            activity.startActivity(intent)
        }
    }
}