package com.dakh.prettynotes.presentation.screens.creation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakh.prettynotes.domain.AddNoteUseCase
import com.dakh.prettynotes.domain.ContentItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase,
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
                        val newContent = state.content
                            .mapIndexed { index, contentItem ->
                                if (index == command.index && contentItem is ContentItem.Text) {
                                    contentItem.copy(content = command.content)
                                } else {
                                    contentItem
                                }
                            }
                        state.copy(
                            content = newContent
                        )
                    } else {
                        state
                    }
                }
            }

            is CreateNoteCommand.InputTitle -> {
                _state.update { state ->
                    if (state is CreateNoteState.Creation) {
                        state.copy(
                            title = command.title
                        )
                    } else {
                        state
                    }
                }
            }

            CreateNoteCommand.SaveNote -> {

                viewModelScope.launch {
                    _state.update { state ->
                        if (state is CreateNoteState.Creation) {
                            val title = state.title
                            val content = state.content.filter {
                                it !is ContentItem.Text || it.content.isNotBlank()
                            }
                            addNoteUseCase(title = title, content = content)
                            CreateNoteState.Finished
                        } else {
                            state
                        }
                    }
                }
            }

            is CreateNoteCommand.AddImage -> {
                _state.update { state ->
                    if (state is CreateNoteState.Creation) {
                        state.content.toMutableList().apply {
                            val lastItem = last()
                            if (lastItem is ContentItem.Text && lastItem.content.isBlank()) {
                                removeAt(lastIndex)
                            }
                            add(ContentItem.Image(command.uri.toString()))
                            add(ContentItem.Text(""))
                        }.let {
                            state.copy(content = it)
                        }
                    } else {
                        state
                    }
                }
            }

            is CreateNoteCommand.DeleteImage -> {
                _state.update { state ->
                    if (state is CreateNoteState.Creation) {
                        state.content.toMutableList().apply {
                            removeAt(command.index)
                        }.let {
                            state.copy(content = it)
                        }
                    } else {
                        state
                    }
                }
            }
        }

    }
}

sealed interface CreateNoteCommand {
    data class InputTitle(val title: String) : CreateNoteCommand
    data class InputContent(val content: String, val index: Int) : CreateNoteCommand
    data class AddImage(val uri: Uri) : CreateNoteCommand
    data class DeleteImage(val index: Int) : CreateNoteCommand
    data object SaveNote : CreateNoteCommand
    data object Back : CreateNoteCommand
}

sealed interface CreateNoteState {

    data class Creation(
        val title: String = "",
        val content: List<ContentItem> = listOf(ContentItem.Text("")),
    ) : CreateNoteState {
        val isSaveEnabled: Boolean
            get() {
                return when {
                    title.isBlank() -> false
                    content.isEmpty() -> false
                    else -> {
                        content.any {
                            it !is ContentItem.Text || it.content.isNotBlank()
                        }
                    }
                }
            }
    }

    data object Finished : CreateNoteState
}