package com.example.rankkings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rankkings.model.Album
import com.example.rankkings.model.Comment
import com.example.rankkings.model.Post
import com.example.rankkings.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    /* ---------- POSTS ---------- */

    val posts: StateFlow<List<Post>> =
        repository.getPublicPosts()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun loadUserPosts(userId: Int): Flow<List<Post>> =
        repository.getPostsByUserId(userId)

    /* ---------- ALBUMS (FIX CLAVE) ---------- */

    fun getAlbumsForPost(postId: Int): Flow<List<Album>> =
        repository.getAlbumsByPostId(postId)

    /* ---------- COMMENTS ---------- */

    fun getCommentsForPost(postId: Int): Flow<List<Comment>> =
        repository.getCommentsByPostId(postId)

    fun addComment(
        postId: Int,
        userId: Int,
        name: String,
        content: String
    ) {
        if (content.isBlank()) return

        viewModelScope.launch {
            repository.addComment(
                Comment(
                    postId = postId,
                    userId = userId,
                    name = name,
                    content = content
                )
            )
        }
    }

    /* ---------- ACTIONS ---------- */

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            repository.toggleLike(post)
        }
    }

    fun toggleSave(post: Post) {
        viewModelScope.launch {
            repository.toggleSave(post)
        }
    }

    fun togglePrivacy(post: Post) {
        viewModelScope.launch {
            repository.togglePrivacy(post)
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            repository.deletePost(post)
        }
    }
    private val _showTutorial = MutableStateFlow(true)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    private val _tutorialText = MutableStateFlow(
        "🔥 Desliza para ver los posts, vota con ❤️ y crea el tuyo con el botón +"
    )
    val tutorialText: StateFlow<String> = _tutorialText.asStateFlow()

    fun closeTutorial() {
        _showTutorial.value = false
    }
}

