package com.o7solutions.freelancing_bot.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.MainActivity
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.Users
import com.o7solutions.freelancing_bot.databinding.ActivitySignupBinding
import com.o7solutions.freelancing_bot.utils.Constants

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    var role = 0
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        val roles = listOf("Client", "Freelancer")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.roleSpinner.adapter = adapter

        binding.roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {


            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val selectedRole = parent?.getItemAtPosition(position).toString()
                if (selectedRole == "Client") {
                    role = 0
                } else if (selectedRole == "Freelancer") {
                    role = 1
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        binding.createAccountBTN.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {
        val name = binding.nameET.text.toString().trim()
        val email = binding.emailET.text.toString().trim()
        val password = binding.passET.text.toString().trim()
        val description = binding.descriptionET.text.toString().trim()

        // Input validation
        if (name.isEmpty()) {
            binding.nameET.error = "Name is required"
            return
        }
        if (email.isEmpty()) {
            binding.emailET.error = "Email is required"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailET.error = "Invalid email format"
            return
        }
        if (password.isEmpty()) {
            binding.passET.error = "Password is required"
            return
        }
        if (password.length < 6) {
            binding.passET.error = "Password must be at least 6 characters"
            return
        }

        // Firebase Authentication: Create user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid ?: return@addOnSuccessListener

                val newUser = Users(
                    id = uid,
                    name = name,
                    email = email,
                    role = role,
                    description = description,
                    experience = ArrayList()
                )

                // Save user data to Firestore
                db.collection(Constants.userCol).document(email)
                    .set(newUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                        // Optionally, navigate to login or home


                        val sharedPref = getSharedPreferences(Constants.userKey, MODE_PRIVATE)
                        sharedPref.edit().putInt("userType", role).apply()


                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save user: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }



}