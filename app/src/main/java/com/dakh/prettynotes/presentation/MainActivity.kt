package com.dakh.prettynotes.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dakh.prettynotes.presentation.screens.note.NotesScreen
import com.dakh.prettynotes.presentation.ui.theme.PrettyNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrettyNotesTheme {
                NotesScreen()
            }
        }
    }
}