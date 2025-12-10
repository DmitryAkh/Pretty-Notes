package com.dakh.prettynotes.domain

import com.dakh.prettynotes.domain.entity.Note
import javax.inject.Inject

class EditNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {

    suspend operator fun invoke(note: Note) {
        repository.editNote(
            note.copy(
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}