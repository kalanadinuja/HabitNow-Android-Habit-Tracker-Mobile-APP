package com.example.madlab3.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.madlab3.R
import com.example.madlab3.models.Habit

class AddHabitDialogFragment : DialogFragment() {

    private lateinit var listener: HabitDialogListener
    private var editingHabit: Habit? = null
    private val emojiList = listOf("📝", "💪", "🏃‍♂️", "🧘‍♀️", "💧", "🍎", "😴", "📚", "🧠", "❤️")
    private var selectedEmoji = "📝"

    interface HabitDialogListener {
        fun onHabitAdded(habit: Habit)
        fun onHabitUpdated(habit: Habit)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as HabitDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement HabitDialogListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_habit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get UI elements
        val etHabitName = view.findViewById<EditText>(R.id.etHabitName)
        val etHabitUnit = view.findViewById<EditText>(R.id.etHabitUnit)
        val npGoal = view.findViewById<NumberPicker>(R.id.npGoal)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val tvEmoji = view.findViewById<TextView>(R.id.tvEmoji)
        val btnNextEmoji = view.findViewById<ImageButton>(R.id.btnNextEmoji)
        val tvDialogTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        
        // Configure number picker
        npGoal.apply {
            minValue = 1
            maxValue = 10
            value = 1
        }
        
        // Check if we're editing an existing habit
        arguments?.let { args ->
            if (args.containsKey("habit")) {
                editingHabit = args.getSerializable("habit") as? Habit
                editingHabit?.let { habit ->
                    tvDialogTitle.text = getString(R.string.edit_habit)
                    etHabitName.setText(habit.name)
                    etHabitUnit.setText(habit.unit)
                    npGoal.value = habit.goal
                    selectedEmoji = habit.icon
                    tvEmoji.text = selectedEmoji
                }
            }
        }
        
        // Set up emoji selector
        tvEmoji.text = selectedEmoji
        btnNextEmoji.setOnClickListener {
            val currentIndex = emojiList.indexOf(selectedEmoji)
            selectedEmoji = emojiList[(currentIndex + 1) % emojiList.size]
            tvEmoji.text = selectedEmoji
        }
        
        // Set up buttons
        btnCancel.setOnClickListener { dismiss() }
        
        btnSave.setOnClickListener {
            val habitName = etHabitName.text.toString().trim()
            val habitUnit = etHabitUnit.text.toString().trim().ifEmpty { "times" }
            val habitGoal = npGoal.value
            
            if (habitName.isNotEmpty()) {
                if (editingHabit != null) {
                    // Update existing habit
                    val updatedHabit = editingHabit!!.copy(
                        name = habitName,
                        unit = habitUnit,
                        goal = habitGoal,
                        icon = selectedEmoji
                    )
                    listener.onHabitUpdated(updatedHabit)
                } else {
                    // Create new habit
                    val newHabit = Habit(
                        name = habitName,
                        unit = habitUnit,
                        goal = habitGoal,
                        icon = selectedEmoji
                    )
                    listener.onHabitAdded(newHabit)
                }
                dismiss()
            }
        }
    }

    // Adjust dialog size to full width and content height when it appears
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