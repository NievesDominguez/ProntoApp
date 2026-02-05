package com.example.persistencia.localdb

class Estructura {
    //BASE DE DATOS
    object DB {
        const val NAME = "DBUsuarios.db" // Nombre de la bd, creación de un archivo con ese nombre
    }

    //TABLA 1: Usuario
    object Usuario {
        const val TABLE_NAME = "USUARIOS" // Nombre de la tabla

        // Nombre de los atributos/campos
        const val IDUSUARIO = "idUsuario"
        const val NOMBRE = "nombreUsuario"
        const val APELLIDOS = "apellidosUsuario"
        const val INCORPORACION = "incorporacionUsuario"
        const val EMAIL = "email"
        const val SEXO = "sexo"
    }

    //TABLA 2: Sesion
    object Sesion{
        const val TABLE_NAME = "SESIONES"
        const val IDSESION = "idSesion"
        const val IDUSUARIO = "idUsuario"
        const val FECHA_INICIO = "fechaInicio"
    }

    //TABLA 3: Amistad
    object Amistad {
        const val TABLE_NAME = "AMISTAD" // Nombre de la tabla

        // Nombre de los atributos/campos
        const val IDUSUARIO1 = "idUsuario1"
        const val IDUSUARIO2 = "idUsuario2"
    }

    //TABLA 4: Inmueble
    object Inmueble {
        const val TABLE_NAME = "INMUEBLES" // Nombre de la tabla

        // Nombre de los atributos/campos
        const val IDINMUEBLE = "idInmueble"
        const val TITULO = "tituloInmueble"
        const val DESCRIPCION = "descripcionInmueble"
        const val IMAGEN = "imagenInmueble"
        const val PRECIO = "precioInmueble"
        const val CONTRATO = "contratoInmueble"
        const val PROPIETARIO = "propietarioInmueble"
    }

}
