package com.example.rankkings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rankkings.model.Album
import com.example.rankkings.model.Comment
import com.example.rankkings.model.Post
import com.example.rankkings.model.PostRequest
import com.example.rankkings.network.ApiService
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
import javax.inject.Named

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: Repository,
    @Named("UserApiService") private val apiService: ApiService
) : ViewModel() {

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
            _uiState.value = PostUiState.Loading

            try {
                val response = apiService.getPosts()
                if (response.isSuccessful) {
                    val postsFromApi = response.body() ?: emptyList()
                    repository.refreshPosts(postsFromApi)
                    _uiState.value = PostUiState.Idle
                } else {
                    _uiState.value = PostUiState.Error(
                        response.errorBody()?.string() ?: "Error al cargar posts"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PostUiState.Error(e.message ?: "Error desconocido")
            }

            repository.getAllPosts()
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error DB") }
                .collect { _posts.value = it }
        }
    }

    fun loadUserPosts(userId: Int) {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            try {
                val response = apiService.getPosts()
                if (response.isSuccessful) {
                    val allPosts = response.body() ?: emptyList()
                    _userPosts.value = allPosts.filter { it.userId == userId }
                    _uiState.value = PostUiState.Idle
                } else {
                    _uiState.value = PostUiState.Error("Error al cargar posts del usuario")
                }
            } catch (e: Exception) {
                _uiState.value = PostUiState.Error(e.message ?: "Error desconocido")
            }
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
        name: String,
        title: String,
        description: String,
        albums: List<Album>
    ) {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading

            if (title.isBlank() || albums.isEmpty()) {
                _uiState.value =
                    PostUiState.Error("El título y al menos un álbum son obligatorios")
                return@launch
            }

            try {
                val postRequest = PostRequest(
                    userId = userId,   // <-- userId como INT (correcto)
                    name = name,
                    title = title,
                    description = description
                )

                val response = apiService.createPost(postRequest)

                if (response.isSuccessful) {
                    val createdPost = response.body()

                    if (createdPost != null) {
                        repository.createPost(createdPost)

                        val albumsWithPostId =
                            albums.map { it.copy(postId = createdPost.id) }

                        repository.insertAlbums(albumsWithPostId)

                        _uiState.value = PostUiState.Success("Post creado exitosamente")
                        loadAllPosts()
                    } else {
                        _uiState.value =
                            PostUiState.Error("La API devolvió un post vacío")
                    }

                } else {
                    _uiState.value = PostUiState.Error(
                        response.errorBody()?.string() ?: "Error en la API"
                    )
                }

            } catch (e: Exception) {
                _uiState.value =
                    PostUiState.Error(e.message ?: "Error desconocido al crear post")
            }
        }
    }

    fun loadPostAlbums(postId: Int) {
        viewModelScope.launch {
            repository.getAlbumsByPostId(postId)
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error álbumes") }
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
            val newStatus = !post.isSaved
            repository.toggleSave(post)

            _snackbarEvent.emit(
                if (newStatus) "Añadido a guardados" else "Eliminado de guardados"
            )
        }
    }

    fun loadComments(postId: Int) {
        viewModelScope.launch {
            repository.getCommentsByPostId(postId)
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error comentarios") }
                .collect { _postComments.value = it }
        }
    }

    fun addComment(postId: Int, userId: Int, name: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val comment = Comment(
                postId = postId,
                userId = userId,
                name = name,
                content = content
            )
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
