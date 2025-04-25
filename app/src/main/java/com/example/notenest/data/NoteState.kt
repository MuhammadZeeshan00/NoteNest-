package com.example.notenest.data

sealed class NoteState {
    object Loading : NoteState()
    data class Success(val notes: List<Note>) : NoteState()
    data class Error(val message: String) : NoteState()
}