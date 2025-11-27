package com.example.rankkings.repository

import androidx.room.*
import com.example.rankkings.model.Comment
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de comentarios
 */
@Dao
interface CommentDao {

    // Insertar un comentario
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long

    // Obtener comentarios de un post
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp DESC")
    fun getCommentsByPostId(postId: Int): Flow<List<Comment>>

    // Contar comentarios de un post
    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun getCommentCountByPostId(postId: Int): Int

    // Eliminar un comentario
    @Delete
    suspend fun deleteComment(comment: Comment)

    // Eliminar todos los comentarios de un post
    @Query("DELETE FROM comments WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: Int)
}
