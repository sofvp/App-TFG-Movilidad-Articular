package com.example.amed.p1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class PacienteViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        PacienteDatabase::class.java,
        "paciente_db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val repository = PacienteRepository(db.pacienteDao(), db.pruebaDao())

    //Lógica para los pacientes
    val pacientes: StateFlow<List<Paciente>> = repository.pacientes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    private val _pacienteSeleccionado = MutableStateFlow<Paciente?>(null)
    val pacienteSeleccionado: StateFlow<Paciente?> = _pacienteSeleccionado

    fun getPacienteById(id: Int) {
        viewModelScope.launch {
            _pacienteSeleccionado.value = repository.getPacienteById(id)
        }
    }

    fun agregarPaciente(p: Paciente) {
        viewModelScope.launch {
            repository.insertar(p)
        }
    }

    fun actualizarPaciente(paciente: Paciente) {
        viewModelScope.launch {
            repository.actualizar(paciente)
        }
    }

    fun eliminarPaciente(paciente: Paciente) {
        viewModelScope.launch {
            repository.eliminar(paciente)
        }
    }


    //Lógica para el historial de pruebas
    private val _pruebas = MutableStateFlow<List<Prueba>>(emptyList())
    val pruebas: StateFlow<List<Prueba>> = _pruebas.asStateFlow()

    private val _pruebaSeleccionada = MutableStateFlow<Prueba?>(null)
    val pruebaSeleccionada: StateFlow<Prueba?> = _pruebaSeleccionada.asStateFlow()

    private val _pruebaAnteriorSeleccionada = MutableStateFlow<Prueba?>(null)
    val pruebaAnteriorSeleccionada: StateFlow<Prueba?> = _pruebaAnteriorSeleccionada.asStateFlow()

    fun cargarPruebas(pacienteId: Int) {
        viewModelScope.launch {
            repository.getPruebasForPaciente(pacienteId).collect {
                _pruebas.value = it
            }
        }
    }

    fun getPruebaById(pruebaId: Int) {
        viewModelScope.launch {
            _pruebaSeleccionada.value = repository.getPruebaById(pruebaId)
        }
    }

    fun guardarPrueba(
        pacienteId: Int,
        articulacion: String,
        movimiento: String,
        anguloBrazo: Float,
        anguloEspalda: Float,
        comentarios: String,
        timestamps: List<Float>,
        angulosBrazoList: List<Float>,
        angulosEspaldaList: List<Float>
    ) {
        viewModelScope.launch {
            val nuevaPrueba = Prueba(
                pacienteId = pacienteId,
                fechaHora = Date().time,
                articulacion = articulacion,
                movimiento = movimiento,
                anguloBrazo = anguloBrazo,
                anguloEspalda = anguloEspalda,
                anguloResultante = anguloBrazo - anguloEspalda,
                comentarios = comentarios,
                timestamps = timestamps,
                angulosBrazoList = angulosBrazoList,
                angulosEspaldaList = angulosEspaldaList
            )
            repository.insertarPrueba(nuevaPrueba)
        }
    }

    fun eliminarPrueba(prueba: Prueba) {
        viewModelScope.launch {
            repository.eliminarPrueba(prueba)
        }
    }

    fun cargarPruebaAnterior(currentPrueba: Prueba) {
        val todasLasPruebas = _pruebas.value
        val currentIndex = todasLasPruebas.indexOf(currentPrueba)

        if (currentIndex != -1) {
            val pruebaAnteriorConMismoMovimiento = todasLasPruebas
                .subList(currentIndex + 1, todasLasPruebas.size)
                .find { it.movimiento == currentPrueba.movimiento }

            _pruebaAnteriorSeleccionada.value = pruebaAnteriorConMismoMovimiento
        } else {
            _pruebaAnteriorSeleccionada.value = null
        }
    }

    fun limpiarComparacion() {
        _pruebaAnteriorSeleccionada.value = null
    }
}