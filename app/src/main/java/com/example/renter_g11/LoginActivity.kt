package com.example.renter_g11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.renter_g11.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private val TAG: String = "RENTER_APP"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnLogin.setOnClickListener {
            // get email and password
            val emailFromUI = binding.etEmail.text.toString()
            val passwordFromUI = binding.etPassword.text.toString()
            // try to login
            loginUser(emailFromUI, passwordFromUI)
        }
    }

    fun loginUser(email:String, password:String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                    task ->
                if (task.isSuccessful) {
                    binding.tvError.isVisible = false
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val intent = Intent(this@LoginActivity,SearchListingActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    binding.tvError.text = "${task.exception?.message}"
                    binding.tvError.isVisible = true
                }
            }
    }
}