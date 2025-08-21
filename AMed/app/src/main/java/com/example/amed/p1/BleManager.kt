package com.example.amed.p1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

@SuppressLint("MissingPermission")
class BleManager(private val context: Application) {

    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager.adapter
    }
    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val _scanResults = MutableStateFlow<Map<String, ScanResult>>(emptyMap())
    val scanResults: StateFlow<Map<String, ScanResult>> = _scanResults.asStateFlow()

    private val _connectionStates = MutableStateFlow<Map<String, ConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<String, ConnectionState>> = _connectionStates.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val gattConnections = mutableMapOf<String, BluetoothGatt>()

    private val _datosNotificaciones = MutableStateFlow<Map<String, ByteArray>>(emptyMap())
    val datosNotificaciones: StateFlow<Map<String, ByteArray>> = _datosNotificaciones.asStateFlow()

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("84582cd0-3df0-4e73-9496-29010d7445dd")
        val DATA_CHARACTERISTIC_UUID: UUID = UUID.fromString("84582cd1-3df0-4e73-9496-29010d7445dd")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (result.device.name != null) {
                _scanResults.update { currentMap ->
                    currentMap + (result.device.address to result)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BleManager", "El escaneo falló con el código de error: $errorCode")
        }
    }

    fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (!bluetoothAdapter.isEnabled) return
        _scanResults.value = emptyMap()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    fun stopBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (!bluetoothAdapter.isEnabled) return
        bleScanner.stopScan(scanCallback)
    }

    fun connect(deviceAddress: String) {
        if (!bluetoothAdapter.isEnabled) {
            Log.e("BleManager", "No se puede conectar, el Bluetooth está desactivado.")
            return
        }
        if (gattConnections.containsKey(deviceAddress)) {
            Log.w("BleManager", "Ya existe una conexión o intento de conexión a $deviceAddress. Forzando reconexión.")
            disconnect(deviceAddress)
            closeGatt(deviceAddress)
        }

        _connectionStates.update { it + (deviceAddress to ConnectionState.CONNECTING) }
        Log.i("BleManager", "Intentando conectar a $deviceAddress...")
        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                val address = gatt.device.address
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i("BleManager", "Conexión exitosa a $address")
                        _connectionStates.update { it + (address to ConnectionState.CONNECTED) }
                        gattConnections[address] = gatt
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i("BleManager", "Desconectado de $address")
                        _connectionStates.update { it + (address to ConnectionState.DISCONNECTED) }
                        closeGatt(address)
                    }
                } else {
                    Log.e("BleManager", "Error de conexión ($status) en $address. Desconectando.")
                    _connectionStates.update { it + (address to ConnectionState.DISCONNECTED) }
                    closeGatt(address)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("BleManager", "Servicios descubiertos para ${gatt.device.address}")
                    enableNotifications(gatt.device.address)
                } else {
                    Log.w("BleManager", "Fallo al descubrir servicios para ${gatt.device.address}: $status")
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (characteristic.uuid == DATA_CHARACTERISTIC_UUID) {
                    val address = gatt.device.address
                    val data = characteristic.value
                    _datosNotificaciones.update { it + (address to data) }
                }
            }
        }

        device.connectGatt(context, false, gattCallback)
    }

    private fun enableNotifications(address: String) {
        val gatt = gattConnections[address] ?: run {
            Log.e("BleManager", "No hay conexión GATT para $address al habilitar notificaciones.")
            return
        }

        val service = gatt.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(DATA_CHARACTERISTIC_UUID)

        if (characteristic == null) {
            Log.e("BleManager", "Característica de datos no encontrada en $address")
            return
        }

        val cccd = characteristic.getDescriptor(CCCD_UUID)
        if (cccd == null) {
            Log.e("BleManager", "Descriptor CCCD no encontrado en $address")
            return
        }

        gatt.setCharacteristicNotification(characteristic, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(cccd)
        }
        Log.i("BleManager", "Habilitando notificaciones para $address")
    }

    fun disconnect(deviceAddress: String) {
        if (gattConnections.containsKey(deviceAddress)) {
            Log.i("BleManager", "Desconectando manualmente de $deviceAddress")
            gattConnections[deviceAddress]?.disconnect()
        }
    }

    private fun closeGatt(deviceAddress: String) {
        gattConnections[deviceAddress]?.close()
        gattConnections.remove(deviceAddress)
    }

    fun close() {
        stopBleScan()
        val addresses = gattConnections.keys.toList()
        addresses.forEach { address ->
            disconnect(address)
        }
        gattConnections.clear()
        try {
            context.unregisterReceiver(bluetoothStateReceiver)
        } catch (e: IllegalArgumentException) {

        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                _isBluetoothEnabled.value = (state == BluetoothAdapter.STATE_ON)

                if (state != BluetoothAdapter.STATE_ON) {
                    Log.w("BleManager", "Bluetooth desactivado. Limpiando estado...")
                    stopBleScan()

                    val addressesToClose = gattConnections.keys.toList()
                    addressesToClose.forEach { address ->
                        disconnect(address)
                    }

                    _connectionStates.update { emptyMap() }
                    _scanResults.update { emptyMap() }
                    _datosNotificaciones.update { emptyMap() }
                }
            }
        }
    }

    init {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filter)
    }
}
