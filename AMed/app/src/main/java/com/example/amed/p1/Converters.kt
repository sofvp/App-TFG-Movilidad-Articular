package com.example.amed.p1

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

//Convierte tipos de datos complejos a tipos que Room puede guardar.

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromFloatList(list: List<Float>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toFloatList(json: String?): List<Float>? {
        if (json == null) {
            return null
        }
        val type = object : TypeToken<List<Float>>() {}.type
        return gson.fromJson(json, type)
    }
}