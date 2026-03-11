package com.example.composepod.demo.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.composepod.R

class NoteWidget : GlanceAppWidget() {

    companion object {
        val NOTE_ID_KEY = stringPreferencesKey("note_id_key")
        val NOTE_TITLE_KEY = stringPreferencesKey("note_title_key")
        val NOTE_CONTENT_KEY = stringPreferencesKey("note_content_key")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        
        provideContent {
            val prefs = currentState<Preferences>()
            val title = prefs[NOTE_TITLE_KEY] ?: "My Note"
            val content = prefs[NOTE_CONTENT_KEY] ?: "Select a note to pin here from the App widget settings."

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ImageProvider(R.drawable.widget_ultra_glass))
//                        .background(Color(0x1AFFFFFF)) // Semi-transparent glass for widget
                        .padding(16.dp)
                        .clickable(actionStartActivity(
                            ComponentName(context, com.example.composepod.MainActivity::class.java)
                        )),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize()
                    ) {
                        Text(
                            text = title,
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Text(
                            text = content,
                            style = TextStyle(
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                }
            }
        }
    }
}
