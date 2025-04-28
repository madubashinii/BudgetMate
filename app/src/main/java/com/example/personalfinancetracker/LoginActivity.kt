package com.example.personalfinancetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

// Auto-login if user is already logged in
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (prefs.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, BaseActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etLoginEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etLoginPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val txtToRegister = findViewById<TextView>(R.id.txtToRegister)

        btnSignIn.setOnClickListener {
            val inputEmail = etEmail.text.toString().trim()
            val inputPassword = etPassword.text.toString()

            val savedEmail = prefs.getString("email", "")
            val savedPassword = prefs.getString("password", "")

            if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else if (inputEmail == savedEmail && inputPassword == savedPassword) {
                // Reset previous user's data and set default values for the new user
                resetStateForNewUser(this)
                initializeDataForNewUser(this, inputEmail)

                prefs.edit().putBoolean("isLoggedIn", true).apply()
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, BaseActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        txtToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun resetStateForNewUser(context: Context) {
        val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Clear all user-specific data (transactions, budget, name)
        val userEmail = sharedPreferences.getString("loggedInEmail", "") ?: "unknown_user"
        val budgetKey = "${userEmail}_monthly_budget"
        val transactionKey = "${userEmail}_transactions"
        val nameKey = "${userEmail}_name"

        // Clear data for the previous user
        editor.remove(budgetKey)
        editor.remove(transactionKey)
        editor.remove(nameKey)

        // Apply changes to reset data
        editor.apply()
    }

    private fun initializeDataForNewUser(context: Context, userEmail: String) {
        val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val budgetKey = "${userEmail}_monthly_budget"
        val transactionKey = "${userEmail}_transactions"
        val nameKey = "${userEmail}_name"

        // Set default values for the new user
        editor.putFloat(budgetKey, 0f)
        editor.putString(transactionKey, "[]")
        editor.putString(nameKey, "New User")

        // Apply the changes
        editor.apply()
    }

}