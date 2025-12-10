package com.dakh.prettynotes.presentation.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dakh.prettynotes.presentation.screen.creation.CreateNoteScreen
import com.dakh.prettynotes.presentation.screen.editing.EditNoteScreen
import com.dakh.prettynotes.presentation.screen.note.NotesScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Notes.route
    ) {
        composable(Screen.Notes.route) {
            NotesScreen(
                onNoteClick = {
                    navController.navigate(Screen.EditNote.createRoute(it.id))
                },
                onAddNoteClick = {
                    navController.navigate(Screen.CreateNote.route)
                }
            )
        }
        composable(Screen.CreateNote.route) {
            CreateNoteScreen(
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.EditNote.route) {
           val id = Screen.EditNote.getId(it.arguments)
            EditNoteScreen(
                id = id,
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {

    data object Notes : Screen("notes")

    data object CreateNote : Screen("create_note")

    data object EditNote : Screen("edit_note/{id}") {

        fun createRoute(id: Int): String {
            return "edit_note/$id"
        }

        fun getId(arguments: Bundle?): Int {
            return arguments?.getString("id")?.toInt() ?: 0

        }

    }
}