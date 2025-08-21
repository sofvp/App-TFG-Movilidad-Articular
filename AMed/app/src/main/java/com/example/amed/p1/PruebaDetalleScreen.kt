package com.example.amed.p1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruebaDetalleScreen(
    pacienteViewModel: PacienteViewModel,
    pruebaId: Int,
    onNavigateBack: () -> Unit
) {
    // Carga de datos
    LaunchedEffect(pruebaId) {
        pacienteViewModel.getPruebaById(pruebaId)
    }

    val prueba by pacienteViewModel.pruebaSeleccionada.collectAsState()
    val paciente by pacienteViewModel.pacienteSeleccionado.collectAsState()
    val todasLasPruebas by pacienteViewModel.pruebas.collectAsState()
    val pruebaAnterior by pacienteViewModel.pruebaAnteriorSeleccionada.collectAsState()

    // Estado para el interruptor de comparación
    var mostrarComparacion by remember { mutableStateOf(false) }

    // Lógica para cargar/quitar la prueba anterior según el interruptor
    LaunchedEffect(mostrarComparacion, prueba) {
        if (mostrarComparacion) {
            prueba?.let { pacienteViewModel.cargarPruebaAnterior(it) }
        } else {
            pacienteViewModel.limpiarComparacion()
        }
    }

    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DETALLES DE LA PRUEBA", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            prueba?.let { p ->
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Paciente: ") }
                                append("${paciente?.nombre}\n")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("ID Paciente: ") }
                                append("${p.pacienteId}\n")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Prueba: ") }
                                append("${p.articulacion} - ${p.movimiento}\n")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Fecha: ") }
                                append(formatoFecha.format(Date(p.fechaHora)))
                            },
                            fontSize = 18.sp,
                            lineHeight = 26.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val existePruebaAnteriorComparable = remember(todasLasPruebas, p) {
                    val currentIndex = todasLasPruebas.indexOf(p)
                    if (currentIndex != -1 && currentIndex < todasLasPruebas.size) {
                        todasLasPruebas
                            .subList(currentIndex + 1, todasLasPruebas.size)
                            .any { it.movimiento == p.movimiento }
                    } else {
                        false
                    }
                }

                // Interruptor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("Comparar con prueba anterior", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = mostrarComparacion,
                        onCheckedChange = { mostrarComparacion = it },
                        enabled = existePruebaAnteriorComparable
                    )
                }

                AnimatedVisibility(visible = mostrarComparacion && pruebaAnterior != null) {
                    pruebaAnterior?.let { anterior ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Fecha prueba anterior: ${formatoFecha.format(Date(anterior.fechaHora))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    AngleDisplay(
                        title = "Brazo",
                        angle = p.anguloBrazo,
                        comparisonAngle = if (mostrarComparacion) pruebaAnterior?.anguloBrazo else null
                    )
                    AngleDisplay(
                        title = "Espalda",
                        angle = p.anguloEspalda,
                        comparisonAngle = if (mostrarComparacion) pruebaAnterior?.anguloEspalda else null
                    )

                    Card(
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.wrapContentSize(),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        AngleDisplay(
                            title = "Resultante",
                            angle = p.anguloResultante,
                            modifier = Modifier.padding(4.dp),
                            comparisonAngle = if (mostrarComparacion) pruebaAnterior?.anguloResultante else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Gráficos de ángulo-tiempo
                if (p.timestamps.isNotEmpty()) {
                    Text("Evolución de ángulos en el tiempo", style = MaterialTheme.typography.titleLarge)

                    val resultanteData = remember(p.angulosBrazoList, p.angulosEspaldaList) {
                        p.angulosBrazoList.zip(p.angulosEspaldaList) { brazo, espalda -> brazo - espalda }
                    }
                    val resultanteDataAnterior = remember(pruebaAnterior) {
                        pruebaAnterior?.angulosBrazoList?.zip(pruebaAnterior?.angulosEspaldaList ?: emptyList()) { brazo, espalda -> brazo - espalda }
                    }

                    AngleTimeLineChart(
                        title = "BRAZO",
                        timestamps = p.timestamps,
                        angles = p.angulosBrazoList,
                        lineColor = MaterialTheme.colorScheme.primary,
                        comparisonTimestamps = if (mostrarComparacion) pruebaAnterior?.timestamps else null,
                        comparisonAngles = if (mostrarComparacion) pruebaAnterior?.angulosBrazoList else null,
                        comparisonLineColor = Color.Red
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AngleTimeLineChart(
                        title = "ESPALDA",
                        timestamps = p.timestamps,
                        angles = p.angulosEspaldaList,
                        lineColor = MaterialTheme.colorScheme.primary,
                        comparisonTimestamps = if (mostrarComparacion) pruebaAnterior?.timestamps else null,
                        comparisonAngles = if (mostrarComparacion) pruebaAnterior?.angulosEspaldaList else null,
                        comparisonLineColor = Color.Red
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AngleTimeLineChart(
                        title = "RESULTANTE",
                        timestamps = p.timestamps,
                        angles = resultanteData,
                        lineColor = MaterialTheme.colorScheme.primary,
                        comparisonTimestamps = if (mostrarComparacion) pruebaAnterior?.timestamps else null,
                        comparisonAngles = if (mostrarComparacion) resultanteDataAnterior else null,
                        comparisonLineColor = Color.Red
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Comentarios
                Text("Comentarios guardados", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = p.comentarios.ifEmpty { "No se guardaron comentarios." },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}