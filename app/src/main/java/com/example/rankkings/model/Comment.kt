package com.example.rankkings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un comentario en un post
 */
@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val postId: Int, // ID del post comentado
    val userId: Int, // ID del usuario que comentó
    val name: String, // Nombre del usuario (cambiado de username)
    val content: String, // Contenido del comentario
    val timestamp: Long = System.currentTimeMillis() // Fecha del comentario
)
