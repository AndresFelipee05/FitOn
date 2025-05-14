package com.example.fiton.data

import android.content.ContentValues
import android.content.Context

class ExerciseRepository(context: Context) {
    private val dbHelper = ExerciseDbHelper(context)

    // Modificación del método insert para incluir imageUri
    fun insert(exercise: Exercise): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", exercise.name)
            put("description", exercise.description)
            put("muscleGroup", exercise.muscleGroup)
            put("imageUri", exercise.imageUri)  // Insertamos la imagen
        }
        return db.insert("exercises", null, values)
    }

    // Modificación del método getAll para incluir imageUri
    fun getAll(): List<Exercise> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "exercises",
            arrayOf("id", "name", "description", "muscleGroup", "imageUri"), // Añadimos imageUri
            null, null, null, null, "id DESC"
        )

        val exercises = mutableListOf<Exercise>()
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val name = getString(getColumnIndexOrThrow("name"))
                val description = getString(getColumnIndexOrThrow("description"))
                val muscleGroup = getString(getColumnIndexOrThrow("muscleGroup"))
                val imageUri = getString(getColumnIndexOrThrow("imageUri")) // Obtenemos imageUri
                exercises.add(Exercise(id, name, description, muscleGroup, imageUri)) // Pasamos imageUri a la clase Exercise
            }
            close()
        }
        return exercises
    }

    // Método delete no cambia, ya que no afecta la imagen
    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete("exercises", "id = ?", arrayOf(id.toString()))
    }

    // Modificación del método update para incluir imageUri
    fun update(exercise: Exercise): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", exercise.name)
            put("description", exercise.description)
            put("muscleGroup", exercise.muscleGroup)
            put("imageUri", exercise.imageUri) // Actualizamos la imagen
        }
        return db.update("exercises", values, "id = ?", arrayOf(exercise.id.toString()))
    }

    // Modificación del método getById para incluir imageUri
    fun getById(id: Long): Exercise? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "exercises",
            arrayOf("id", "name", "description", "muscleGroup", "imageUri"), // Añadimos imageUri
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        var exercise: Exercise? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val muscleGroup = cursor.getString(cursor.getColumnIndexOrThrow("muscleGroup"))
            val imageUri = cursor.getString(cursor.getColumnIndexOrThrow("imageUri")) // Obtenemos imageUri
            exercise = Exercise(id, name, description, muscleGroup, imageUri) // Creamos el ejercicio con imageUri
        }
        cursor.close()
        return exercise
    }
}
