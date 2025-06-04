package com.example.fiton.utiles

import android.content.Context
import android.net.Uri
import android.widget.Toast
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

        println("Imagen guardada en: ${file.absolutePath}")

        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error al copiar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }
}
