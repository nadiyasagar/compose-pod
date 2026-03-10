package com.example.composepod.demo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brine.composepod.compose.rememberProvider
import com.brine.composepod.compose.watchProvider
import com.example.composepod.demo.viewmodel.NotesIntent
import com.example.composepod.demo.viewmodel.notesProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen() {
    val state by watchProvider(notesProvider)
    val viewModel = rememberProvider(notesProvider)
    
    val editingNote = remember(state.editingNoteId) {
        state.notes.find { it.id == state.editingNoteId }
    }
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    // Pre-fill if editing
    LaunchedEffect(editingNote) {
        if (editingNote != null) {
            title = editingNote.title
            content = editingNote.content
        } else {
            title = ""
            content = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingNote != null) "Edit Note" else "Add New Note") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(NotesIntent.NavigateToList) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        if (editingNote != null) {
                            viewModel.processIntent(NotesIntent.UpdateNote(editingNote.id, title, content))
                        } else {
                            viewModel.processIntent(NotesIntent.AddNote(title, content))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Note")
            }
        }
    }
}
