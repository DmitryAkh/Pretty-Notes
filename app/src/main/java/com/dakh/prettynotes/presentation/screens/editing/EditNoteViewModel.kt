package com.dakh.prettynotes.presentation.screens.editing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakh.prettynotes.domain.DeleteNoteUseCase
import com.dakh.prettynotes.domain.EditNoteUseCase
import com.dakh.prettynotes.domain.GetNoteUseCase
import com.dakh.prettynotes.domain.Note
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditNoteViewModel.Factory::class)
class EditNoteViewModel @AssistedInject constructor(
    @Assisted("id") private val id: Int,
    private val editNoteUseCase: EditNoteUseCase,
    private val getNoteUseCase: GetNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {


    private val _state = MutableStateFlow<EditNoteState>(EditNoteState.Initial)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val note =  getNoteUseCase(id)
            _state.update { state ->
                EditNoteState.Editing(note)
            }
        }
    }

    fun processCommand(command: EditNoteCommand) {
        when (command) {
            EditNoteCommand.Back -> {
                _state.update {
                    EditNoteState.Finished
                }
            }

            is EditNoteCommand.InputContent -> {
                _state.update { state ->
                    if (state is EditNoteState.Editing) {
                        val newNote = state.note.copy(content = command.content)
                        state.copy(note = newNote)
                    } else {
                        state
                    }
                }
            }

            is EditNoteCommand.InputTitle -> {
                _state.update { state ->
                    if (state is EditNoteState.Editing) {
                        val newNote = state.note.copy(title = command.title)
                        state.copy(note = newNote)
                    } else {
                        state
                    }
                }
            }

            EditNoteCommand.SaveNote -> {
                viewModelScope.launch {
                    _state.update { state ->
                        if (state is EditNoteState.Editing) {
                            val note = state.note
                            editNoteUseCase(note)
                            EditNoteState.Finished
                        } else {
                            state
                        }
                    }
                }
            }

            EditNoteCommand.Delete -> {
                viewModelScope.launch {
                    _state.update { state ->
                        if (state is EditNoteState.Editing) {
                            deleteNoteUseCase(state.note.id)
                            EditNoteState.Finished
                        } else {
                            state
                        }
                    }
                }
            }
        }
    }
    @AssistedFactory
    interface Factory {
        fun create(
           @Assisted("id") id: Int
        ): EditNoteViewModel
    }
}

sealed interface EditNoteCommand {
    data class InputTitle(val title: String) : EditNoteCommand
    data class InputContent(val content: String) : EditNoteCommand
    data object SaveNote : EditNoteCommand
    data object Back : EditNoteCommand
    data object Delete : EditNoteCommand
}

sealed interface EditNoteState {

    data object Initial : EditNoteState

    data class Editing(
        val note: Note,
    ) : EditNoteState {
        val isSaveEnabled: Boolean
            get() = note.title.isNotBlank() && note.content.isNotBlank()
    }

    data object Finished : EditNoteState
}