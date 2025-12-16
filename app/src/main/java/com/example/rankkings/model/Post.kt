package com.example.rankkings.model

import androidx.room.ColumnInfo
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

    val userId: Int,
    val name: String,
    val title: String,
    val description: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    var likesCount: Int = 0,
    var commentsCount: Int = 0,
    var savesCount: Int = 0,

    var isLiked: Boolean = false,
    var isSaved: Boolean = false,

    @ColumnInfo(name = "is_private")
    val isPrivate: Boolean = false
)

