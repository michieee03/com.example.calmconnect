package com.example.calmconnect.view

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.calmconnect.R
import com.example.calmconnect.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        applyAnimations()

        binding.tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSignup.setOnClickListener {
            signUpUser()
        }
    }

    private fun applyAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        binding.ivLogo.startAnimation(fadeIn)
        binding.cardSignup.startAnimation(slideUp)
    }

    private fun signUpUser() {
        val fullName = binding.etFullName.text?.toString()?.trim().orEmpty()
        val email = binding.etSignupEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etSignupPassword.text?.toString()?.trim().orEmpty()
        val confirmPassword = binding.etConfirmPassword.text?.toString()?.trim().orEmpty()

        clearErrors()

        when {
            fullName.isEmpty() -> {
                binding.tilFullName.error = "Enter your full name"
                binding.etFullName.requestFocus()
            }

            email.isEmpty() -> {
                binding.tilSignupEmail.error = "Enter your email"
                binding.etSignupEmail.requestFocus()
            }

            password.isEmpty() -> {
                binding.tilSignupPassword.error = "Enter your password"
                binding.etSignupPassword.requestFocus()
            }

            password.length < 6 -> {
                binding.tilSignupPassword.error = "Password must be at least 6 characters"
                binding.etSignupPassword.requestFocus()
            }

            confirmPassword.isEmpty() -> {
                binding.tilConfirmPassword.error = "Confirm your password"
                binding.etConfirmPassword.requestFocus()
            }

            password != confirmPassword -> {
                binding.tilConfirmPassword.error = "Passwords do not match"
                binding.etConfirmPassword.requestFocus()
            }

            else -> {
                binding.btnSignup.isEnabled = false

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        binding.btnSignup.isEnabled = true

                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build()

                            user?.updateProfile(profileUpdates)

                            Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()

                            auth.signOut()

                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                task.exception?.message ?: "Signup failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }
    }

    private fun clearErrors() {
        binding.tilFullName.error = null
        binding.tilSignupEmail.error = null
        binding.tilSignupPassword.error = null
        binding.tilConfirmPassword.error = null
    }
}