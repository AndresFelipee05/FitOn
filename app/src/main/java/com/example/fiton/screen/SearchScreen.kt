package com.example.fiton.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fiton.R
import com.example.fiton.data.Exercise
import com.example.fiton.data.ExerciseRepository

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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(50.dp))

            Text(
                text = "Buscar ejercicios",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(48.dp))
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

        // Filtro por grupo muscular
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Chip "Ver todos"
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

            // Chips de grupos musculares
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

        // Mensajes personalizados según estado de búsqueda y filtro
        val noResultsText = when {
            query.isNotBlank() && selectedGroup != null -> "No se encotró el ejercicio '$query' para $selectedGroup"
            query.isNotBlank() -> "No hay ejercicios para '$query'"
            selectedGroup != null -> "No hay ejercicios para $selectedGroup"
            else -> "No se encontraron ejercicios"
        }

        if (exercisesState.value.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(exercisesState.value) { exercise ->
                    ExerciseCardSearch(exercise = exercise) { id ->
                        navController.navigate("edit_exercise/$id")
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseCardSearch(exercise: Exercise, onClick: (Long) -> Unit) {
    val painter = if (!exercise.imageUri.isNullOrEmpty()) {
        rememberAsyncImagePainter(exercise.imageUri)
    } else {
        painterResource(id = R.drawable.ic_launcher_foreground)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp)
            .clickable { onClick(exercise.id) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painter,
                contentDescription = "Imagen del ejercicio",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    text = "Grupo muscular: ${exercise.muscleGroup}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}
