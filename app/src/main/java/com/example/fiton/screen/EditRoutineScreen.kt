package com.example.fiton.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fiton.data.RutinaRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutineScreen(
    navController: NavController,
    rutinaId: Long
) {
    val context = LocalContext.current
    val rutinaRepository = remember { RutinaRepository(context) }
    val exerciseRepository = remember { com.example.fiton.data.ExerciseRepository(context) }

    var rutina by remember { mutableStateOf<com.example.fiton.data.RutinaModels.Rutina?>(null) }
    var ejercicios by remember { mutableStateOf<List<com.example.fiton.data.RutinaModels.RutinaEjercicio>>(emptyList()) }

    var nombre by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now()) }
    val ejerciciosEliminados = remember { mutableStateListOf<Long>() }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fecha.toEpochDay() * 24 * 60 * 60 * 1000
    )

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(rutinaId) {
        rutina = rutinaRepository.obtenerRutinaPorId(rutinaId)
        rutina?.let {
            nombre = it.nombre
            fecha = it.fecha
            ejercicios = rutinaRepository.obtenerEjerciciosPorRutina(it.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Rutina", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Cancelar", tint = Color.White)
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                }

                Button(
                    onClick = {
                        rutina?.let {
                            rutinaRepository.actualizarRutina(it.id, nombre, it.fecha.dayOfWeek.value, fecha)

                            ejercicios.forEach {
                                if (!ejerciciosEliminados.contains(it.ejercicioId)) {
                                    rutinaRepository.actualizarEjercicioDeRutina(
                                        it.rutinaId,
                                        it.ejercicioId,
                                        it.series,
                                        it.repeticiones,
                                        it.anotaciones
                                    )
                                }
                            }

                            ejerciciosEliminados.forEach { ejercicioId ->
                                rutinaRepository.eliminarEjercicioDeRutina(rutina!!.id, ejercicioId)
                            }

                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar", color = Color.White)
                }
            }
        }
    ) { padding ->
        rutina?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                TextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la rutina") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Fecha: $fecha", modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario")
                    }
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    fecha = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                }
                                showDatePicker = false
                            }) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Ejercicios", style = MaterialTheme.typography.titleMedium)

                ejercicios.forEachIndexed { index, ejercicio ->
                    val isEliminado = ejerciciosEliminados.contains(ejercicio.ejercicioId)

                    var repeticiones by remember { mutableStateOf(ejercicio.repeticiones.toString()) }
                    var series by remember { mutableStateOf(ejercicio.series.toString()) }
                    var anotaciones by remember { mutableStateOf(ejercicio.anotaciones ?: "") }
                    val ejercicioInfo = exerciseRepository.getById(ejercicio.ejercicioId)

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(modifier = Modifier.fillMaxWidth().alpha(if (isEliminado) 0.5f else 1f)) {
                        Box {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Ejercicio: ${ejercicioInfo?.name ?: "Desconocido"}",
                                    textDecoration = if (isEliminado) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    "Grupo muscular: ${ejercicioInfo?.muscleGroup ?: "N/A"}",
                                    textDecoration = if (isEliminado) TextDecoration.LineThrough else TextDecoration.None
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextField(
                                        value = series,
                                        onValueChange = {
                                            if (it.all { c -> c.isDigit() }) {
                                                series = it
                                                ejercicios = ejercicios.toMutableList().also { list ->
                                                    list[index] = list[index].copy(series = it.toIntOrNull() ?: 3)
                                                }
                                            }
                                        },
                                        label = { Text("Series") },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isEliminado,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    TextField(
                                        value = repeticiones,
                                        onValueChange = {
                                            if (it.all { c -> c.isDigit() }) {
                                                repeticiones = it
                                                ejercicios = ejercicios.toMutableList().also { list ->
                                                    list[index] = list[index].copy(repeticiones = it.toIntOrNull() ?: 0)
                                                }
                                            }
                                        },
                                        label = { Text("Repeticiones") },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isEliminado,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                TextField(
                                    value = anotaciones,
                                    onValueChange = {
                                        anotaciones = it
                                        ejercicios = ejercicios.toMutableList().also { list ->
                                            list[index] = list[index].copy(anotaciones = it)
                                        }
                                    },
                                    label = { Text("Anotaciones") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isEliminado
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (!isEliminado) {
                                        ejerciciosEliminados.add(ejercicio.ejercicioId)
                                    } else {
                                        ejerciciosEliminados.remove(ejercicio.ejercicioId)
                                    }
                                },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar ejercicio")
                            }
                        }
                    }
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        // Dialogo de confirmación para eliminar rutina
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar rutina") },
                text = { Text("¿Estás seguro de que deseas eliminar esta rutina? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        rutina?.let {
                            rutinaRepository.eliminarRutina(it.id)
                        }
                        showDeleteDialog = false
                        navController.popBackStack()
                    }) {
                        Text("Eliminar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}