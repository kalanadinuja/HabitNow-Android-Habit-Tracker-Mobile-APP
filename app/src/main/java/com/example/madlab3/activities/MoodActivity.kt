package com.example.madlab3.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madlab3.R
import com.example.madlab3.adapters.MoodEntryAdapter
import com.example.madlab3.fragments.AddMoodDialogFragment
import com.example.madlab3.models.MoodEntry
import com.example.madlab3.utils.MoodManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MoodActivity : AppCompatActivity(), AddMoodDialogFragment.MoodDialogListener {
    private lateinit var moodManager: MoodManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoodEntryAdapter
    private lateinit var moodChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)

        moodManager = MoodManager(this)
        
        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerMoods)
        moodChart = findViewById(R.id.moodChart)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnShare = findViewById<ImageButton>(R.id.btnShare)
        val fabAddMood = findViewById<FloatingActionButton>(R.id.fabAddMood)
        
        // Set up back button
        btnBack.setOnClickListener { finish() }
        
        // Set up share button
        btnShare.setOnClickListener { shareMoodSummary() }
        
        // Set up RecyclerView
        setupRecyclerView()
        
        // Set up mood chart
        setupMoodChart()
        
        // Set up FAB for adding moods
        fabAddMood.setOnClickListener {
            val dialogFragment = AddMoodDialogFragment()
            dialogFragment.show(supportFragmentManager, "AddMoodDialog")
        }
    }

    private fun setupRecyclerView() {
        adapter = MoodEntryAdapter(moodManager.getMoodEntries()) { moodEntry ->
            // Show edit dialog when mood entry is clicked
            val dialogFragment = AddMoodDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("mood_entry", moodEntry)
                }
            }
            dialogFragment.show(supportFragmentManager, "EditMoodDialog")
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MoodActivity)
            adapter = this@MoodActivity.adapter
        }
    }
    
    private fun setupMoodChart() {
        // Get mood entries for the past 7 days
        val pastWeekEntries = getLastWeekMoodData()
        
        // Prepare chart data
        val entries = ArrayList<Entry>()
        val dateLabels = ArrayList<String>()
        val dateFormat = SimpleDateFormat("E", Locale.getDefault()) // Day name (e.g., Mon, Tue)
        
        // Map mood names to numerical values (1-5 scale)
        val moodValues = mapOf(
            "Excited" to 5f,
            "Happy" to 4f,
            "Neutral" to 3f,
            "Sad" to 2f,
            "Angry" to 1f
        )
        
        // Default value for days with no entries
        val defaultMoodValue = 3f // Neutral
        
        // For each day in the past week
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -6) // Start 6 days ago
        
        for (i in 0 until 7) {
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val dayLabel = dateFormat.format(calendar.time)
            
            // Find mood entry for this day
            val entry = pastWeekEntries.find { it.date == dateString }
            
            // Add entry with mood value or default
            val moodValue = if (entry != null) {
                moodValues[entry.mood] ?: defaultMoodValue
            } else {
                defaultMoodValue
            }
            
            entries.add(Entry(i.toFloat(), moodValue))
            dateLabels.add(dayLabel)
            
            // Move to next day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        // Create dataset
        val dataSet = LineDataSet(entries, getString(R.string.mood_trend))
        
        // Style the dataset
        dataSet.apply {
            color = ContextCompat.getColor(this@MoodActivity, R.color.colorPrimary)
            lineWidth = 3f
            setDrawCircles(true)
            setCircleColor(ContextCompat.getColor(this@MoodActivity, R.color.colorPrimary))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        
        // Create LineData and set to chart
        val lineData = LineData(dataSet)
        moodChart.data = lineData
        
        // Style chart
        moodChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            setPinchZoom(false)
            setDrawGridBackground(false)
            
            // X-axis styling
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(dateLabels)
                setDrawGridLines(false)
            }
            
            // Y-axis styling
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 6f
                setDrawGridLines(true)
                setLabelCount(6, true)
            }
            
            // Disable right axis
            axisRight.isEnabled = false
            
            // Animate
            animateX(1000)
        }
        
        // Refresh
        moodChart.invalidate()
    }
    
    private fun getLastWeekMoodData(): List<MoodEntry> {
        val allEntries = moodManager.getMoodEntries()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7) // One week ago
        
        val cutoffDate = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Find entries from the last 7 days, and take the most recent for each day
        return allEntries
            .groupBy { it.date }
            .filter {
                try {
                    val entryDate = dateFormat.parse(it.key)
                    entryDate != null && entryDate.after(cutoffDate)
                } catch (e: Exception) {
                    false
                }
            }
            .map { it.value.maxByOrNull { entry -> entry.id } ?: it.value.first() }
    }
    
    private fun shareMoodSummary() {
        val moodEntries = moodManager.getMoodEntries().take(10) // Get the most recent 10 entries
        
        // Create a summary text
        val summary = buildString {
            append(getString(R.string.share_mood_summary_title))
            append("\n\n")
            
            if (moodEntries.isEmpty()) {
                append(getString(R.string.no_mood_entries))
            } else {
                moodEntries.forEach { entry ->
                    append("${entry.date} (${entry.time}): ${entry.emoji} ${entry.mood}")
                    if (entry.note.isNotEmpty()) {
                        append(" - \"${entry.note}\"")
                    }
                    append("\n")
                }
                
                // Add a conclusion based on the most recent mood
                val mostRecentMood = moodEntries.first()
                append("\n")
                append(getString(R.string.mood_summary_conclusion, mostRecentMood.mood, mostRecentMood.emoji))
            }
        }
        
        // Create a share intent
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_mood_subject))
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        
        // Show the share sheet
        startActivity(Intent.createChooser(intent, getString(R.string.share_mood_via)))
    }

    override fun onMoodAdded(moodEntry: MoodEntry) {
        moodManager.saveMoodEntry(moodEntry)
        refreshData()
    }

    override fun onMoodUpdated(moodEntry: MoodEntry) {
        moodManager.updateMoodEntry(moodEntry)
        refreshData()
    }

    private fun refreshData() {
        adapter.updateData(moodManager.getMoodEntries())
        setupMoodChart() // Refresh chart with updated data
    }
}