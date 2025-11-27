package com.example.rankkings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un álbum musical en la base de datos
 * Cada álbum está asociado a un post específico
 */
@Entity(tableName = "albums")
data class Album(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val postId: Int, // ID del post al que pertenece este álbum
    val albumImageUri: String, // URI de la imagen del álbum
    val albumName: String, // Nombre del álbum
    val artistName: String, // Nombre del artista
    val ranking: Int // Posición en el ranking
)
