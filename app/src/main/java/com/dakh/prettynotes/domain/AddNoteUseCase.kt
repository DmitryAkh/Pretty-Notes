package com.dakh.prettynotes.domain

class AddNoteUseCase(
    private val repository: NotesRepository
) {

    operator fun invoke(
        title: String,
        content: String
    ) {
        repository.addNote(title, content)
    }
}