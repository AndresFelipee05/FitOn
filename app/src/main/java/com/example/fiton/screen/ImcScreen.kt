package com.example.fiton.screen

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlin.math.*
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun ImcScreen(navController: NavController) {
    var altura by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var imc by remember { mutableStateOf<Float?>(null) }
    var mostrarResultado by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    val dialogTitleTextSize = when {
        screenWidthDp < 360.dp -> 20.sp
        screenWidthDp < 480.dp -> 24.sp
        else -> 28.sp
    }

    val dialogContentTextSize = when {
        screenWidthDp < 360.dp -> 12.sp
        screenWidthDp < 480.dp -> 14.sp
        else -> 16.sp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Contenido principal con padding bottom para evitar que se solape con los botones
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 100.dp) // Bottom padding para botones
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Calculadora de IMC",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = altura,
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("^\\d{0,3}$"))) {
                        altura = newValue
                    }
                    mostrarResultado = false
                    mostrarDialogo = false
                },
                label = { Text("Altura (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = peso,
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))) {
                        peso = newValue
                    }
                    mostrarResultado = false
                    mostrarDialogo = false
                },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            ImcGraficoSemicirculo(imc = imc, mostrarAnimacion = mostrarResultado)

            Spacer(modifier = Modifier.height(50.dp))

            val context = LocalContext.current
            Text(
                text = "¿Qué es el IMC?",
                color = Color(0xFFFF9800),
                fontSize = 17.sp,
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tuasaude.com/es/imc/"))
                    context.startActivity(intent)
                }
            )
        }

        // Row con botones fijos en la parte inferior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier.width(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            FloatingActionButton(
                onClick = {
                    if (altura.isNotEmpty() && peso.isNotEmpty()) {
                        val alturaFloat = altura.toFloatOrNull()
                        val pesoFloat = peso.toFloatOrNull()

                        if (alturaFloat != null && pesoFloat != null && alturaFloat > 0) {
                            val alturaMetros = alturaFloat / 100f
                            imc = pesoFloat / (alturaMetros * alturaMetros)
                            mostrarResultado = true
                            mostrarDialogo = false
                        } else {
                            mostrarResultado = false
                            mostrarDialogo = false
                            imc = null
                        }
                    }
                },
                containerColor = if (altura.isNotEmpty() && peso.isNotEmpty()) {
                    Color(0xFFFF9800)
                } else {
                    Color(0xFFFF9800).copy(alpha = 0.2f)
                },
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = "Calcular",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }

    LaunchedEffect(mostrarResultado) {
        if (mostrarResultado && imc != null) {
            delay(2000)
            mostrarDialogo = true
        }
    }

    if (mostrarDialogo && imc != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            confirmButton = {},
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Resultado de IMC",
                        fontWeight = FontWeight.Bold,
                        fontSize = dialogTitleTextSize,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable { mostrarDialogo = false }
                            .padding(top = 2.dp, end = 2.dp),
                        tint = Color.Gray
                    )
                }
            },
            text = {
                ImcResultadoDetallado(
                    imc = imc!!,
                    altura = altura.toFloatOrNull() ?: 0f,
                    contentFontSize = dialogContentTextSize
                )
            }
        )
    }
}


