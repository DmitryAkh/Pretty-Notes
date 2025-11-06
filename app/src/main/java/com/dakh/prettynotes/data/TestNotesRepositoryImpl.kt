package com.dakh.prettynotes.data

import com.dakh.prettynotes.domain.Note
import com.dakh.prettynotes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

object TestNotesRepositoryImpl : NotesRepository {

    private val notesListFlow = MutableStateFlow<List<Note>>(mutableListOf())

    override fun addNote(
        title: String,
        content: String,
    ) {
        notesListFlow.update {oldList ->
            val note = Note(
                id = oldList.size,
                title = title,
                content = content,
                updatedAt = System.currentTimeMillis(),
                isPinned = false
            )
            oldList + note
        }
    }

    override fun deleteNote(id: Int) {
        notesListFlow.update { currentList ->
            currentList.filterNot { it.id == id }
        }
    }

    override fun editNote(note: Note) {
        notesListFlow.update { currentList ->
            currentList.map {
                if (it.id == note.id) {
                    note
                } else {
                    it
                }
            }
        }
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return notesListFlow.asStateFlow()
    }

    override fun getNote(id: Int): Note {
        return notesListFlow.value.first {
            it.id == id
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return notesListFlow.map { oldList ->
            oldList.filterNot {
                it.title.contains(query) || it.content.contains(query)
            }
        }
    }

    override fun switchPinnedStatus(id: Int) {
        notesListFlow.update { currentList ->
            currentList.map {
                if (it.id == id) {
                    it.copy(isPinned = !it.isPinned)
                } else {
                    it
                }
            }
        }
    }
}