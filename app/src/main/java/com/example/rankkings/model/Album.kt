package com.example.rankkings.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un álbum musical en la base de datos
 * Cada álbum está asociado a un post específico
 */
@Entity(
    tableName = "albums",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("postId")]
)
data class Album(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val postId: Int,
    val albumImageUri: String,
    val albumName: String,
    val artistName: String,
    val ranking: Int
)

