package com.example.persistencia.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AmistadDao {

    @Query("SELECT * FROM ${Estructura.Amistad.TABLE_NAME} WHERE idUsuario1 = :idSesionActual")
    fun getAmistadUsuario(idSesionActual: Int): List<AmistadData>

    @Insert
    fun nuevaAmistad(amistadData: AmistadData)

    @Query("DELETE FROM ${Estructura.Amistad.TABLE_NAME} WHERE idUsuario1 = :idUsuario1 AND idUsuario2 = :idUsuario2")
    fun eliminarAmistad(idUsuario1: Int, idUsuario2: Int)
}