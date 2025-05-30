package com.example.fiton.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fiton.data.RutinaModels
import com.example.fiton.data.RutinaRepository

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val repository = remember { RutinaRepository(context) }

    var rutinas by remember { mutableStateOf<List<RutinaModels.Rutina>>(emptyList()) }
    var query by rememberSaveable { mutableStateOf("") }

    // Carga rutinas al iniciar la pantalla
    LaunchedEffect(Unit) {
        rutinas = repository.obtenerRutinas()
    }

    val filteredRutinas = remember(rutinas, query) {
        rutinas.filter { rutina ->
            query.isBlank() || rutina.nombre.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            CenterAlignedTopAppBar( // Centrar el título
                title = {
                    Text(
                        text = "Rutinas",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            )
        },
        floatingActionButton = { // Botón de agregar rutina en la esquina inferior derecha
            FloatingActionButton(
                onClick = {
                    navController.navigate("create_routine_screen")
                },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier.width(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar rutina",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
            ) {
                // Barra de búsqueda
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF2C2C2C), shape = MaterialTheme.shapes.small)
                            .padding(12.dp)
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Buscar rutinas...",
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
                    IconButton(onClick = { /* Acción de búsqueda */ }) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Buscar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val noResultsText = if (query.isNotBlank())
                    "No hay rutinas que coincidan con '$query'"
                else
                    "No se encontraron rutinas"

                if (filteredRutinas.isEmpty()) {
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 0.dp,
                                bottom = 100.dp // margen inferior para evitar superposición con los FABs
                            ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(filteredRutinas, key = { it.id }) { rutina ->
                            RoutineCard(rutina) {
                                navController.navigate("edit_routine_screen/${rutina.id}")
                            }
                        }
                    }
                }
            }

            // Botón para ir hacia atrás en la esquina inferior izquierda
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier
                    .align(Alignment.BottomStart) // Ubicación en la parte inferior izquierda
                    .padding(start = 16.dp, bottom = 16.dp) // Espacio para evitar el borde
                    .width(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun RoutineCard(rutina: RutinaModels.Rutina, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = rutina.nombre,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Día: ${rutina.diaSemana}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fecha: ${rutina.fecha}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
