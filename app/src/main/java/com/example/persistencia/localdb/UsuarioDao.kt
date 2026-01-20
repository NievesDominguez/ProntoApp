package com.example.persistencia.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface UsuarioDao {
    //Aquí van las Querys con las operaciones que se quieran realizar.
    //Insertar usuario
    @Insert
    fun nuevoUsuario(usuarioData: UsuarioData)

    @Update
    suspend fun update(usuario: UsuarioData)

    @Query("""
    UPDATE ${Estructura.Usuario.TABLE_NAME}
    SET nombreUsuario = :nombreNuevo,
        apellidosUsuario = :apellidosNuevos,
        incorporacionUsuario = :incorporacionNueva,
        email = :emailNuevo,
        sexo = :sexoNuevo
    WHERE email = :emailViejo
""")
    fun editar(
        nombreNuevo: String,
        apellidosNuevos: String,
        emailNuevo: String,
        incorporacionNueva: String,
        sexoNuevo: String,
        emailViejo: String?
    )


    //Leer toda la lista de usuarios
    @Query("SELECT * FROM ${Estructura.Usuario.TABLE_NAME}")
    fun getListaUsuarios(): List<UsuarioData>

    //Leer un usuario específico a través de su email
    @Query("SELECT * FROM ${Estructura.Usuario.TABLE_NAME} WHERE email = :email")
    fun getUnUser(email: String): UsuarioData?

    //Leer un usuario específico a través de su id
    @Query("SELECT * FROM ${Estructura.Usuario.TABLE_NAME} WHERE idUsuario = :idUsuario")
    fun getUsuario(idUsuario: Int?): UsuarioData?

    @Query("SELECT * FROM ${Estructura.Usuario.TABLE_NAME} WHERE idUsuario != :idSesionActual")
    fun getListaUsuariosMY(idSesionActual: Int?): List<UsuarioData>

}