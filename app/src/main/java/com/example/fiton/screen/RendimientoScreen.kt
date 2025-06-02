package com.example.fiton.screen

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fiton.data.Exercise
import com.example.fiton.data.ExerciseRepository
import com.example.fiton.data.RutinaModels
import com.example.fiton.data.RutinaRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RendimientoScreen(navController: NavController, rutinaRepository: RutinaRepository) {
    val context = LocalContext.current
    val exerciseRepository = remember { ExerciseRepository(context) }

    var rutinas by remember { mutableStateOf<List<RutinaModels.Rutina>>(emptyList()) }
    var allExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var muscleGroups by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var fechaInicio by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var fechaFin by remember { mutableStateOf(LocalDate.now().plusDays(7)) }

    var ejercicioSeleccionadoId by remember { mutableStateOf<Long?>(null) }
    var muscleGroupSeleccionado by remember { mutableStateOf<String?>(null) }

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    var modoAgrupacion by remember { mutableStateOf("Ejercicio") }

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        rutinas = rutinaRepository.obtenerRutinas()
        allExercises = exerciseRepository.getAll()
        muscleGroups = allExercises.map { it.muscleGroup }.distinct()
        isLoading = false

        if (allExercises.isNotEmpty()) {
            ejercicioSeleccionadoId = allExercises.first().id
        }
        if (muscleGroups.isNotEmpty()) {
            muscleGroupSeleccionado = muscleGroups.first()
        }
    }

    // Verificar si no hay datos
    if (!isLoading && (rutinas.isEmpty() || allExercises.isEmpty())) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Contenido centrado
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Sin datos",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No hay datos disponibles para mostrar",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Agrega rutinas y ejercicios para ver tu rendimiento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }

            // Fila con ambos botones abajo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
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
                        contentDescription = "Volver atrás",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }

                FloatingActionButton(
                    onClick = { navController.navigate("create_routine_screen") },
                    containerColor = Color(0xFFFF9800),
                    modifier = Modifier
                        .width(80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear rutina",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }
            }
        }
        return
    }

    // Filtrar rutinas
    val rutinasFiltradas = rutinas.filter { it.fecha in fechaInicio..fechaFin }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Rendimiento", fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                FloatingActionButton(
                    onClick = { navController.popBackStack() },
                    containerColor = Color(0xFFFF9800),
                    modifier = Modifier
                        .width(80.dp)
                        .navigationBarsPadding()
                )
                {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver atrás",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Selector de fechas
            Text("Selecciona el rango de fechas:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateSelector("Desde", fechaInicio) { nuevaFecha ->
                    if (nuevaFecha <= fechaFin) fechaInicio = nuevaFecha
                }
                DateSelector("Hasta", fechaFin) { nuevaFecha ->
                    if (nuevaFecha >= fechaInicio) fechaFin = nuevaFecha
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            // Chips para modo de visualización
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ver rendimiento:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomChip(
                        text = "Ejercicio",
                        isSelected = modoAgrupacion == "Ejercicio",
                        onClick = { modoAgrupacion = "Ejercicio" }
                    )
                    CustomChip(
                        text = "Grupo Muscular",
                        isSelected = modoAgrupacion == "Grupo Muscular",
                        onClick = { modoAgrupacion = "Grupo Muscular" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            when (modoAgrupacion) {
                "Ejercicio" -> {
                    Text("Selecciona ejercicio:", style = MaterialTheme.typography.titleMedium)
                    ExerciseDropdown(
                        ejercicios = allExercises,
                        ejercicioSeleccionadoId = ejercicioSeleccionadoId,
                        onEjercicioSeleccionado = { ejercicioSeleccionadoId = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ejercicioSeleccionadoId?.let { selectedId ->
                        MostrarDatosEjercicioMejorado(
                            selectedId,
                            allExercises,
                            rutinasFiltradas,
                            rutinaRepository,
                            formatter
                        )
                    }
                }

                "Grupo Muscular" -> {
                    Text("Selecciona grupo muscular:", style = MaterialTheme.typography.titleMedium)
                    MuscleGroupDropdown(
                        muscleGroups = muscleGroups,
                        muscleGroupSeleccionado = muscleGroupSeleccionado,
                        onMuscleGroupSeleccionado = { muscleGroupSeleccionado = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    muscleGroupSeleccionado?.let { selectedGroup ->
                        MostrarDatosGrupoMuscularMejorado(
                            selectedGroup,
                            allExercises,
                            rutinasFiltradas,
                            rutinaRepository,
                            formatter
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        label = {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color(0xFFFF9800)
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (isSelected) Color(0xFFFF9800) else Color(0xFF2C2C2C),
            labelColor = Color.Unspecified
        )
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MostrarDatosEjercicioMejorado(
    ejercicioId: Long,
    allExercises: List<Exercise>,
    rutinasFiltradas: List<RutinaModels.Rutina>,
    rutinaRepository: RutinaRepository,
    formatter: DateTimeFormatter
) {
    // Cargar TODOS los ejercicios de las rutinas filtradas en tiempo real
    var ejerciciosFiltrados by remember {
        mutableStateOf<List<RutinaModels.RutinaEjercicio>>(
            emptyList()
        )
    }

    LaunchedEffect(rutinasFiltradas, ejercicioId) {
        val todosEjercicios = mutableListOf<RutinaModels.RutinaEjercicio>()
        rutinasFiltradas.forEach { rutina ->
            val ejerciciosRutina = rutinaRepository.obtenerEjerciciosPorRutina(rutina.id)
            todosEjercicios.addAll(ejerciciosRutina.filter { it.ejercicioId == ejercicioId })
        }
        ejerciciosFiltrados = todosEjercicios
    }

    if (ejerciciosFiltrados.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Sin datos",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "No hay registros de este ejercicio",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                "en el rango de fechas seleccionado",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
        return
    }

    // Agrupar por rutina (cada rutina es una sesión de entrenamiento)
    val progresoPorRutina = mutableMapOf<Long, MutableList<Double>>()

    ejerciciosFiltrados.forEach { ejercicio ->
        val rm = ejercicio.peso * (1 + ejercicio.repeticiones / 30.0)
        progresoPorRutina.getOrPut(ejercicio.rutinaId) { mutableListOf() }.add(rm)
    }

    val todosRM = ejerciciosFiltrados.map { it.peso * (1 + it.repeticiones / 30.0) }
    val promedioRM = todosRM.average()

    // Para estadísticas por fecha
    val progresoPorFecha = mutableMapOf<LocalDate, MutableList<Double>>()
    ejerciciosFiltrados.forEach { ejercicio ->
        val rm = ejercicio.peso * (1 + ejercicio.repeticiones / 30.0)
        val fechaRutina = rutinasFiltradas.find { it.id == ejercicio.rutinaId }?.fecha
        if (fechaRutina != null) {
            progresoPorFecha.getOrPut(fechaRutina) { mutableListOf() }.add(rm)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val nombreEjercicio = allExercises.find { it.id == ejercicioId }?.name ?: "Ejercicio"
            Text("Ejercicio: $nombreEjercicio", style = MaterialTheme.typography.titleMedium)
            Text("Promedio estimado de 1RM: ${"%.2f".format(promedioRM)} kg")
            Text("Días entrenados: ${progresoPorFecha.size}")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Crear rutinas ordenadas basándose en los ejercicios encontrados
    val rutinasConEjercicios = progresoPorRutina.keys.mapNotNull { rutinaId ->
        rutinasFiltradas.find { it.id == rutinaId }
    }.sortedBy { it.fecha }

    // Crear puntos del gráfico (mejor rendimiento por sesión)
    val puntosGrafico = rutinasConEjercicios.map { rutina ->
        progresoPorRutina[rutina.id]?.maxOrNull()?.toFloat() ?: 0f
    }

    // Crear etiquetas para el eje X
    val labels = rutinasConEjercicios.mapIndexed { index, rutina ->
        if (rutinasConEjercicios.size <= 7) {
            rutina.fecha.format(formatter)
        } else {
            rutina.fecha.format(DateTimeFormatter.ofPattern("dd/MM"))
        }
    }


    Spacer(modifier = Modifier.height(8.dp))

    var tipoGrafico by remember { mutableStateOf("Barras") }

    // Chips para tipo de gráfico
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Tipo de gráfico:", style = MaterialTheme.typography.titleMedium)
        CustomChip(
            text = "Barras",
            isSelected = tipoGrafico == "Barras",
            onClick = { tipoGrafico = "Barras" }
        )
        CustomChip(
            text = "Lineal",
            isSelected = tipoGrafico == "Lineal",
            onClick = { tipoGrafico = "Lineal" }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (puntosGrafico.isNotEmpty()) {
        when (tipoGrafico) {
            "Barras" -> BarChart(puntos = puntosGrafico, labels = labels)
            "Lineal" -> LineChart(puntos = puntosGrafico, labels = labels)
        }
    } else {
        Text("No hay datos suficientes para mostrar el gráfico")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MostrarDatosGrupoMuscularMejorado(
    muscleGroup: String,
    allExercises: List<Exercise>,
    rutinasFiltradas: List<RutinaModels.Rutina>,
    rutinaRepository: RutinaRepository,
    formatter: DateTimeFormatter
) {
    var ejerciciosFiltrados by remember {
        mutableStateOf<List<RutinaModels.RutinaEjercicio>>(
            emptyList()
        )
    }

    LaunchedEffect(rutinasFiltradas, muscleGroup) {
        val ejerciciosGrupo = allExercises.filter { it.muscleGroup == muscleGroup }
        val todosEjercicios = mutableListOf<RutinaModels.RutinaEjercicio>()

        rutinasFiltradas.forEach { rutina ->
            val ejerciciosRutina = rutinaRepository.obtenerEjerciciosPorRutina(rutina.id)
            todosEjercicios.addAll(ejerciciosRutina.filter { ej ->
                ejerciciosGrupo.any { it.id == ej.ejercicioId }
            })
        }
        ejerciciosFiltrados = todosEjercicios
    }

    if (ejerciciosFiltrados.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Sin datos",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "No hay registros del grupo $muscleGroup",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                "en el rango de fechas seleccionado",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
        return
    }

    val progresoPorRutina = mutableMapOf<Long, MutableList<Double>>()

    ejerciciosFiltrados.forEach { ejercicio ->
        val rm = ejercicio.peso * (1 + ejercicio.repeticiones / 30.0)
        progresoPorRutina.getOrPut(ejercicio.rutinaId) { mutableListOf() }.add(rm)
    }

    val todosRM = ejerciciosFiltrados.map { it.peso * (1 + it.repeticiones / 30.0) }
    val promedioRM = todosRM.average()

    val progresoPorFecha = mutableMapOf<LocalDate, MutableList<Double>>()
    ejerciciosFiltrados.forEach { ejercicio ->
        val rm = ejercicio.peso * (1 + ejercicio.repeticiones / 30.0)
        val fechaRutina = rutinasFiltradas.find { it.id == ejercicio.rutinaId }?.fecha
        if (fechaRutina != null) {
            progresoPorFecha.getOrPut(fechaRutina) { mutableListOf() }.add(rm)
        }
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Grupo muscular: $muscleGroup", style = MaterialTheme.typography.titleMedium)
            Text("Promedio estimado de 1RM: ${"%.2f".format(promedioRM)} kg")
            Text("Días entrenados: ${progresoPorFecha.size}")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val rutinasConEjercicios = progresoPorRutina.keys.mapNotNull { rutinaId ->
        rutinasFiltradas.find { it.id == rutinaId }
    }.sortedBy { it.fecha }

    val puntosGrafico = rutinasConEjercicios.map { rutina ->
        progresoPorRutina[rutina.id]?.average()?.toFloat() ?: 0f
    }

    val labels = rutinasConEjercicios.mapIndexed { index, rutina ->
        if (rutinasConEjercicios.size <= 7) {
            rutina.fecha.format(formatter)
        } else {
            rutina.fecha.format(DateTimeFormatter.ofPattern("dd/MM"))
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    var tipoGrafico by remember { mutableStateOf("Barras") }

    // Chips para tipo de gráfico
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Tipo de gráfico:",
            style = MaterialTheme.typography.titleMedium
        )
        CustomChip(
            text = "Barras",
            isSelected = tipoGrafico == "Barras",
            onClick = { tipoGrafico = "Barras" }
        )
        CustomChip(
            text = "Lineal",
            isSelected = tipoGrafico == "Lineal",
            onClick = { tipoGrafico = "Lineal" }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (puntosGrafico.isNotEmpty()) {
        when (tipoGrafico) {
            "Barras" -> BarChart(puntos = puntosGrafico, labels = labels)
            "Lineal" -> LineChart(puntos = puntosGrafico, labels = labels)
        }
    } else {
        Text("No hay datos suficientes para mostrar el gráfico")
    }
}

@Composable
fun MuscleGroupDropdown(
    muscleGroups: List<String>,
    muscleGroupSeleccionado: String?,
    onMuscleGroupSeleccionado: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = muscleGroupSeleccionado ?: "Selecciona un grupo muscular",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Desplegar",
                tint = Color.Gray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
        ) {
            muscleGroups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group) },
                    onClick = {
                        onMuscleGroupSeleccionado(group)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseDropdown(
    ejercicios: List<Exercise>,
    ejercicioSeleccionadoId: Long?,
    onEjercicioSeleccionado: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val ejercicioSeleccionado = ejercicios.find { it.id == ejercicioSeleccionadoId }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = ejercicioSeleccionado?.name ?: "Selecciona un ejercicio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Desplegar",
                tint = Color.Gray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
        ) {
            ejercicios.forEach { ejercicio ->
                DropdownMenuItem(
                    text = { Text(ejercicio.name) },
                    onClick = {
                        onEjercicioSeleccionado(ejercicio.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateSelector(label: String, fecha: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    calendar.set(fecha.year, fecha.monthValue - 1, fecha.dayOfMonth)

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                onDateSelected(LocalDate.of(y, m + 1, d))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Surface(
        modifier = Modifier
            .width(150.dp)
            .clickable { datePickerDialog.show() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE0E0E0), // gris claro suave
        shadowElevation = 2.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ${fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Seleccionar fecha",
                tint = Color.Gray
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@Composable
fun BarChart(puntos: List<Float>, labels: List<String>) {
    if (puntos.isEmpty()) {
        Text("No hay datos para mostrar el gráfico")
        return
    }

    val minValue = puntos.minOrNull() ?: 0f
    val maxValue = puntos.maxOrNull() ?: 0f

    val (adjustedMinValue, adjustedMaxValue) = if (minValue == maxValue) {
        val fixedPadding = 10f
        Pair(minValue - fixedPadding, maxValue + fixedPadding)
    } else {
        val paddingVertical = (maxValue - minValue) * 0.1f
        Pair(minValue - paddingVertical, maxValue + paddingVertical)
    }

    val rango = (adjustedMaxValue - adjustedMinValue).coerceAtLeast(1f)
    val stepSize = (adjustedMaxValue - adjustedMinValue) / 5

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KG",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(60.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val paddingRight = 80f
                    val paddingTop = 40f
                    val paddingBottom = 90f
                    val paddingLeft = 70f
                    val graphHeight = size.height - paddingTop - paddingBottom
                    val graphWidth = size.width - paddingLeft - paddingRight

                    // Dibujar ejes
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(paddingLeft, paddingTop),
                        end = Offset(paddingLeft, size.height - paddingBottom),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(paddingLeft, size.height - paddingBottom),
                        end = Offset(size.width - paddingRight, size.height - paddingBottom),
                        strokeWidth = 4f
                    )

                    // Marcas del eje Y
                    for (i in 0..5) {
                        val yValue = adjustedMinValue + i * stepSize
                        val yPos = paddingTop + graphHeight - (i * (graphHeight / 5))
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(paddingLeft - 20f, yPos),
                            end = Offset(paddingLeft, yPos),
                            strokeWidth = 3f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "%.1f".format(yValue),
                            paddingLeft - 70f,
                            yPos + 10f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 35f
                                textAlign = android.graphics.Paint.Align.RIGHT
                                isAntiAlias = true
                                isFakeBoldText = true
                            }
                        )
                    }

                    // Configuración de las barras
                    val numBarras = puntos.size
                    val espacioEntreBarraFinal = if (numBarras > 1) {
                        (graphWidth * 0.1f) / (numBarras + 1)
                    } else {
                        graphWidth * 0.45f
                    }
                    val anchoBarraFinal =
                        ((graphWidth - (espacioEntreBarraFinal * (numBarras + 1))) / numBarras)
                            .coerceIn(40f..120f)

                    // Dibujar barras y etiquetas del eje X
                    puntos.forEachIndexed { index, valor ->
                        val x =
                            paddingLeft + espacioEntreBarraFinal + index * (anchoBarraFinal + espacioEntreBarraFinal)
                        val alturaBarra = ((valor - adjustedMinValue) / rango) * graphHeight
                        val y = paddingTop + graphHeight - alturaBarra

                        // Barra
                        drawRoundRect(
                            color = Color(0xFFFF9800),
                            topLeft = Offset(x, y),
                            size = Size(anchoBarraFinal, alturaBarra),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // Valor encima de la barra
                        if (alturaBarra > 40) {
                            drawContext.canvas.nativeCanvas.apply {
                                drawRoundRect(
                                    x + anchoBarraFinal / 2 - 45f,
                                    y - 60f,
                                    x + anchoBarraFinal / 2 + 45f,
                                    y - 20f,
                                    15f,
                                    15f,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.DKGRAY
                                        isAntiAlias = true
                                    }
                                )
                                drawText(
                                    "%.1f".format(valor),
                                    x + anchoBarraFinal / 2,
                                    y - 30f,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 35f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isAntiAlias = true
                                        isFakeBoldText = true
                                    }
                                )
                            }
                        }

                        // Etiqueta bajo la barra
                        // Etiqueta rotada bajo la barra
                        if (index < labels.size) {
                            val label = labels[index]

                            // Guardamos el estado actual del canvas
                            drawContext.canvas.nativeCanvas.save()

                            // Movemos el punto de dibujo al centro de la barra en X y posición Y final
                            drawContext.canvas.nativeCanvas.translate(
                                x + anchoBarraFinal / 2,
                                size.height - 40f
                            )

                            // Rotamos el texto -45 grados (hacia arriba)
                            drawContext.canvas.nativeCanvas.rotate(-19f)

                            // Dibujamos el texto centrado
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                0f,
                                0f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 35f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                }
                            )

                            // Restauramos el estado del canvas
                            drawContext.canvas.nativeCanvas.restore()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart(puntos: List<Float>, labels: List<String>) {
    if (puntos.isEmpty()) {
        Text("No hay datos para mostrar el gráfico")
        return
    }

    // Añadir 0 al principio de los puntos y ajustar las etiquetas
    val puntosConCero = listOf(0f) + puntos
    val labelsConInicio = listOf("Inicio") + labels

    val minValue = puntosConCero.minOrNull() ?: 0f
    val maxValue = puntosConCero.maxOrNull() ?: 0f

    val (adjustedMinValue, adjustedMaxValue) = if (minValue == maxValue) {
        val fixedPadding = 10f
        Pair(minValue, maxValue + fixedPadding)
    } else {
        val paddingVertical = (maxValue - minValue) * 0.1f
        Pair(minValue, maxValue + paddingVertical)
    }

    val rango = (adjustedMaxValue - adjustedMinValue).coerceAtLeast(1f)
    val stepSize = (adjustedMaxValue - adjustedMinValue) / 5

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KG",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(60.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val paddingRight = 80f
                    val paddingTop = 40f
                    val paddingBottom = 90f
                    val paddingLeft = 70f
                    val graphHeight = size.height - paddingTop - paddingBottom
                    val graphWidth = size.width - paddingLeft - paddingRight

                    // Dibujar ejes
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(paddingLeft, paddingTop),
                        end = Offset(paddingLeft, size.height - paddingBottom),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(paddingLeft, size.height - paddingBottom),
                        end = Offset(size.width - paddingRight, size.height - paddingBottom),
                        strokeWidth = 4f
                    )

                    // Marcas del eje Y
                    for (i in 0..5) {
                        val yValue = adjustedMinValue + i * stepSize
                        val yPos = paddingTop + graphHeight - (i * (graphHeight / 5))
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(paddingLeft - 20f, yPos),
                            end = Offset(paddingLeft, yPos),
                            strokeWidth = 3f
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "%.1f".format(yValue),
                            paddingLeft - 70f,
                            yPos + 10f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 35f
                                textAlign = android.graphics.Paint.Align.RIGHT
                                isAntiAlias = true
                                isFakeBoldText = true
                            }
                        )
                    }

                    // Calcular puntos para la línea
                    val numPuntos = puntosConCero.size
                    val espacioEntrePuntos = if (numPuntos > 1) {
                        graphWidth / (numPuntos - 1)
                    } else {
                        graphWidth
                    }

                    // Convertir valores a coordenadas
                    val puntosCoords = puntosConCero.mapIndexed { index, valor ->
                        val x = paddingLeft + (index * espacioEntrePuntos)
                        val y =
                            paddingTop + graphHeight - ((valor - adjustedMinValue) / rango) * graphHeight
                        Offset(x, y)
                    }

                    // Dibujar línea
                    if (puntosCoords.size > 1) {
                        for (i in 0 until puntosCoords.size - 1) {
                            drawLine(
                                color = Color(0xFFFF9800),
                                start = puntosCoords[i],
                                end = puntosCoords[i + 1],
                                strokeWidth = 5f
                            )
                        }
                    }

                    // Dibujar puntos y etiquetas
                    puntosCoords.forEachIndexed { index, punto ->
                        // Punto circular
                        drawCircle(
                            color = Color(0xFFFF9800),
                            center = punto,
                            radius = 12f
                        )

                        // Valor encima del punto (excepto para el punto 0 inicial)
                        if (index > 0) {
                            drawContext.canvas.nativeCanvas.apply {
                                drawRoundRect(
                                    punto.x - 45f,
                                    punto.y - 60f,
                                    punto.x + 45f,
                                    punto.y - 20f,
                                    15f,
                                    15f,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.DKGRAY
                                        isAntiAlias = true
                                    }
                                )
                                drawText(
                                    "%.1f".format(puntosConCero[index]),
                                    punto.x,
                                    punto.y - 30f,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 35f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isAntiAlias = true
                                        isFakeBoldText = true
                                    }
                                )
                            }
                        }

                        // Etiqueta bajo el punto
                        if (index < labelsConInicio.size) {
                            drawContext.canvas.nativeCanvas.drawText(
                                labelsConInicio[index],
                                punto.x,
                                size.height - 40f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 35f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

