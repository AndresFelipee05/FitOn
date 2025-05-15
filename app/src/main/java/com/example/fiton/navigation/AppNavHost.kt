package com.example.fiton.navigation

import MainScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fiton.data.ExerciseRepository
import com.example.fiton.screen.CreateExerciseScreen
import com.example.fiton.screen.EditExerciseScreen
import com.example.fiton.screen.ExercisesScreen
import com.example.fiton.screen.SearchScreen

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
        composable("search") {
            val context = LocalContext.current
            val repository = remember { ExerciseRepository(context) }
            SearchScreen(navController = navController, repository = repository)
        }
        composable("exercises/{muscleGroup}") { backStackEntry ->
            // Obtener el grupo muscular desde los argumentos de la URL
            val muscleGroup = backStackEntry.arguments?.getString("muscleGroup") ?: ""
            ExercisesScreen(
                muscleGroup = muscleGroup,
                navController = navController
            ) // Pasa el grupo muscular a la pantalla de ejercicios
        }
        composable("edit_exercise/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")?.toLongOrNull()
            if (exerciseId != null) {
                EditExerciseScreen(exerciseId = exerciseId, navController = navController)
            } else {
                // Mostrar diálogo de error
                var showDialog by remember { mutableStateOf(true) }
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = false
                            navController.popBackStack()
                        },
                        title = { Text("Error") },
                        text = { Text("ID de ejercicio inválido") },
                        confirmButton = {
                            Button(onClick = {
                                showDialog = false
                                navController.popBackStack()
                            }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }
            }
        }
        composable("create_exercise_screen/{muscleGroup}") { backStackEntry ->
            val group = backStackEntry.arguments?.getString("muscleGroup") ?: ""
            CreateExerciseScreen(navController = navController, defaultMuscleGroup = group)
        }
    }
}
