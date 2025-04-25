package com.example.notenest

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.notenest.data.Note
import com.example.notenest.data.NoteState
import com.example.notenest.databinding.ActivityNoteDetailBinding
import com.example.notenest.firebase.FirebaseDatabaseManager
import com.example.notenest.vm.NoteViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class NoteDetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }

    private lateinit var binding: ActivityNoteDetailBinding
    private val viewModel: NoteViewModel by viewModels()
    private var noteId: String? = null
    private lateinit var firebaseDatabaseManager: FirebaseDatabaseManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if editing an existing note
        noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        if (noteId != null) {
            viewModel.getNoteById(noteId!!)
            viewModel.noteState.observe(this) { state ->
                binding.progressNoteDetail.visibility = View.GONE
                binding.tvNoteError.visibility = View.GONE
                binding.etNoteTitle.visibility = View.GONE
                binding.etNoteContent.visibility = View.GONE

                when (state) {
                    is NoteState.Loading -> {
                        binding.progressNoteDetail.visibility = View.VISIBLE
                    }

                    is NoteState.Success -> {
                        val note = state.notes.firstOrNull()
                        if (note != null) {
                            binding.etNoteTitle.visibility = View.VISIBLE
                            binding.etNoteContent.visibility = View.VISIBLE
                            binding.etNoteTitle.setText(note.title)
                            binding.etNoteContent.setText(note.content)
                        } else {
                            binding.tvNoteError.visibility = View.VISIBLE
                            binding.tvNoteError.text = "Note not found"
                        }
                    }

                    is NoteState.Error -> {
                        binding.tvNoteError.visibility = View.VISIBLE
                        binding.tvNoteError.text = state.message
                    }

                    null -> {
                        // Initial state before fetching
                    }
                }
            }
        } else {
            // New note, make EditTexts visible
            binding.etNoteTitle.visibility = View.VISIBLE
            binding.etNoteContent.visibility = View.VISIBLE
        }

        // Save button click
        binding.btnSaveNote.setOnClickListener {
            val title = binding.etNoteTitle.text.toString()
            val content = binding.etNoteContent.text.toString()

            if (title.isNotEmpty() && content.isNotEmpty()) {
                val note = if (noteId != null) {
                    // Update existing note
                    Note(
                        id = noteId!!,
                        title = title,
                        content = content,
                        timestamp = System.currentTimeMillis()
                    )
                } else {
                    // Create new note
                    Note(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        content = content,
                        timestamp = System.currentTimeMillis()
                    )
                }

                if (noteId != null) {
                    viewModel.updateNote(note)
                } else {
                    viewModel.insertNote(note)
                }

                // Save to local database
                viewModel.insertNote(note)

                // Sync to Firebase
                firebaseDatabaseManager.syncNotesToFirebase(note)

                finish()
            }
        }
    }
}