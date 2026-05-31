package com.example.madlab3.models

data class HabitProgress(
    //stores how much of a habit the user completed on a particular date.
    val habitId: String,
    val date: String, // Format: yyyy-MM-dd
    var progress: Int = 0,
    var isCompleted: Boolean = false
)