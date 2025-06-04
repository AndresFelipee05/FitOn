package com.example.fiton.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.fiton.data.RutinaRepository
import com.example.fiton.data.RutinaModels
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

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

    var rutina by remember { mutableStateOf<RutinaModels.Rutina?>(null) }
    var ejercicios by remember { mutableStateOf<List<RutinaModels.RutinaEjercicio>>(emptyList()) }

    var nombre by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(LocalDate.now()) }
    // Usar lista de IDs para ejercicios eliminados
    val ejerciciosEliminadosIds = remember { mutableStateListOf<Long>() }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fecha.toEpochDay() * 24 * 60 * 60 * 1000
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMinExerciseWarning by remember { mutableStateOf(false) }

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    val allExercises = remember { exerciseRepository.getAll() }

    // Variable para generar IDs únicos temporales para nuevos ejercicios
    var nextTempId by remember { mutableStateOf(-1L) }

    // Función para contar ejercicios activos (no eliminados)
    fun contarEjerciciosActivos(): Int {
        return ejercicios.count { !ejerciciosEliminadosIds.contains(it.id) }
    }

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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Editar Rutina", fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(
                    onClick = { navController.popBackStack() },
                    containerColor = Color(0xFFFF9800),
                    modifier = Modifier
                        .width(80.dp)
                        .navigationBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    containerColor = Color.Red,
                    modifier = Modifier
                        .width(80.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                }

                FloatingActionButton(
                    onClick = {
                        // Validar que hay al menos un ejercicio activo antes de guardar
                        if (contarEjerciciosActivos() == 0) {
                            showMinExerciseWarning = true
                            return@FloatingActionButton
                        }

                        rutina?.let {
                            rutinaRepository.actualizarRutina(
                                it.id,
                                nombre,
                                it.fecha.dayOfWeek.value,
                                fecha
                            )

                            ejercicios.forEach { ejercicio ->
                                if (!ejerciciosEliminadosIds.contains(ejercicio.id)) {
                                    if (ejercicio.id <= 0L) { // IDs negativos o 0 son nuevos ejercicios
                                        rutinaRepository.insertarRutinaEjercicio(
                                            rutinaId = rutina!!.id,
                                            ejercicioId = ejercicio.ejercicioId,
                                            series = ejercicio.series,
                                            repeticiones = ejercicio.repeticiones,
                                            peso = ejercicio.peso,
                                            anotaciones = ejercicio.anotaciones
                                        )
                                    } else {
                                        rutinaRepository.actualizarEjercicioDeRutina(
                                            ejercicio.id,
                                            ejercicio.repeticiones,
                                            ejercicio.series,
                                            ejercicio.peso,
                                            ejercicio.anotaciones
                                        )
                                    }
                                }
                            }

                            // Solo eliminar ejercicios que existen en la base de datos (ID > 0)
                            ejerciciosEliminadosIds.forEach { ejercicioId ->
                                if (ejercicioId > 0) {
                                    val ejercicio = ejercicios.find { it.id == ejercicioId }
                                    ejercicio?.let {
                                        rutinaRepository.eliminarEjercicioDeRutina(
                                            it.rutinaId,
                                            it.ejercicioId
                                        )
                                    }
                                }
                            }

                            navController.popBackStack()
                        }
                    },
                    containerColor = Color(0xFFFF9800),
                    modifier = Modifier
                        .width(80.dp)
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.DarkGray) // Fondo gris
                        .padding(8.dp)
                        .clickable { showDatePicker = true } // Hace que todo el Box abra el calendario
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendario",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp) // Espacio entre el icono y el texto
                        )
                        Text(
                            text = "Fecha: $fecha",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }, // También abre el calendario al hacer clic en el texto
                            style = MaterialTheme.typography.bodyLarge
                        )
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ejercicios", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "(${contarEjerciciosActivos()} activos)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (contarEjerciciosActivos() == 1) Color.Red else Color.Gray
                    )
                }

                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .weight(1f)
                ) {
                    ejercicios.forEachIndexed { index, ejercicio ->
                        // Usar el ID único del ejercicio para verificar si está eliminado
                        val isEliminado = ejerciciosEliminadosIds.contains(ejercicio.id)
                        val esUltimoActivo = contarEjerciciosActivos() == 1 && !isEliminado

                        var repeticiones by remember { mutableStateOf(ejercicio.repeticiones.toString()) }
                        var series by remember { mutableStateOf(ejercicio.series.toString()) }
                        var peso by remember { mutableStateOf(ejercicio.peso.toString()) }
                        var anotaciones by remember { mutableStateOf(ejercicio.anotaciones ?: "") }
                        val ejercicioInfo = exerciseRepository.getById(ejercicio.ejercicioId)

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isEliminado) 0.5f else 1f)
                            .then(
                                if (esUltimoActivo) {
                                    Modifier.border(2.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                } else {
                                    Modifier
                                }
                            )) {
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

                                    if (esUltimoActivo) {
                                        Text(
                                            "⚠️ Último ejercicio activo",
                                            color = Color.Red,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextField(
                                            value = series,
                                            onValueChange = {
                                                if (it.all { c -> c.isDigit() }) {
                                                    series = it
                                                    ejercicios =
                                                        ejercicios.toMutableList().also { list ->
                                                            list[index] = list[index].copy(
                                                                series = it.toIntOrNull() ?: 3
                                                            )
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
                                                    ejercicios =
                                                        ejercicios.toMutableList().also { list ->
                                                            list[index] = list[index].copy(
                                                                repeticiones = it.toIntOrNull() ?: 0
                                                            )
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
                                        value = peso,
                                        onValueChange = {
                                            peso = it
                                            val newWeight = it.toDoubleOrNull() ?: 0.0
                                            ejercicios = ejercicios.toMutableList().also { list ->
                                                list[index] = list[index].copy(peso = newWeight)
                                            }
                                        },
                                        label = { Text("Peso (kg)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isEliminado,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

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
                                            // Si es el último ejercicio activo, mostrar advertencia
                                            if (esUltimoActivo) {
                                                showMinExerciseWarning = true
                                            } else {
                                                ejerciciosEliminadosIds.add(ejercicio.id)
                                            }
                                        } else {
                                            ejerciciosEliminadosIds.remove(ejercicio.id)
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        if (isEliminado) Icons.Default.Undo else Icons.Default.Close,
                                        contentDescription = if (isEliminado) "Restaurar ejercicio" else "Eliminar ejercicio",
                                        tint = if (esUltimoActivo) Color.Red.copy(alpha = 0.5f) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { showAddExerciseDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Añadir ejercicio",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir ejercicio", color = Color.White)
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

        if (showAddExerciseDialog) {
            AlertDialog(
                onDismissRequest = { showAddExerciseDialog = false },
                containerColor = Color(0xFF1E1E1E),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color(0xFF90CAF9),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Seleccionar ejercicio",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                    ) {
                        items(allExercises) { ejercicio ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val nuevoEjercicio = RutinaModels.RutinaEjercicio(
                                            id = nextTempId,
                                            rutinaId = rutina?.id ?: 0L,
                                            ejercicioId = ejercicio.id,
                                            series = 3,
                                            repeticiones = 10,
                                            peso = 0.0,
                                            anotaciones = ""
                                        )
                                        ejercicios = ejercicios + nuevoEjercicio
                                        nextTempId--
                                        showAddExerciseDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "${ejercicio.muscleGroup} - ${ejercicio.name}",
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAddExerciseDialog = false }) {
                        Text("Cancelar", color = Color(0xFF90CAF9), fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        // Diálogo de advertencia para ejercicio mínimo
        if (showMinExerciseWarning) {
            AlertDialog(
                onDismissRequest = { showMinExerciseWarning = false },
                containerColor = Color(0xFF1E1E1E),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ejercicio requerido")
                    }
                },
                text = {
                    Text(
                        "Una rutina debe tener al menos un ejercicio. Agrega otro ejercicio antes de eliminar este.",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showMinExerciseWarning = false }
                    ) {
                        Text("Entendido", color = Color(0xFF90CAF9), fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        if (showDeleteDialog) {
            Dialog(onDismissRequest = { showDeleteDialog = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFF1E1E1E),
                    tonalElevation = 8.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .widthIn(min = 280.dp, max = 360.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Eliminar rutina",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Esta acción eliminará la rutina permanentemente. ¿Estás seguro de que deseas continuar?",
                            fontSize = 16.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                        ) {
                            OutlinedButton(
                                onClick = { showDeleteDialog = false },
                                border = BorderStroke(1.dp, Color.Gray),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Cancelar")
                            }

                            Button(
                                onClick = {
                                    rutina?.let {
                                        rutinaRepository.eliminarRutina(it.id)
                                    }
                                    showDeleteDialog = false
                                    navController.popBackStack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // rojo fuerte
                            ) {
                                Text("Eliminar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}