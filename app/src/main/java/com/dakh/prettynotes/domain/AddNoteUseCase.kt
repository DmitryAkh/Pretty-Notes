package com.dakh.prettynotes.domain

class AddNoteUseCase(
    private val repository: NotesRepository
) {

    fun addNote(note: Note) {
        repository.addNote(note)
    }
}