package com.example.fiton.navigation

import MainScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fiton.data.ExerciseRepository
import com.example.fiton.data.RutinaRepository
import com.example.fiton.screen.CreateExerciseScreen
import com.example.fiton.screen.CreateRoutineDetailScreen
import com.example.fiton.screen.CreateRoutineScreen
import com.example.fiton.screen.EditExerciseScreen
import com.example.fiton.screen.EditRoutineScreen
import com.example.fiton.screen.ExercisesScreen
import com.example.fiton.screen.RoutinesScreen
import com.example.fiton.screen.SearchScreen
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(modifier: Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Crear los repositorios solo una vez y compartir
    val exerciseRepository = remember { ExerciseRepository(context) }
    val rutinaRepository = remember { RutinaRepository(context) }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(navController = navController)
        }
        composable("search") {
            SearchScreen(navController = navController, repository = exerciseRepository)
        }
        composable("create_routine_screen") {
            CreateRoutineScreen(
                navController = navController,
                exerciseRepository = exerciseRepository,
                rutinaRepository = rutinaRepository
            )
        }
        composable(
            route = "crear_rutina_detalle/{fecha}",
            arguments = listOf(navArgument("fecha") { type = NavType.StringType })
        ) { backStackEntry ->
            val fechaString = backStackEntry.arguments?.getString("fecha")!!
            val fecha = LocalDate.parse(fechaString)

            CreateRoutineDetailScreen(
                date = fecha,
                navController = navController,
                exerciseRepository = exerciseRepository,
                rutinaRepository = rutinaRepository
            )
        }
        composable("exercises/{muscleGroup}") { backStackEntry ->
            val muscleGroup = backStackEntry.arguments?.getString("muscleGroup") ?: ""
            ExercisesScreen(
                muscleGroup = muscleGroup,
                navController = navController
            )
        }
        composable("edit_exercise/{exerciseId}") { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")?.toLongOrNull()
            if (exerciseId != null) {
                EditExerciseScreen(exerciseId = exerciseId, navController = navController)
            } else {
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
        composable("routines_screen") {
            RoutinesScreen(navController = navController)
        }
        composable("edit_routine_screen/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            if (id != null) {
                EditRoutineScreen(navController, rutinaId = id)
            } else {
                var showDialog by remember { mutableStateOf(true) }
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = false
                            navController.popBackStack()
                        },
                        title = { Text("Error") },
                        text = { Text("ID de rutina inválido") },
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
    }
}
