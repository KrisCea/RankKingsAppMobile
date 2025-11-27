package com.example.rankkings.repository

import androidx.room.*
import com.example.rankkings.model.Album
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de álbumes
 */
@Dao
interface AlbumDao {

    // Insertar un álbum
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album): Long

    // Insertar múltiples álbumes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<Album>)

    // Obtener todos los álbumes de un post específico
    @Query("SELECT * FROM albums WHERE postId = :postId ORDER BY ranking ASC")
    fun getAlbumsByPostId(postId: Int): Flow<List<Album>>

    // Obtener todos los álbumes
    @Query("SELECT * FROM albums")
    fun getAllAlbums(): Flow<List<Album>>

    // Actualizar un álbum
    @Update
    suspend fun updateAlbum(album: Album)

    // Eliminar un álbum
    @Delete
    suspend fun deleteAlbum(album: Album)

    // Eliminar todos los álbumes de un post
    @Query("DELETE FROM albums WHERE postId = :postId")
    suspend fun deleteAlbumsByPostId(postId: Int)

    // Contar álbumes de un post
    @Query("SELECT COUNT(*) FROM albums WHERE postId = :postId")
    suspend fun getAlbumCountByPostId(postId: Int): Int
}
