package com.example.fiton.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class RutinaDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE rutinas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                diaSemana INTEGER NOT NULL,
                fecha TEXT NOT NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE rutina_ejercicios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                rutinaId INTEGER NOT NULL,
                ejercicioId INTEGER NOT NULL,
                repeticiones INTEGER NOT NULL,
                series INTEGER NOT NULL,
                anotaciones TEXT,
                FOREIGN KEY (rutinaId) REFERENCES rutinas(id),
                FOREIGN KEY (ejercicioId) REFERENCES exercises(id)
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE rutinas ADD COLUMN fecha TEXT DEFAULT '2025-01-01'")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE rutina_ejercicios ADD COLUMN series INTEGER NOT NULL DEFAULT 3")
        }
    }

    // Insertar rutina, ahora con fecha obligatoria
    fun insertRutina(nombre: String, diaSemana: Int, fecha: LocalDate): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("diaSemana", diaSemana)
            put("fecha", fecha.toString()) // Guardar fecha en formato ISO (yyyy-MM-dd)
        }
        return db.insert("rutinas", null, values)
    }

    // Insertar ejercicio en rutina con repeticiones, series y anotaciones
    fun insertRutinaEjercicio(
        rutinaId: Long,
        ejercicioId: Long,
        repeticiones: Int,
        series: Int,
        anotaciones: String?
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("rutinaId", rutinaId)
            put("ejercicioId", ejercicioId)
            put("repeticiones", repeticiones)
            put("series", series)
            put("anotaciones", anotaciones)
        }
        return db.insert("rutina_ejercicios", null, values)
    }

    // Leer todas las rutinas con la fecha convertida a LocalDate
    @RequiresApi(Build.VERSION_CODES.O)
    fun getRutinas(): List<RutinaModels.Rutina> {
        val rutinas = mutableListOf<RutinaModels.Rutina>()
        val db = readableDatabase
        val cursor: Cursor = db.query("rutinas", null, null, null, null, null, "diaSemana ASC")

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val nombre = getString(getColumnIndexOrThrow("nombre"))
                val diaSemana = getInt(getColumnIndexOrThrow("diaSemana"))
                val fechaStr = getString(getColumnIndexOrThrow("fecha"))
                val fecha = LocalDate.parse(fechaStr)
                rutinas.add(RutinaModels.Rutina(id, nombre, diaSemana, fecha))
            }
        }
        cursor.close()
        return rutinas
    }

    // Leer ejercicios de una rutina específica incluyendo series
    fun getEjerciciosPorRutina(rutinaId: Long): List<RutinaModels.RutinaEjercicio> {
        val ejercicios = mutableListOf<RutinaModels.RutinaEjercicio>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            "rutina_ejercicios",
            null,
            "rutinaId = ?",
            arrayOf(rutinaId.toString()),
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow("id"))
                val rutinaId = getLong(getColumnIndexOrThrow("rutinaId"))
                val ejercicioId = getLong(getColumnIndexOrThrow("ejercicioId"))
                val repeticiones = getInt(getColumnIndexOrThrow("repeticiones"))
                val series = getInt(getColumnIndexOrThrow("series"))
                val anotaciones = getString(getColumnIndexOrThrow("anotaciones"))
                ejercicios.add(RutinaModels.RutinaEjercicio(id, rutinaId, ejercicioId, repeticiones, series, anotaciones))
            }
        }
        cursor.close()
        return ejercicios
    }

    companion object {
        const val DATABASE_NAME = "rutinas.db"
        const val DATABASE_VERSION = 3
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRutinaById(id: Long): RutinaModels.Rutina? {
        val db = readableDatabase
        val cursor = db.query(
            "rutinas",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val nombre = it.getString(it.getColumnIndexOrThrow("nombre"))
                val diaSemana = it.getInt(it.getColumnIndexOrThrow("diaSemana"))
                val fechaStr = it.getString(it.getColumnIndexOrThrow("fecha"))
                val fecha = LocalDate.parse(fechaStr)
                return RutinaModels.Rutina(id, nombre, diaSemana, fecha)
            }
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateRutina(id: Long, nombre: String, diaSemana: Int, fecha: LocalDate): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("diaSemana", diaSemana)
            put("fecha", fecha.toString())
        }
        val rowsAffected = db.update("rutinas", values, "id = ?", arrayOf(id.toString()))
        return rowsAffected > 0
    }

    fun updateRutinaEjercicio(
        rutinaId: Long,
        ejercicioId: Long,
        repeticiones: Int,
        series: Int,
        anotaciones: String?
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("repeticiones", repeticiones)
            put("series", series)
            put("anotaciones", anotaciones)
        }
        val rowsAffected = db.update(
            "rutina_ejercicios",
            values,
            "rutinaId = ? AND ejercicioId = ?",
            arrayOf(rutinaId.toString(), ejercicioId.toString())
        )
        return rowsAffected > 0
    }

    fun deleteEjercicioDeRutina(rutinaId: Long, ejercicioId: Long): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            "rutina_ejercicios",
            "rutinaId = ? AND ejercicioId = ?",
            arrayOf(rutinaId.toString(), ejercicioId.toString())
        )
        return rowsDeleted > 0
    }

    fun deleteRutina(rutinaId: Long): Boolean {
        val db = writableDatabase

        // Eliminar primero los ejercicios asociados a la rutina (clave foránea)
        db.delete("rutina_ejercicios", "rutinaId = ?", arrayOf(rutinaId.toString()))

        // Luego eliminar la rutina
        val rowsDeleted = db.delete("rutinas", "id = ?", arrayOf(rutinaId.toString()))

        return rowsDeleted > 0
    }
}
