package com.example.madlab3.activities

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.madlab3.R
import com.example.madlab3.receivers.HydrationReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HydrationActivity : AppCompatActivity() {
    private lateinit var etHours: EditText
    private lateinit var etMinutes: EditText
    private lateinit var tvStartTime: TextView
    private var startTimeCalendar = Calendar.getInstance()
    private var isReminderActive = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_hydration)
        
        createNotificationChannel()
        
        etHours = findViewById(R.id.etHours)
        etMinutes = findViewById(R.id.etMinutes)
        tvStartTime = findViewById(R.id.tvStartTime)
        
        findViewById<Button>(R.id.btnSetTime).setOnClickListener { showTimePickerDialog() }
        findViewById<Button>(R.id.btnStartReminder).setOnClickListener { startReminderSettings() }
        findViewById<Button>(R.id.btnStopReminder).setOnClickListener { stopReminder() }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        
        // Load saved settings if they exist
        val sharedPref = getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        val savedHours = sharedPref.getInt("interval_hours", 2)
        val savedMinutes = sharedPref.getInt("interval_minutes", 0)
        val savedStartTimeMillis = sharedPref.getLong("start_time_millis", 0L)
        isReminderActive = sharedPref.getBoolean("is_reminder_active", false)
        
        etHours.setText(savedHours.toString())
        etMinutes.setText(savedMinutes.toString())
        
        // Set start time from saved preferences or default
        if (savedStartTimeMillis > 0) {
            startTimeCalendar.timeInMillis = savedStartTimeMillis
        } else {
            startTimeCalendar.set(Calendar.HOUR_OF_DAY, 8)
            startTimeCalendar.set(Calendar.MINUTE, 0)
        }
        
        updateTimeDisplay()
        updateButtonState()
    }
    
    private fun showTimePickerDialog() {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                startTimeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                startTimeCalendar.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
            },
            startTimeCalendar.get(Calendar.HOUR_OF_DAY),
            startTimeCalendar.get(Calendar.MINUTE),
            false
        ).show()
    }
    
    private fun updateTimeDisplay() {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        tvStartTime.text = format.format(startTimeCalendar.time)
    }
    
    private fun startReminderSettings() {
        try {
            val hours = if (etHours.text.toString().isEmpty()) 0 else etHours.text.toString().toInt()
            val minutes = if (etMinutes.text.toString().isEmpty()) 0 else etMinutes.text.toString().toInt()
            
            // Validate input - at least some time interval must be specified
            if (hours <= 0 && minutes <= 0) {
                Toast.makeText(this, "Please enter a valid interval", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Save settings to shared preferences
            val sharedPref = getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("interval_hours", hours)
                putInt("interval_minutes", minutes)
                putLong("start_time_millis", startTimeCalendar.timeInMillis)
                putBoolean("is_reminder_active", true)
                apply()
            }
            
            // Schedule the alarm
            scheduleHydrationReminder(hours, minutes)
            isReminderActive = true
            updateButtonState()
            
            Toast.makeText(this, R.string.reminder_started, Toast.LENGTH_SHORT).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent)
        
        // Update shared preferences
        val sharedPref = getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_reminder_active", false)
            apply()
        }
        
        isReminderActive = false
        updateButtonState()
        Toast.makeText(this, R.string.reminder_stopped, Toast.LENGTH_SHORT).show()
    }
    
    private fun updateButtonState() {
        val startButton = findViewById<Button>(R.id.btnStartReminder)
        val stopButton = findViewById<Button>(R.id.btnStopReminder)
        
        if (isReminderActive) {
            startButton.isEnabled = false
            startButton.alpha = 0.5f
            stopButton.isEnabled = true
            stopButton.alpha = 1.0f
        } else {
            startButton.isEnabled = true
            startButton.alpha = 1.0f
            stopButton.isEnabled = false
            stopButton.alpha = 0.5f
        }
    }
    
    private fun scheduleHydrationReminder(hours: Int, minutes: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent)
        
        // Calculate total interval in milliseconds
        val intervalMillis = (hours * 60 * 60 * 1000L) + (minutes * 60 * 1000L)
        
        // Set repeating alarm from the selected start time
        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startTimeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, startTimeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            // If the time is in the past, set it for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime.timeInMillis,
            intervalMillis,
            pendingIntent
        )
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("hydration_channel", name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}