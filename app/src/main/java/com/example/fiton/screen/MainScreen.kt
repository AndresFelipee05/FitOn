import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fiton.R
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TipData(val imageResId: Int, val titulo: String, val tip: String, val link: String)
data class MuscleGroup(val imageResId: Int, val name: String)

fun <T> List<T>.chunkedByThree(): List<List<T>> {
    return this.chunked(3)
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainScreen(navController: NavController, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Dimensiones responsivas basadas en el tamaño de pantalla
    val tipSlideHeight = (screenHeight * 0.3f).coerceAtLeast(200.dp).coerceAtMost(350.dp)
    val imageSize = (screenWidth * 0.35f).coerceAtLeast(120.dp).coerceAtMost(180.dp)
    val muscleImageSize = (screenWidth * 0.25f).coerceAtLeast(100.dp).coerceAtMost(140.dp)
    val buttonWidth = (screenWidth * 0.25f).coerceAtLeast(100.dp).coerceAtMost(130.dp)
    val buttonHeight = (screenHeight * 0.07f).coerceAtLeast(50.dp).coerceAtMost(70.dp)

    // Tamaños de texto responsivos
    val titleTextSize = when {
        screenWidth < 360.dp -> 20.sp
        screenWidth < 480.dp -> 24.sp
        else -> 26.sp
    }

    val tipTextSize = when {
        screenWidth < 360.dp -> 13.sp
        screenWidth < 480.dp -> 14.sp
        else -> 16.sp
    }

    val tips = listOf(
        TipData(
            R.drawable.agua,
            "Hidratación",
            "El cuerpo necesita suficiente agua para regular la temperatura y llevar nutrientes.",
            "https://fundaciondelcorazon.com/blog-impulso-vital/2275-hidratacion-ejercicio-fisico.html"
        ),
        TipData(
            R.drawable.calentar,
            "Calentamiento",
            "Calentar prepara las articulaciones y músculos para moverse con seguridad y eficacia.",
            "https://medlineplus.gov/spanish/ency/patientinstructions/000859.htm"
        ),
        TipData(
            R.drawable.comer,
            "Alimentación",
            "Comer bien te da energía para rendir mejor y recuperarte más rápido después del ejercicio.",
            "https://www.alianzateam.com/pilares-de-dieta-balanceada/"
        ),
        TipData(
            R.drawable.descansar,
            "Recuperación",
            "El descanso es esencial para que el cuerpo repare tejidos, evite lesiones y mejore el rendimiento.",
            "https://cienciasdeportivas.com/descanso-para-deportistas/"
        )
    )

    val muscleGroups = listOf(
        MuscleGroup(R.drawable.pecho, "Pecho"),
        MuscleGroup(R.drawable.espalda, "Espalda"),
        MuscleGroup(R.drawable.biceps, "Bíceps"),
        MuscleGroup(R.drawable.triceps, "Tríceps"),
        MuscleGroup(R.drawable.hombro, "Hombro"),
        MuscleGroup(R.drawable.cuadriceps, "Cuádriceps"),
        MuscleGroup(R.drawable.femoral, "Femoral"),
        MuscleGroup(R.drawable.gemelos, "Gemelos"),
        MuscleGroup(R.drawable.abdominales, "Abdominales")
    )

    val realPageCount = tips.size
    val virtualPageCount = Int.MAX_VALUE
    val initialPage = Int.MAX_VALUE / 2

    val pagerState = rememberPagerState(initialPage = initialPage)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(7000)
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Carrusel de tips con altura responsiva
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tipSlideHeight)
                .background(Color(0xFF2C2C2C))
                .padding(8.dp)
        ) {
            HorizontalPager(
                count = virtualPageCount,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val realIndex = page % realPageCount
                val tip = tips[realIndex]
                FitnessTipSlide(
                    tip.imageResId,
                    tip.tip,
                    tip.link,
                    imageSize = imageSize,
                    tipTextSize = tipTextSize
                )
            }
        }

        // Indicadores del carrusel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val currentIndex = pagerState.currentPage % realPageCount
            repeat(realPageCount) { index ->
                val isSelected = index == currentIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 9.dp else 8.dp)
                        .background(
                            color = if (isSelected) Color(0xFFFF9800) else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable {
                            val basePage =
                                pagerState.currentPage - (pagerState.currentPage % realPageCount)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(basePage + index)
                            }
                        }
                )
            }
        }

        // Título + "Ver todos"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grupos musculares",
                fontSize = titleTextSize,
                color = Color.White
            )

            Text(
                text = "Ver todos",
                fontSize = (titleTextSize.value * 0.54f).sp,
                color = Color(0xFFFF9800),
                modifier = Modifier.clickable {
                    navController.navigate("search")
                }
            )
        }

        // Carrusel de grupos musculares
        val musclePages = muscleGroups.chunkedByThree()
        HorizontalPager(
            count = musclePages.size,
            contentPadding = PaddingValues(start = 0.dp, end = 42.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) { page ->
            Column(
                modifier = Modifier.width(screenWidth * 0.85f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                musclePages[page].forEach { muscle ->
                    MuscleGroupItem(
                        muscle = muscle,
                        navController = navController,
                        imageSize = muscleImageSize,
                        screenWidth = screenWidth
                    )
                }
            }
        }

        // Botones inferiores responsivos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("create_routine_screen") },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight)
                    .weight(1f)
            ) {
                Text(
                    text = "Crear rutina",
                    color = Color.White,
                    fontSize = (titleTextSize.value * 0.5f).sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = { navController.navigate("routines_screen") },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight)
                    .weight(1f)
            ) {
                Text(
                    text = "Modificar rutina",
                    color = Color.White,
                    fontSize = (titleTextSize.value * 0.5f).sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = { menuExpanded = true },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier
                    .width(buttonWidth * 0.8f)
                    .height(buttonHeight)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Otras opciones",
                        tint = Color.White,
                        modifier = Modifier.size((buttonHeight.value * 0.4f).dp)
                    )
                }
            }
        }

        // Dialog del menú
        if (menuExpanded) {
            AlertDialog(
                onDismissRequest = { menuExpanded = false },
                confirmButton = {},
                dismissButton = {},
                containerColor = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(16.dp),
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Selecciona una opción",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = (titleTextSize.value * 0.8f).sp,
                                color = Color.White
                            )
                        )
                        IconButton(
                            onClick = { menuExpanded = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.LightGray
                            )
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                menuExpanded = false
                                navController.navigate("imc_screen")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Text(
                                text = "Calcular IMC",
                                fontSize = (titleTextSize.value * 0.65f).sp,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                menuExpanded = false
                                navController.navigate("ver_rendimiento")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Text(
                                text = "Ver rendimiento",
                                fontSize = (titleTextSize.value * 0.65f).sp,
                                color = Color.White
                            )
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun FitnessTipSlide(
    imageResId: Int,
    tip: String,
    link: String,
    imageSize: Dp,
    tipTextSize: androidx.compose.ui.unit.TextUnit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Contenido principal que puede expandirse o contraerse
        Column(
            modifier = Modifier
                .weight(1f) // Ocupa el espacio disponible pero se ajusta
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(imageSize)
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                )

                Text(
                    text = tip,
                    fontSize = tipTextSize,
                    color = Color(0xFFD6D6D6),
                    textAlign = TextAlign.Center,
                    maxLines = 6, // Limita las líneas para evitar texto excesivo
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 15.dp)
                )
            }
        }

        // Botón siempre visible en la parte inferior
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 8.dp)
        ) {
            Text(
                text = "Leer más",
                color = Color.White,
                fontSize = (tipTextSize.value * 0.9f).sp
            )
        }
    }
}


@Composable
fun MuscleGroupItem(
    muscle: MuscleGroup,
    navController: NavController,
    imageSize: androidx.compose.ui.unit.Dp,
    screenWidth: androidx.compose.ui.unit.Dp
) {
    val textSize = when {
        screenWidth < 360.dp -> 16.sp
        screenWidth < 480.dp -> 17.sp
        else -> 18.sp
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("exercises/${muscle.name}")
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(imageSize)
                .background(Color.Gray)
        ) {
            Image(
                painter = painterResource(id = muscle.imageResId),
                contentDescription = muscle.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = muscle.name,
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}