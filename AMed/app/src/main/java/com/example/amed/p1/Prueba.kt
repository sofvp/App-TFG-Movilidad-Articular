package com.example.amed.p1

data class Prueba(
    val id: Int = 0,
    val pacienteId: Int,
    val fechaHora: Long,
    val articulacion: String,
    val movimiento: String,
    val anguloBrazo: Float,
    val anguloEspalda: Float,
    val anguloResultante: Float,
    val comentarios: String,
    val timestamps: List<Float> = emptyList(),
    val angulosBrazoList: List<Float> = emptyList(),
    val angulosEspaldaList: List<Float> = emptyList()
)