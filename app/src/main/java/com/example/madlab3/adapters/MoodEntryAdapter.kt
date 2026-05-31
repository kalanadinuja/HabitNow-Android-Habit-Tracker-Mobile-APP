package com.example.madlab3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.madlab3.R
import com.example.madlab3.models.MoodEntry

class MoodEntryAdapter(
    private var moodEntries: List<MoodEntry>,
    private val onMoodClicked: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodEntryAdapter.MoodViewHolder>() {

    class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvMoodEmoji)
        val tvMood: TextView = view.findViewById(R.id.tvMoodName)
        val tvDateTime: TextView = view.findViewById(R.id.tvMoodDateTime)
        val tvNote: TextView = view.findViewById(R.id.tvMoodNote)
        val cardView: CardView = view.findViewById(R.id.cardMood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val moodEntry = moodEntries[position]
        
        holder.apply {
            tvEmoji.text = moodEntry.emoji
            tvMood.text = moodEntry.mood
            tvDateTime.text = "${moodEntry.date} at ${moodEntry.time}"
            
            // Show note if available, hide otherwise
            if (moodEntry.note.isNotEmpty()) {
                tvNote.visibility = View.VISIBLE
                tvNote.text = moodEntry.note
            } else {
                tvNote.visibility = View.GONE
            }
            
            // Set card color based on mood
            val colorAccent = ContextCompat.getColor(itemView.context, moodEntry.colorResId)
            cardView.setCardBackgroundColor(colorAccent.withAlpha(30)) // Lighter version
            
            // Set click listener
            itemView.setOnClickListener { onMoodClicked(moodEntry) }
        }
    }

    override fun getItemCount() = moodEntries.size

    fun updateData(newEntries: List<MoodEntry>) {
        moodEntries = newEntries
        notifyDataSetChanged()
    }
    
    // Helper function to make colors more subtle
    private fun Int.withAlpha(alpha: Int): Int {
        return this and 0x00FFFFFF or (alpha shl 24)
    }
}