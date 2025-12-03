package com.example.rankkings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rankkings.model.Album
import com.example.rankkings.model.Comment
import com.example.rankkings.model.Post
import com.example.rankkings.model.PostRequest
import com.example.rankkings.model.UserDto
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
import javax.inject.Named // Importar @Named

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: Repository, // Para operaciones locales (Room)
    @Named("UserApiService") private val apiService: ApiService // Para operaciones de Posts con Xano
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
                    repository.refreshPosts(postsFromApi) // Actualizar la base de datos local
                    _uiState.value = PostUiState.Idle // Reiniciar el estado UI a Idle después de cargar
                } else {
                    _uiState.value = PostUiState.Error(response.errorBody()?.string() ?: "Error al cargar posts de la API")
                }
            } catch (e: Exception) {
                _uiState.value = PostUiState.Error(e.message ?: "Error desconocido al cargar posts")
            }
            // Observar siempre la base de datos local
            repository.getAllPosts()
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error al observar posts de la DB") }
                .collect { _posts.value = it }
        }
    }

    fun loadUserPosts(userId: Int) {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            try {
                val response = apiService.getPosts()
                if (response.isSuccessful) {
                    val allPostsFromApi = response.body() ?: emptyList()
                    val userPosts = allPostsFromApi.filter { it.userId == userId }
                    _userPosts.value = userPosts // Actualizar el StateFlow de posts de usuario
                    _uiState.value = PostUiState.Idle
                } else {
                    _uiState.value = PostUiState.Error(response.errorBody()?.string() ?: "Error al cargar posts de usuario de la API")
                }
            } catch (e: Exception) {
                _uiState.value = PostUiState.Error(e.message ?: "Error desconocido al cargar posts de usuario")
            }
        }
    }

    suspend fun getPostById(postId: Int): Post? {
        // TODO: Podrías querer obtener este de la API en el futuro
        return repository.getPostById(postId)
    }

    fun getAlbumsForPost(postId: Int): Flow<List<Album>> {
        // TODO: Podrías querer obtener estos de la API en el futuro
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
                _uiState.value = PostUiState.Error("El título y al menos un álbum son obligatorios")
                return@launch
            }
            try {
                // === LLAMADA A LA API DE XANO para crear el post ===
                val postRequest = PostRequest(
                    userId = userId, // Ya es Int
                    name = name,
                    title = title,
                    description = description
                )
                val response = apiService.createPost(postRequest)

                if (response.isSuccessful) {
                    val createdPost = response.body()
                    if (createdPost != null) {
                        // Aquí, Xano debería devolver el Post creado con su ID
                        // Insertar el post creado en la base de datos local de Room
                        repository.createPost(createdPost)

                        // Asociar los álbumes con el ID del post recién creado y guardarlos localmente
                        val albumsWithPostId = albums.map { it.copy(postId = createdPost.id) }
                        repository.insertAlbums(albumsWithPostId)

                        _uiState.value = PostUiState.Success("Post creado exitosamente en Xano")
                        loadAllPosts() // Recargar todos los posts para reflejar el nuevo post en la UI
                    } else {
                        _uiState.value = PostUiState.Error("Respuesta de la API de posts vacía")
                    }
                } else {
                    _uiState.value = PostUiState.Error(response.errorBody()?.string() ?: "Error al crear post en Xano")
                }
            } catch (e: Exception) {
                _uiState.value = PostUiState.Error(e.message ?: "Error desconocido al crear post")
            }
        }
    }

    fun loadPostAlbums(postId: Int) {
        viewModelScope.launch {
            // TODO: Podrías querer cargar estos de la API en el futuro
            repository.getAlbumsByPostId(postId)
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error al cargar álbumes") }
                .collect { _postAlbums.value = it }
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            // TODO: Implementar interacción con la API de Xano para likes
            repository.toggleLike(post)
        }
    }

    fun toggleSave(post: Post) {
        viewModelScope.launch {
            // TODO: Implementar interacción con la API de Xano para guardar posts
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
            // TODO: Podrías querer cargar estos de la API en el futuro
            repository.getCommentsByPostId(postId)
                .catch { e -> _uiState.value = PostUiState.Error(e.message ?: "Error al cargar comentarios") }
                .collect { _postComments.value = it }
        }
    }

    fun addComment(postId: Int, userId: Int, name: String, content: String) {
        viewModelScope.launch {
            if (content.isBlank()) return@launch
            // TODO: Implementar interacción con la API de Xano para añadir comentarios
            val comment = Comment(postId = postId, userId = userId, name = name, content = content)
            repository.addComment(comment)
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            // TODO: Implementar interacción con la API de Xano para eliminar posts
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
