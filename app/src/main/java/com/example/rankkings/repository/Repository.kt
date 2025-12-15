package com.example.rankkings.repository

import com.example.rankkings.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val albumDao: AlbumDao,
    private val commentDao: CommentDao
) {

    /* ---------- USER ---------- */

    fun getUserById(userId: Int): Flow<User?> =
        userDao.getUserById(userId)

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    /* ---------- POSTS ---------- */

    fun getPublicPosts(): Flow<List<Post>> =
        postDao.getPublicPosts()

    fun getPostsByUserId(userId: Int): Flow<List<Post>> =
        postDao.getPostsByUserId(userId)

    suspend fun getPostById(postId: Int): Post? =
        postDao.getPostById(postId)

    suspend fun createPost(post: Post): Long =
        postDao.insertPost(post)

    suspend fun toggleLike(post: Post) {
        val liked = !post.isLiked
        val count = if (liked) post.likesCount + 1 else post.likesCount - 1
        postDao.updateLikes(post.id, count, liked)
    }

    suspend fun toggleSave(post: Post) {
        val saved = !post.isSaved
        val count = if (saved) post.savesCount + 1 else post.savesCount - 1
        postDao.updateSaves(post.id, count, saved)
    }

    suspend fun togglePrivacy(post: Post) {
        postDao.setPostPrivate(post.id, !post.isPrivate)
    }

    suspend fun deletePost(post: Post) {
        albumDao.deleteAlbumsByPostId(post.id)
        commentDao.deleteCommentsByPostId(post.id)
        postDao.deletePost(post)
    }

    /* ---------- ALBUMS ---------- */

    fun getAlbumsByPostId(postId: Int): Flow<List<Album>> =
        albumDao.getAlbumsByPostId(postId)

    /* ---------- COMMENTS ---------- */

    fun getCommentsByPostId(postId: Int): Flow<List<Comment>> =
        commentDao.getCommentsByPostId(postId)

    suspend fun addComment(comment: Comment) {
        commentDao.insertComment(comment)
        val count = commentDao.getCommentCountByPostId(comment.postId)
        postDao.updateComments(comment.postId, count)
    }
}
