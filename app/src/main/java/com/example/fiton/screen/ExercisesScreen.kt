package com.example.fiton.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Volver
                Box(
                    modifier = Modifier
                        .padding(start = 30.dp)
                ) {
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
                }

                // Botón Agregar
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                ) {
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
        }
    ) { paddingValues ->
        val filteredExercises = exercises.filter { it.muscleGroup == muscleGroup }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Título
            Text(
                text = "Ejercicios para $muscleGroup",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            if (filteredExercises.isEmpty()) {
                // Mensaje centrado en pantalla
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Sin datos",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No hay ejercicios registrados para:",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            muscleGroup,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Lista de ejercicios
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 70.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

    val painter: Painter = when {
        exercise.imageUri?.startsWith("http") == true -> {
            rememberAsyncImagePainter(model = exercise.imageUri)
        }
        !exercise.imageUri.isNullOrEmpty() && File(exercise.imageUri).exists() -> {
            rememberAsyncImagePainter(model = File(exercise.imageUri!!))
        }
        else -> {
            painterResource(id = R.drawable.ic_launcher_foreground)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(exercise.id) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painter,
                contentDescription = "Imagen del ejercicio",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
