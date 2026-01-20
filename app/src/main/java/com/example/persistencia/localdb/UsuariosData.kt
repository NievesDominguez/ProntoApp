package com.example.persistencia.localdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

//data class UsuariosData()

//Tabla Usuario
@Entity(
    tableName = Estructura.Usuario.TABLE_NAME,
    indices = [Index(value = [Estructura.Usuario.EMAIL], unique = true)]
) // Marca la clase como una entidad que corresponde a una tabla en la BD, accediendo por el nombre. Se indica que email será un campo único, creando el índice.
data class UsuarioData(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Estructura.Usuario.IDUSUARIO) val
    idUsuario: Int = 0, // Clave primaria autogenerada automáticamente, generando valores únicos incrementales. El indicar que toma el valor 0 permite que cuando se cree un objeto, no sea necesario asignar manualmente un valor.
    @ColumnInfo(name = Estructura.Usuario.NOMBRE) val nombreUsuario: String, // Columna nombre del usuario, mapeado a través de @ColumnInfo a una columna específica en la tabla.
    @ColumnInfo(name = Estructura.Usuario.APELLIDOS) val apellidosUsuario: String, // Apellidos del usuario, también mapeado a una columna específica.
    @ColumnInfo(name = Estructura.Usuario.INCORPORACION) val incorporacionUsuario: String, // Fecha.
    @ColumnInfo(name = Estructura.Usuario.EMAIL) val email: String, // Email.
    @ColumnInfo(name = Estructura.Usuario.SEXO) val sexo: String // Sexo.
)


// Tabla Sesion
@Entity( // Marca la clase como una entidad que corresponde a una tabla en la BD, accediendo por el nombre. Además, se crea la clave foránea con foreignKeys.
    tableName = Estructura.Sesion.TABLE_NAME,
    foreignKeys = [ForeignKey(
        entity = UsuarioData::class, // Crea una relación entre esta tabla y otra llamada UsuarioData, es decir, la clave foránea viene de la entidad(tabla) UsuarioData .
        parentColumns = [Estructura.Usuario.IDUSUARIO], // Define el campo de la tabla padre (UsuarioData) que será referenciado .
        childColumns = [Estructura.Sesion.IDUSUARIO], // Define el campo de esta entidad que contendrá la clave (clave foránea).
        onDelete =
            ForeignKey.CASCADE // Si se elimina un usuario en la tabla UsuarioData, todas las sesiones relacionadas en la tabla de sesiones también se eliminarán automáticamente(borrado en cascada).
    )]
)
data class SesionData(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Estructura.Sesion.IDSESION) val idSesion: Int = 0, // Clave primaria autogenerada automáticamente, generando valores únicos incrementales. El indicar que toma el valor 0 permite que cuando se cree un objeto, no sea necesario asignar manualmente un valor.
    @ColumnInfo(name = Estructura.Sesion.IDUSUARIO) val idUsuario: Int, // Clave foránea hacia UsuariosData.idUsuario
    @ColumnInfo(name = Estructura.Sesion.FECHA_INICIO) val fechaInicio: String
)

//Tabla Amistad
@Entity(
    tableName = Estructura.Amistad.TABLE_NAME,
    indices = [Index(value = [Estructura.Amistad.IDUSUARIO1], unique = true)]
) // Marca la clase como una entidad que corresponde a una tabla en la BD, accediendo por el nombre. Se indica que email será un campo único, creando el índice.
data class AmistadData(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = Estructura.Amistad.IDUSUARIO1) val idUsuario1: Int, // Columna nombre del usuario, mapeado a través de @ColumnInfo a una columna específica en la tabla.
    @ColumnInfo(name = Estructura.Amistad.IDUSUARIO2) val idUsuario2: Int, // Columna nombre del usuario, mapeado a través de @ColumnInfo a una columna específica en la tabla.
)


//Tabla Inmuebles
@Entity(
    tableName = Estructura.Inmueble.TABLE_NAME,
    indices = [Index(value = [Estructura.Inmueble.IDINMUEBLE], unique = true)],
    foreignKeys = [ForeignKey(
        entity = UsuarioData::class, // Crea una relación entre esta tabla y otra llamada UsuarioData, es decir, la clave foránea viene de la entidad(tabla) UsuarioData .
        parentColumns = [Estructura.Usuario.IDUSUARIO], // Define el campo de la tabla padre (UsuarioData) que será referenciado .
        childColumns = [Estructura.Inmueble.PROPIETARIO], // Define el campo de esta entidad que contendrá la clave (clave foránea).
        onDelete =
            ForeignKey.CASCADE // Si se elimina un usuario en la tabla UsuarioData, todas las sesiones relacionadas en la tabla de sesiones también se eliminarán automáticamente(borrado en cascada).
    )]
) // Marca la clase como una entidad que corresponde a una tabla en la BD, accediendo por el nombre. Se indica que email será un campo único, creando el índice.
data class InmuebleData(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = Estructura.Inmueble.IDINMUEBLE) val
    idInmueble: Int = 0, // Clave primaria autogenerada automáticamente, generando valores únicos incrementales. El indicar que toma el valor 0 permite que cuando se cree un objeto, no sea necesario asignar manualmente un valor.
    @ColumnInfo(name = Estructura.Inmueble.TITULO) val tituloInmueble: String, // Título.
    @ColumnInfo(name = Estructura.Inmueble.DESCRIPCION) val descripcionInmueble: String, // Descripción
    @ColumnInfo(name = Estructura.Inmueble.IMAGEN) val imagenInmueble: String, // Url de la imagen.
    @ColumnInfo(name = Estructura.Inmueble.PRECIO) val precioInmueble: String, // Precio.
    @ColumnInfo(name = Estructura.Inmueble.CONTRATO) val contratoInmueble: String, // Tipo de contrato.
    @ColumnInfo(name = Estructura.Inmueble.PROPIETARIO) val propietarioInmueble: Int // Propietario.
)
