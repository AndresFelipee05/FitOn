package com.example.fiton.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fiton.data.ExerciseRepository
import com.example.fiton.data.RutinaRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateRoutineScreen(
    navController: NavController,
    exerciseRepository: ExerciseRepository,
    rutinaRepository: RutinaRepository
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val existingRoutineDates by remember(currentYearMonth) {
        derivedStateOf {
            rutinaRepository.obtenerRutinas()
                .map { it.fecha }
                .filter { YearMonth.from(it) == currentYearMonth }
                .toSet()
        }
    }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Crear Rutina",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            MonthSelector(
                currentYearMonth,
                onPrevious = { currentYearMonth = currentYearMonth.minusMonths(1) },
                onNext = { currentYearMonth = currentYearMonth.plusMonths(1) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MonthlyDayList(
                currentYearMonth,
                selectedDate,
                existingRoutineDates,
                onDateSelected = { date ->
                    selectedDate = date
                    showDialog = true
                }
            )
        }

        if (showDialog && selectedDate != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Crear Rutina") },
                text = { Text("¿Deseas crear una rutina para el día ${selectedDate.toString()}?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        navController.navigate("crear_rutina_detalle/${selectedDate}")
                    }) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthSelector(
    currentYearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val monthName = remember(currentYearMonth) {
        currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
            .replaceFirstChar { it.titlecase(Locale("es", "ES")) }
    }
    val year = currentYearMonth.year

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrevious) { Text("<") }
        Text("$monthName $year", style = MaterialTheme.typography.titleLarge, color = Color.White)
        TextButton(onClick = onNext) { Text(">") }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyDayList(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    existingRoutineDates: Set<LocalDate>, // Nuevo parámetro
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeekValue = (firstDayOfMonth.dayOfWeek.value - 1)

    val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val totalCells = daysInMonth + firstDayOfWeekValue
        val rows = (totalCells + 6) / 7

        Column {
            for (rowIndex in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (colIndex in 0..6) {
                        val cellIndex = rowIndex * 7 + colIndex
                        val dayNumber = cellIndex - firstDayOfWeekValue + 1

                        if (dayNumber in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayNumber)
                            val isSelected = selectedDate == date

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        else Color.Transparent,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )

                                    // Círculo rojo para días con rutinas
                                    if (existingRoutineDates.contains(date)) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(Color.Red, CircleShape)
                                                .padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
