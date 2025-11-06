package com.example.xcuci.utils

class CounterManager {
    var countKaos = 0
    var countCelana = 0
    var countHanduk = 0

    fun getTotalPcs(): Int {
        return countKaos + countCelana + countHanduk
    }

    fun incrementKaos() {
        countKaos++
    }

    fun decrementKaos() {
        if (countKaos > 0) countKaos--
    }

    fun incrementCelana() {
        countCelana++
    }

    fun decrementCelana() {
        if (countCelana > 0) countCelana--
    }

    fun incrementHanduk() {
        countHanduk++
    }

    fun decrementHanduk() {
        if (countHanduk > 0) countHanduk--
    }

    fun reset() {
        countKaos = 0
        countCelana = 0
        countHanduk = 0
    }
}