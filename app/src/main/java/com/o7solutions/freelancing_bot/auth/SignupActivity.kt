package com.o7solutions.freelancing_bot.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.o7solutions.freelancing_bot.Admin.AdminActivity
import com.o7solutions.freelancing_bot.MainActivity
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.data_classes.User
import com.o7solutions.freelancing_bot.databinding.ActivitySignupBinding
import com.o7solutions.freelancing_bot.utils.Constants

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var role = 1 // Default to Job Seeker (1)
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Retrieve the role from Intent (sent from Onboarding)
        // If no value is passed, it defaults to 1 (Job Seeker)
        role = intent.getIntExtra("USER_ROLE", 1)

        // UI Feedback: Update a TextView or Title to show which role they are signing up for
        updateUIBasedOnRole()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbRef = FirebaseDatabase.getInstance().getReference(Constants.userCol)
        auth = FirebaseAuth.getInstance()

        binding.loginBTN.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.createAccountBTN.setOnClickListener {
            createUser()
        }
    }

    private fun updateUIBasedOnRole() {
        val roleName = when (role) {
            0 -> "Employer"
            1 -> "Job Seeker"
            2 -> "Admin"
            else -> "User"
        }
        // Assuming you have a heading or can change the button text
        binding.createAccountBTN.text = "Register as $roleName"

        // If you still have the spinner in XML, you might want to hide it
        // or set its selection so it matches the intent data
        // binding.roleSpinner.visibility = View.GONE
    }

    private fun createUser() {
        val name = binding.nameET.text.toString().trim()
        val email = binding.emailET.text.toString().trim()
        val password = binding.passET.text.toString().trim()
        val description = binding.descriptionET.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.length < 6) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                // 2. The 'role' variable here is already set from the Intent
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

                dbRef.child(uid).setValue(newUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

                        // 3. Save to SharedPrefs for session management
                        val sharedPref = getSharedPreferences(Constants.userKey, MODE_PRIVATE)
                        sharedPref.edit().putInt("userType", role).apply()


                        if(role != 2) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this, AdminActivity::class.java))
                            finish()
                        }

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