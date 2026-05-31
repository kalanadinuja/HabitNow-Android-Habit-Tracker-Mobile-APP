package com.example.madlab3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.madlab3.R
import com.example.madlab3.models.Habit
import com.example.madlab3.models.HabitProgress

class HabitAdapter(
    private var habits: List<Habit>,
    private var progress: List<HabitProgress>,
    private val actionListener: HabitActionListener
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    interface HabitActionListener {
        fun onCompleteClicked(habit: Habit)
        fun onEditClicked(habit: Habit)
        fun onDeleteClicked(habit: Habit)
    }

    class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvName: TextView = view.findViewById(R.id.tvHabitName)
        val tvGoal: TextView = view.findViewById(R.id.tvHabitGoal)
        val progressBar: ProgressBar = view.findViewById(R.id.habitProgressBar)
        val btnComplete: Button = view.findViewById(R.id.btnComplete)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        val habitProgress = progress.find { it.habitId == habit.id }
        val isCompleted = habitProgress?.isCompleted ?: false
        val currentProgress = habitProgress?.progress ?: 0
        val progressPercentage = if (habit.goal > 0) (currentProgress * 100) / habit.goal else 0

        holder.apply {

            tvName.text = habit.name
            tvGoal.text = itemView.context.getString(
                R.string.habit_goal_format,
                habit.goal,
                habit.unit
            )
            progressBar.progress = progressPercentage
            
            // Set button text based on completion status
            btnComplete.text = if (isCompleted) 
                itemView.context.getString(R.string.habit_completed) 
            else 
                itemView.context.getString(R.string.habit_complete)
            
            // Set button color based on completion status
            btnComplete.setBackgroundResource(
                if (isCompleted) R.drawable.button_completed else R.drawable.button_complete
            )
            
            // Set click listeners
            btnComplete.setOnClickListener { actionListener.onCompleteClicked(habit) }
            btnEdit.setOnClickListener { actionListener.onEditClicked(habit) }
            btnDelete.setOnClickListener { actionListener.onDeleteClicked(habit) }
        }
    }

    override fun getItemCount() = habits.size

    fun updateData(newHabits: List<Habit>, newProgress: List<HabitProgress>) {
        habits = newHabits
        progress = newProgress
        notifyDataSetChanged()
    }
}