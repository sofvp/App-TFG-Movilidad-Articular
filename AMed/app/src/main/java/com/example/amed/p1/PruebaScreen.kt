package com.example.amed.p1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PruebaScreen(
    modifier: Modifier = Modifier,
    pacienteViewModel: PacienteViewModel,
    bleViewModel: BleViewModel,
    pruebaViewModel: PruebaViewModel = viewModel(),
    pacienteId: Int,
    onNavigateBack: () -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var hasPermissions by remember { mutableStateOf(false) }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            println("Bluetooth activado por el usuario.")
        } else {
            println("El usuario no activó el Bluetooth.")
        }
    }

    val enableLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {}

    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions.values.all { it }
            hasPermissions = granted
            if (granted) {
                println("Todos los permisos BLE fueron concedidos.")
            } else {
                println("Los permisos BLE fueron denegados.")
            }
        }
    )

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    var locationEnabled by remember { mutableStateOf(isLocationEnabled()) }
    LaunchedEffect(Unit) {
        locationEnabled = isLocationEnabled()
    }

    LaunchedEffect(key1 = true) {
        permissionLauncher.launch(permissionsToRequest)
    }

    LaunchedEffect(pacienteId) {
        pacienteViewModel.getPacienteById(pacienteId)
    }

    DisposableEffect(Unit) {
        onDispose {
            pruebaViewModel.reset()
        }
    }

    val paciente by pacienteViewModel.pacienteSeleccionado.collectAsState()
    val scanResults by bleViewModel.scanResults.collectAsState()
    val connectionStates by bleViewModel.connectionStates.collectAsState()
    val isBluetoothEnabled by bleViewModel.isBluetoothEnabled.collectAsState()
    val datosNotificaciones by bleViewModel.datosNotificaciones.collectAsState()

    val anguloEspalda by pruebaViewModel.anguloEspalda.collectAsState()
    val anguloBrazo by pruebaViewModel.anguloBrazo.collectAsState()
    val isMeasuring by pruebaViewModel.isMeasuring.collectAsState()
    val puedeReiniciar by pruebaViewModel.puedeReiniciar.collectAsState()

    var articulacionExpanded by remember { mutableStateOf(false) }
    val articulaciones = listOf("Hombro")
    var selectedArticulacion by remember { mutableStateOf<String?>(null) }

    var movimientoExpanded by remember { mutableStateOf(false) }
    val movimientos = listOf(
        "Flexión", "Extensión", "Abducción", "Aducción",
        "Rotación externa", "Rotación interna", "Rotación con 90º de abducción"
    )
    var selectedMovimiento by remember { mutableStateOf<String?>(null) }
    var comentarios by remember { mutableStateOf("") }

    val imuBrazoScanResult = scanResults.values.find { it.device.name == "IMU_Brazo" }
    val imuEspaldaScanResult = scanResults.values.find { it.device.name == "IMU_Espalda" }

    val estadoBrazo = imuBrazoScanResult?.let { connectionStates[it.device.address] } ?: ConnectionState.DISCONNECTED
    val estadoEspalda = imuEspaldaScanResult?.let { connectionStates[it.device.address] } ?: ConnectionState.DISCONNECTED

    LaunchedEffect(datosNotificaciones, imuEspaldaScanResult) {
        imuEspaldaScanResult?.device?.address?.let { address ->
            datosNotificaciones[address]?.let { data ->
                pruebaViewModel.onNuevosDatosEspalda(data)
            }
        }
    }

    LaunchedEffect(datosNotificaciones, imuBrazoScanResult) {
        imuBrazoScanResult?.device?.address?.let { address ->
            datosNotificaciones[address]?.let { data ->
                pruebaViewModel.onNuevosDatosBrazo(data)
            }
        }
    }


    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("REALIZAR PRUEBA", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!isBluetoothEnabled) {
                WarningCard(
                    title = "Bluetooth desactivado",
                    text = "Por favor, activa el Bluetooth para poder escanear dispositivos.",
                    buttonText = "Activar Bluetooth"
                ) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    enableBluetoothLauncher.launch(enableBtIntent)
                }
            } else if (!hasPermissions) {
                WarningCard(
                    title = "Permisos Requeridos",
                    text = "Se necesita permiso de 'Ubicación' y 'Dispositivos Cercanos' para encontrar los IMUs.",
                    buttonText = "Conceder Permisos"
                ) { permissionLauncher.launch(permissionsToRequest) }
            } else if (!locationEnabled) {
                WarningCard(
                    title = "Ubicación desactivada",
                    text = "Por favor, activa los servicios de ubicación de la tablet para poder escanear.",
                    buttonText = "Activar Ubicación"
                ) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    enableLocationLauncher.launch(intent)
                }
            }

            paciente?.let { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${p.nombre}\nID: ${p.id}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val areButtonsEnabled = isBluetoothEnabled && hasPermissions && locationEnabled
                    Button(onClick = { bleViewModel.startScan() }, enabled = areButtonsEnabled) {
                        Text("Escanear")
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "Estado de conexión de los sensores:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    StatusIndicator(deviceName = "IMU Brazo", state = estadoBrazo)
                    StatusIndicator(deviceName = "IMU Espalda", state = estadoEspalda)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            //Lista de dispositivos
            Text("Dispositivos encontrados:", fontWeight = FontWeight.Bold)
            LazyColumn(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                items(scanResults.values.toList(), key = { it.device.address }) { result ->
                    val state = connectionStates[result.device.address] ?: ConnectionState.DISCONNECTED
                    DeviceItem(
                        deviceName = result.device.name ?: "Desconocido",
                        deviceAddress = result.device.address,
                        connectionState = state,
                        onClick = {
                            if (state == ConnectionState.DISCONNECTED) {
                                bleViewModel.connect(result.device.address)
                            } else {
                                bleViewModel.disconnect(result.device.address)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            //Selección de articulación y movimiento
            val areSelectorsEnabled = estadoBrazo == ConnectionState.CONNECTED && estadoEspalda == ConnectionState.CONNECTED
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { articulacionExpanded = true },
                        enabled = areSelectorsEnabled && !isMeasuring,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedArticulacion ?: "Selecciona articulación",
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar")
                    }
                    DropdownMenu(expanded = articulacionExpanded, onDismissRequest = { articulacionExpanded = false }) {
                        articulaciones.forEach { label ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 18.sp) },
                                onClick = {
                                    selectedArticulacion = label
                                    articulacionExpanded = false
                                }
                            )
                        }
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { movimientoExpanded = true },
                        enabled = areSelectorsEnabled && selectedArticulacion != null && !isMeasuring,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedMovimiento ?: "Selecciona movimiento",
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Desplegar")
                    }
                    DropdownMenu(expanded = movimientoExpanded, onDismissRequest = { movimientoExpanded = false }) {
                        movimientos.forEach { label ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 18.sp) },
                                onClick = {
                                    selectedMovimiento = label
                                    pruebaViewModel.selectMovimiento(label)
                                    movimientoExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val areMeasurementControlsEnabled = estadoEspalda == ConnectionState.CONNECTED &&
                        estadoBrazo == ConnectionState.CONNECTED &&
                        selectedArticulacion != null &&
                        selectedMovimiento != null

                Button(
                    onClick = {
                        pruebaViewModel.setReferencia()
                        Toast.makeText(context, "Referencia registrada correctamente", Toast.LENGTH_SHORT).show()
                    },
                    enabled = areMeasurementControlsEnabled
                ) { Text("Referencia") }

                if (!isMeasuring && !puedeReiniciar) {
                    Button(
                        onClick = { pruebaViewModel.startMedicion() },
                        enabled = areMeasurementControlsEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("Iniciar") }
                }

                if (isMeasuring) {
                    Button(
                        onClick = { pruebaViewModel.stopMedicion() },
                        enabled = areMeasurementControlsEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Detener") }
                }

                AnimatedVisibility(visible = puedeReiniciar) {
                    Button(
                        onClick = {
                            selectedMovimiento = null
                            pruebaViewModel.reiniciarMedicion()
                        },
                        enabled = areMeasurementControlsEnabled,
                    ) { Text("Reiniciar") }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            //Gráficos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                val anguloResultante = anguloBrazo - anguloEspalda

                AngleDisplay(title = "Brazo", angle = anguloBrazo)
                AngleDisplay(title = "Espalda", angle = anguloEspalda)

                Card(
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.wrapContentSize(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    AngleDisplay(title = "Resultante", angle = anguloResultante, modifier = Modifier.padding(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            //Comentarios
            OutlinedTextField(
                value = comentarios,
                onValueChange = { comentarios = it },
                label = { Text("Comentarios") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            //Guardar
            Button(
                onClick = {
                    // Solo se puede guardar si hay una prueba finalizada
                    if (puedeReiniciar && selectedArticulacion != null && selectedMovimiento != null) {
                        coroutineScope.launch {
                            pacienteViewModel.guardarPrueba(
                                pacienteId = pacienteId,
                                articulacion = selectedArticulacion!!,
                                movimiento = selectedMovimiento!!,
                                anguloBrazo = anguloBrazo,
                                anguloEspalda = anguloEspalda,
                                comentarios = comentarios,

                                timestamps = pruebaViewModel.timeData,
                                angulosBrazoList = pruebaViewModel.brazoData,
                                angulosEspaldaList = pruebaViewModel.espaldaData
                            )
                            Toast.makeText(context, "Prueba guardada en el historial", Toast.LENGTH_SHORT).show()
                            onNavigateBack() // Vuelve a la pantalla anterior
                        }
                    } else {
                        Toast.makeText(context, "Realice y detenga una medición para poder guardar", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Guardar") }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}