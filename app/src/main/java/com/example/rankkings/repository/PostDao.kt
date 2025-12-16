package com.example.rankkings.repository

import androidx.room.*
import com.example.rankkings.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de posts
 */
@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<Post>)

    // 🔓 SOLO POSTS PÚBLICOS
    @Query("SELECT * FROM posts WHERE IFNULL(is_private, 0) = 0 ORDER BY timestamp DESC")
    fun getPublicPosts(): Flow<List<Post>>


    // 👤 POSTS DEL USUARIO (INCLUYE PRIVADOS)
    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPostsByUserId(userId: Int): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: Int): Post?

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

    @Query("UPDATE posts SET likesCount = :count, isLiked = :isLiked WHERE id = :postId")
    suspend fun updateLikes(postId: Int, count: Int, isLiked: Boolean)

    @Query("UPDATE posts SET savesCount = :count, isSaved = :isSaved WHERE id = :postId")
    suspend fun updateSaves(postId: Int, count: Int, isSaved: Boolean)

    @Query("UPDATE posts SET commentsCount = :count WHERE id = :postId")
    suspend fun updateComments(postId: Int, count: Int)

    // 🔒 CAMBIAR PRIVACIDAD
    @Query("UPDATE posts SET is_private = :isPrivate WHERE id = :postId")
    suspend fun setPostPrivate(postId: Int, isPrivate: Boolean)
}
