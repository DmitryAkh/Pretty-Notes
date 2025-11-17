package com.dakh.prettynotes.presentation.screens.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakh.prettynotes.domain.AddNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase
) : ViewModel() {


    private val _state = MutableStateFlow<CreateNoteState>(CreateNoteState.Creation())
    val state = _state.asStateFlow()

    fun processCommand(command: CreateNoteCommand) {
        when (command) {
            CreateNoteCommand.Back -> {
                _state.update {
                    CreateNoteState.Finished
                }
            }

            is CreateNoteCommand.InputContent -> {
                _state.update { state ->
                    if (state is CreateNoteState.Creation) {
                        state.copy(
                            content = command.content,
                            isSaveEnabled = state.title.isNotBlank() && command.content.isNotBlank()
                        )
                    } else {
                        CreateNoteState.Creation(content = command.content)
                    }
                }
            }

                is CreateNoteCommand.InputTitle -> {
                    _state.update { state ->
                        if (state is CreateNoteState.Creation) {
                            state.copy(
                                title = command.title,
                                isSaveEnabled = command.title.isNotBlank() && state.content.isNotBlank()
                            )
                        } else {
                            CreateNoteState.Creation(title = command.title)
                        }
                    }
                }

                CreateNoteCommand.SaveNote -> {

                    viewModelScope.launch {
                        _state.update { state ->
                            if (state is CreateNoteState.Creation) {
                                val title = state.title
                                val content = state.content
                                addNoteUseCase(title = title, content = content)
                                CreateNoteState.Finished
                            } else {
                                state
                            }
                        }
                    }
                }
            }

        }
    }

sealed interface CreateNoteCommand {
    data class InputTitle(val title: String) : CreateNoteCommand
    data class InputContent(val content: String) : CreateNoteCommand
    data object SaveNote : CreateNoteCommand
    data object Back : CreateNoteCommand
}

sealed interface CreateNoteState {

    data class Creation(
        val title: String = "",
        val content: String = "",
        val isSaveEnabled: Boolean = false,
    ) : CreateNoteState

    data object Finished : CreateNoteState
}