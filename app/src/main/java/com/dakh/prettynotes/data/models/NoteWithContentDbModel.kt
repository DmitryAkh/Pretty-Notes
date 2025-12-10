package com.dakh.prettynotes.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithContentDbModel(
    @Embedded
    val noteDBModel: NoteDBModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val content: List<ContentItemDbModel>
) {
}