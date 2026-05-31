package com.example.madlab3.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madlab3.R
import com.example.madlab3.adapters.EmojiAdapter
import com.example.madlab3.models.MoodEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMoodDialogFragment : DialogFragment() {

    private lateinit var listener: MoodDialogListener
    private var selectedEmoji = "😊"
    private var selectedMood = "Happy"
    private var selectedColor = R.color.colorPrimary
    private var editingMood: MoodEntry? = null
    
    // List of emoji options with their mood names and colors
    private val emojiOptions = listOf(
        Triple("😊", "Happy", R.color.colorAccent),
        Triple("😐", "Neutral", R.color.colorPrimary),
        Triple("😢", "Sad", R.color.colorSecondary),
        Triple("😡", "Angry", R.color.buttonColor),
        Triple("😴", "Tired", R.color.colorPrimaryDark),
        Triple("🤔", "Thoughtful", R.color.textColorPrimary),
        Triple("😎", "Cool", R.color.colorPrimary),
        Triple("😰", "Anxious", R.color.buttonColor),
        Triple("🥰", "Loved", R.color.colorSecondary),
        Triple("🤩", "Excited", R.color.colorAccent)
    )

    interface MoodDialogListener {
        fun onMoodAdded(moodEntry: MoodEntry)
        fun onMoodUpdated(moodEntry: MoodEntry)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as MoodDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement MoodDialogListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get UI elements
        val recyclerEmojis = view.findViewById<RecyclerView>(R.id.recyclerEmojis)
        val tvSelectedEmoji = view.findViewById<TextView>(R.id.tvSelectedEmoji)
        val tvSelectedMood = view.findViewById<TextView>(R.id.tvSelectedMood)
        val etNote = view.findViewById<EditText>(R.id.etNote)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        
        // Check if we're editing an existing mood
        arguments?.let { args ->
            if (args.containsKey("mood_entry")) {
                editingMood = args.getSerializable("mood_entry") as? MoodEntry
                editingMood?.let { mood ->
                    tvDialogTitle.text = getString(R.string.edit_mood)
                    selectedEmoji = mood.emoji
                    selectedMood = mood.mood
                    selectedColor = mood.colorResId
                    etNote.setText(mood.note)
                    tvSelectedEmoji.text = selectedEmoji
                    tvSelectedMood.text = selectedMood
                }
            }
        }
        
        // Set up emoji recycler view
        val emojiAdapter = EmojiAdapter(emojiOptions) { emoji, mood, color ->
            selectedEmoji = emoji
            selectedMood = mood
            selectedColor = color
            tvSelectedEmoji.text = emoji
            tvSelectedMood.text = mood
        }
        
        recyclerEmojis.apply {
            layoutManager = GridLayoutManager(context, 5) // 5 emojis per row
            adapter = emojiAdapter
        }
        
        // Set initial selected emoji and mood
        tvSelectedEmoji.text = selectedEmoji
        tvSelectedMood.text = selectedMood
        
        // Set up buttons
        btnCancel.setOnClickListener { dismiss() }
        
        btnSave.setOnClickListener {
            val note = etNote.text.toString().trim()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentDate = Date()
            
            if (editingMood != null) {
                // Update existing mood entry
                val updatedMood = MoodEntry(
                    id = editingMood!!.id,
                    emoji = selectedEmoji,
                    mood = selectedMood,
                    date = editingMood!!.date,
                    time = editingMood!!.time,
                    note = note,
                    colorResId = selectedColor
                )
                listener.onMoodUpdated(updatedMood)
            } else {
                // Create new mood entry
                val newMood = MoodEntry(
                    id = System.currentTimeMillis(),
                    emoji = selectedEmoji,
                    mood = selectedMood,
                    date = dateFormat.format(currentDate),
                    time = timeFormat.format(currentDate),
                    note = note,
                    colorResId = selectedColor
                )
                listener.onMoodAdded(newMood)
            }
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }
}