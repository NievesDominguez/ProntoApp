package com.example.persistencia.localdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface InmuebleDao {
    //Insertar inmueble
    @Insert
    fun nuevoInmueble(inmuebleData: InmuebleData)

    @Query("""
    UPDATE ${Estructura.Inmueble.TABLE_NAME}
    SET tituloInmueble = :tituloNuevo,
        descripcionInmueble = :descripcionNueva,
        imagenInmueble = :imagenNueva,
        precioInmueble = :precioNuevo,
        contratoInmueble = :contratoNuevo
    WHERE idInmueble = :id
""")
    fun editar(
        tituloNuevo: String,
        descripcionNueva: String,
        imagenNueva: String,
        precioNuevo: String,
        contratoNuevo: String,
        id: Int
    )

    //Leer toda la lista de inmuebles
    @Query("SELECT * FROM ${Estructura.Inmueble.TABLE_NAME}")
    fun getListaInmuebles(): List<InmuebleData>

    //Leer un inmueble específico a través de su id
    @Query("SELECT * FROM ${Estructura.Inmueble.TABLE_NAME} WHERE idInmueble = :idInmueble")
    fun getInmueble(idInmueble: Int?): InmuebleData?

    //Eliminar un inmueble
    @Query("DELETE FROM ${Estructura.Inmueble.TABLE_NAME} WHERE idInmueble = :idInmueble")
    fun deleteInmueble(idInmueble: Int?)

    //Leer toda la lista de inmuebles de un mismo propietario
    @Query("SELECT * FROM ${Estructura.Inmueble.TABLE_NAME} WHERE propietarioInmueble = :propietarioInmueble")
    fun getListaInmueblesMy(propietarioInmueble: Int): List<InmuebleData>
}
