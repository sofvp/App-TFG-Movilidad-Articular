package com.example.amed.p1

import android.app.Application
import android.bluetooth.le.ScanResult
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

class BleViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = BleManager(application)

    val scanResults: StateFlow<Map<String, ScanResult>> = bleManager.scanResults
    val connectionStates: StateFlow<Map<String, ConnectionState>> = bleManager.connectionStates
    val isBluetoothEnabled: StateFlow<Boolean> = bleManager.isBluetoothEnabled
    val datosNotificaciones: StateFlow<Map<String, ByteArray>> = bleManager.datosNotificaciones

    fun startScan() {
        bleManager.startBleScan()
    }

    fun stopScan() {
        bleManager.stopBleScan()
    }

    fun connect(deviceAddress: String) {
        bleManager.connect(deviceAddress)
    }

    fun disconnect(deviceAddress: String) {
        bleManager.disconnect(deviceAddress)
    }

    override fun onCleared() {
        super.onCleared()
        bleManager.close()
    }
}
