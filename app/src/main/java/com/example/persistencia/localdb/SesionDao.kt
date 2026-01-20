package com.example.persistencia.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SesionDao {
    //Aquí van las Querys con las operaciones que se quieran realizar.

    //Iniciar sesion
    @Insert
    fun nuevaSesion(user: SesionData)

    //Consultar inicio de sesión
    @Query("SELECT * FROM ${Estructura.Sesion.TABLE_NAME}")
    fun getEstadoSesion(): SesionData?

    @Query("SELECT idUsuario FROM ${Estructura.Sesion.TABLE_NAME} ORDER BY idSesion DESC LIMIT 1")
    fun getDatosSesion() : Int?

}