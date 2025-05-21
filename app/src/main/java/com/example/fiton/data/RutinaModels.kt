package com.example.fiton.data

import java.time.LocalDate

class RutinaModels {

    data class Rutina(
        val id: Long = 0L,            // id autogenerado en la base de datos
        val nombre: String,
        val diaSemana: Int,        // Representa el día de la semana (1 = lunes, 7 = domingo)
        val fecha: LocalDate
    )

    data class RutinaEjercicio(
        val id: Long = 0L,            // id autogenerado en la base de datos
        val rutinaId: Long,           // FK a Rutina.id
        val ejercicioId: Long,        // FK al ejercicio que está en tu base de datos de ejercicios
        val repeticiones: Int,
        val series: Int,
        val peso: Double = 0.0,
        val anotaciones: String? = null
    )
}
