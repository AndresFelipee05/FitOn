package com.example.fiton.screen

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fiton.data.Exercise
import com.example.fiton.data.ExerciseRepository
import com.example.fiton.data.RutinaRepository
import java.time.LocalDate
import com.example.fiton.R

data class ExerciseSelection(
    val exercise: Exercise,
    var reps: Int = 10,
    var series: Int = 3,
    var notes: String? = null
) {
    var repsState by mutableStateOf(reps)
    var seriesState by mutableStateOf(series)  // Estado para series
    var notesState by mutableStateOf(notes ?: "")
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineDetailScreen(
    date: LocalDate,
    navController: NavController,
    exerciseRepository: ExerciseRepository,
    rutinaRepository: RutinaRepository
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val allExercises = remember { exerciseRepository.getAll() }
    var tempSelectedExercises by remember { mutableStateOf(mutableListOf<Exercise>()) }

    // Utilizamos mutableStateListOf para que los cambios en la lista se reflejen automáticamente en la UI
    var savedExercises by remember { mutableStateOf(mutableStateListOf<ExerciseSelection>()) }

    // Estados para los diálogos
    var showEditDialog by remember { mutableStateOf(false) }
    var showCreateRoutineDialog by remember { mutableStateOf(false) }

    // Estados para búsqueda
    var query by rememberSaveable { mutableStateOf("") }
    var selectedGroup by rememberSaveable { mutableStateOf<String?>(null) }

    // Filtramos la lista según búsqueda y grupo
    val filteredExercises = remember(query, selectedGroup, allExercises) {
        allExercises.filter {
            val matchesQuery = it.name.contains(query, ignoreCase = true)
            val matchesGroup =
                selectedGroup == null || it.muscleGroup.equals(selectedGroup, ignoreCase = true)
            matchesQuery && matchesGroup
        }
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutina del ${date.toString()}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF121212),
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botón 1: Guardar ejercicios
                    Button(
                        onClick = {
                            // Añadimos los ejercicios seleccionados temporalmente a la lista de guardados
                            tempSelectedExercises.forEach { exercise ->
                                if (!savedExercises.any { it.exercise.id == exercise.id }) {
                                    savedExercises.add(ExerciseSelection(exercise))
                                }
                            }
                            // Limpiamos la selección temporal
                            tempSelectedExercises = mutableListOf()
                        },
                        enabled = tempSelectedExercises.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF2C2C2C),
                            disabledContentColor = Color.Gray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Guardar ejercicios", modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón 2: Editar ejercicios
                    Button(
                        onClick = { showEditDialog = true },
                        enabled = savedExercises.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showEditDialog) Color(0xFFFF9800) else Color(
                                0xFF2C2C2C
                            ),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF1E1E1E),
                            disabledContentColor = Color.Gray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Editar ejercicios",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón 3: Crear rutina
                    Button(
                        onClick = { showCreateRoutineDialog = true },
                        enabled = savedExercises.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF2C2C2C),
                            disabledContentColor = Color.Gray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Crear rutina", modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // --- BUSCADOR ---
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

            // --- FILTRO POR GRUPO MUSCULAR ---
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
                        label = {
                            Text(
                                group.name,
                                color = if (isSelected) Color.White else Color(0xFFFF9800)
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            selectedGroup = if (isSelected) null else group.name
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = if (isSelected) Color(0xFFFF9800) else Color(0xFF2C2C2C),
                            labelColor = Color.Unspecified
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar el número de ejercicios seleccionados
            if (tempSelectedExercises.isNotEmpty()) {
                Text(
                    text = "Ejercicios seleccionados: ${tempSelectedExercises.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Mostrar el número de ejercicios guardados
            if (savedExercises.isNotEmpty()) {
                Text(
                    text = "Ejercicios guardados: ${savedExercises.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Green,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Mensaje cuando no hay resultados
            if (filteredExercises.isEmpty()) {
                val noResultsText = when {
                    query.isNotBlank() && selectedGroup != null -> "No se encontró el ejercicio '$query' para $selectedGroup"
                    query.isNotBlank() -> "No hay ejercicios para '$query'"
                    selectedGroup != null -> "No hay ejercicios para $selectedGroup"
                    else -> "No se encontraron ejercicios"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(filteredExercises) { exercise ->
                        val isTempSelected = tempSelectedExercises.any { it.id == exercise.id }
                        // Importante: Recalculamos en tiempo real si el ejercicio está guardado
                        val isSaved = savedExercises.any { it.exercise.id == exercise.id }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isSaved -> Color(0xFF1A3A1A) // Verde oscuro para guardados
                                    isTempSelected -> Color(0xFF3A3A1A) // Amarillo oscuro para seleccionados
                                    else -> Color(0xFF1E1E1E) // Color normal
                                }
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = isTempSelected,
                                        onValueChange = { selected ->
                                            if (selected && !isSaved) {
                                                tempSelectedExercises =
                                                    (tempSelectedExercises + exercise).toMutableList()
                                            } else if (!selected && !isSaved) {
                                                tempSelectedExercises =
                                                    tempSelectedExercises.filter { it.id != exercise.id }
                                                        .toMutableList()
                                            }
                                        }
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox para selección
                                if (!isSaved) {
                                    Checkbox(
                                        checked = isTempSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFFFF9800),
                                            uncheckedColor = Color.Gray
                                        ),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                } else {
                                    // Icono de guardado para ejercicios ya guardados
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Guardado",
                                        tint = Color.Green,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }

                                // Imagen del ejercicio
                                val painter = if (!exercise.imageUri.isNullOrEmpty()) {
                                    rememberAsyncImagePainter(exercise.imageUri)
                                } else {
                                    painterResource(id = R.drawable.ic_launcher_foreground)
                                }

                                Image(
                                    painter = painter,
                                    contentDescription = "Imagen del ejercicio",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .padding(end = 12.dp)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.titleMedium,
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
                }
            }
        }

        // Diálogo para editar los ejercicios guardados
        if (showEditDialog) {
            Dialog(onDismissRequest = { showEditDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFF1E1E1E)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Ejercicios guardados",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth(), textAlign = TextAlign.Center
                        )

                        // Si no hay ejercicios, mostrar un mensaje
                        if (savedExercises.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No hay ejercicios guardados",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            // Utilizamos key para forzar la recomposición de la lista cuando se elimina un elemento
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                            ) {
                                items(
                                    items = savedExercises,
                                    key = { it.exercise.id } // Clave única para cada elemento
                                ) { selection ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = selection.exercise.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                // Eliminamos el ejercicio con una animación visual
                                                savedExercises.remove(selection)

                                                // Si no quedan ejercicios, cerramos el diálogo automáticamente
                                                if (savedExercises.isEmpty()) {
                                                    showEditDialog = false
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                    Divider(color = Color(0xFF2C2C2C))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { showEditDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800)
                                )
                            ) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para crear la rutina (configurar repeticiones y notas)
        // Diálogo para crear la rutina (configurar repeticiones, series y notas)
        if (showCreateRoutineDialog) {
            var routineName by remember {
                mutableStateOf(
                    "Rutina del ${
                        date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
                    }"
                )
            }

            Dialog(onDismissRequest = { showCreateRoutineDialog = false }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.medium,
                    color = Color(0xFF1E1E1E)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Configurar rutina",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Campo para el nombre de la rutina
                        OutlinedTextField(
                            value = routineName,
                            onValueChange = { routineName = it },
                            label = { Text("Nombre de la rutina", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF3C3C3C),
                                unfocusedContainerColor = Color(0xFF3C3C3C),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFFFF9800),
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) {
                            items(savedExercises) { selection ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF2C2C2C)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = selection.exercise.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        // Campo de repeticiones (ahora arriba)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                "Repeticiones: ",
                                                color = Color.White,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            OutlinedTextField(
                                                value = selection.repsState.toString(),
                                                onValueChange = { newValue ->
                                                    val reps = newValue.toIntOrNull()
                                                    if (reps != null && reps >= 0) {
                                                        selection.repsState = reps
                                                        selection.reps = reps
                                                    }
                                                },
                                                modifier = Modifier.width(100.dp),
                                                singleLine = true,
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0xFF3C3C3C),
                                                    unfocusedContainerColor = Color(0xFF3C3C3C),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                )
                                            )
                                        }

                                        // Campo de series (ahora abajo)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                "Series: ",
                                                color = Color.White,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            OutlinedTextField(
                                                value = selection.seriesState.toString(),
                                                onValueChange = { newValue ->
                                                    val series = newValue.toIntOrNull()
                                                    if (series != null && series >= 0) {
                                                        selection.seriesState = series
                                                        selection.series = series
                                                    }
                                                },
                                                modifier = Modifier.width(100.dp),
                                                singleLine = true,
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0xFF3C3C3C),
                                                    unfocusedContainerColor = Color(0xFF3C3C3C),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                )
                                            )
                                        }

                                        OutlinedTextField(
                                            value = selection.notesState,
                                            onValueChange = { newNotes ->
                                                selection.notesState = newNotes
                                                selection.notes = newNotes
                                            },
                                            label = { Text("Anotaciones", color = Color.Gray) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFF3C3C3C),
                                                unfocusedContainerColor = Color(0xFF3C3C3C),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedLabelColor = Color(0xFFFF9800),
                                                unfocusedLabelColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { showCreateRoutineDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800)
                                )
                            ) {
                                Text("Cancelar", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    if (routineName.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Por favor ingresa un nombre para la rutina",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    val dayOfWeek = date.dayOfWeek.value
                                    val rutinaId = rutinaRepository.insertarRutina(
                                        routineName,
                                        dayOfWeek,
                                        date
                                    )
                                    savedExercises.forEach {
                                        rutinaRepository.insertarRutinaEjercicio(
                                            rutinaId,
                                            it.exercise.id,
                                            it.series,
                                            it.reps,
                                            it.notes
                                        )
                                    }
                                    Toast.makeText(
                                        context,
                                        "Rutina '$routineName' guardada con éxito",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800)
                                )
                            ) {
                                Text("Guardar rutina", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}