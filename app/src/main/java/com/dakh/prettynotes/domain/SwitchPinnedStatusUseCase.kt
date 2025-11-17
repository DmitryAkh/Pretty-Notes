package com.dakh.prettynotes.domain

import javax.inject.Inject

class SwitchPinnedStatusUseCase @Inject constructor(
    private val repository: NotesRepository
) {

    suspend operator fun invoke(id: Int) {
        repository.switchPinnedStatus(id)
    }
}