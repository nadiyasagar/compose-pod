package com.example.composepod.demo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.brine.composepod.compose.rememberProvider
import com.brine.composepod.compose.watchProvider
import com.example.composepod.demo.di.notesProvider
import com.example.composepod.demo.ui.theme.VibrantBackground
import com.example.composepod.demo.viewmodel.NotesIntent
import com.example.composepod.demo.viewmodel.ScreenRoute

@Composable
fun NotesAppScreen() {
    val state by watchProvider(notesProvider)
    val viewModel = rememberProvider(notesProvider)
    
    VibrantBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            when (state.currentRoute) {
                ScreenRoute.List -> NotesListScreen()
                ScreenRoute.Add -> AddNoteScreen()
                ScreenRoute.Advanced -> AdvancedDemoScreen(
                    onBack = { 
                        viewModel.processIntent(NotesIntent.NavigateToList)
                    }
                )
            }
        }
    }
}
