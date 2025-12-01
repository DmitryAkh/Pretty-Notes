package com.dakh.prettynotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val updatedAt: Long,
    val isPinned: Boolean
)