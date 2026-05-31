package com.example.madlab3.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madlab3.R
import com.example.madlab3.models.User
import com.example.madlab3.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView

class RegisterActivity : AppCompatActivity() {
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        prefManager = PreferenceManager(this)

        val emailInput = findViewById<TextInputEditText>(R.id.inputEmail)
        val usernameInput = findViewById<TextInputEditText>(R.id.inputUsername)
        val passwordInput = findViewById<TextInputEditText>(R.id.inputPassword)
        val fullNameInput = findViewById<TextInputEditText>(R.id.inputFullName)
        val registerBtn = findViewById<MaterialButton>(R.id.btnRegister)
        val loginLink = findViewById<TextView>(R.id.linkLogin)

        registerBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val fullName = fullNameInput.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(email, username, password, fullName, System.currentTimeMillis())
            val success = prefManager.registerUser(user)
            if (success) {
                Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.register_failed), Toast.LENGTH_SHORT).show()
            }
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}