package com.example.fiton.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ExercisesScreen(muscleGroup: String, navController: NavController) {
    // No es necesario este LaunchedEffect si no haces nada dentro
    // Puedes eliminarlo a menos que pienses usarlo después

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Espaciado general
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ejercicios para $muscleGroup",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(onClick = { /* Lógica para agregar ejercicio */ }) {
                Text(text = "Agregar ejercicio")
            }

            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Volver")
            }
        }
    }
}
