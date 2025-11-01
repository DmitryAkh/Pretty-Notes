package com.dakh.prettynotes.domain

import kotlinx.coroutines.flow.Flow

class SearchNotesUseCase {

    operator fun invoke(query: String): Flow<List<Note>> {
        TODO()
    }
}