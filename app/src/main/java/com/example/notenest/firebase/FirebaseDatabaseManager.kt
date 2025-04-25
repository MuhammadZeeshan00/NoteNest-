package com.example.notenest.firebase

import com.example.notenest.data.Note
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class FirebaseDatabaseManager @Inject constructor(private val authManager: FirebaseAuthManager) {
    private val database = FirebaseDatabase.getInstance().reference
    private val notesRef
        get() = authManager.getCurrentUser()?.uid?.let {
            database.child("notes").child(it)
        }

    fun syncNotesToFirebase(note: Note) {
        val userNotesRef = notesRef ?: return
        userNotesRef.child(note.id).setValue(note)
    }

    fun getNotesFlow(): Flow<List<Note>> = callbackFlow {
        val userNotesRef = notesRef ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = snapshot.children.mapNotNull { it.getValue(Note::class.java) }
                trySend(notes)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        userNotesRef.addValueEventListener(listener)

        awaitClose {
            userNotesRef.removeEventListener(listener)
        }
    }

    fun deleteNote(noteId: String) {
        val userNotesRef = notesRef ?: return
        userNotesRef.child(noteId).removeValue()
    }
}