package com.dakh.prettynotes.data

import com.dakh.prettynotes.data.db.NotesDao
import com.dakh.prettynotes.data.db.toContentItemDbModels
import com.dakh.prettynotes.data.db.toDBModel
import com.dakh.prettynotes.data.db.toListNote
import com.dakh.prettynotes.data.db.toNote
import com.dakh.prettynotes.data.models.NoteDBModel
import com.dakh.prettynotes.domain.NotesRepository
import com.dakh.prettynotes.domain.entity.ContentItem
import com.dakh.prettynotes.domain.entity.Note
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
        val processedContent = content.processForStorage()
        val noteDbModel = NoteDBModel(
            id = 0,
            title = title,
            isPinned = isPinned,
            updatedAt = updatedAt
        )
        notesDao.addNoteWithContent(noteDbModel, processedContent)
    }

    override suspend fun deleteNote(id: Int) {
        val note = notesDao.getNote(id).toNote()
        notesDao.deleteNote(id)

        note.content
            .filterIsInstance<ContentItem.Image>()
            .map { it.url }
            .forEach {
                imageFileManager.deleteImageFromInternalStorage(it)
            }
    }

    override suspend fun editNote(note: Note) {
        val oldNote = notesDao.getNote(note.id).toNote()

        val oldUrls =
            oldNote.content.filterIsInstance<ContentItem.Image>().map { it.url }
        val newUrls =
            note.content.filterIsInstance<ContentItem.Image>().map { it.url }
        val removedUrls = oldUrls - newUrls.toSet()

        removedUrls.forEach {
            imageFileManager.deleteImageFromInternalStorage(it)
        }

        val processedContent = note.content.processForStorage()
        val processedNote = note.copy(content = processedContent)

        notesDao.updateNote(
            noteDbModel = processedNote.toDBModel(),
            content = processedContent.toContentItemDbModels(note.id)
        )
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