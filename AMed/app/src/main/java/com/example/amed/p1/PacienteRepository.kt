package com.example.amed.p1

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PacienteRepository(private val pacienteDao: PacienteDao, private val pruebaDao: PruebaDao) {

    val pacientes: Flow<List<Paciente>> =
        pacienteDao.getAllPacientes().map { lista -> lista.map { it.toPaciente() } }

    suspend fun insertar(p: Paciente) {
        pacienteDao.insertPaciente(PacienteEntity.fromPaciente(p))
    }

    suspend fun actualizar(p: Paciente) {
        pacienteDao.updatePaciente(PacienteEntity.fromPaciente(p))
    }

    suspend fun eliminar(p: Paciente) {
        pacienteDao.deletePaciente(PacienteEntity.fromPaciente(p))
    }

    suspend fun getPacienteById(id: Int): Paciente? {
        return pacienteDao.getPacienteById(id)?.toPaciente()
    }

    fun getPruebasForPaciente(pacienteId: Int): Flow<List<Prueba>> {
        return pruebaDao.getPruebasForPaciente(pacienteId).map { lista ->
            lista.map { it.toPrueba() }
        }
    }

    suspend fun getPruebaById(pruebaId: Int): Prueba? {
        return pruebaDao.getPruebaById(pruebaId)?.toPrueba()
    }

    suspend fun insertarPrueba(prueba: Prueba) {
        pruebaDao.insertPrueba(PruebaEntity.fromPrueba(prueba))
    }

    suspend fun eliminarPrueba(prueba: Prueba) {
        pruebaDao.deletePrueba(PruebaEntity.fromPrueba(prueba))
    }
}
