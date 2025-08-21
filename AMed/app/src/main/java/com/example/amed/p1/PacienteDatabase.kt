package com.example.amed.p1

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PacienteEntity::class, PruebaEntity::class], version = 4)
@TypeConverters(Converters::class)
abstract class PacienteDatabase : RoomDatabase() {
    abstract fun pacienteDao(): PacienteDao
    abstract fun pruebaDao(): PruebaDao
}