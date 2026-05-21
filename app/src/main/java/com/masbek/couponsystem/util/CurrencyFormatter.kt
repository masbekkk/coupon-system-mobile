package com.masbek.couponsystem.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val idLocale = Locale("id", "ID")

    fun formatRupiah(amount: Int): String {
        if (amount == 0) return "—"
        val formatter = NumberFormat.getCurrencyInstance(idLocale)
        formatter.maximumFractionDigits = 0
        return formatter.format(amount.toLong())
    }

    fun formatNumber(number: Int): String {
        val formatter = NumberFormat.getNumberInstance(idLocale)
        return formatter.format(number.toLong())
    }

    fun formatRupiahWithZero(amount: Int): String {
        val formatter = NumberFormat.getCurrencyInstance(idLocale)
        formatter.maximumFractionDigits = 0
        return formatter.format(amount.toLong())
    }
}
