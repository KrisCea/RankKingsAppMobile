package com.example.rankkings.viewmodel

import android.util.Log
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

sealed interface CreatePostState {
    object Idle : CreatePostState
    object Loading : CreatePostState
    object Success : CreatePostState
    data class Error(val message: String) : CreatePostState
}

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    /* ---------- CREATE POST STATE ---------- */

    private val _createPostState =
        MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val createPostState = _createPostState.asStateFlow()

    fun resetCreatePostState() {
        _createPostState.value = CreatePostState.Idle
    }

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

    fun createPostWithAlbums(post: Post, albums: List<Album>) {
        viewModelScope.launch {
            _createPostState.value = CreatePostState.Loading
            try {
                repository.createPostWithAlbums(post, albums)
                _createPostState.value = CreatePostState.Success
            } catch (e: Exception) {
                Log.e("CREATE_POST", "Error creando post", e)
                _createPostState.value =
                    CreatePostState.Error(e.message ?: "Error creando post")
            }
        }
    }

    /* ---------- ALBUMS ---------- */

    fun getAlbumsByPost(postId: Int): Flow<List<Album>> =
        repository.getAlbumsByPostId(postId)

    /* ---------- COMMENTS ---------- */

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    fun loadComments(postId: Int) {
        viewModelScope.launch {
            repository.getCommentsByPostId(postId)
                .collect { list ->
                    _comments.value = list
                }
        }
    }

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

    fun toggleLike(post: Post) =
        viewModelScope.launch { repository.toggleLike(post) }

    fun toggleSave(post: Post) =
        viewModelScope.launch { repository.toggleSave(post) }

    fun togglePrivacy(post: Post) =
        viewModelScope.launch { repository.togglePrivacy(post) }

    fun deletePost(post: Post) =
        viewModelScope.launch { repository.deletePost(post) }

    /* ---------- TUTORIAL ---------- */

    private val _showTutorial = MutableStateFlow(true)
    val showTutorial = _showTutorial.asStateFlow()

    val tutorialText = MutableStateFlow(
        "¡Bienvenido a RankKings! Desliza para descubrir nuevos rankings y pulsa '+' para crear el tuyo."
    )

    fun closeTutorial() {
        _showTutorial.value = false
    }
}
