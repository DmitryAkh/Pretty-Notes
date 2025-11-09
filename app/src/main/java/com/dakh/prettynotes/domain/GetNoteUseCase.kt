package com.dakh.prettynotes.domain

class GetNoteUseCase(
    private val repository: NotesRepository
) {

    operator fun invoke(id: Int): Note {
       return repository.getNote(id)
    }
}