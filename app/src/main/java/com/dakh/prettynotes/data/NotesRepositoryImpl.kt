package com.dakh.prettynotes.data

import com.dakh.prettynotes.domain.ContentItem
import com.dakh.prettynotes.domain.Note
import com.dakh.prettynotes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val notesDao: NotesDao,
    private val imageFileManager: ImageFileManager,
) : NotesRepository {


    override suspend fun addNote(
        title: String,
        content: List<ContentItem>,
        isPinned: Boolean,
        updatedAt: Long,
    ) {
        val note = Note(
            id = 0,
            title = title,
            content = content.processForStorage(),
            isPinned = isPinned,
            updatedAt = updatedAt
        )

        notesDao.addOrEditNote(note.toDBModel())
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

    private suspend fun List<ContentItem>.processForStorage(): List<ContentItem> {
        return map { contentItem ->
            when (contentItem) {
                is ContentItem.Image -> {
                    if (imageFileManager.isInternal(contentItem.url)) {
                        contentItem
                    } else {
                        val internalPath =
                            imageFileManager.copyImageToInternalStorage(contentItem.url)
                        ContentItem.Image(url = internalPath)
                    }
                }

                is ContentItem.Text -> {
                    contentItem
                }
            }
        }
    }
}