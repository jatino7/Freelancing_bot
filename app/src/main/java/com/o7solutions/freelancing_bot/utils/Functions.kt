package com.o7solutions.freelancing_bot.utils

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.o7solutions.freelancing_bot.R
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

    fun showAlert(message: String, context: Context) {

        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Issue")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog,_->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()


    }


    fun Fragment.setupToolbarWithPop(rootView: View, title: String? = null) {
        val toolbar: Toolbar = rootView.findViewById<Toolbar>(R.id.toolbar_custom)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        title?.let { toolbar.title = it }

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

}