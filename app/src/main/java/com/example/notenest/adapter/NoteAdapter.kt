package com.example.notenest.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notenest.data.Note
import com.example.notenest.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(private val onNoteClick: (Note) -> Unit,
                  private val onNoteDelete: (Note) -> Unit)
    : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note, onNoteClick: (Note) -> Unit) {
            binding.tvTitle.text = note.title
            binding.tvContent.text = note.content
            val date = Date(note.timestamp)
            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            binding.tvTimestamp.text = sdf.format(date)
            // Handle clicks
            binding.root.setOnClickListener { onNoteClick(note) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position), onNoteClick)
    }

    fun getNoteAt(position: Int): Note = getItem(position)

    private class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
/*

    // Click listener interface
    var onItemClick: ((Note) -> Unit)? = null
    var onItemLongClick: ((Note) -> Boolean)? = null

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note, onNoteClick:(Note) -> Unit) {
            binding.tvNoteTitle.text = note.title
            binding.tvNoteContent.text = note.content

            // Set click listeners
            binding.root.setOnClickListener {
                onItemClick?.invoke(note)
            }

            binding.root.setOnLongClickListener {
                onItemLongClick?.invoke(note) ?: false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position), onNoteClick)
    }

    private class NoteDiffCallback :  DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.content == newItem.content
        }
    }
}*/
