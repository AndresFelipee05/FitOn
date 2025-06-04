package com.example.fiton.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fiton.R
import com.example.fiton.data.Exercise
import com.example.fiton.data.ExerciseRepository
import com.example.fiton.utiles.copyImageToInternalStorage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseScreen(
    exerciseId: Long,
    navController: NavController
) {
    val context = LocalContext.current
    val repository = remember { ExerciseRepository(context) }
    val scrollState = rememberScrollState()

    var exercise by remember { mutableStateOf<Exercise?>(null) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf("") }

    // Para mantener la ruta interna de la imagen
    var imagePath by rememberSaveable { mutableStateOf<String?>(null) }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Para saber si hemos cambiado la imagen
    var imageChanged by rememberSaveable { mutableStateOf(false) }

    val muscleGroups = listOf(
        MuscleGroup(R.drawable.pecho, "Pecho").name,
        MuscleGroup(R.drawable.espalda, "Espalda").name,
        MuscleGroup(R.drawable.biceps, "Bíceps").name,
        MuscleGroup(R.drawable.triceps, "Tríceps").name,
        MuscleGroup(R.drawable.hombro, "Hombro").name,
        MuscleGroup(R.drawable.cuadriceps, "Cuádriceps").name,
        MuscleGroup(R.drawable.femoral, "Femoral").name,
        MuscleGroup(R.drawable.gemelos, "Gemelos").name,
        MuscleGroup(R.drawable.abdominales, "Abdominales").name
    )

    var showDeleteDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                imageChanged = true

                // Copiar la imagen al almacenamiento interno
                val internalPath = copyImageToInternalStorage(context, uri)
                if (internalPath != null) {
                    imagePath = internalPath
                    println("Nueva imagen copiada a: $internalPath")
                } else {
                    Toast.makeText(context, "Error al copiar imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        } else {
            Toast.makeText(
                context,
                "Se necesita permiso para acceder a la galería",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    LaunchedEffect(Unit) {
        val loaded = repository.getById(exerciseId)
        if (loaded != null) {
            exercise = loaded
            name = loaded.name
            description = loaded.description
            muscleGroup = loaded.muscleGroup

            if (!loaded.imageUri.isNullOrEmpty()) {
                imagePath = loaded.imageUri
                // Verificar si el archivo existe
                val file = File(loaded.imageUri!!)
                if (file.exists()) {
                    println("Imagen existente encontrada en: ${loaded.imageUri}")
                    imageUri = Uri.fromFile(file)
                } else {
                    println("Advertencia: La imagen no existe en la ruta: ${loaded.imageUri}")
                    // No establecer imageUri si el archivo no existe
                }
            }
        }
    }

    if (exercise == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF9800))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Editar Ejercicio",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding(),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = muscleGroup,
                onValueChange = {},
                readOnly = true,
                label = { Text("Grupo muscular") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 250.dp)
            ) {
                muscleGroups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group) },
                        onClick = {
                            muscleGroup = group
                            expanded = false
                        }
                    )
                }
            }
        }

        // Imagen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Imagen del ejercicio",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (imageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = if (imageChanged) {
                                    imageUri // URI directa si es una imagen nueva
                                } else {
                                    // Para imágenes existentes, usar File
                                    File(imagePath!!)
                                }
                            ),
                            contentDescription = "Imagen del ejercicio",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { openGallery() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = "Cambiar imagen",
                                modifier = Modifier.padding(end = 4.dp),
                                tint = Color.White
                            )
                            Text("Cambiar", color = Color.White)
                        }

                        Button(
                            onClick = {
                                imagePath = null
                                imageUri = null
                                imageChanged = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar imagen",
                                modifier = Modifier.padding(end = 4.dp),
                                tint = Color.White
                            )
                            Text("Eliminar", color = Color.White)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .clickable { openGallery() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Añadir imagen",
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Tap para añadir imagen", color = Color.Gray)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier.width(70.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }

            FloatingActionButton(
                onClick = { showDeleteDialog = true },
                containerColor = Color.Red,
                modifier = Modifier.width(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar ejercicio",
                    tint = Color.White
                )
            }

            FloatingActionButton(
                onClick = {
                    // Verificar que si hay imagen, exista el archivo
                    if (imagePath != null) {
                        val file = File(imagePath!!)
                        if (!file.exists()) {
                            Toast.makeText(
                                context,
                                "Error: La imagen no se guardó correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@FloatingActionButton
                        }
                    }

                    repository.update(
                        Exercise(
                            id = exerciseId,
                            name = name,
                            description = description,
                            muscleGroup = muscleGroup,
                            imageUri = imagePath
                        )
                    )
                    Toast.makeText(
                        context,
                        "Ejercicio actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier.width(80.dp)
            ) {
                Text("Guardar", color = Color.White)
            }
        }
    }

    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Eliminar ejercicio",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "¿Estás seguro de que quieres eliminar este ejercicio? Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            12.dp,
                            Alignment.CenterHorizontally
                        )
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                try {
                                    exercise?.imageUri?.takeIf { it.isNotEmpty() }?.let {
                                        File(it).takeIf { file -> file.exists() }?.delete()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                repository.delete(exerciseId)
                                Toast.makeText(
                                    context,
                                    "Ejercicio eliminado con éxito",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Eliminar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}