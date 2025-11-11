package com.dakh.prettynotes.presentation.screens.note


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakh.prettynotes.data.TestNotesRepositoryImpl
import com.dakh.prettynotes.domain.GetAllNotesUseCase
import com.dakh.prettynotes.domain.Note
import com.dakh.prettynotes.domain.SearchNotesUseCase
import com.dakh.prettynotes.domain.SwitchPinnedStatusUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel : ViewModel() {

    private val repository = TestNotesRepositoryImpl

    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val switchPinnedStatusUseCase = SwitchPinnedStatusUseCase(repository)
    private val searchNotesUseCase = SearchNotesUseCase(repository)

    private val query = MutableStateFlow("")

    private val _state = MutableStateFlow(NotesScreenState())
    val state = _state.asStateFlow()


    init {
        query
            .onEach { input ->
                _state.update {
                    it.copy(query = input)
                }
            }
            .flatMapLatest { input ->
                if (input.isBlank()) {
                    getAllNotesUseCase()
                } else {
                    searchNotesUseCase(input)
                }
            }
            .onEach { allNotes ->
                val pinnedNotes = allNotes.filter { it.isPinned }
                val otherNotes = allNotes.filter { !it.isPinned }
                _state.update {
                    it.copy(pinnedNotes = pinnedNotes, otherNotes = otherNotes)
                }
            }
            .launchIn(viewModelScope)
    }

    fun processCommand(command: NotesCommand) {
        viewModelScope.launch {
            when (command) {

                is NotesCommand.InputSearchQuery -> {
                    query.update { command.query.trim() }
                }

                is NotesCommand.SwitchPinnedStatus -> {
                    switchPinnedStatusUseCase(command.id)
                }
            }
        }

    }

}

sealed interface NotesCommand {
    data class InputSearchQuery(val query: String) : NotesCommand
    data class SwitchPinnedStatus(val id: Int) : NotesCommand
}

data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note> = listOf(),
    val otherNotes: List<Note> = listOf(),
)