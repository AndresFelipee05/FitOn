package com.example.fiton

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.fiton.ui.theme.FitOnTheme
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.fiton.data.ExerciseDbHelper
import com.example.fiton.data.RutinaDbHelper
import com.example.fiton.navigation.AppNavHost


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar para manejar insets manualmente
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Forzar iconos blancos en la barra de estado
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        val dbHelper = ExerciseDbHelper(this)
        val dbRutinaHelper = RutinaDbHelper(this)

        val db = dbHelper.writableDatabase
        val dbRutina = dbRutinaHelper.writableDatabase

        setContent {
            FitOnTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
