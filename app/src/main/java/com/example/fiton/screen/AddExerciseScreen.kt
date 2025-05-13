package com.example.fiton.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddExerciseScreen(
    modifier: Modifier = Modifier,
    onSaveClick: (nombre: String, descripcion: String, grupo: String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var grupoMuscular by remember { mutableStateOf("Seleccionar grupo muscular") }
    var expanded by remember { mutableStateOf(false) }
    val grupos = listOf("Pecho", "Espalda", "Cuadriceps", "Femoral", "Hombro", "Bíceps", "Tríceps")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Nuevo Ejercicio", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Spacer(Modifier.height(8.dp))

        Text("Grupo muscular", style = MaterialTheme.typography.bodyMedium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                .padding(12.dp)) {
            Text(grupoMuscular)
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                grupos.forEach {
                    DropdownMenuItem(onClick = {
                        grupoMuscular = it
                        expanded = false
                    }, text = { Text(it) })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                onSaveClick(nombre, descripcion, grupoMuscular)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar ejercicio")
        }
    }
}
