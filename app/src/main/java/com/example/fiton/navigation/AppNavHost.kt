package com.example.fiton.navigation

import MainScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fiton.screen.EditExerciseScreen
import com.example.fiton.screen.ExercisesScreen

@Composable
fun AppNavHost(modifier: Modifier) {
    val navController = rememberNavController()

    // El NavHost gestiona las rutas de la aplicación
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController = navController) // Pasa el NavController a MainScreen
        }
        composable("exercises/{muscleGroup}") { backStackEntry ->
            // Obtener el grupo muscular desde los argumentos de la URL
            val muscleGroup = backStackEntry.arguments?.getString("muscleGroup") ?: ""
            ExercisesScreen(muscleGroup = muscleGroup, navController = navController) // Pasa el grupo muscular a la pantalla de ejercicios
        }
        composable("edit_exercise/{exerciseId}") { backStackEntry ->
            // Convertir el argumento de exerciseId a Long
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")?.toLongOrNull()
            if (exerciseId != null) {
                EditExerciseScreen(exerciseId = exerciseId, navController = navController)
            } else {
                // Maneja el caso en el que el exerciseId no se pueda convertir a Long
                // Por ejemplo, podrías navegar a otra pantalla o mostrar un error
            }
        }
    }
}
