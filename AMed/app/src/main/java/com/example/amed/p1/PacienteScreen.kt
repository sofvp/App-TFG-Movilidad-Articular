package com.example.amed.p1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle


@Composable
fun PacienteScreen(modifier: Modifier = Modifier,viewModel: PacienteViewModel, onVerHistorial: (Paciente) -> Unit, onEditar: (Paciente) -> Unit, onHacerPrueba: (Paciente) -> Unit) {
    val pacientes by viewModel.pacientes.collectAsState()
    var pacienteAEliminar by remember { mutableStateOf<Paciente?>(null) }
    var pacienteAEditar by remember { mutableStateOf<Paciente?>(null) }
    var textoBusqueda by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.weight(1f).fillMaxSize().padding(16.dp)) {
            Spacer(modifier = Modifier.height(30.dp))
            Text("PACIENTES REGISTRADOS", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            if (pacientes.isEmpty()) {
                Text("No hay pacientes registrados")
            } else {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    label = { Text("Buscar por nombre") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                val listaFiltrada =
                    pacientes.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }

                if (listaFiltrada.isEmpty()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("No se han encontrado resultados", fontWeight = FontWeight.Bold)
                        Text("Compruebe que su nombre haya sido introducido correctamente o registre al paciente")
                    }
                } else {
                    LazyColumn {
                        items(pacientes.filter {
                            it.nombre.contains(
                                textoBusqueda,
                                ignoreCase = true
                            )
                        }) { paciente ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(Color(0xFFEDE7F6)),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("Nombre: ")
                                        }
                                        append(paciente.nombre)
                                    })

                                    Text(buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("ID: ")
                                        }
                                        append(paciente.id.toString())
                                    })

                                    Text(buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("Observaciones: ")
                                        }
                                        append(paciente.observaciones ?: "Ninguna")
                                    }, fontSize = 15.sp)
                                }

                                Row {
                                    IconButton(onClick = { onVerHistorial(paciente) }) {
                                        Icon(
                                            Icons.Filled.Info,
                                            contentDescription = "Historial",
                                            tint = Color(0xFF1976D2)
                                        )
                                    }
                                    IconButton(onClick = { pacienteAEditar = paciente }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = Color(0xFFFF9800)
                                        )
                                    }

                                    IconButton(onClick = { onHacerPrueba(paciente) }) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Prueba",
                                            tint = Color(0xFF388E3C)
                                        )
                                    }
                                    IconButton(onClick = { pacienteAEliminar = paciente }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color.Red
                                        )
                                    }


                                }
                            }
                        }
                        item {
                            Divider()
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(46.dp))
            Text("REGISTRAR PACIENTE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            var nuevoNombre by remember { mutableStateOf("") }
            var nuevasObservaciones by remember { mutableStateOf("") }

            OutlinedTextField(
                value = nuevoNombre,
                onValueChange = { nuevoNombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nuevasObservaciones,
                onValueChange = { nuevasObservaciones = it },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.Button(
                onClick = {
                    val nuevoPaciente =
                        Paciente(nombre = nuevoNombre, observaciones = nuevasObservaciones)
                    viewModel.agregarPaciente(nuevoPaciente)
                    nuevoNombre = ""
                    nuevasObservaciones = ""
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Guardar")
            }
            pacienteAEditar?.let { paciente ->
                var nuevoNombre by remember { mutableStateOf(paciente.nombre) }
                var nuevasObservaciones by remember { mutableStateOf(paciente.observaciones) }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("EDITAR PACIENTE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevoNombre,
                        onValueChange = { nuevoNombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevasObservaciones,
                        onValueChange = { nuevasObservaciones = it },
                        label = { Text("Observaciones") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.TextButton(onClick = {
                            pacienteAEditar = null
                        }) {
                            Text("Cancelar")
                        }
                        androidx.compose.material3.Button(onClick = {
                            val actualizado = paciente.copy(
                                nombre = nuevoNombre,
                                observaciones = nuevasObservaciones
                            )
                            viewModel.actualizarPaciente(actualizado)
                            pacienteAEditar = null
                        }) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }


        pacienteAEliminar?.let { paciente ->
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { pacienteAEliminar = null },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Seguro que quieres eliminar a \"${paciente.nombre}\"?") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.eliminarPaciente(paciente)
                            pacienteAEliminar = null
                        }
                    ) {
                        Text("Eliminar", color = androidx.compose.ui.graphics.Color.Red)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        pacienteAEliminar = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}