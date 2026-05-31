package com.example.madlab3.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madlab3.R
import com.example.madlab3.adapters.HabitAdapter
import com.example.madlab3.fragments.AddHabitDialogFragment
import com.example.madlab3.models.Habit
import com.example.madlab3.models.HabitProgress
import com.example.madlab3.utils.HabitManager
import com.example.madlab3.widgets.HabitProgressWidget
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitActivity : AppCompatActivity(), AddHabitDialogFragment.HabitDialogListener {
    private lateinit var habitManager: HabitManager
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit)

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerHabits)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.tvProgress)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val fabAddHabit = findViewById<ExtendedFloatingActionButton>(R.id.fabAddHabit)

        // Set up back button
        btnBack.setOnClickListener { finish() }

        // Initialize habit manager
        habitManager = HabitManager(this)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up FAB for adding habits
        fabAddHabit.setOnClickListener {
            val dialogFragment = AddHabitDialogFragment()
            dialogFragment.show(supportFragmentManager, "AddHabitDialog")
        }

        // Update progress display
        updateProgress()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            habitManager.getHabits(),
            habitManager.getHabitProgressForDate(today),
            object : HabitAdapter.HabitActionListener {
                override fun onCompleteClicked(habit: Habit) {
                    toggleHabitCompletion(habit)
                }

                override fun onEditClicked(habit: Habit) {
                    val dialogFragment = AddHabitDialogFragment().apply {
                        arguments = Bundle().apply {
                            putSerializable("habit", habit)
                        }
                    }
                    dialogFragment.show(supportFragmentManager, "EditHabitDialog")
                }

                override fun onDeleteClicked(habit: Habit) {
                    habitManager.deleteHabit(habit.id)
                    refreshData()
                }
            }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HabitActivity)
            adapter = habitAdapter
        }
    }

    override fun onHabitAdded(habit: Habit) {
        habitManager.saveHabit(habit)
        refreshData()
    }

    override fun onHabitUpdated(habit: Habit) {
        habitManager.updateHabit(habit)
        refreshData()
    }

    private fun toggleHabitCompletion(habit: Habit) {
        val progress = habitManager.getHabitProgressForDate(today, habit.id)
        
        if (progress == null) {
            // Create new progress if it doesn't exist
            habitManager.saveHabitProgress(
                HabitProgress(
                    habitId = habit.id,
                    date = today,
                    progress = habit.goal,
                    isCompleted = true
                )
            )
        } else {
            // Toggle completion status
            val newProgress = progress.copy(
                progress = if (progress.isCompleted) 0 else habit.goal,
                isCompleted = !progress.isCompleted
            )
            habitManager.saveHabitProgress(newProgress)
        }
        refreshData()
    }

    private fun updateProgress() {
        val habits = habitManager.getHabits()
        val progressList = habitManager.getHabitProgressForDate(today)
        
        // Calculate completion percentage
        val totalHabits = habits.size
        val completedHabits = progressList.count { it.isCompleted }
        
        val percentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
        
        // Update UI
        progressBar.progress = percentage
        progressText.text = getString(R.string.habit_progress_text, completedHabits, totalHabits)
    }

    private fun refreshData() {
        habitAdapter.updateData(
            habitManager.getHabits(),
            habitManager.getHabitProgressForDate(today)
        )
        updateProgress()
        
        // Update widget whenever habit data changes
        HabitProgressWidget.updateAllWidgets(this)
    }
}