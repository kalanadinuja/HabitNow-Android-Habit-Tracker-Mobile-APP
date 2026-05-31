package com.example.madlab3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.madlab3.R

class EmojiAdapter(
    private val emojiOptions: List<Triple<String, String, Int>>,
    private val onEmojiSelected: (String, String, Int) -> Unit
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    class EmojiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvEmoji)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emoji, parent, false)
        return EmojiViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val (emoji, mood, color) = emojiOptions[position]
        
        holder.tvEmoji.text = emoji
        
        holder.itemView.setOnClickListener {
            onEmojiSelected(emoji, mood, color)
        }
    }

    override fun getItemCount() = emojiOptions.size
}