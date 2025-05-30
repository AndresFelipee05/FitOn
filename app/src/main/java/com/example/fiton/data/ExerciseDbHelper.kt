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
            Quad("Press banca", "Pecho", "Ejercicio clásico para trabajar el pectoral mayor con barra en banco plano.", "https://as01.epimg.net/deporteyvida/imagenes/2018/03/13/portada/1520951231_590856_1520951370_noticia_normal.jpg"),
            Quad("Press inclinado", "Pecho", "Variante del press para enfatizar la parte superior del pecho.", "https://lifestyle.fit/wp-content/uploads/2018/11/New-Project-5-1.jpg"),
            Quad("Aperturas en máquina", "Pecho", "Ejercicio para aislar y estirar el pectoral.", "https://templodelfitness.com/wp-content/uploads/2024/06/Aperturas-en-maquina-2.jpg"),
            Quad("Fondos en paralelas", "Pecho", "Ejercicio para pectorales y tríceps usando el peso corporal.", "https://d3dnwnveix5428.cloudfront.net/eyJrZXkiOiJzdG9yZV8yY2U5MzIyNy1iMGJhLTQ0MjUtYWE5YS0zZDE1NTg1YjJjYTZcL2ltYWdlc1wvMTNlNWI4NzU3MGI1MTZiNDc4ZTJmNmU4MWE5ZGViN2UuanBnIiwiZWRpdHMiOnsicmVzaXplIjp7IndpZHRoIjo4MDAsImhlaWdodCI6ODAwLCJmaXQiOiJpbnNpZGUifX19"),

            // Espalda
            Quad("Dominadas", "Espalda", "Ejercicio de peso corporal para dorsal ancho y bíceps.", "https://th.bing.com/th/id/OIP.0KOPVDb_5Pm54iiBLzK_TAHaE8?cb=iwc2&rs=1&pid=ImgDetMain"),
            Quad("Jalón al pecho", "Espalda", "Ejercicio en polea para dorsal ancho, alternativa a dominadas.", "https://blogdasaude.com.br/wp-content/uploads/ecxercicios-1024x683.jpg"),
            Quad("Remo con barra", "Espalda", "Ejercicio para espaldas gruesas con barra.", "https://www.soypowerlifter.com/wp-content/uploads/2020/12/remo-barra_750.jpg"),
            Quad("Pullover", "Espalda", "Ejercicio para expandir la caja torácica y trabajar dorsal.", "https://www.coachmagazine.fr/wp-content/uploads/2022/11/pull-over-poulie-haute-exercice.jpeg"),

            // Bíceps
            Quad("Curl martillo", "Bíceps", "Curl con agarre neutro para braquiorradial y bíceps.", "https://th.bing.com/th/id/R.1053e25858468d0a7d4c8444605d865f?rik=cwBReOlAYdhGUA&pid=ImgRaw&r=0"),
            Quad("Curl con barra", "Bíceps", "Curl clásico para trabajar el bíceps braquial.", "https://th.bing.com/th/id/OIP.QI2Fj4Zf2jC40P890zmKowHaE9?cb=iwc2&rs=1&pid=ImgDetMain"),
            Quad("Curl predicador", "Bíceps", "Curl enfocado para aislar el bíceps y evitar balanceo.", "https://th.bing.com/th/id/R.5dc805f79bb56562b8c667af77dfee1b?rik=7TUGAJe49KLR1A&riu=http%3a%2f%2ffitness-guia.es%2fwp-content%2fuploads%2f2017%2f11%2fcurl-predicador-1.jpg&ehk=d0mXkJlZjEugiFw5XTsI1XORtX%2brmpRmGd%2bWbAJ13mc%3d&risl=&pid=ImgRaw&r=0"),

            // Tríceps
            Quad("Extensión de tríceps", "Tríceps", "Ejercicio para trabajar la cabeza larga del tríceps.", "https://muscu-street-et-crossfit.fr/wp-content/uploads/2022/08/Muscles-extension-triceps-poulie.001.jpeg"),
            Quad("Press francés con barra", "Tríceps", "Ejercicio para desarrollar el tríceps con barra.", "https://th.bing.com/th/id/R.27b75e552d8ba3c7db2204e3d8c3257b?rik=2VK%2bF2xhjlIASg&pid=ImgRaw&r=0"),
            Quad("Press de banca cerrado", "Tríceps", "Variante del press para enfatizar tríceps.", "https://th.bing.com/th/id/OIP._vGVnTSvJzPWB-v8F_CrUAAAAA?cb=iwc2&rs=1&pid=ImgDetMain"),

            // Hombro
            Quad("Press militar con mancuernas", "Hombro", "Press para deltoides anterior con mancuernas.", "https://th.bing.com/th/id/OIP.XUUqDUD7Yy4GwwDOVsi0rAHaE8?cb=iwc2&rs=1&pid=ImgDetMain"),
            Quad("Elevaciones laterales", "Hombro", "Ejercicio para deltoides medio y definición.", "https://nutricion360.es/wp-content/uploads/2020/05/elevaciones-laterales.jpg"),
            Quad("Face Pulls", "Hombro", "Ejercicio para deltoides posteriores y estabilidad escapular.", "https://th.bing.com/th/id/OIP.d_uCaVnTZokq7NR3QqaSowAAAA?cb=iwc2&rs=1&pid=ImgDetMain"),

            // Cuádriceps
            Quad("Sentadilla", "Cuádriceps", "Ejercicio básico para cuádriceps y glúteos.", "https://www.musculosespartanos.com/wp-content/uploads/2020/05/sentadilla.jpg"),
            Quad("Extensión de cuadríceps", "Cuádriceps", "Ejercicio de aislamiento para el cuádriceps.", "https://th.bing.com/th/id/OIP.qAm6poY-uJg08-t8R5PR-QHaE0?cb=iwc2&rs=1&pid=ImgDetMain"),
            Quad("Sentadilla búlgara", "Cuádriceps", "Sentadilla unilateral para fuerza y equilibrio.", "https://th.bing.com/th/id/OIP.itCpaP6MQj_3Z3NJJNdVrgHaEK?cb=iwc2&rs=1&pid=ImgDetMain"),
            Quad("Hack squat", "Cuádriceps", "Variante de sentadilla para enfocar cuádriceps.", "https://images.ctfassets.net/ipjoepkmtnha/2l8CseR7fkJEsa2f4mJ0rY/61dd873935a9b0f68693e935acd802d5/pure_hack_squat_mainfeature03.jpg"),

            // Femoral
            Quad("Peso muerto rumano", "Femoral", "Ejercicio para isquiotibiales y glúteos.", "https://static.strengthlevel.com/images/exercises/romanian-deadlift/romanian-deadlift-400.jpg"),
            Quad("Curl de piernas sentado en máquina", "Femoral", "Ejercicio de aislamiento para femoral.", "https://th.bing.com/th/id/R.535284f646acb60a422bc6044c6addf5?rik=wvHRtahDqv9Ewg&pid=ImgRaw&r=0"),
            Quad("Curl de piernas tumbado", "Femoral", "Otro ejercicio para femoral tumbado.", "https://th.bing.com/th/id/OIP.8coWBpjFxg1sDC_ZDMI2lwHaEg?cb=iwc2&rs=1&pid=ImgDetMain"),
            Quad("Hip thrust", "Femoral", "Ejercicio para glúteos con énfasis en femoral.", "https://images.ctfassets.net/0k812o62ndtw/5vSUqzr8O6ykILEmptc77H/fa9597c292288971280bf6a5193d06c3/Untitled_design___2023_01_25T155249.550_en14bebe8fe09ff6f5dd673cf9b6807e07.jpg"),

            // Gemelos
            Quad("Elevación de talones de pie", "Gemelos", "Ejercicio para gemelos con peso corporal o barra.", "https://i.blogs.es/310362/elevacion2/1366_2000.jpg"),
            Quad("Elevación de talones sentado", "Gemelos", "Ejercicio enfocado en el sóleo.", "https://s3assets.skimble.com/assets/1496307/image_iphone.jpg"),

            // Abdominales
            Quad("Crunch abdominal", "Abdominales", "Ejercicio clásico para la zona abdominal.", "https://th.bing.com/th/id/OIP.OmlyMVLC67UBEm8HlXM0WwHaEK?rs=1&pid=ImgDetMain"),
            Quad("Mountain climbers", "Abdominales", "Ejercicio dinámico para core y cardio.", "https://blogscdn.thehut.net/app/uploads/sites/450/2021/03/shutterstock_1457872541opt_hero_1616774408_1617011419.jpg")
        )

        for ((name, group, description, imageUri) in ejercicios) {
            db.execSQL(
                "INSERT INTO exercises (name, muscleGroup, description, imageUri) VALUES (?, ?, ?, ?)",
                arrayOf(name, group, description, imageUri)
            )
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            // Borra la tabla y la crea de nuevo con los datos por defecto
            db.execSQL("DROP TABLE IF EXISTS exercises")
            onCreate(db)
        }
    }

    companion object {
        const val DATABASE_NAME = "exercises.db"
        const val DATABASE_VERSION = 4  // Subimos a 3 para forzar upgrade
    }

    // Usamos esta data class interna para mejor legibilidad de los datos
    private data class Quad(val name: String, val group: String, val description: String, val imageUri: String)
}
