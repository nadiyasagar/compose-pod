package com.example.composepod.demo.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.brine.composepod.compose.watchProvider
import com.brine.composepod.core.select
import com.example.composepod.demo.viewmodel.ScreenRoute
import com.example.composepod.demo.viewmodel.notesProvider

@Composable
fun NotesAppScreen() {
    val currentRoute by watchProvider(notesProvider.select { it.currentRoute })

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(targetState = currentRoute, label = "NotesApp Router") { route ->
            when (route) {
                ScreenRoute.List -> NotesListScreen()
                ScreenRoute.Add -> AddNoteScreen()
            }
        }
    }
}
