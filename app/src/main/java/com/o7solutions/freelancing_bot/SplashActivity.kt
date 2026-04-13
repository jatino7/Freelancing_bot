package com.o7solutions.freelancing_bot

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.o7solutions.freelancing_bot.Admin.AdminActivity
import com.o7solutions.freelancing_bot.auth.LoginActivity
import com.o7solutions.freelancing_bot.utils.Constants

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            if (auth.currentUser == null) {
                // User not logged in -> Go to Login
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                // User is logged in -> Check their role from SharedPrefs
                val sharedPref = getSharedPreferences(Constants.userKey, MODE_PRIVATE)
                val userRole = sharedPref.getInt("userType", -1)

                val intent = when (userRole) {
                    0 -> Intent(this, MainActivity::class.java) // Employer
                    1 -> Intent(this, MainActivity::class.java) // Job Seeker
                    2 -> Intent(this, AdminActivity::class.java)    // Admin
                    else -> Intent(this, LoginActivity::class.java)     // Fallback to Login
                }
                startActivity(intent)
            }
            finish()
        }, 2000)
    }
}