package com.dakh.prettynotes.presentation.screens.editing

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dakh.prettynotes.domain.ContentItem
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
    private val deleteNoteUseCase: DeleteNoteUseCase,
) : ViewModel() {


    private val _state = MutableStateFlow<EditNoteState>(EditNoteState.Initial)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val note = getNoteUseCase(id)
            val content = if (note.content.lastOrNull() !is ContentItem.Text) {
                note.content + ContentItem.Text("")
            } else {
                note.content
            }
            _state.update { state ->
                EditNoteState.Editing(note.copy(content = content))
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
                        val newContent = state.note.content
                            .mapIndexed { index, contentItem ->
                                if (index == command.index && contentItem is ContentItem.Text) {
                                    contentItem.copy(content = command.content)
                                } else {
                                    contentItem
                                }
                            }
                        val newNote = state.note.copy(content = newContent)
                        state.copy(
                            note = newNote
                        )
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

            is EditNoteCommand.AddImage -> {
                _state.update { state ->
                    if (state is EditNoteState.Editing) {
                        val oldNote = state.note
                        state.note.content.toMutableList().apply {
                            val lastItem = last()
                            if (lastItem is ContentItem.Text && lastItem.content.isBlank()) {
                                removeAt(lastIndex)
                            }
                            add(ContentItem.Image(command.uri.toString()))
                            add(ContentItem.Text(""))

                        }.let {
                            val newNote = oldNote.copy(content = it)
                            state.copy(note = newNote)
                        }
                    } else {
                        state
                    }
                }
            }

            is EditNoteCommand.DeleteImage -> {
                _state.update { state ->
                    if (state is EditNoteState.Editing) {
                        val oldNote = state.note
                        state.note.content.toMutableList().apply {
                            removeAt(command.index)
                        }.let {
                            val newNote = oldNote.copy(content = it)
                            state.copy(note = newNote)
                        }
                    } else {
                        state
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("id") id: Int,
        ): EditNoteViewModel
    }
}

sealed interface EditNoteCommand {
    data class InputTitle(val title: String) : EditNoteCommand
    data class InputContent(val content: String, val index: Int) : EditNoteCommand
    data class AddImage(val uri: Uri) : EditNoteCommand
    data class DeleteImage(val index: Int) : EditNoteCommand
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
            get() {
                return when {
                    note.title.isBlank() -> false
                    note.content.isEmpty() -> false
                    else -> {
                        note.content.any {
                            it !is ContentItem.Text || it.content.isNotBlank()
                        }
                    }
                }
            }
    }

    data object Finished : EditNoteState
}