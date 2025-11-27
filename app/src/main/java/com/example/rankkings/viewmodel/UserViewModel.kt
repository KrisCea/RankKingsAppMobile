package com.example.rankkings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rankkings.model.User
import com.example.rankkings.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel para manejo de perfil de usuario
 */
class UserViewModel(private val repository: Repository) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Idle)
    val uiState: StateFlow<UserUiState> = _uiState

    /**
     * Carga los datos de un usuario por ID
     */
    fun loadUser(userId: Int) {
        viewModelScope.launch {
            try {
                repository.getUserById(userId).collect {
                    _user.value = it
                }
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Error al cargar usuario")
            }
        }
    }

    /**
     * Actualiza los datos del usuario
     */
    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                _uiState.value = UserUiState.Loading
                repository.updateUser(user)
                _user.value = user
                _uiState.value = UserUiState.Success("Perfil actualizado")
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Error al actualizar perfil")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UserUiState.Idle
    }
}

sealed class UserUiState {
    object Idle : UserUiState()
    object Loading : UserUiState()
    data class Success(val message: String) : UserUiState()
    data class Error(val message: String) : UserUiState()
}

class UserViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
