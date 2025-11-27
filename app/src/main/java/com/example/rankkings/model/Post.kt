package com.example.rankkings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un post con ranking de álbumes
 * Contiene información sobre likes, guardados y comentarios
 */
@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String, // ID del usuario que creó el post
    val username: String, // Nombre del usuario
    val title: String, // Título del ranking
    val description: String, // Descripción del ranking
    val timestamp: Long = System.currentTimeMillis(), // Fecha de creación
    var likesCount: Int = 0, // Contador de likes
    var commentsCount: Int = 0, // Contador de comentarios
    var savesCount: Int = 0, // Contador de guardados
    var isLiked: Boolean = false, // Si el usuario actual dio like
    var isSaved: Boolean = false // Si el usuario actual guardó el post
)
