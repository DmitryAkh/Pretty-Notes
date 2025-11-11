package com.dakh.prettynotes.presentation.screens.creation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakh.prettynotes.data.TestNotesRepositoryImpl
import com.dakh.prettynotes.domain.AddNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateNoteViewModel() : ViewModel() {
    private val repository = TestNotesRepositoryImpl
    private val addNoteUseCase = AddNoteUseCase(repository)

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
                _state.update { prevState ->
                    if (prevState is CreateNoteState.Creation) {
                        prevState.copy(
                            content = command.content,
                            isSaveEnabled = prevState.title.isNotBlank() && command.content.isNotBlank()
                        )
                    } else {
                        CreateNoteState.Creation(content = command.content)
                    }
                }
            }

                is CreateNoteCommand.InputTitle -> {
                    _state.update { prevState ->
                        if (prevState is CreateNoteState.Creation) {
                            prevState.copy(
                                title = command.title,
                                isSaveEnabled = command.title.isNotBlank() && prevState.content.isNotBlank()
                            )
                        } else {
                            CreateNoteState.Creation(title = command.title)
                        }
                    }
                }

                CreateNoteCommand.SaveNote -> {

                    viewModelScope.launch {
                        _state.update { prevState ->
                            if (prevState is CreateNoteState.Creation) {
                                val title = prevState.title
                                val content = prevState.content
                                addNoteUseCase(title = title, content = content)
                                CreateNoteState.Finished
                            } else {
                                prevState
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