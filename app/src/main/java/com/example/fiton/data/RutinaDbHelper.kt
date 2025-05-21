package com.example.fiton.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RutinaDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "fiton_rutinas.db"
        private const val DATABASE_VERSION = 1

        // Tabla Rutinas
        private const val TABLE_RUTINAS = "rutinas"
        private const val COL_RUTINA_ID = "id"
        private const val COL_RUTINA_NOMBRE = "nombre"
        private const val COL_RUTINA_DIA_SEMANA = "dia_semana"
        private const val COL_RUTINA_FECHA = "fecha"

        // Tabla Rutina_Ejercicios (tabla relacional)
        private const val TABLE_RUTINA_EJERCICIOS = "rutina_ejercicios"
        private const val COL_RUTINA_EJERCICIO_ID = "id"
        private const val COL_RUTINA_EJERCICIO_RUTINA_ID = "rutina_id"
        private const val COL_RUTINA_EJERCICIO_EJERCICIO_ID = "ejercicio_id"
        private const val COL_RUTINA_EJERCICIO_REPETICIONES = "repeticiones"
        private const val COL_RUTINA_EJERCICIO_SERIES = "series"
        private const val COL_RUTINA_EJERCICIO_PESO = "peso"
        private const val COL_RUTINA_EJERCICIO_ANOTACIONES = "anotaciones"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla Rutinas
        val createRutinasTable = """
            CREATE TABLE $TABLE_RUTINAS (
                $COL_RUTINA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_RUTINA_NOMBRE TEXT NOT NULL,
                $COL_RUTINA_DIA_SEMANA INTEGER NOT NULL,
                $COL_RUTINA_FECHA TEXT NOT NULL
            )
        """.trimIndent()

        // Crear tabla Rutina_Ejercicios
        val createRutinaEjerciciosTable = """
            CREATE TABLE $TABLE_RUTINA_EJERCICIOS (
                $COL_RUTINA_EJERCICIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_RUTINA_EJERCICIO_RUTINA_ID INTEGER NOT NULL,
                $COL_RUTINA_EJERCICIO_EJERCICIO_ID INTEGER NOT NULL,
                $COL_RUTINA_EJERCICIO_REPETICIONES INTEGER NOT NULL,
                $COL_RUTINA_EJERCICIO_SERIES INTEGER NOT NULL,
                $COL_RUTINA_EJERCICIO_PESO REAL NOT NULL DEFAULT 0,
                $COL_RUTINA_EJERCICIO_ANOTACIONES TEXT,
                FOREIGN KEY($COL_RUTINA_EJERCICIO_RUTINA_ID) REFERENCES $TABLE_RUTINAS($COL_RUTINA_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createRutinasTable)
        db.execSQL(createRutinaEjerciciosTable)
    }

    // Añadir este método para habilitar las restricciones de clave foránea
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        // Habilitar la restricción de clave foránea
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys=ON")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En caso de actualización de la base de datos
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RUTINA_EJERCICIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RUTINAS")
        onCreate(db)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertRutina(nombre: String, diaSemana: Int, fecha: LocalDate): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RUTINA_NOMBRE, nombre)
            put(COL_RUTINA_DIA_SEMANA, diaSemana)
            put(COL_RUTINA_FECHA, fecha.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
        return db.insert(TABLE_RUTINAS, null, values)
    }

    fun insertRutinaEjercicio(
        rutinaId: Long,
        ejercicioId: Long,
        repeticiones: Int,
        series: Int,
        peso: Double = 0.0,
        anotaciones: String? = null
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RUTINA_EJERCICIO_RUTINA_ID, rutinaId)
            put(COL_RUTINA_EJERCICIO_EJERCICIO_ID, ejercicioId)
            put(COL_RUTINA_EJERCICIO_REPETICIONES, repeticiones)
            put(COL_RUTINA_EJERCICIO_SERIES, series)
            put(COL_RUTINA_EJERCICIO_PESO, peso)
            put(COL_RUTINA_EJERCICIO_ANOTACIONES, anotaciones)
        }
        return db.insert(TABLE_RUTINA_EJERCICIOS, null, values)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRutinas(): List<RutinaModels.Rutina> {
        val rutinas = mutableListOf<RutinaModels.Rutina>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_RUTINAS,
            null,
            null,
            null,
            null,
            null,
            "$COL_RUTINA_DIA_SEMANA ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COL_RUTINA_ID))
                val nombre = getString(getColumnIndexOrThrow(COL_RUTINA_NOMBRE))
                val diaSemana = getInt(getColumnIndexOrThrow(COL_RUTINA_DIA_SEMANA))
                val fechaStr = getString(getColumnIndexOrThrow(COL_RUTINA_FECHA))
                val fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE)

                rutinas.add(RutinaModels.Rutina(id, nombre, diaSemana, fecha))
            }
        }
        cursor.close()
        return rutinas
    }

    fun getEjerciciosPorRutina(rutinaId: Long): List<RutinaModels.RutinaEjercicio> {
        val ejercicios = mutableListOf<RutinaModels.RutinaEjercicio>()
        val db = this.readableDatabase
        val selection = "$COL_RUTINA_EJERCICIO_RUTINA_ID = ?"
        val selectionArgs = arrayOf(rutinaId.toString())
        val cursor = db.query(
            TABLE_RUTINA_EJERCICIOS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_ID))
                val ejercicioId = getLong(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_EJERCICIO_ID))
                val repeticiones = getInt(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_REPETICIONES))
                val series = getInt(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_SERIES))
                val peso = getDouble(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_PESO))
                val anotaciones = getString(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_ANOTACIONES))

                ejercicios.add(
                    RutinaModels.RutinaEjercicio(
                        id, rutinaId, ejercicioId, repeticiones, series, peso, anotaciones
                    )
                )
            }
        }
        cursor.close()
        return ejercicios
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRutinaById(id: Long): RutinaModels.Rutina? {
        val db = this.readableDatabase
        val selection = "$COL_RUTINA_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor = db.query(
            TABLE_RUTINAS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var rutina: RutinaModels.Rutina? = null
        with(cursor) {
            if (moveToFirst()) {
                val nombre = getString(getColumnIndexOrThrow(COL_RUTINA_NOMBRE))
                val diaSemana = getInt(getColumnIndexOrThrow(COL_RUTINA_DIA_SEMANA))
                val fechaStr = getString(getColumnIndexOrThrow(COL_RUTINA_FECHA))
                val fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE)

                rutina = RutinaModels.Rutina(id, nombre, diaSemana, fecha)
            }
        }
        cursor.close()
        return rutina
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateRutina(id: Long, nombre: String, diaSemana: Int, fecha: LocalDate): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RUTINA_NOMBRE, nombre)
            put(COL_RUTINA_DIA_SEMANA, diaSemana)
            put(COL_RUTINA_FECHA, fecha.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }

        val selection = "$COL_RUTINA_ID = ?"
        val selectionArgs = arrayOf(id.toString())

        return db.update(TABLE_RUTINAS, values, selection, selectionArgs) > 0
    }

    fun updateRutinaEjercicio(
        id: Long,
        repeticiones: Int,
        series: Int,
        peso: Double,
        anotaciones: String?
    ): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RUTINA_EJERCICIO_REPETICIONES, repeticiones)
            put(COL_RUTINA_EJERCICIO_SERIES, series)
            put(COL_RUTINA_EJERCICIO_PESO, peso)
            put(COL_RUTINA_EJERCICIO_ANOTACIONES, anotaciones)
        }

        val selection = "$COL_RUTINA_EJERCICIO_ID = ?"
        val selectionArgs = arrayOf(id.toString())

        return db.update(TABLE_RUTINA_EJERCICIOS, values, selection, selectionArgs) > 0
    }

    // Método para buscar un ejercicio específico en una rutina
    fun getRutinaEjercicio(rutinaId: Long, ejercicioId: Long): RutinaModels.RutinaEjercicio? {
        val db = this.readableDatabase
        val selection = "$COL_RUTINA_EJERCICIO_RUTINA_ID = ? AND $COL_RUTINA_EJERCICIO_EJERCICIO_ID = ?"
        val selectionArgs = arrayOf(rutinaId.toString(), ejercicioId.toString())

        val cursor = db.query(
            TABLE_RUTINA_EJERCICIOS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var rutinaEjercicio: RutinaModels.RutinaEjercicio? = null
        with(cursor) {
            if (moveToFirst()) {
                val id = getLong(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_ID))
                val repeticiones = getInt(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_REPETICIONES))
                val series = getInt(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_SERIES))
                val peso = getDouble(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_PESO))
                val anotaciones = getString(getColumnIndexOrThrow(COL_RUTINA_EJERCICIO_ANOTACIONES))

                rutinaEjercicio = RutinaModels.RutinaEjercicio(
                    id, rutinaId, ejercicioId, repeticiones, series, peso, anotaciones
                )
            }
        }
        cursor.close()
        return rutinaEjercicio
    }

    // Añade un método para actualizar un ejercicio identificándolo por rutinaId y ejercicioId
    fun updateRutinaEjercicioByIds(
        rutinaId: Long,
        ejercicioId: Long,
        repeticiones: Int,
        series: Int,
        peso: Double,
        anotaciones: String?
    ): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_RUTINA_EJERCICIO_REPETICIONES, repeticiones)
            put(COL_RUTINA_EJERCICIO_SERIES, series)
            put(COL_RUTINA_EJERCICIO_PESO, peso)
            put(COL_RUTINA_EJERCICIO_ANOTACIONES, anotaciones)
        }

        val selection = "$COL_RUTINA_EJERCICIO_RUTINA_ID = ? AND $COL_RUTINA_EJERCICIO_EJERCICIO_ID = ?"
        val selectionArgs = arrayOf(rutinaId.toString(), ejercicioId.toString())

        return db.update(TABLE_RUTINA_EJERCICIOS, values, selection, selectionArgs) > 0
    }

    fun deleteEjercicioDeRutina(rutinaId: Long, ejercicioId: Long): Boolean {
        val db = this.writableDatabase
        val selection = "$COL_RUTINA_EJERCICIO_RUTINA_ID = ? AND $COL_RUTINA_EJERCICIO_EJERCICIO_ID = ?"
        val selectionArgs = arrayOf(rutinaId.toString(), ejercicioId.toString())

        return db.delete(TABLE_RUTINA_EJERCICIOS, selection, selectionArgs) > 0
    }

    fun deleteRutina(id: Long): Boolean {
        val db = this.writableDatabase
        val selection = "$COL_RUTINA_ID = ?"
        val selectionArgs = arrayOf(id.toString())

        // Gracias a ON DELETE CASCADE, los ejercicios asociados se eliminarán automáticamente
        return db.delete(TABLE_RUTINAS, selection, selectionArgs) > 0
    }
}