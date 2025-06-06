package com.example.fiton.screen

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import java.io.File

data class ExerciseSelection(
    val exercise: Exercise,
    var reps: Int = 10,
    var series: Int = 3,
    var peso: Double = 0.0,
    var notes: String? = null
) {
    var repsState by mutableStateOf(reps)
    var seriesState by mutableStateOf(series)  // Estado para series
    var pesoState by mutableStateOf(peso)
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
                title = {
                    Text(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White, text = "Rutina del ${date.toString()}"
                    )
                },
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
                            text = "Guardar\nejercicos",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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
                            text = "Editar\nejercicos",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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
                            text = "Crear\nrutina",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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
                .padding(horizontal = 16.dp)
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
                                val painter: Painter = when {
                                    // URL de internet
                                    exercise.imageUri?.startsWith("http") == true -> {
                                        rememberAsyncImagePainter(model = exercise.imageUri)
                                    }
                                    // Drawable (no contiene "/")
                                    !exercise.imageUri.isNullOrEmpty() && !exercise.imageUri!!.contains("/") -> {
                                        val resourceUri = "android.resource://com.example.fiton/drawable/${exercise.imageUri}"
                                        rememberAsyncImagePainter(model = resourceUri)
                                    }
                                    // Archivo local
                                    !exercise.imageUri.isNullOrEmpty() && File(exercise.imageUri!!).exists() -> {
                                        rememberAsyncImagePainter(model = File(exercise.imageUri!!))
                                    }
                                    // Imagen por defecto
                                    else -> {
                                        painterResource(id = R.drawable.ic_launcher_foreground)
                                    }
                                }

                                Image(
                                    painter = painter,
                                    contentDescription = "Imagen del ejercicio",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(85.dp)
                                        .padding(end = 12.dp)
                                        .clip(RoundedCornerShape(8.dp))
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
                                            text = "${selection.exercise.name} - ${selection.exercise.muscleGroup}",
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
                                Text("Cerrar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para crear la rutina (configurar repeticiones, series, notas y el nuevo atributo double)
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
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1E1E),
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {
                        // Título
                        Text(
                            text = "Configurar rutina",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        // Input nombre rutina
                        OutlinedTextField(
                            value = routineName,
                            onValueChange = { routineName = it },
                            label = { Text("Nombre de la rutina", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2C2C2C),
                                unfocusedContainerColor = Color(0xFF2C2C2C),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFFFF9800),
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        // Lista de ejercicios
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
                                        containerColor = Color(0xFF2A2A2A)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = "${selection.exercise.name} - ${selection.exercise.muscleGroup}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        // Campos de configuración
                                        listOf(
                                            "Repeticiones" to selection.repsState.toString(),
                                            "Series" to selection.seriesState.toString(),
                                            "Peso (kg)" to selection.pesoState.toString()
                                        ).forEachIndexed { index, (label, value) ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Text("$label: ", color = Color.White)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                OutlinedTextField(
                                                    value = value,
                                                    onValueChange = { newValue ->
                                                        val num = newValue.toDoubleOrNull()
                                                        if (num != null && num >= 0) {
                                                            when (index) {
                                                                0 -> {
                                                                    selection.repsState =
                                                                        num.toInt()
                                                                    selection.reps = num.toInt()
                                                                }

                                                                1 -> {
                                                                    selection.seriesState =
                                                                        num.toInt()
                                                                    selection.series = num.toInt()
                                                                }

                                                                2 -> {
                                                                    selection.pesoState = num
                                                                    selection.peso = num
                                                                }
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.width(100.dp),
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    colors = TextFieldDefaults.colors(
                                                        focusedContainerColor = Color(0xFF3C3C3C),
                                                        unfocusedContainerColor = Color(0xFF3C3C3C),
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White
                                                    )
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = selection.notesState,
                                            onValueChange = {
                                                selection.notesState = it
                                                selection.notes = it
                                            },
                                            label = { Text("Anotaciones", color = Color.Gray) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
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

                        // Botones
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { showCreateRoutineDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancelar", color = Color.White)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

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
                                            it.reps,
                                            it.series,
                                            it.peso,
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
                                    containerColor = Color(
                                        0xFFFF9800
                                    )
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Guardar", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}