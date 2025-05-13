import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll // Importante para el scroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
    val tips = listOf(
        TipData(
            imageResId = R.drawable.agua,
            titulo = "Hidratación",
            tip = "El cuerpo necesita suficiente agua para regular la temperatura y llevar nutrientes.",
            link = "https://fundaciondelcorazon.com/blog-impulso-vital/2275-hidratacion-ejercicio-fisico.html"
        ),
        TipData(
            imageResId = R.drawable.calentar,
            titulo = "Calentamiento",
            tip = "Calentar prepara las articulaciones y músculos para moverse con seguridad y eficacia.",
            link = "https://medlineplus.gov/spanish/ency/patientinstructions/000859.htm"
        ),
        TipData(
            imageResId = R.drawable.comer,
            titulo = "Alimentación",
            tip = "Comer bien te da energía para rendir mejor y recuperarte más rápido después del ejercicio.",
            link = "https://www.alianzateam.com/pilares-de-dieta-balanceada/"
        ),
        TipData(
            imageResId = R.drawable.descansar,
            titulo = "Recuperación",
            tip = "El descanso es esencial para que el cuerpo repare tejidos, evite lesiones y mejore el rendimiento.",
            link = "https://cienciasdeportivas.com/descanso-para-deportistas/"
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

    // Scroll state para hacer scroll en la pantalla
    val scrollState = rememberScrollState()

    // Auto-scroll cada 7 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(7000)
            val nextPage = pagerState.currentPage + 1
            pagerState.animateScrollToPage(nextPage)
        }
    }

    // Aquí envolvemos todo el contenido con una columna desplazable
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // Agregamos el scroll vertical
            .padding(horizontal = 0.dp, vertical = 16.dp) // solo padding vertical
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.33f)
                .background(Color.DarkGray)
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
                    tip.titulo,
                    tip.tip,
                    tip.link
                )
            }
        }

        // Indicadores de página fijos
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

        // Título de grupos musculares
        Text(
            text = "Grupos musculares",
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp)
        )

        // Carrusel de músculos (columna deslizable horizontal)
        val musclePages = muscleGroups.chunkedByThree()
        HorizontalPager(
            count = musclePages.size,
            contentPadding = PaddingValues(start = 0.dp, end = 42.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) { page ->
            Column(
                modifier = Modifier
                    .width(300.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                musclePages[page].forEach { muscle ->
                    MuscleGroupItem(muscle = muscle, navController = navController)
                }
            }
        }

        // Botón al final
        Button(
            onClick = { /* Acción del botón */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Crear rutina", color = Color.White)
        }
    }
}

@Composable
fun FitnessTipSlide(
    imageResId: Int,
    titulo: String,
    tip: String,
    link: String
) {
    val context = LocalContext.current

    // Contenedor principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Fila superior: imagen a la izquierda y tip a la derecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Imagen a la izquierda
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier
                    .size(110.dp)
                    .weight(1f)
            )

            // Tip a la derecha
            Text(
                text = tip,
                fontSize = 12.sp,
                color = Color(0xFFBDBDBD),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 15.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila inferior: Título y Botón centrados
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center // Centra los elementos
        ) {
            // Título centrado
            Text(
                text = titulo,
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(end = 50.dp) // Ajusta el margen
            )

            // Botón "Leer más" centrado
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier
                    .padding(start = 8.dp) // Ajusta el margen
            ) {
                Text(text = "Leer más", color = Color.White)
            }
        }
    }
}

@Composable
fun MuscleGroupItem(muscle: MuscleGroup, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("exercises/${muscle.name}")
            }
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(100.dp) // Tamaño fijo
                .background(Color.Gray) // Opcional, para ver el área ocupada
        ) {
            Image(
                painter = painterResource(id = muscle.imageResId),
                contentDescription = muscle.name,
                modifier = Modifier.fillMaxSize(), // Llenar el espacio del Box
                contentScale = ContentScale.Crop // Recorta para llenar el área
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = muscle.name,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}
