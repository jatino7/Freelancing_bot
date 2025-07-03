package com.o7solutions.freelancing_bot.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.o7solutions.freelancing_bot.MainActivity
import com.o7solutions.freelancing_bot.R
import com.o7solutions.freelancing_bot.databinding.ActivityLoginBinding
import com.o7solutions.freelancing_bot.utils.Constants

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding


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
        binding.apply {

            createAccount.setOnClickListener {
                val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                startActivity(intent)
            }

            loginBTN.setOnClickListener {


                binding.pgBar.visibility = View.VISIBLE
                if (emailET.text!!.isEmpty()) {
                    binding.pgBar.visibility = View.GONE

                    emailET.error = "Please enter email"
                } else if (passET.text!!.isEmpty()) {
                    binding.pgBar.visibility = View.GONE

                    passET.error = "Please enter password"
                } else {
                    val email = emailET.text.toString().trim()
                    val pass = passET.text.toString().trim()

                    auth.signInWithEmailAndPassword(email, pass).addOnFailureListener { e ->
                        binding.pgBar.visibility = View.GONE

                        Toast.makeText(
                            this@LoginActivity,
                            "Unable to login->${e}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                        .addOnSuccessListener {
                            binding.pgBar.visibility = View.GONE
                            Toast.makeText(
                                this@LoginActivity,
                                "Login Successful!",
                                Toast.LENGTH_SHORT
                            ).show()

                            getUserData()

                        }
                }
            }

        }
    }

    fun getUserData() {


        val db = FirebaseFirestore.getInstance()
        db.collection(Constants.userCol).document(auth.currentUser!!.uid)
            .get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {

                    val role = documentSnapshot.get("role")

                    if(role == 1) {
                        val sharedPref = getSharedPreferences(Constants.userKey, MODE_PRIVATE)
                        sharedPref.edit().putInt("userType",1).apply()
                    } else {
                        val sharedPref = getSharedPreferences(Constants.userKey, MODE_PRIVATE)
                        sharedPref.edit().putInt("userType", 0).apply()
                    }


                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {

                }
            }

    }
}
