package com.o7solutions.freelancing_bot.utils

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.Message
import com.o7solutions.freelancing_bot.data_classes.RecentMessage
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


    fun sendMessage(senderId: String,receiverId: String,message: String,context: Context) {
        val db = FirebaseFirestore.getInstance()
        val messageIntented = Message(
            senderId,message, System.currentTimeMillis()
        )

        val recentMessage = RecentMessage(senderId,receiverId,message.toString(), System.currentTimeMillis())

        db.collection(Constants.msg).document(senderId).collection(receiverId).document().set(messageIntented)
        db.collection(Constants.msg).document(receiverId).collection(senderId).document().set(messageIntented)

        db.collection(Constants.userCol).document(senderId).collection(Constants.recMes).document(receiverId).set(recentMessage)
        db.collection(Constants.userCol).document(receiverId).collection(Constants.recMes).document(senderId).set(recentMessage)



        Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
    }



    fun showMessage(title: String,message: String, context: Context) {

        val alertDialog = android.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

            .create()

        alertDialog.show()


    }

    fun createOrWriteRole(roleValue: Int,context: Context) {
        val sharedPreferences = context.getSharedPreferences(Constants.userKey, Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(Constants.userKey, roleValue).apply()
    }

    fun readRole(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(Constants.userKey, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(Constants.userKey, 0) // 0 is default
    }

    fun updateRole(newRole: Int,context: Context) {
        createOrWriteRole(newRole,context) // Reuse create/write function
    }

    fun deleteRole(context: Context) {
        val sharedPreferences = context.getSharedPreferences(Constants.userKey, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(Constants.userKey).apply()
    }

}