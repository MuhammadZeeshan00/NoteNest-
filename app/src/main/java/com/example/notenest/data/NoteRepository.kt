package com.example.notenest.data

import android.util.Log
import com.example.notenest.firebase.FirebaseDatabaseManager
import com.example.notenest.network.QuoteApiService
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val quoteApiService : QuoteApiService,
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseDatabaseManager: FirebaseDatabaseManager
) {
    // Create Firebase reference
    private val notesRef = firebaseDatabase.getReference("notes")
//    val notes: Flow<List<Note>> = noteDao.getAllNotes()

    // Combined Flow of local (Room) and remote (Firebase) notes
    val notes: Flow<List<Note>> = combine(
        noteDao.getAllNotes(),
        firebaseNotesFlow()
    ) { localNotes, firebaseNotes ->
        // Merge strategy: Firebase notes take precedence over local ones
        (localNotes + firebaseNotes)
            .distinctBy { it.id }
            .sortedByDescending { it.timestamp }
    }

    suspend fun insertNote(note: Note){
        noteDao.insert(note)
    }

    suspend fun updateNote(note: Note){
        noteDao.update(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.delete(note)
        firebaseDatabaseManager.deleteNote(note.id)
    }

    suspend fun getNoteById(id: String): Note? {
        // First check local database
        return noteDao.getNoteById(id) ?: run {
            // If not found locally, try Firebase
            try {
                val snapshot = notesRef.child(id).get().await()
                snapshot.getValue(Note::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getQuotes() : List<Quote>{
        return quoteApiService.getQuotes()
    }

    // Private helper functions
    private suspend fun syncNoteToFirebase(note: Note) {
        try {
            notesRef.child(note.id).setValue(note).await()
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to sync note to Firebase", e)
            // You might want to implement a retry mechanism here
        }
    }

    private fun firebaseNotesFlow(): Flow<List<Note>> = callbackFlow {
        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = snapshot.children.mapNotNull { it.getValue(Note::class.java) }
                trySend(notes)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        notesRef.addValueEventListener(valueListener)

        awaitClose {
            notesRef.removeEventListener(valueListener)
        }
    }
}