package com.o7solutions.freelancing_bot.auth

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.o7solutions.freelancing_bot.MainActivity
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.User
import com.o7solutions.freelancing_bot.databinding.ActivitySignupBinding
import com.o7solutions.freelancing_bot.utils.Constants

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var role = 0
    private lateinit var dbRef: DatabaseReference // Changed from Firestore to DatabaseReference
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

        // Initialize Realtime Database and Auth
        dbRef = FirebaseDatabase.getInstance().getReference(Constants.userCol)
        auth = FirebaseAuth.getInstance()

        val roles = listOf("Employer", "Job Seeker")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.roleSpinner.adapter = adapter

        binding.roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRole = parent?.getItemAtPosition(position).toString()
                // Fixed: Matching the logic to the 'roles' list strings
                role = if (selectedRole == "Employer") 0 else 1
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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

        // Validation logic
        if (name.isEmpty() || email.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                val newUser = User(
                    id = uid,
                    name = name,
                    email = email,
                    headline = "",
                    profileImageUrl = "",
                    coverImageUrl = "",
                    location = "",
                    about = description,
                    role = role,
                    skills = ArrayList(),
                    experience = ArrayList(),
                    connectionsCount = 0
                )

                // Realtime Database save logic
                dbRef.child(uid).setValue(newUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Welcome to the network!", Toast.LENGTH_SHORT).show()

                        val sharedPref = getSharedPreferences(Constants.userKey, MODE_PRIVATE)
                        sharedPref.edit().putInt("userType", role).apply()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Database Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Auth Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}