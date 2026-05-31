package com.example.madlab3.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madlab3.R
import com.example.madlab3.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    private lateinit var prefManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefManager = PreferenceManager(this)

        val emailInput = findViewById<TextInputEditText>(R.id.inputEmail)
        val passwordInput = findViewById<TextInputEditText>(R.id.inputPassword)
        val loginBtn = findViewById<MaterialButton>(R.id.btnLogin)
        val registerLink = findViewById<TextView>(R.id.linkRegister)

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val user = prefManager.loginUser(email, password)
            if (user != null) {
                Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
            }
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}