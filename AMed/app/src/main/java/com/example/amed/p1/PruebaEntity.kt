package com.example.amed.p1

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pruebas",
    foreignKeys = [ForeignKey(
        entity = PacienteEntity::class,
        parentColumns = ["id"],
        childColumns = ["pacienteId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PruebaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pacienteId: Int,
    val fechaHora: Long,
    val articulacion: String,
    val movimiento: String,
    val anguloBrazo: Float,
    val anguloEspalda: Float,
    val anguloResultante: Float,
    val comentarios: String,
    val timestamps: List<Float>,
    val angulosBrazoList: List<Float>,
    val angulosEspaldaList: List<Float>
) {

    fun toPrueba(): Prueba = Prueba(
        id = id,
        pacienteId = pacienteId,
        fechaHora = fechaHora,
        articulacion = articulacion,
        movimiento = movimiento,
        anguloBrazo = anguloBrazo,
        anguloEspalda = anguloEspalda,
        anguloResultante = anguloResultante,
        comentarios = comentarios,
        timestamps = timestamps,
        angulosBrazoList = angulosBrazoList,
        angulosEspaldaList = angulosEspaldaList
    )

    companion object {
        fun fromPrueba(prueba: Prueba): PruebaEntity = PruebaEntity(
            id = prueba.id,
            pacienteId = prueba.pacienteId,
            fechaHora = prueba.fechaHora,
            articulacion = prueba.articulacion,
            movimiento = prueba.movimiento,
            anguloBrazo = prueba.anguloBrazo,
            anguloEspalda = prueba.anguloEspalda,
            anguloResultante = prueba.anguloResultante,
            comentarios = prueba.comentarios,
            timestamps = prueba.timestamps,
            angulosBrazoList = prueba.angulosBrazoList,
            angulosEspaldaList = prueba.angulosEspaldaList
        )
    }
}