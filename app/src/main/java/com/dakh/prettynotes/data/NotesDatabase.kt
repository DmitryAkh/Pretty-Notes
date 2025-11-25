package com.dakh.prettynotes.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteDBModel::class, ContentItemDbModel::class],
    version = 3,
    exportSchema = false
)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun notesDao(): NotesDao

}