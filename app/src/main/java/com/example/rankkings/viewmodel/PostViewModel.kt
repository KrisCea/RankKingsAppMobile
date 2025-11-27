package com.example.rankkings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rankkings.model.Album
import com.example.rankkings.model.Comment
import com.example.rankkings.model.Post
import com.example.rankkings.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts

    private val _postAlbums = MutableStateFlow<List<Album>>(emptyList())
    val postAlbums: StateFlow<List<Album>> = _postAlbums

    private val _postComments = MutableStateFlow<List<Comment>>(emptyList())
    val postComments: StateFlow<List<Comment>> = _postComments

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    val uiState: StateFlow<PostUiState> = _uiState

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    init {
        loadAllPosts()
    }

    fun loadAllPosts() {
        viewModelScope.launch {
            repository.getAllPosts()
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error al cargar posts") }
                .collect { _posts.value = it }
        }
    }

    fun loadUserPosts(userId: Int) {
        viewModelScope.launch {
            repository.getPostsByUserId(userId)
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error al cargar posts del usuario") }
                .collect { _userPosts.value = it }
        }
    }

    suspend fun getPostById(postId: Int): Post? {
        return repository.getPostById(postId)
    }

    fun getAlbumsForPost(postId: Int): Flow<List<Album>> {
        return repository.getAlbumsByPostId(postId)
    }

    fun createPost(
        userId: Int,
        username: String,
        title: String,
        description: String,
        albums: List<Album>
    ) {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            if (title.isBlank() || albums.isEmpty()) {
                _uiState.value = PostUiState.Error("El título y al menos un álbum son obligatorios")
                return@launch
            }
            try {
                val post = Post(userId = userId.toString(), username = username, title = title, description = description)
                val postId = repository.createPost(post)
                val albumsWithPostId = albums.map { it.copy(postId = postId.toInt()) }
                repository.insertAlbums(albumsWithPostId)
                _uiState.value = PostUiState.Success("Post creado exitosamente")
            } catch (e: Exception) {
                _uiState.value = PostUiState.Error(e.message ?: "Error al crear post")
            }
        }
    }

    fun loadPostAlbums(postId: Int) {
        viewModelScope.launch {
            repository.getAlbumsByPostId(postId)
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error al cargar álbumes") }
                .collect { _postAlbums.value = it }
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            repository.toggleLike(post)
        }
    }

    fun toggleSave(post: Post) {
        viewModelScope.launch {
            val newSaveStatus = !post.isSaved
            repository.toggleSave(post)
            if (newSaveStatus) {
                _snackbarEvent.emit("Añadido a guardados")
            } else {
                _snackbarEvent.emit("Eliminado de guardados")
            }
        }
    }

    fun loadComments(postId: Int) {
        viewModelScope.launch {
            repository.getCommentsByPostId(postId)
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error al cargar comentarios") }
                .collect { _postComments.value = it }
        }
    }

    fun addComment(postId: Int, userId: Int, username: String, content: String) {
        viewModelScope.launch {
            if (content.isBlank()) return@launch
            val comment = Comment(postId = postId, userId = userId, username = username, content = content)
            repository.addComment(comment)
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            repository.deletePost(post)
        }
    }

    fun resetUiState() {
        _uiState.value = PostUiState.Idle
    }
}

sealed class PostUiState {
    object Idle : PostUiState()
    object Loading : PostUiState()
    data class Success(val message: String) : PostUiState()
    data class Error(val message: String) : PostUiState()
}
