package calmconnectapplication.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import calmconnectapplication.R
import calmconnectapplication.databinding.ActivityLoginBinding
import calmconnectapplication.db.AppDatabase
import calmconnectapplication.util.FirestoreSyncService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        prefs = getSharedPreferences("calm_connect_prefs", MODE_PRIVATE)

        applyAnimations()
        checkRememberedUser()

        binding.tvGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun applyAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        binding.ivLogo.startAnimation(fadeIn)
        binding.cardLogin.startAnimation(slideUp)
    }

    private fun checkRememberedUser() {
        val rememberMe = prefs.getBoolean("remember_me", false)
        val currentUser = auth.currentUser

        if (rememberMe && currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        clearErrors()

        when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Enter your email"
                binding.etEmail.requestFocus()
            }

            password.isEmpty() -> {
                binding.tilPassword.error = "Enter your password"
                binding.etPassword.requestFocus()
            }

            else -> {
                binding.btnLogin.isEnabled = false

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        binding.btnLogin.isEnabled = true

                        if (task.isSuccessful) {
                            prefs.edit()
                                .putBoolean("remember_me", binding.cbRememberMe.isChecked)
                                .apply()

                            // Sync cloud data to local on login
                            lifecycleScope.launch {
                                FirestoreSyncService.syncFromCloud(
                                    AppDatabase.getInstance(applicationContext)
                                )
                            }

                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                task.exception?.message ?: "Login failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
    }
}