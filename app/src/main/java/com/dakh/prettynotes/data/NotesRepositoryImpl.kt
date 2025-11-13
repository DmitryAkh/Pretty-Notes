package com.dakh.prettynotes.data

import android.content.Context
import com.dakh.prettynotes.domain.Note
import com.dakh.prettynotes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotesRepositoryImpl private constructor(context: Context) : NotesRepository {

    private val notesDatabase = NotesDatabase.getInstance(context)
    private val notesDao = notesDatabase.notesDao()

    override suspend fun addNote(
        title: String,
        content: String,
        isPinned: Boolean,
        updatedAt: Long,
    ) {
        val noteDBModel = NoteDBModel(
            id = 0,
            title = title,
            content = content,
            isPinned = isPinned,
            updatedAt = updatedAt
        )
        notesDao.addOrEditNote(noteDBModel)
    }

    override suspend fun deleteNote(id: Int) {
        notesDao.deleteNote(id)
    }

    override suspend fun editNote(note: Note) {
        notesDao.addOrEditNote(note.toDBModel())
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return notesDao.getAllNotes().map {
            it.toListNote()
        }
    }

    override suspend fun getNote(id: Int): Note {
        return notesDao.getNote(id).toNote()
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return notesDao.searchNotes(query).map {
            it.toListNote()
        }
    }

    override suspend fun switchPinnedStatus(id: Int) {
        notesDao.switchPinnedStatus(id)
    }

    companion object {

        private var instance: NotesRepositoryImpl? = null

        private val LOCK = Any()

        fun getInstance(context: Context): NotesRepositoryImpl {

            instance?.let {return it}

            synchronized(LOCK) {
                instance?.let {return it}
                return NotesRepositoryImpl(context).also { instance = it }
            }
        }
    }
}