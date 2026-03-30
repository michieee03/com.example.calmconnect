package com.example.calmconnect.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.calmconnect.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("calm_connect_prefs", MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateUser()
        }, 2000)
    }

    private fun navigateUser() {
        val currentUser = auth.currentUser
        val rememberMe = prefs.getBoolean("remember_me", false)

        val intent = when {
            // User already logged in and checked "Remember me"
            currentUser != null && rememberMe -> {
                Intent(this, MainActivity::class.java)
            }

            // No account yet → go to Signup
            currentUser == null -> {
                Intent(this, SignupActivity::class.java)
            }

            // User exists but not remembered → go to Login
            else -> {
                Intent(this, LoginActivity::class.java)
            }
        }

        startActivity(intent)
        finish()
    }
}