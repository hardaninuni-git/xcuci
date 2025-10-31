package com.example.xcuci.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.anggastudio.printama.Printama
import com.example.xcuci.data.model.Order
import java.text.NumberFormat
import java.util.Locale

object PrinterUtils {

    private val numberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))

    fun printReceipt(
        context: Context,
        order: Order,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Check Bluetooth permissions first
        if (!hasBluetoothPermissions(context)) {
            onError("Aplikasi membutuhkan izin Bluetooth untuk mencetak struk")
            return
        }

        try {
            val printama = Printama.with(context)

            // Print receipt content line by line
            val receiptLines = getReceiptContent(order)
            receiptLines.forEach { line ->
                printama.printTextln(line)
            }

            // Add some empty lines at the end
            printama.addNewLine(3)

            onSuccess()

        } catch (e: SecurityException) {
            onError("Izin Bluetooth ditolak: ${e.message}")
        } catch (e: Exception) {
            onError("Print error: ${e.message}")
        }
    }

    fun connectAndPrint(
        context: Context,
        order: Order,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Check Bluetooth permissions first
        if (!hasBluetoothPermissions(context)) {
            onError("Aplikasi membutuhkan izin Bluetooth untuk mencetak struk")
            return
        }

        val printama = Printama.with(context)

        // TANPA NAMED ARGUMENTS - sesuai dengan Java interop
        printama.connect(
            { connectedPrintama ->
                // Setelah terhubung, print receipt
                try {
                    val receiptLines = getReceiptContent(order)
                    receiptLines.forEach { line ->
                        connectedPrintama.printTextln(line)
                    }
                    connectedPrintama.addNewLine(3)
                    onSuccess()
                } catch (e: Exception) {
                    onError("Print error: ${e.message}")
                }
            },
            { error ->
                onError("Koneksi printer gagal: $error")
            }
        )
    }

    // Versi alternatif yang lebih sederhana
    fun simplePrint(context: Context, order: Order, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!hasBluetoothPermissions(context)) {
            onError("Aplikasi membutuhkan izin Bluetooth untuk mencetak struk")
            return
        }

        try {
            val printama = Printama.with(context)

            // Coba print langsung tanpa connect
            val receiptLines = getReceiptContent(order)
            receiptLines.forEach { line ->
                printama.printTextln(line)
            }
            printama.addNewLine(3)

            onSuccess()
        } catch (e: Exception) {
            onError("Print gagal: ${e.message}. Pastikan printer sudah terhubung.")
        }
    }

    private fun hasBluetoothPermissions(context: Context): Boolean {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN
            )
        }

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getReceiptContent(order: Order): List<String> {
        return listOf(
            "LAUNDRY XCUCI",
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
            "Selesai   : ${order.completionDate.formatCompletionDate()}",
            "========================",
            "Terima kasih",
            "www.xcuci.com"
        )
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
}