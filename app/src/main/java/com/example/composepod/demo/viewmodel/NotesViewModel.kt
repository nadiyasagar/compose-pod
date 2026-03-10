package com.example.composepod.demo.viewmodel

import com.brine.composepod.mvi.MVIViewModel
import com.brine.composepod.mvi.StateNotifierProvider
import com.brine.composepod.mvi.UiIntent
import com.brine.composepod.mvi.UiState
import com.brine.composepod.mvi.stateNotifierProvider
import com.example.composepod.demo.models.Note
import java.util.UUID

enum class ScreenRoute {
    List, Add
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
}

class NotesViewModel : MVIViewModel<NotesState, NotesIntent>(
    initialState = NotesState()
) {
    override fun processIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.AddNote -> {
                val newNote = Note(
                    id = UUID.randomUUID().toString(),
                    title = intent.title,
                    content = intent.content
                )
                state = state.copy(
                    notes = state.notes + newNote,
                    currentRoute = ScreenRoute.List, // Auto-navigate back
                    editingNoteId = null
                )
            }
            is NotesIntent.UpdateNote -> {
                state = state.copy(
                    notes = state.notes.map { if (it.id == intent.id) it.copy(title = intent.title, content = intent.content) else it },
                    currentRoute = ScreenRoute.List,
                    editingNoteId = null
                )
            }
            is NotesIntent.DeleteNote -> {
                state = state.copy(
                    notes = state.notes.filter { it.id != intent.id }
                )
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
        }
    }
}

val notesProvider: StateNotifierProvider<NotesViewModel, NotesState> =
    stateNotifierProvider {
        NotesViewModel()
    }
