package com.example.composepod.demo.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.example.composepod.demo.di.databaseProvider
import com.example.composepod.demo.ui.theme.VibrantBackground
import com.example.composepod.demo.ui.theme.glassEffect
import com.example.composepod.demo.models.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NoteSelectionActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Find the widget id from the intent. 
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        setContent {
            val context = applicationContext
            val database = androidx.room.Room.databaseBuilder(
                context,
                com.example.composepod.demo.data.local.NotesDatabase::class.java,
                "notes_db"
            ).build()
            
            VibrantBackground {
                val notesEntities by database.noteDao().getAllNotes().collectAsState(initial = emptyList())
                val notes = notesEntities.map { Note(it.id, it.title, it.content, it.timestamp) }
                
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = { Text("Select a Note to Pin", color = Color.White, fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        if (notes.isEmpty()) {
                            Text("No notes available. Please create one first.", modifier = Modifier.padding(16.dp), color = Color.White.copy(alpha=0.7f))
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(notes) { note ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .glassEffect(cornerRadius = 16f, blurRadius = 30f)
                                            .clickable {
                                                onNoteSelected(note)
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(note.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                            Text(note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 1, color = Color.White.copy(alpha = 0.7f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onNoteSelected(note: Note) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@NoteSelectionActivity).getGlanceIdBy(appWidgetId)
            
            updateAppWidgetState(this@NoteSelectionActivity, glanceId) { prefs ->
                prefs[NoteWidget.NOTE_ID_KEY] = note.id
                prefs[NoteWidget.NOTE_TITLE_KEY] = note.title
                prefs[NoteWidget.NOTE_CONTENT_KEY] = note.content
            }
            NoteWidget().update(this@NoteSelectionActivity, glanceId)
            
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}
