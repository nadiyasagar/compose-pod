package com.example.composepod.demo.viewmodel

import androidx.lifecycle.viewModelScope
import com.brine.composepod.mvi.MVIViewModel
import com.brine.composepod.mvi.StateNotifierProvider
import com.brine.composepod.mvi.UiIntent
import com.brine.composepod.mvi.UiState
import com.brine.composepod.mvi.stateNotifierProvider
import com.example.composepod.demo.domain.repository.NoteRepository
import com.example.composepod.demo.models.Note
import kotlinx.coroutines.launch
import java.util.UUID

enum class ScreenRoute {
    List, Add, Advanced
}

data class NotesState(
    val notes: List<Note> = emptyList(),
    val currentRoute: ScreenRoute = ScreenRoute.List,
    val editingNoteId: String? = null
) : UiState

sealed class NotesIntent : UiIntent {
    data class AddNote(val title: String, val content: String) : NotesIntent()
    data class UpdateNote(val id: String, val title: String, val content: String) : NotesIntent()
    data class DeleteNote(val id: String) : NotesIntent()
    object NavigateToAdd : NotesIntent()
    data class NavigateToEdit(val id: String) : NotesIntent()
    object NavigateToList : NotesIntent()
    object NavigateToAdvanced : NotesIntent()
}

class NotesViewModel(
    private val repository: NoteRepository
) : MVIViewModel<NotesState, NotesIntent>(
    initialState = NotesState()
) {

    init {
        viewModelScope.launch {
            repository.getAllNotes().collect { notes ->
                state = state.copy(notes = notes)
            }
        }
    }

    override fun processIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.AddNote -> {
                viewModelScope.launch {
                    val newNote = Note(
                        id = UUID.randomUUID().toString(),
                        title = intent.title,
                        content = intent.content
                    )
                    repository.insertNote(newNote)
                    state = state.copy(currentRoute = ScreenRoute.List, editingNoteId = null)
                }
            }
            is NotesIntent.UpdateNote -> {
                viewModelScope.launch {
                    val note = repository.getNoteById(intent.id)
                    if (note != null) {
                        repository.updateNote(note.copy(title = intent.title, content = intent.content))
                    }
                    state = state.copy(currentRoute = ScreenRoute.List, editingNoteId = null)
                }
            }
            is NotesIntent.DeleteNote -> {
                viewModelScope.launch {
                    repository.deleteNote(intent.id)
                }
            }
            NotesIntent.NavigateToAdd -> {
                state = state.copy(currentRoute = ScreenRoute.Add, editingNoteId = null)
            }
            is NotesIntent.NavigateToEdit -> {
                state = state.copy(currentRoute = ScreenRoute.Add, editingNoteId = intent.id)
            }
            NotesIntent.NavigateToList -> {
                state = state.copy(currentRoute = ScreenRoute.List, editingNoteId = null)
            }
            NotesIntent.NavigateToAdvanced -> {
                state = state.copy(currentRoute = ScreenRoute.Advanced, editingNoteId = null)
            }
        }
    }
}
