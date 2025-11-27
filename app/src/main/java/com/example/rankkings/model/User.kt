package com.example.rankkings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un usuario del sistema
 * Almacena credenciales y datos de perfil
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String, // Nombre de usuario único
    val email: String, // Email del usuario
    val password: String, // Contraseña (en producción debería estar hasheada)
    var profileImageUri: String? = null, // URI de imagen de perfil (opcional)
    val bio: String? = null, // Biografía del usuario (opcional)
    val interests: List<String> = emptyList(), // Intereses musicales del usuario
    val createdAt: Long = System.currentTimeMillis() // Fecha de registro
)
