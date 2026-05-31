package com.example.madlab3.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.madlab3.R
import com.example.madlab3.utils.PreferenceManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefManager = PreferenceManager(this)
        val user = prefManager.getCurrentUser()

        
        // Set click listeners for buttons
        findViewById<Button>(R.id.btnHydration).setOnClickListener {
            startActivity(Intent(this, HydrationActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnHabit).setOnClickListener {
            startActivity(Intent(this, HabitActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnMood).setOnClickListener {
            startActivity(Intent(this, MoodActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}