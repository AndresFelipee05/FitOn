import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
            .padding(8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
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
                FitnessTipSlide(tip.imageResId, tip.titulo, tip.tip, tip.link)
            }
        }

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

        Text(
            text = "Grupos musculares",
            fontSize = 26.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp)
        )

        val musclePages = muscleGroups.chunkedByThree()
        HorizontalPager(
            count = musclePages.size,
            contentPadding = PaddingValues(start = 0.dp, end = 42.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) { page ->
            Column(
                modifier = Modifier.width(300.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                musclePages[page].forEach { muscle ->
                    MuscleGroupItem(muscle = muscle, navController = navController)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("create_routine_screen") },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier
                    .width(110.dp)
                    .height(60.dp)
            ) {
                Text(text = "Crear rutina", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            FloatingActionButton(
                onClick = { navController.navigate("routines_screen") },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier
                    .width(110.dp)
                    .height(60.dp)
            ) {
                Text(text = "Modificar rutina", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            FloatingActionButton(
                onClick = {
                    navController.navigate("search")
                },
                containerColor = Color(0xFFFF9800),
                modifier = Modifier
                    .width(80.dp)
                    .height(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Buscar ejercicios",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun FitnessTipSlide(imageResId: Int, titulo: String, tip: String, link: String) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
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
                modifier = Modifier
                    .size(150.dp)
                    .weight(1f)
            )

            Text(
                text = tip,
                fontSize = 14.sp,
                color = Color(0xFFD6D6D6),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 15.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = titulo,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.padding(end = 50.dp)
            )

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.padding(start = 8.dp)
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
                .size(140.dp)
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
            fontSize = 20.sp,
            color = Color.White
        )
    }
}
