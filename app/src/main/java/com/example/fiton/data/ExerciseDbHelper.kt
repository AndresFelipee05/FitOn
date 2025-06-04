package com.example.fiton.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExerciseDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
        CREATE TABLE exercises (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            description TEXT DEFAULT '',
            muscleGroup TEXT DEFAULT '',
            imageUri TEXT
        );
        """.trimIndent())

        insertarEjerciciosPorDefecto(db)
    }

    private fun insertarEjerciciosPorDefecto(db: SQLiteDatabase) {
        // Ejercicios con nombre, grupo muscular, descripción y url de imagen ("" para poner tú la url)
        val ejercicios = listOf(
            // Pecho
            Quad("Press banca", "Pecho", "Ejercicio clásico para trabajar el pectoral mayor con barra en banco plano.", "exercise_press_banca"),
            Quad("Press inclinado", "Pecho", "Variante del press para enfatizar la parte superior del pecho.", "exercise_press_inclinado"),
            Quad("Aperturas en máquina", "Pecho", "Ejercicio para aislar y estirar el pectoral.", "exercise_apertura_maquina"),
            Quad("Fondos en paralelas", "Pecho", "Ejercicio para pectorales y tríceps usando el peso corporal.", "exercise_fondos_paralelas"),

            // Espalda
            Quad("Dominadas", "Espalda", "Ejercicio de peso corporal para dorsal ancho y bíceps.", "exercise_dominadas"),
            Quad("Jalón al pecho", "Espalda", "Ejercicio en polea para dorsal ancho, alternativa a dominadas.", "exercise_jalon_pecho"),
            Quad("Remo con barra", "Espalda", "Ejercicio para espaldas gruesas con barra.", "exercise_remo_barra"),
            Quad("Pullover", "Espalda", "Ejercicio para expandir la caja torácica y trabajar dorsal.", "exercise_pullover"),

            // Bíceps
            Quad("Curl martillo", "Bíceps", "Curl con agarre neutro para braquiorradial y bíceps.", "exercise_curl_martillo"),
            Quad("Curl con barra", "Bíceps", "Curl clásico para trabajar el bíceps braquial.", "exercise_curl_barra"),
            Quad("Curl predicador", "Bíceps", "Curl enfocado para aislar el bíceps y evitar balanceo.", "exercise_curl_predicador"),

            // Tríceps
            Quad("Extensión de tríceps", "Tríceps", "Ejercicio para trabajar la cabeza larga del tríceps.", "exercise_extension_triceps"),
            Quad("Press francés con barra", "Tríceps", "Ejercicio para desarrollar el tríceps con barra.", "exercise_press_frances"),
            Quad("Press de banca cerrado", "Tríceps", "Variante del press para enfatizar tríceps.", "exercise_press_banca_cerrado"),

            // Hombro
            Quad("Press militar con mancuernas", "Hombro", "Press para deltoides anterior con mancuernas.", "exercise_press_militar"),
            Quad("Elevaciones laterales", "Hombro", "Ejercicio para deltoides medio y definición.", "exercise_elevaciones_laterales"),
            Quad("Face Pulls", "Hombro", "Ejercicio para deltoides posteriores y estabilidad escapular.", "exercise_face_pulls"),

            // Cuádriceps
            Quad("Sentadilla", "Cuádriceps", "Ejercicio básico para cuádriceps y glúteos.", "exercise_sentadilla"),
            Quad("Extensión de cuadríceps", "Cuádriceps", "Ejercicio de aislamiento para el cuádriceps.", "exercise_extension_cuadriceps"),
            Quad("Sentadilla búlgara", "Cuádriceps", "Sentadilla unilateral para fuerza y equilibrio.", "exercise_sentadilla_bulgara"),
            Quad("Hack squat", "Cuádriceps", "Variante de sentadilla para enfocar cuádriceps.", "exercise_hack_squatl"),

            // Femoral
            Quad("Peso muerto rumano", "Femoral", "Ejercicio para isquiotibiales y glúteos.", "exercise_peso_muerto_rumano"),
            Quad("Curl de piernas sentado en máquina", "Femoral", "Ejercicio de aislamiento para femoral.", "exercise_curl_sentado"),
            Quad("Curl de piernas tumbado", "Femoral", "Otro ejercicio para femoral tumbado.", "exercise_curl_tumbado"),
            Quad("Hip thrust", "Femoral", "Ejercicio para glúteos con énfasis en femoral.", "exercise_hip_thrust"),

            // Gemelos
            Quad("Elevación de talones de pie", "Gemelos", "Ejercicio para gemelos con peso corporal o barra.", "exercise_elevacion_talones_pie"),
            Quad("Elevación de talones sentado", "Gemelos", "Ejercicio enfocado en el sóleo.", "exercise_elevacion_talones_sentado"),

            // Abdominales
            Quad("Crunch abdominal", "Abdominales", "Ejercicio clásico para la zona abdominal.", "exercise_crunch_abdominal"),
            Quad("Mountain climbers", "Abdominales", "Ejercicio dinámico para core y cardio.", "exercise_mountain_climbers")
        )

        for ((name, group, description, imageUri) in ejercicios) {
            db.execSQL(
                "INSERT INTO exercises (name, muscleGroup, description, imageUri) VALUES (?, ?, ?, ?)",
                arrayOf(name, group, description, imageUri)
            )
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            // Borra la tabla y la crea de nuevo con los datos por defecto
            db.execSQL("DROP TABLE IF EXISTS exercises")
            onCreate(db)
        }
    }

    companion object {
        const val DATABASE_NAME = "exercises.db"
        const val DATABASE_VERSION = 5  // Subimos a 5 para forzar upgrade
    }

    // Usamos esta data class interna para mejor legibilidad de los datos
    private data class Quad(val name: String, val group: String, val description: String, val imageUri: String)
}
