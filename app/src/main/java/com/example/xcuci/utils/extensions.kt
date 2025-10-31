package com.example.xcuci.utils

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

inline fun <T> safeApiCall(
    call: Call<T>,
    crossinline onSuccess: (T) -> Unit,
    crossinline onError: (String) -> Unit
) {
    call.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            when {
                response.isSuccessful -> {
                    response.body()?.let { onSuccess(it) }
                        ?: onError("Response body is null")
                }
                else -> {
                    val errorMsg = when (response.code()) {
                        404 -> "Endpoint not found (404)"
                        500 -> "Server error (500)"
                        401 -> "Unauthorized (401)"
                        else -> "HTTP Error ${response.code()}: ${response.message()}"
                    }
                    onError(errorMsg)
                }
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            onError("Network error: ${t.message}")
        }
    })
}

// Extension function untuk String
fun String?.formatCompletionDate(): String {
    return if (!this.isNullOrEmpty()) {
        try {
            // Format dari "2024-12-25" menjadi "25 Des 2024"
            if (this.contains("-")) {
                val parts = this.split("-")
                if (parts.size == 3) {
                    val year = parts[0]
                    val month = parts[1].toInt()
                    val day = parts[2]

                    val monthNames = arrayOf(
                        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
                    )

                    "$day ${monthNames.getOrNull(month - 1) ?: month} $year"
                } else {
                    this
                }
            } else {
                this
            }
        } catch (e: Exception) {
            this ?: "-"
        }
    } else {
        "-"
    }
}

// Versi dengan custom prefix
fun String?.formatCompletionDate(prefix: String = ""): String {
    val formattedDate = this.formatCompletionDate()
    return if (prefix.isNotEmpty()) "$prefix $formattedDate" else formattedDate
}