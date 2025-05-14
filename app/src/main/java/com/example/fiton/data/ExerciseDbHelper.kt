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
                imageUri TEXT  -- Nueva columna para la imagen
            );
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE exercises ADD COLUMN imageUri TEXT;")  // Añadimos la columna imageUri
        }
    }

    companion object {
        const val DATABASE_NAME = "exercises.db"
        const val DATABASE_VERSION = 2  // Aumentamos la versión
    }
}

