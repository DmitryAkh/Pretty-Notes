package com.dakh.prettynotes.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dakh.prettynotes.data.models.ContentItemDbModel
import com.dakh.prettynotes.data.models.NoteDBModel

@Database(
    entities = [NoteDBModel::class, ContentItemDbModel::class],
    version = 3,
    exportSchema = false
)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao

}