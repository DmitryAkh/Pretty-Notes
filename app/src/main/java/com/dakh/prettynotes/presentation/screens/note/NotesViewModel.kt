package com.dakh.prettynotes.presentation.screens.note


import androidx.lifecycle.ViewModel
import com.dakh.prettynotes.data.TestNotesRepositoryImpl
import com.dakh.prettynotes.domain.AddNoteUseCase
import com.dakh.prettynotes.domain.DeleteNoteUseCase
import com.dakh.prettynotes.domain.EditNoteUseCase
import com.dakh.prettynotes.domain.GetAllNotesUseCase
import com.dakh.prettynotes.domain.GetNoteUseCase
import com.dakh.prettynotes.domain.Note
import com.dakh.prettynotes.domain.SearchNotesUseCase
import com.dakh.prettynotes.domain.SwitchPinnedStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel : ViewModel() {

    private val repository = TestNotesRepositoryImpl

    private val addNoteUseCase = AddNoteUseCase(repository)
    private val editNoteUseCase = EditNoteUseCase(repository)
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)
    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val getNoteUseCase = GetNoteUseCase(repository)
    private val switchPinnedStatusUseCase = SwitchPinnedStatusUseCase(repository)
    private val searchNotesUseCase = SearchNotesUseCase(repository)

    private val query = MutableStateFlow("")

    private val _state = MutableStateFlow(NotesScreenState())
    val state = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        addSomeNotes()
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
            .launchIn(scope)
    }

    // TODO: don`t forget remove it

    private fun addSomeNotes() {
        repeat(50_000) {
        addNoteUseCase(title = "Title №$it", content = "Content №$it")
        }
    }

    fun processCommand(command: NotesCommand) {
        when (command) {
            is NotesCommand.DeleteNote -> {
                deleteNoteUseCase(command.id)
            }

            is NotesCommand.EditNote -> {
                val note = getNoteUseCase(command.note.id)
                val title = command.note.title
                editNoteUseCase(note.copy(title = "$title edited"))
            }

            is NotesCommand.InputSearchQuery -> {
                query.update { command.query.trim() }
            }

            is NotesCommand.SwitchPinnedStatus -> {
                switchPinnedStatusUseCase(command.id)
            }
        }
    }

}

sealed interface NotesCommand {
    data class InputSearchQuery(val query: String) : NotesCommand
    data class SwitchPinnedStatus(val id: Int) : NotesCommand

    //


    data class DeleteNote(val id: Int) : NotesCommand
    data class EditNote(val note: Note) : NotesCommand
}

data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note> = listOf(),
    val otherNotes: List<Note> = listOf(),
)