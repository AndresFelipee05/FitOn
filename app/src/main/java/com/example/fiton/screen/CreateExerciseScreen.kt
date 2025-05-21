package com.example.fiton.screen

import android.Manifest
import android.app.Activity
import android.content.Context
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.fiton.R
import com.example.fiton.data.Exercise
import com.example.fiton.data.ExerciseRepository
import java.io.File
import java.io.FileOutputStream

fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "exercise_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        // Log para depuración
        println("Imagen guardada en: ${file.absolutePath}")

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al copiar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExerciseScreen(
    navController: NavController,
    defaultMuscleGroup: String = ""
) {
    val context = LocalContext.current
    val repository = remember { ExerciseRepository(context) }
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var muscleGroup by remember { mutableStateOf(defaultMuscleGroup) }

    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var imageSource by rememberSaveable { mutableStateOf("") }

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

    val imagePath = rememberSaveable { mutableStateOf<String?>(null) } // ✅ NUEVO

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri

                // ✅ Copia al almacenamiento interno
                val internalPath = copyImageToInternalStorage(context, uri)
                if (internalPath != null) {
                    imageSource = internalPath
                    imagePath.value = internalPath
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
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Crear nuevo ejercicio",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre *") },
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
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                muscleGroups.forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            muscleGroup = it.name
                            expanded = false
                        }
                    )
                }
            }
        }

        // Imagen
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Imagen del ejercicio", style = MaterialTheme.typography.titleMedium)

                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                    )

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
                                Icons.Default.Photo,
                                contentDescription = "Cambiar",
                                tint = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Cambiar", color = Color.White)
                        }

                        Button(
                            onClick = {
                                imageUri = null
                                imageSource = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
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
                                Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(40.dp)
                            )
                            Text("Tap para añadir imagen", color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier.width(80.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }

            FloatingActionButton(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        // Verificar que tengamos una ruta válida
                        if (imageUri != null && imagePath.value != null) {
                            // Verificar que el archivo existe
                            val file = File(imagePath.value!!)
                            if (!file.exists()) {
                                Toast.makeText(
                                    context,
                                    "Error: La imagen no se guardó correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@FloatingActionButton
                            }
                        }

                        // Guardar el ejercicio
                        val newExercise = Exercise(
                            id = 0,
                            name = name,
                            description = description,
                            muscleGroup = muscleGroup,
                            imageUri = imagePath.value
                        )

                        // Log para depuración
                        println("Guardando ejercicio con imagen: ${imagePath.value}")

                        repository.insert(newExercise)
                        navController.popBackStack()
                    }
                },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier.width(100.dp)
            ) {
                Text("Guardar", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
