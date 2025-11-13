package com.dakh.prettynotes.data

import com.dakh.prettynotes.domain.Note

fun Note.toDBModel(): NoteDBModel {
    return NoteDBModel(
        id = this.id,
        title = this.title,
        content = this.content,
        updatedAt = this.updatedAt,
        isPinned = this.isPinned
    )
}

fun NoteDBModel.toNote(): Note {
    return Note(
        id = this.id,
        title = this.title,
        content = this.content,
        updatedAt = this.updatedAt,
        isPinned = this.isPinned
    )
}

fun List<NoteDBModel>.toListNote(): List<Note> {
   return this.map {
        it.toNote()
    }
}