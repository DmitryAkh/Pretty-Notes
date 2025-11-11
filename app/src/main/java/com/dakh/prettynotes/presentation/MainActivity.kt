package com.dakh.prettynotes.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dakh.prettynotes.presentation.screens.creation.CreateNoteScreen
import com.dakh.prettynotes.presentation.ui.theme.PrettyNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrettyNotesTheme {
//                NotesScreen(
//                    onNoteClick = {
//                        Log.d("MainActivity", "onNoteClick: $it")
//                    },
//                    onAddNoteClick = {
//                        Log.d("MainActivity", "onAddNoteClick")
//                    }
//
                CreateNoteScreen(
                    onFinished = {
                        Log.d("CreateNoteScreen", " Finished")
                    }
                )
            }
        }
    }
}