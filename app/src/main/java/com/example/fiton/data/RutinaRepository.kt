package com.example.fiton.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

class RutinaRepository(context: Context) {

    private val dbHelper = RutinaDbHelper(context)

    @RequiresApi(Build.VERSION_CODES.O)
    fun insertarRutina(nombre: String, diaSemana: Int, fecha: LocalDate): Long {
        return dbHelper.insertRutina(nombre, diaSemana, fecha)
    }

    fun insertarRutinaEjercicio(
        rutinaId: Long,
        ejercicioId: Long,
        repeticiones: Int,
        series: Int,
        anotaciones: String?
    ): Long {
        return dbHelper.insertRutinaEjercicio(rutinaId, ejercicioId, repeticiones, series, anotaciones)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerRutinas(): List<RutinaModels.Rutina> {
        return dbHelper.getRutinas()
    }

    fun obtenerEjerciciosPorRutina(rutinaId: Long): List<RutinaModels.RutinaEjercicio> {
        return dbHelper.getEjerciciosPorRutina(rutinaId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerRutinaPorId(id: Long): RutinaModels.Rutina? {
        return dbHelper.getRutinaById(id)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun actualizarRutina(id: Long, nombre: String, diaSemana: Int, fecha: LocalDate): Boolean {
        return dbHelper.updateRutina(id, nombre, diaSemana, fecha)
    }

    fun actualizarEjercicioDeRutina(
        rutinaId: Long,
        ejercicioId: Long,
        repeticiones: Int,
        series: Int,
        anotaciones: String?
    ): Boolean {
        return dbHelper.updateRutinaEjercicio(rutinaId, ejercicioId, repeticiones, series, anotaciones)
    }

    fun eliminarEjercicioDeRutina(rutinaId: Long, ejercicioId: Long): Boolean {
        return dbHelper.deleteEjercicioDeRutina(rutinaId, ejercicioId)
    }

    fun eliminarRutina(id: Long): Boolean {
        return dbHelper.deleteRutina(id)
    }
}
