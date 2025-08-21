package com.example.amed.p1

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PacienteDao {
    @Query("SELECT * FROM pacientes")
    fun getAllPacientes(): Flow<List<PacienteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaciente(paciente: com.example.amed.p1.PacienteEntity)

    @Update
    suspend fun updatePaciente(paciente: PacienteEntity)

    @Delete
    suspend fun deletePaciente(paciente: PacienteEntity)

    @Query("SELECT * FROM pacientes WHERE id = :pacienteId LIMIT 1")
    suspend fun getPacienteById(pacienteId: Int): PacienteEntity?
}