@Composable
fun ImcResultadoDetallado(imc: Float, altura: Float, contentFontSize: TextUnit) {
    val alturaMetros = altura / 100f
    val pesoMinimo = 18.5f * (alturaMetros * alturaMetros)
    val pesoMaximo = 24.9f * (alturaMetros * alturaMetros)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Categoría",
                    fontWeight = FontWeight.Bold,
                    fontSize = contentFontSize
                )

                Spacer(modifier = Modifier.height(8.dp))
                val categoria = when {
                    imc <= 15.9f -> "Delgadez muy extrema"
                    imc <= 16.9f -> "Delgadez extrema"
                    imc <= 18.4f -> "Delgadez"
                    imc <= 24.9f -> "Normal"
                    imc <= 29.9f -> "Sobrepeso"
                    imc <= 34.9f -> "Obesidad grado 1"
                    imc <= 39.9f -> "Obesidad grado 2"
                    else -> "Obesidad grado 3"
                }

                Text(
                    text = categoria,
                    fontSize = contentFontSize,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        imc <= 15.9f -> Color(0xFF9C27B0)
                        imc <= 16.9f -> Color(0xFF673AB7)
                        imc <= 18.4f -> Color(0xFF2196F3)
                        imc <= 24.9f -> Color(0xFF4CAF50)
                        imc <= 29.9f -> Color(0xFFFF9800)
                        imc <= 34.9f -> Color(0xFFF44336)
                        imc <= 39.9f -> Color(0xFFD32F2F)
                        else -> Color(0xFFB71C1C)
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                val categorias = listOf(
                    "Delgadez muy extrema",
                    "Delgadez extrema",
                    "Delgadez",
                    "Normal",
                    "Sobrepeso",
                    "Obesidad grado 1",
                    "Obesidad grado 2",
                    "Obesidad grado 3"
                )

                categorias.forEach { cat ->
                    Text(
                        text = cat,
                            fontSize = contentFontSize, // Reducido de 13sp a 12sp
                        color = if (cat == categoria) Color.Yellow else Color.LightGray,
                        fontWeight = if (cat == categoria) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 1.dp), // Reducido de 2dp a 1dp
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Diferencia",
                    fontWeight = FontWeight.Bold,
                    fontSize = contentFontSize
                )

                Spacer(modifier = Modifier.height(8.dp))

                val pesoActual = imc * (alturaMetros * alturaMetros)

                val ajusteTexto = when {
                    imc in 18.5f..24.9f -> "Peso ideal"
                    imc < 18.5f -> {
                        val pesoMinimoNormal = 18.5f * (alturaMetros * alturaMetros)
                        val diferencia = pesoActual - pesoMinimoNormal
                        "${String.format("%.1f", diferencia)} kg"
                    }
                    else -> {
                        val pesoMaximoNormal = 24.9f * (alturaMetros * alturaMetros)
                        val diferencia = pesoActual - pesoMaximoNormal
                        "+${String.format("%.1f", diferencia)} kg"
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ajusteTexto,
                        fontWeight = FontWeight.Bold,
                        fontSize = contentFontSize,
                        color = when {
                            imc in 18.5f..24.9f -> Color(0xFF4CAF50)
                            imc < 18.5f -> Color(0xFF2196F3)
                            else -> Color(0xFFF44336)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val rangos = listOf(
                    "≤ 15.9",
                    "16.0-16.9",
                    "17.0-18.4",
                    "18.5-24.9",
                    "25.0-29.9",
                    "30.0-34.9",
                    "35.0-39.9",
                    "≥ 40.0"
                )

                val rangoActual = when {
                    imc <= 15.9f -> "≤ 15.9"
                    imc <= 16.9f -> "16.0-16.9"
                    imc <= 18.4f -> "17.0-18.4"
                    imc <= 24.9f -> "18.5-24.9"
                    imc <= 29.9f -> "25.0-29.9"
                    imc <= 34.9f -> "30.0-34.9"
                    imc <= 39.9f -> "35.0-39.9"
                    else -> "≥ 40.0"
                }

                rangos.forEach { rango ->
                    Text(
                        text = rango,
                        fontSize = contentFontSize, // Reducido de 14sp a 12sp
                        color = if (rango == rangoActual) Color.Yellow else Color.LightGray,
                        fontWeight = if (rango == rangoActual) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 1.dp), // Reducido de 2dp a 1dp
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider(
            color = Color.LightGray.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Peso normal:",
                    fontWeight = FontWeight.Bold,
                    fontSize = contentFontSize,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${String.format("%.1f", pesoMinimo)} - ${
                        String.format(
                            "%.1f",
                            pesoMaximo
                        )
                    } kg",
                    fontWeight = FontWeight.Bold,
                    fontSize = contentFontSize, // Reducido de 16sp a 14sp
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ImcGraficoSemicirculo(imc: Float?, mostrarAnimacion: Boolean) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (mostrarAnimacion && imc != null) 1f else 0f,
        animationSpec = tween(
            durationMillis = 2000,
            easing = FastOutSlowInEasing
        ),
        label = "IMC Progress"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val radius = (canvasWidth * 0.8f) / 2f
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight - 20.dp.toPx()
                val strokeWidth = 35.dp.toPx()

                val startAngle = 180f
                val totalSweep = 180f
                val sectionSweep = totalSweep / 3f

                // Dibujar arco "Poco peso" (Azul)
                drawArc(
                    color = Color(0xFF2196F3),
                    startAngle = startAngle,
                    sweepAngle = sectionSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Dibujar arco "Normal" (Verde)
                drawArc(
                    color = Color(0xFF4CAF50),
                    startAngle = startAngle + sectionSweep,
                    sweepAngle = sectionSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Dibujar arco "Sobrepeso" (Rojo)
                drawArc(
                    color = Color(0xFFF44336),
                    startAngle = startAngle + (sectionSweep * 2),
                    sweepAngle = sectionSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )

                // Dibujar el indicador animado
                if (mostrarAnimacion && imc != null && animatedProgress > 0f) {
                    val targetAngle = when {
                        imc < 18.5f -> {
                            val progress = ((imc - 16f) / (18.5f - 16f)).coerceIn(0f, 1f)
                            180f - (progress * sectionSweep)
                        }

                        imc < 25f -> {
                            val progress = ((imc - 18.5f) / (25f - 18.5f)).coerceIn(0f, 1f)
                            120f - (progress * sectionSweep)
                        }

                        else -> {
                            val progress = ((imc - 25f) / (40f - 25f)).coerceIn(0f, 1f)
                            60f - (progress * sectionSweep)
                        }
                    }

                    val currentAngle = 180f - ((180f - targetAngle) * animatedProgress)
                    val angleRad = Math.toRadians(currentAngle.toDouble())
                    val circleRadius = radius - strokeWidth / 2f

                    val circleX = centerX + cos(angleRad).toFloat() * circleRadius
                    val circleY = centerY - sin(angleRad).toFloat() * circleRadius

                    drawCircle(
                        color = Color.White,
                        radius = 14.dp.toPx(),
                        center = Offset(circleX, circleY)
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 14.dp.toPx(),
                        center = Offset(circleX, circleY),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    val arrowLength = 18.dp.toPx()
                    val arrowAngle = angleRad - Math.PI / 2

                    val arrowEndX = circleX + cos(arrowAngle).toFloat() * arrowLength
                    val arrowEndY = circleY - sin(arrowAngle).toFloat() * arrowLength

                    drawLine(
                        color = Color.Black,
                        start = Offset(circleX, circleY),
                        end = Offset(arrowEndX, arrowEndY),
                        strokeWidth = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Mostrar el valor del IMC en el centro del gráfico
            if (imc != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = 30.dp) // Ajuste para posicionar debajo del centro
                ) {
                    Text(
                        text = "IMC",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = String.format("%.1f", imc),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            imc <= 15.9f -> Color(0xFF9C27B0)
                            imc <= 16.9f -> Color(0xFF673AB7)
                            imc <= 18.4f -> Color(0xFF2196F3)
                            imc <= 24.9f -> Color(0xFF4CAF50)
                            imc <= 29.9f -> Color(0xFFFF9800)
                            imc <= 34.9f -> Color(0xFFF44336)
                            imc <= 39.9f -> Color(0xFFD32F2F)
                            else -> Color(0xFFB71C1C)
                        }
                    )
                }
            }

            // Números de límites
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val radius = (canvasWidth * 0.8f) / 2f
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight - 20.dp.toPx()
                val sectionSweep = 60f
                val textRadius = radius + 40.dp.toPx()

                // Número 16.0
                val angle16 = Math.toRadians(180.0)
                val x16 = centerX + cos(angle16).toFloat() * textRadius
                val y16 = centerY - sin(angle16).toFloat() * textRadius

                drawContext.canvas.nativeCanvas.drawText(
                    "16.0",
                    x16,
                    y16 + 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 16.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                )

                // Número 18.5
                val angle18_5 = Math.toRadians((180 - sectionSweep).toDouble())
                val x18_5 = centerX + cos(angle18_5).toFloat() * textRadius
                val y18_5 = centerY - sin(angle18_5).toFloat() * textRadius

                drawContext.canvas.nativeCanvas.drawText(
                    "18.5",
                    x18_5,
                    y18_5 + 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 16.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                )

                // Número 25.0
                val angle25 = Math.toRadians((180 - sectionSweep * 2).toDouble())
                val x25 = centerX + cos(angle25).toFloat() * textRadius
                val y25 = centerY - sin(angle25).toFloat() * textRadius

                drawContext.canvas.nativeCanvas.drawText(
                    "25.0",
                    x25,
                    y25 + 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 16.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                )

                // Número 40.0
                val angle40 = Math.toRadians(0.0)
                val x40 = centerX + cos(angle40).toFloat() * textRadius
                val y40 = centerY - sin(angle40).toFloat() * textRadius

                drawContext.canvas.nativeCanvas.drawText(
                    "40.0",
                    x40,
                    y40 + 5.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 16.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                )
            }
        }

        // Etiquetas centradas respecto a cada sección del gráfico
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            // Poco peso
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Poco peso",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2196F3)
                )
            }

            // Normal
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .fillMaxHeight()
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Normal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4CAF50)
                )
            }

            // Sobrepeso
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sobrepeso",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}