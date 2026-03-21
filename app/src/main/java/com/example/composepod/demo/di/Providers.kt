package com.example.composepod.demo.di

import android.content.Context
import androidx.room.Room
import com.brine.composepod.core.provider
import com.brine.composepod.mvi.StateNotifierProvider
import com.brine.composepod.mvi.stateNotifierProvider
import com.example.composepod.demo.data.local.NotesDatabase
import com.example.composepod.demo.data.repository.NoteRepositoryImpl
import com.example.composepod.demo.domain.repository.NoteRepository
import com.example.composepod.demo.viewmodel.NotesState
import com.example.composepod.demo.viewmodel.NotesViewModel

// Simulated Context Provider (In a real app, you inject the Application Context into the container)
val applicationContextProvider = provider<Context>("applicationContext") {
    throw IllegalStateException("Context not provided in ProviderScope overrides")
}

val databaseProvider = provider("database") { ref ->
    val context = ref.read(applicationContextProvider)
    Room.databaseBuilder(
        context,
        NotesDatabase::class.java,
        "notes_db"
    ).build()
}

val noteRepositoryProvider = provider<NoteRepository>("noteRepository") { ref ->
    val database = ref.read(databaseProvider)
    NoteRepositoryImpl(database.noteDao())
}

val notesProvider: StateNotifierProvider<NotesViewModel, NotesState> =
    stateNotifierProvider { ref ->
        val repository = ref.read(noteRepositoryProvider)
        NotesViewModel(repository)
    }
