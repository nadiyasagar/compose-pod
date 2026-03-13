package com.example.composepod.demo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.brine.composepod.compose.rememberProvider
import com.brine.composepod.compose.watchProvider
import com.example.composepod.demo.di.notesProvider
import com.example.composepod.demo.ui.theme.glassEffect
import com.example.composepod.demo.viewmodel.NotesIntent

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

    // Handle system back button
    BackHandler {
        viewModel.processIntent(NotesIntent.NavigateToList)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (editingNote != null) "Edit Note" else "Add New Note", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.processIntent(NotesIntent.NavigateToList) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .glassEffect(cornerRadius = 24f, blurRadius = 30f)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFF37370), 
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color(0xFFF37370),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFF37370), 
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color(0xFFF37370),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            androidx.compose.material3.Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        if (editingNote != null) {
                            viewModel.processIntent(NotesIntent.UpdateNote(editingNote.id, title, content))
                        } else {
                            viewModel.processIntent(NotesIntent.AddNote(title, content))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF37370),
                    contentColor = Color.White
                )
            ) {
                Text("Save Note", fontWeight = FontWeight.Bold)
            }
        }
    }
}
