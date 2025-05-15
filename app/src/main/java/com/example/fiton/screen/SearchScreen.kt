package com.example.fiton.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun SearchScreen(
    navController: NavController,
    repository: ExerciseRepository
) {
    // rememberSaveable para mantener el texto aún si recomponen la pantalla
    var query by rememberSaveable { mutableStateOf("") }

    val exercisesState = produceState<List<Exercise>>(initialValue = emptyList(), query) {
        value = if (query.isBlank()) {
            emptyList()
        } else {
            repository.getAll().filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, top = 66.dp),  // menos padding top para el botón
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row con botón volver y título
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
                fontSize = 22.sp,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Espacio invisible para igualar el IconButton de la izquierda y centrar texto en pantalla
            Box(modifier = Modifier.size(48.dp)) // Ajusta tamaño igual al IconButton
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF2C2C2C), shape = MaterialTheme.shapes.small)
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (exercisesState.value.isEmpty() && query.isNotBlank()) {
            Text(text = "No se encontraron ejercicios", color = Color.Gray)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
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
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Grupo muscular: ${exercise.muscleGroup}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
