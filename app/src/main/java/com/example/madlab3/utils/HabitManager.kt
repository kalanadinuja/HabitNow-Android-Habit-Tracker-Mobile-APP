package com.example.madlab3.utils

import android.content.Context
import com.example.madlab3.models.Habit
import com.example.madlab3.models.HabitProgress
import com.example.madlab3.widgets.HabitProgressWidget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//Manages the data related to user habits

class HabitManager(private val context: Context) {
    private val sharedPref = context.getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val KEY_HABITS = "habits"
        const val KEY_HABIT_PROGRESS = "habit_progress"
    }

    // Habit CRUD operations
    fun getHabits(): List<Habit> {
        val habitsJson = sharedPref.getString(KEY_HABITS, null) ?: return emptyList()
        val type = object : TypeToken<List<Habit>>() {}.type
        return gson.fromJson(habitsJson, type)
    }

    fun saveHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        habits.add(habit)
        saveHabits(habits)
        
        // Update widget when a habit is added
        HabitProgressWidget.updateAllWidgets(context)
    }

    fun updateHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
            
            // Update widget when a habit is updated
            HabitProgressWidget.updateAllWidgets(context)
        }
    }

    fun deleteHabit(habitId: String) {
        val habits = getHabits().filter { it.id != habitId }
        saveHabits(habits)
        
        // Also delete associated progress
        val allProgress = getAllHabitProgress().filter { it.habitId != habitId }
        saveAllHabitProgress(allProgress)
        
        // Update widget when a habit is deleted
        HabitProgressWidget.updateAllWidgets(context)
    }

    private fun saveHabits(habits: List<Habit>) {
        val habitsJson = gson.toJson(habits)
        sharedPref.edit().putString(KEY_HABITS, habitsJson).apply()
    }

    // Habit Progress operations , Loads all stored habit progress records.
    fun getAllHabitProgress(): List<HabitProgress> {
        val progressJson = sharedPref.getString(KEY_HABIT_PROGRESS, null) ?: return emptyList()
        val type = object : TypeToken<List<HabitProgress>>() {}.type
        return gson.fromJson(progressJson, type)
    }

    fun getHabitProgressForDate(date: String): List<HabitProgress> {
        return getAllHabitProgress().filter { it.date == date }
    }

    fun getHabitProgressForDate(date: String, habitId: String): HabitProgress? {
        return getAllHabitProgress().find { it.date == date && it.habitId == habitId }
    }

    fun saveHabitProgress(progress: HabitProgress) {
        val allProgress = getAllHabitProgress().toMutableList()
        val existingIndex = allProgress.indexOfFirst { 
            it.habitId == progress.habitId && it.date == progress.date 
        }
        
        if (existingIndex != -1) {
            allProgress[existingIndex] = progress
        } else {
            allProgress.add(progress)
        }
        
        saveAllHabitProgress(allProgress)
        
        // Update widget when progress changes
        HabitProgressWidget.updateAllWidgets(context)
    }

    private fun saveAllHabitProgress(progressList: List<HabitProgress>) {
        val progressJson = gson.toJson(progressList)
        sharedPref.edit().putString(KEY_HABIT_PROGRESS, progressJson).apply()
    }
}