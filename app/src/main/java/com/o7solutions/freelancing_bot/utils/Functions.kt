package com.o7solutions.freelancing_bot.utils

import android.os.Binder
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Functions {

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}