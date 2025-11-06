package com.dakh.prettynotes.domain

import kotlinx.coroutines.flow.Flow

interface NotesRepository {

    fun addNote(
        title: String,
        content: String
    )

    fun deleteNote(id: Int)

    fun editNote(note: Note)

    fun getAllNotes(): Flow<List<Note>>

    fun getNote(id: Int): Note

    fun searchNotes(query: String): Flow<List<Note>>

    fun switchPinnedStatus(id: Int)
}