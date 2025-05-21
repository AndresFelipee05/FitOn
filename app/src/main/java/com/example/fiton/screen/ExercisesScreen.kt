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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fiton.R
import com.example.fiton.data.Exercise
import com.example.fiton.data.ExerciseRepository
import java.io.File

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

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón Volver
                FloatingActionButton(
                    onClick = { navController.popBackStack() },
                    containerColor = Color(0xFFFF9800),
                    modifier = Modifier.width(80.dp)
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
                        navController.navigate("create_exercise_screen/${muscleGroup}")
                    },
                    containerColor = Color(0xFFFF9800),
                    modifier = Modifier.width(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar ejercicio",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Título centrado
            Text(
                text = "Ejercicios para $muscleGroup",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            // Lista scrollable
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp), // espacio reducido ya que los FAB están en Scaffold
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredExercises = exercises.filter { it.muscleGroup == muscleGroup }

                if (filteredExercises.isEmpty()) {
                    item {
                        Text(
                            "No hay ejercicios registrados para este grupo muscular.",
                            modifier = Modifier.padding(16.dp)
                        )
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
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onClick: (Long) -> Unit) {
    val context = LocalContext.current

    // Verificar que la URI no sea nula y que el archivo exista
    val imageExists = remember(exercise.imageUri) {
        !exercise.imageUri.isNullOrEmpty() && File(exercise.imageUri).exists()
    }

    // Usar un pintor que maneje correctamente la carga de imágenes locales
    val painter: Painter = if (imageExists) {
        // Usar File directamente para imágenes en almacenamiento interno
        val imageFile = File(exercise.imageUri!!)
        // Log para depuración
        println("Cargando imagen desde: ${imageFile.absolutePath} (Existe: ${imageFile.exists()})")
        rememberAsyncImagePainter(model = imageFile)
    } else {
        // Imagen por defecto
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
                    .size(120.dp)
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