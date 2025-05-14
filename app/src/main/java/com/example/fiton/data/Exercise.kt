package com.example.fiton.data

data class Exercise(
    val id: Long = 0,
    val name: String,
    val description: String,
    val muscleGroup: String,
    val imageUri: String? = null
)
