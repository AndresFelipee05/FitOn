package com.example.fiton.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fiton.R
import com.example.fiton.data.Exercise
import com.example.fiton.data.ExerciseRepository
import java.io.File

data class MuscleGroup(val imageResId: Int, val name: String)

@Composable
fun SearchScreen(
    navController: NavController,
    repository: ExerciseRepository
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }

    val muscleGroups = listOf(
        MuscleGroup(R.drawable.pecho, "Pecho"),
        MuscleGroup(R.drawable.espalda, "Espalda"),
        MuscleGroup(R.drawable.biceps, "Bíceps"),
        MuscleGroup(R.drawable.triceps, "Tríceps"),
        MuscleGroup(R.drawable.hombro, "Hombro"),
        MuscleGroup(R.drawable.cuadriceps, "Cuádriceps"),
        MuscleGroup(R.drawable.femoral, "Femoral"),
        MuscleGroup(R.drawable.gemelos, "Gemelos"),
        MuscleGroup(R.drawable.abdominales, "Abdominales")
    )

    val exercisesState =
        produceState<List<Exercise>>(initialValue = emptyList(), query, selectedGroup) {
            value = repository.getAll().filter {
                val matchesQuery = it.name.contains(query, ignoreCase = true)
                val matchesGroup =
                    selectedGroup == null || it.muscleGroup.equals(selectedGroup, ignoreCase = true)
                matchesQuery && matchesGroup
            }
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Buscar ejercicios",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2C2C2C), shape = MaterialTheme.shapes.small)
                        .padding(12.dp)
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Buscar ejercicios...",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isSelected = selectedGroup == null
                    FilterChip(
                        label = {
                            Text(
                                "Ver todos",
                                color = if (isSelected) Color.White else Color(0xFFFF9800)
                            )
                        },
                        selected = isSelected,
                        onClick = { selectedGroup = null },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) Color(0xFFFF9800) else Color(0xFF2C2C2C),
                            labelColor = Color.Unspecified
                        )
                    )
                }

                items(muscleGroups) { group ->
                    val isSelected = selectedGroup == group.name
                    FilterChip(
                        label = { Text(group.name) },
                        selected = isSelected,
                        onClick = {
                            selectedGroup = if (isSelected) null else group.name
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) Color(0xFFFF9800) else Color(0xFF2C2C2C),
                            labelColor = if (isSelected) Color.White else Color(0xFFFF9800)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val noResultsText = when {
                query.isNotBlank() && selectedGroup != null -> "No se encontró el ejercicio '$query' para $selectedGroup"
                query.isNotBlank() -> "No hay ejercicios para '$query'"
                selectedGroup != null -> "No hay ejercicios para $selectedGroup"
                else -> "No se encontraron ejercicios"
            }

            if (exercisesState.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = noResultsText,
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 50.dp), // 👈 Espacio para los FABs
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exercisesState.value) { exercise ->
                        ExerciseCardSearch(exercise = exercise) { id ->
                            navController.navigate("edit_exercise/$id")
                        }
                    }
                }
            }
        }

        // Floating Action Buttons en parte inferior
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            containerColor = Color(0xFFFF9800),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .width(80.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        // Botón Añadir en la esquina inferior derecha
        FloatingActionButton(
            onClick = {
                val group = selectedGroup ?: "Pecho" // Valor por defecto
                navController.navigate("create_exercise_screen/$group")
            },
            containerColor = Color(0xFFFF9800),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .width(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir ejercicio",
                tint = Color.White
            )
        }
    }
}

@Composable
fun ExerciseCardSearch(exercise: Exercise, onClick: (Long) -> Unit) {
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
                    text = "Grupo muscular: ${exercise.muscleGroup}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
