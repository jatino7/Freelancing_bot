package com.o7solutions.freelancing_bot
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.o7solutions.freelancing_bot.auth.LoginActivity
import com.o7solutions.freelancing_bot.auth.SignupActivity

class OnBoardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        val cardEmployer = findViewById<MaterialCardView>(R.id.cardEmployer)
        val cardJobSeeker = findViewById<MaterialCardView>(R.id.cardJobSeeker)
        val cardAdmin = findViewById<MaterialCardView>(R.id.cardAdmin)

        // Set Click Listeners with specific role IDs
        cardEmployer.setOnClickListener { navigateToRegister(0) }
        cardJobSeeker.setOnClickListener { navigateToRegister(1) }
        cardAdmin.setOnClickListener { navigateToRegister(2) }

        findViewById<Button>(R.id.loginBTN).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun navigateToRegister(roleId: Int) {
        val intent = Intent(this, SignupActivity::class.java).apply {
            putExtra("USER_ROLE", roleId)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}