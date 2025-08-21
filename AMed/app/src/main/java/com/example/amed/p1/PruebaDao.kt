package com.example.amed.p1

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PruebaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrueba(prueba: PruebaEntity)

    @Delete
    suspend fun deletePrueba(prueba: PruebaEntity)

    @Query("SELECT * FROM pruebas WHERE pacienteId = :pacienteId ORDER BY fechaHora DESC")
    fun getPruebasForPaciente(pacienteId: Int): Flow<List<PruebaEntity>>

    @Query("SELECT * FROM pruebas WHERE id = :pruebaId LIMIT 1")
    suspend fun getPruebaById(pruebaId: Int): PruebaEntity?
}