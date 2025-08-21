package com.example.amed.p1

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    pacienteViewModel: PacienteViewModel,
    pacienteId: Int,
    onNavigateBack: () -> Unit,
    onVerDetalle: (Prueba) -> Unit
) {
    // Carga los datos del paciente y su historial de pruebas al entrar a la pantalla
    LaunchedEffect(pacienteId) {
        pacienteViewModel.getPacienteById(pacienteId)
        pacienteViewModel.cargarPruebas(pacienteId)
    }

    val paciente by pacienteViewModel.pacienteSeleccionado.collectAsState()
    val pruebas by pacienteViewModel.pruebas.collectAsState()
    var pruebaAEliminar by remember { mutableStateOf<Prueba?>(null) }

    // Formateador para mostrar la fecha y la hora
    val formatoFecha = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HISTORIAL DEL PACIENTE", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
        ) {
            paciente?.let { p ->
                // Muestra el nombre del paciente en la parte superior
                Text(
                    text = p.nombre,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Divider()

                // Muestra la lista de pruebas, o un mensaje si está vacía
                if (pruebas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Este paciente no tiene pruebas guardadas.", fontSize = 16.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(pruebas, key = { it.id }) { prueba ->
                            PruebaHistorialItem(
                                prueba = prueba,
                                formatoFecha = formatoFecha,
                                onVerDetalle = { onVerDetalle(prueba) },
                                onEliminar = { pruebaAEliminar = prueba }
                            )
                        }
                    }
                }
            } ?: run {
                // Muestra un indicador de carga mientras se obtienen los datos del paciente
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        // Diálogo de confirmación para eliminar una prueba
        pruebaAEliminar?.let { prueba ->
            AlertDialog(
                onDismissRequest = { pruebaAEliminar = null },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Seguro que quieres eliminar esta prueba del ${formatoFecha.format(Date(prueba.fechaHora))}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pacienteViewModel.eliminarPrueba(prueba)
                            pruebaAEliminar = null
                        }
                    ) {
                        Text("Eliminar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pruebaAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Composable para cada elemento de la lista del historial
@Composable
fun PruebaHistorialItem(
    prueba: Prueba,
    formatoFecha: SimpleDateFormat,
    onVerDetalle: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${prueba.articulacion} - ${prueba.movimiento}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatoFecha.format(Date(prueba.fechaHora)), fontSize = 14.sp)
            }
            Row {
                IconButton(onClick = onVerDetalle) {
                    Icon(Icons.Default.Visibility, contentDescription = "Ver Detalle", tint = Color(0xFF388E3C))
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Prueba", tint = Color.Red)
                }
            }
        }
    }
}