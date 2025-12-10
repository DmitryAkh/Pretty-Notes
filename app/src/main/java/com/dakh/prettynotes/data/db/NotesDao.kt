package com.dakh.prettynotes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dakh.prettynotes.data.models.ContentItemDbModel
import com.dakh.prettynotes.data.models.NoteDBModel
import com.dakh.prettynotes.data.models.NoteWithContentDbModel
import com.dakh.prettynotes.domain.entity.ContentItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Transaction
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteWithContentDbModel>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id == :id")
    suspend fun getNote(id: Int): NoteWithContentDbModel

    @Transaction
    @Query("""
        SELECT DISTINCT notes.* FROM notes JOIN content 
        ON notes.id == content.noteId
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%' 
        ORDER BY updatedAt DESC
        """)
    fun searchNotes(query: String): Flow<List<NoteWithContentDbModel>>

    @Transaction
    @Query("DELETE FROM notes WHERE id == :id")
    suspend fun deleteNote(id: Int)

    @Query("UPDATE notes SET isPinned = NOT isPinned WHERE id == :id")
    suspend fun switchPinnedStatus(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrEditNote(noteDBModel: NoteDBModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrEditNoteContent(content: List<ContentItemDbModel>)

    @Query("DELETE FROM content WHERE noteId == :id")
    suspend fun deleteNoteContent(id: Int)

    @Transaction
    suspend fun addNoteWithContent(
        noteDbModel: NoteDBModel,
        content: List<ContentItem>
    ) {
        val noteId = addOrEditNote(noteDbModel).toInt()
        val contentItems = content.toContentItemDbModels(noteId)
        addOrEditNoteContent(contentItems)
    }

    @Transaction
    suspend fun updateNote(
        noteDbModel: NoteDBModel,
        content: List<ContentItemDbModel>
    ) {
        addOrEditNote(noteDbModel)
        deleteNoteContent(noteDbModel.id)
        addOrEditNoteContent(content)
    }
}
