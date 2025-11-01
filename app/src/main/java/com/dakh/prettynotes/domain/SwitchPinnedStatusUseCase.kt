package com.dakh.prettynotes.domain

class SwitchPinnedStatusUseCase(
    private val repository: NotesRepository
) {

    operator fun invoke(id: Int) {
        repository.switchPinnedStatus(id)
    }
}