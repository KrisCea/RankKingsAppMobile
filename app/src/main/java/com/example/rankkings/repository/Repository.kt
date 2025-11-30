package com.example.rankkings.repository

import com.example.rankkings.model.Album
import com.example.rankkings.model.Comment
import com.example.rankkings.model.Post
import com.example.rankkings.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio centralizado que coordina todas las operaciones de datos.
 * Actúa como única fuente de verdad para los ViewModels.
 */
@Singleton
class Repository @Inject constructor(
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val albumDao: AlbumDao,
    private val commentDao: CommentDao
) {

    // ============ OPERACIONES DE USUARIOS ============

    suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }

    // Eliminamos la función 'loginUser' ya que la verificación de contraseña se hace en el ViewModel con BCrypt.

    fun getUserById(userId: Int): Flow<User?> {
        return userDao.getUserById(userId)
    }

    suspend fun getUserByName(name: String): User? {
        return userDao.getUserByName(name)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }


    suspend fun updateUserProfileImage(userId: Int, imageUri: String) {
        userDao.updateUserProfileImage(userId, imageUri)
    }

    // ============ OPERACIONES DE POSTS ============

    suspend fun createPost(post: Post): Long {
        return postDao.insertPost(post)
    }

    fun getAllPosts(): Flow<List<Post>> {
        return postDao.getAllPosts()
    }

    fun getPostsByUserId(userId: Int): Flow<List<Post>> {
        return postDao.getPostsByUserId(userId)
    }

    suspend fun getPostById(postId: Int): Post? {
        return postDao.getPostById(postId)
    }

    suspend fun toggleLike(post: Post) {
        val newLikeStatus = !post.isLiked
        val newCount = if (newLikeStatus) post.likesCount + 1 else post.likesCount - 1
        postDao.updateLikes(post.id, newCount, newLikeStatus)
    }

    suspend fun toggleSave(post: Post) {
        val newSaveStatus = !post.isSaved
        val newCount = if (newSaveStatus) post.savesCount + 1 else post.savesCount - 1
        postDao.updateSaves(post.id, newCount, newSaveStatus)
    }

    suspend fun updatePost(post: Post) {
        postDao.updatePost(post)
    }

    suspend fun deletePost(post: Post) {
        albumDao.deleteAlbumsByPostId(post.id)
        commentDao.deleteCommentsByPostId(post.id)
        postDao.deletePost(post)
    }

    // ============ OPERACIONES DE ÁLBUMES ============

    suspend fun insertAlbums(albums: List<Album>) {
        albumDao.insertAlbums(albums)
    }



    fun getAlbumsByPostId(postId: Int): Flow<List<Album>> {
        return albumDao.getAlbumsByPostId(postId)
    }

    suspend fun getAlbumCount(postId: Int): Int {
        return albumDao.getAlbumCountByPostId(postId)
    }

    // ============ OPERACIONES DE COMENTARIOS ============

    suspend fun addComment(comment: Comment): Long {
        val commentId = commentDao.insertComment(comment)
        val count = commentDao.getCommentCountByPostId(comment.postId)
        postDao.updateComments(comment.postId, count)
        return commentId
    }

    fun getCommentsByPostId(postId: Int): Flow<List<Comment>> {
        return commentDao.getCommentsByPostId(postId)
    }

    suspend fun deleteComment(comment: Comment) {
        commentDao.deleteComment(comment)
        val count = commentDao.getCommentCountByPostId(comment.postId)
        postDao.updateComments(comment.postId, count)
    }
}