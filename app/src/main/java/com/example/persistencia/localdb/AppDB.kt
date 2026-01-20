package com.example.persistencia.localdb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database( // Indica que esta clase define una base de datos Room
    // Especifica que la base de datos contiene una tabla representada por la entidad UsuarioData. Si tenemos más tablas en la base de datos, se ponen a continuación separadas por comas. Ej: [UsuariosData::class, SesionData::class]
    // Establece la versión actual de la base de datos, necesaria para el manejo de migraciones y cambios realizados. Ir incrementando a medida que se va modificando para notificar y adaptar los cambios.
    // Indica que la estructura de la base de datos se exportará a un archivo para mantener un historial de esquemas.
    entities = [
        UsuarioData::class, // Tabla Usuario
        SesionData::class,   // Tabla Sesion
        AmistadData ::class,   // Tabla Sesion
        InmuebleData ::class   // Tabla Sesion
    ],
    version = 1,
    exportSchema = true
)

//AppDB hereda de RoomDatabase, que es la clase base para bases de datos Room.
abstract class AppDB : RoomDatabase() {
    // Metodo abstracto usuariosDao() para proporcionar instancias de acceso a las operaciones definidas en la interfaz UsuarioDao. Room genera la implementación automáticamente. Implementar este metodo para proporcionar la instancia funcional de acceso a los datos.
    abstract fun usuarioDao(): UsuarioDao

    // Metodo abstracto sesionDao() para proporcionar instancias de acceso a las operaciones definidas en la interfaz SesionDao. Room genera la implementación automáticamente. Implementar este metodo para proporcionar la instancia funcional de acceso a los datos.
    abstract fun sesionDao(): SesionDao

    // Metodo abstracto amistadDao() para proporcionar instancias de acceso a las operaciones definidas en la interfaz AmistadDao. Room genera la implementación automáticamente. Implementar este metodo para proporcionar la instancia funcional de acceso a los datos.
    abstract fun amistadDao(): AmistadDao

    // Metodo abstracto inmuebleDao() para proporcionar instancias de acceso a las operaciones definidas en la interfaz InmuebleDao. Room genera la implementación automáticamente. Implementar este metodo para proporcionar la instancia funcional de acceso a los datos.
    abstract fun inmuebleDao(): InmuebleDao

}
