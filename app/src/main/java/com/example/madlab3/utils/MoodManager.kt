package com.example.madlab3.utils

import android.content.Context
import com.example.madlab3.models.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//Stores and retrieves user mood entries

class MoodManager(private val context: Context) {
    private val sharedPref = context.getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val KEY_MOOD_ENTRIES = "mood_entries"
    }

    fun getMoodEntries(): List<MoodEntry> {
        val entriesJson = sharedPref.getString(KEY_MOOD_ENTRIES, null) ?: return emptyList()
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        val entries = gson.fromJson<List<MoodEntry>>(entriesJson, type)
        
        // Return entries sorted by date and time (newest first)
        return entries.sortedByDescending { it.id }
    }

    fun saveMoodEntry(entry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        entries.add(entry)
        saveMoodEntries(entries)
    }

    fun updateMoodEntry(entry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == entry.id }
        if (index != -1) {
            entries[index] = entry
            saveMoodEntries(entries)
        }
    }

    fun deleteMoodEntry(id: Long) {
        val entries = getMoodEntries().filter { it.id != id }
        saveMoodEntries(entries)
    }

    private fun saveMoodEntries(entries: List<MoodEntry>) {
        val entriesJson = gson.toJson(entries)
        sharedPref.edit().putString(KEY_MOOD_ENTRIES, entriesJson).apply()
    }
}