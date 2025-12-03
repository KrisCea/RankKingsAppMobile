package com.example.rankkings.repository

import androidx.room.*
import com.example.rankkings.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de posts
 */
@Dao
interface PostDao {

    // Insertar un post
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    // Insertar una lista de posts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<Post>)

    // Obtener todos los posts ordenados por fecha
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<Post>>

    // Obtener posts de un usuario específico
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPostsByUserId(userId: Int): Flow<List<Post>>

    // Obtener un post por ID
    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: Int): Post?

    // Actualizar un post
    @Update
    suspend fun updatePost(post: Post)

    // Eliminar un post
    @Delete
    suspend fun deletePost(post: Post)

    // Eliminar todos los posts
    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

    // Actualizar contador de likes
    @Query("UPDATE posts SET likesCount = :count, isLiked = :isLiked WHERE id = :postId")
    suspend fun updateLikes(postId: Int, count: Int, isLiked: Boolean)

    // Actualizar contador de guardados
    @Query("UPDATE posts SET savesCount = :count, isSaved = :isSaved WHERE id = :postId")
    suspend fun updateSaves(postId: Int, count: Int, isSaved: Boolean)

    // Actualizar contador de comentarios
    @Query("UPDATE posts SET commentsCount = :count WHERE id = :postId")
    suspend fun updateComments(postId: Int, count: Int)
}
