package com.example.amed.p1

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pacientes")
data class PacienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val observaciones: String
) {
    fun toPaciente(): Paciente = Paciente(id, nombre, observaciones)
    companion object {
        fun fromPaciente(p: Paciente) = PacienteEntity(p.id, p.nombre, p.observaciones)
    }
}
