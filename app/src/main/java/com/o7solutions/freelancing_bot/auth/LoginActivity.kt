package com.o7solutions.freelancing_bot.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.o7solutions.freelancing_bot.MainActivity
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.databinding.ActivityLoginBinding
import com.o7solutions.freelancing_bot.utils.Constants
import com.o7solutions.freelancing_bot.utils.Functions

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbRef: DatabaseReference // Added for Realtime Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        // Initialize Realtime Database reference
        dbRef = FirebaseDatabase.getInstance().getReference(Constants.userCol)

        binding.apply {
            createAccount.setOnClickListener {
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            }

            loginBTN.setOnClickListener {
                val email = emailET.text.toString().trim()
                val pass = passET.text.toString().trim()

                if (email.isEmpty()) {
                    emailET.error = "Please enter email"
                    return@setOnClickListener
                }
                if (pass.isEmpty()) {
                    passET.error = "Please enter password"
                    return@setOnClickListener
                }

                pgBar.visibility = View.VISIBLE

                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        getUserData()
                    }
                    .addOnFailureListener { e ->
                        pgBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Login failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun getUserData() {
        val uid = auth.currentUser?.uid ?: return

        // Fetching from Realtime Database using the UID
        dbRef.child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Realtime Database returns Long for integers by default
                val role = snapshot.child("role").getValue(Int::class.java) ?: 0

                Log.e("Role on login screen", role.toString())

                val sharedPref = getSharedPreferences(Constants.userKey, MODE_PRIVATE)
                sharedPref.edit().putInt("userType", role).apply()

                binding.pgBar.visibility = View.GONE
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                binding.pgBar.visibility = View.GONE
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            binding.pgBar.visibility = View.GONE
            Functions.showAlert(e.localizedMessage ?: "Unknown error", this)
        }
    }
}