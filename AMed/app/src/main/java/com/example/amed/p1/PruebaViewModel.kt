package com.example.amed.p1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class PruebaViewModel : ViewModel() {

    private val _anguloEspalda = MutableStateFlow(0f)
    val anguloEspalda: StateFlow<Float> = _anguloEspalda.asStateFlow()

    private val _anguloBrazo = MutableStateFlow(0f)
    val anguloBrazo: StateFlow<Float> = _anguloBrazo.asStateFlow()

    private val _isMeasuring = MutableStateFlow(false)
    val isMeasuring: StateFlow<Boolean> = _isMeasuring.asStateFlow()

    private val _puedeReiniciar = MutableStateFlow(false)
    val puedeReiniciar: StateFlow<Boolean> = _puedeReiniciar.asStateFlow()

    private var selectedMovimiento: String? = null

    private var referenciaYawEspalda = 0f
    private var rawYawEspalda = 0f
    private var referenciaPitchEspalda = 0f
    private var rawPitchEspalda = 0f

    private var referenciaAnguloBrazo = 0f
    private var rawRoll_v_Brazo = 0f
    private var rawPitch_v_Brazo = 0f
    private var rawYaw_v_Brazo = 0f
    private var rawRoll_h_Brazo = 0f

    private var measurementJob: Job? = null
    private val _timeData = mutableListOf<Float>()
    val timeData: List<Float> = _timeData
    private val _brazoData = mutableListOf<Float>()
    val brazoData: List<Float> = _brazoData
    private val _espaldaData = mutableListOf<Float>()
    val espaldaData: List<Float> = _espaldaData

    fun selectMovimiento(movimiento: String) {
        selectedMovimiento = movimiento
        _puedeReiniciar.value = false
        _anguloBrazo.value = 0f
        _anguloEspalda.value = 0f
    }

    fun onNuevosDatosEspalda(datos: ByteArray) {
        viewModelScope.launch {
            try {
                val datosString = String(datos, Charsets.UTF_8)
                val valores = datosString.split(",")

                if (valores.size >= 3) {
                    val pitch = valores[1].toFloatOrNull() ?: 0f
                    val yaw = valores[2].toFloatOrNull() ?: 0f
                    rawPitchEspalda = pitch
                    rawYawEspalda = yaw

                    if (_isMeasuring.value) {
                        _anguloEspalda.value = when (selectedMovimiento) {
                            "Flexión", "Extensión", "Rotación con 90º de abducción" -> {
                                rawPitchEspalda - referenciaPitchEspalda
                            }
                            else -> {
                                -(rawYawEspalda - referenciaYawEspalda)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error al parsear los datos del IMU Espalda: ${e.message}")
            }
        }
    }

    fun onNuevosDatosBrazo(datos: ByteArray) {
        viewModelScope.launch {
            try {
                val datosString = String(datos, Charsets.UTF_8)
                val valores = datosString.split(",")

                if (valores.size >= 4) {
                    rawRoll_v_Brazo = valores[0].toFloatOrNull() ?: 0f
                    rawPitch_v_Brazo = valores[1].toFloatOrNull() ?: 0f
                    rawYaw_v_Brazo = valores[2].toFloatOrNull() ?: 0f
                    rawRoll_h_Brazo = valores[3].toFloatOrNull() ?: 0f

                    if (_isMeasuring.value) {
                        calcularAnguloBrazo()
                    }
                }
            } catch (e: Exception) {
                println("Error al parsear los datos del IMU Brazo: ${e.message}")
            }
        }
    }

    private fun calcularAnguloBrazo() {
        val anguloRelativo = when (selectedMovimiento) {
            "Flexión", "Extensión" -> rawYaw_v_Brazo - referenciaAnguloBrazo
            "Abducción", "Aducción" -> rawPitch_v_Brazo - referenciaAnguloBrazo
            "Rotación externa", "Rotación interna" -> rawRoll_v_Brazo - referenciaAnguloBrazo
            "Rotación con 90º de abducción" -> rawRoll_h_Brazo - referenciaAnguloBrazo
            else -> 0f
        }
        _anguloBrazo.value = abs(anguloRelativo)
    }

    fun setReferencia() {
        referenciaYawEspalda = rawYawEspalda
        referenciaPitchEspalda = rawPitchEspalda
        _anguloEspalda.value = 0f
        println("Nueva referencia establecida para la espalda: Yaw=$referenciaYawEspalda, Pitch=$referenciaPitchEspalda")

        referenciaAnguloBrazo = when (selectedMovimiento) {
            "Flexión", "Extensión" -> rawYaw_v_Brazo
            "Abducción", "Aducción" -> rawPitch_v_Brazo
            "Rotación externa", "Rotación interna" -> rawRoll_v_Brazo
            "Rotación con 90º de abducción" -> rawRoll_h_Brazo
            else -> 0f
        }
        _anguloBrazo.value = 0f
        println("Nueva referencia para Brazo (movimiento: $selectedMovimiento): $referenciaAnguloBrazo")
    }

    fun startMedicion() {
        setReferencia()
        _isMeasuring.value = true
        _puedeReiniciar.value = false
        measurementJob?.cancel()
        _timeData.clear()
        _brazoData.clear()
        _espaldaData.clear()

        measurementJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000f
                _timeData.add(elapsedTime)
                _brazoData.add(_anguloBrazo.value)
                _espaldaData.add(_anguloEspalda.value)
                delay(100)
            }
        }
        println("Medición iniciada.")
    }

    fun stopMedicion() {
        _isMeasuring.value = false
        _puedeReiniciar.value = true
        measurementJob?.cancel()
        println("Medición detenida. Último ángulo espalda: ${_anguloEspalda.value}, Último ángulo brazo: ${_anguloBrazo.value}")
    }

    fun reiniciarMedicion() {
        _isMeasuring.value = false
        _puedeReiniciar.value = false
        _anguloBrazo.value = 0f
        _anguloEspalda.value = 0f
        measurementJob?.cancel()
        _timeData.clear()
        _brazoData.clear()
        _espaldaData.clear()
        println("Medición reiniciada. Listo para una nueva selección o inicio.")
    }

    fun reset() {
        _isMeasuring.value = false
        _puedeReiniciar.value = false
        _anguloEspalda.value = 0f
        _anguloBrazo.value = 0f

        referenciaYawEspalda = 0f
        rawYawEspalda = 0f
        referenciaPitchEspalda = 0f
        rawPitchEspalda = 0f

        referenciaAnguloBrazo = 0f
        rawRoll_v_Brazo = 0f
        rawPitch_v_Brazo = 0f
        rawYaw_v_Brazo = 0f
        rawRoll_h_Brazo = 0f

        selectedMovimiento = null

        measurementJob?.cancel()
        _timeData.clear()
        _brazoData.clear()
        _espaldaData.clear()
    }
}