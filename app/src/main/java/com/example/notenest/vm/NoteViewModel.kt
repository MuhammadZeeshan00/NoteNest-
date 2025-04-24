package com.example.notenest.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notenest.data.Note
import com.example.notenest.data.NoteRepository
import com.example.notenest.data.NoteState
import com.example.notenest.data.QuoteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _noteState = MutableLiveData<NoteState>(NoteState.Loading)
    val noteState: LiveData<NoteState> = _noteState

    private val _singleNoteState = MutableLiveData<NoteState>()

    private val _quoteState = MutableLiveData<QuoteState>(QuoteState.Loading)
    val quoteState: LiveData<QuoteState> = _quoteState

    init {
        viewModelScope.launch {
            try {
                noteRepository.notes.collect { noteList ->
                    _noteState.postValue(NoteState.Success(noteList))
                }
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error loading notes: $e")
                _noteState.postValue(NoteState.Error("Failed to load notes: ${e.message}"))
            }
        }

        fetchQuotes()
    }

    fun fetchQuotes() {
        viewModelScope.launch {
            _quoteState.postValue(QuoteState.Loading)
            try {
                val quoteList = noteRepository.getQuotes()
                Log.d("NoteViewModel", "Quotes fetched: $quoteList")
                _quoteState.postValue(QuoteState.Success(quoteList))
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error fetching quotes: $e")
                _quoteState.postValue(QuoteState.Error("Failed to load quote: ${e.message}"))
            }
        }
    }

    fun getNoteById(noteId: String) {
        viewModelScope.launch {
            _singleNoteState.postValue(NoteState.Loading)
            try {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    _singleNoteState.postValue(NoteState.Success(listOf(note)))
                } else {
                    _singleNoteState.postValue(NoteState.Error("Note not found"))
                }
            } catch (e: Exception) {
                Log.e("NoteViewModel", "Error fetching note: $e")
                _singleNoteState.postValue(NoteState.Error("Failed to load note: ${e.message}"))
            }
        }
    }

    fun insertNote(note: Note) {
        viewModelScope.launch {
            noteRepository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
        }
    }
}