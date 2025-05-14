package com.example.fiton.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fiton.R
import com.example.fiton.data.Exercise
import com.example.fiton.data.ExerciseRepository

@Composable
fun ExercisesScreen(
    muscleGroup: String,
    navController: NavController
) {
    val context = LocalContext.current
    val repository = remember { ExerciseRepository(context) }

    var exercises by remember { mutableStateOf(emptyList<Exercise>()) }

    LaunchedEffect(Unit) {
        exercises = repository.getAll()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Título
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ejercicios para $muscleGroup",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista scrollable
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp), // deja espacio para los botones flotantes
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredExercises = exercises.filter { it.muscleGroup == muscleGroup }

                if (filteredExercises.isEmpty()) {
                    item {
                        Text("No hay ejercicios registrados para este grupo muscular.")
                    }
                } else {
                    items(filteredExercises.size) { index ->
                        val exercise = filteredExercises[index]
                        ExerciseCard(exercise = exercise) { exerciseId ->
                            navController.navigate("edit_exercise/$exerciseId")
                        }
                    }
                }
            }
        }

        // Botón Volver
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            containerColor = Color(0xFFFF9800),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        // Botón Agregar
        FloatingActionButton(
            onClick = {
                repository.insert(
                    Exercise(
                        name = "Plancha",
                        description = "Mantener posición firme con el abdomen contraído",
                        muscleGroup = muscleGroup
                    )
                )
                exercises = repository.getAll()
            },
            containerColor = Color(0xFFFF9800),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar ejercicio",
                tint = Color.White
            )
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onClick: (Long) -> Unit) {
    val painter: Painter = if (!exercise.imageUri.isNullOrEmpty()) {
        rememberAsyncImagePainter(exercise.imageUri)
    } else {
        painterResource(id = R.drawable.ic_launcher_foreground)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(exercise.id) }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painter,
                contentDescription = "Imagen del ejercicio",
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = exercise.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

