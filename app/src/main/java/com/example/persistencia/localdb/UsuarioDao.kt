package com.example.persistencia.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface UsuarioDao {
    //Aquí van las Querys con las operaciones que se quieran realizar.
    //Insertar usuario
    @Insert
    fun nuevoUsuario(usuarioData: UsuarioData)

    //Leer toda la lista de usuarios
    @Query("SELECT * FROM ${Estructura.Usuario.TABLE_NAME}")
    fun getListaUsuarios(): List<UsuarioData>

    //Leer un usuario específico a través de su email
    @Query("SELECT * FROM ${Estructura.Usuario.TABLE_NAME} WHERE email = :email")
    fun getUnUser(email: String): UsuarioData?
}