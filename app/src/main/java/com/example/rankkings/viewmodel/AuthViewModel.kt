package com.example.rankkings.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rankkings.model.User
import com.example.rankkings.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import at.favre.lib.crypto.bcrypt.BCrypt
import javax.inject.Inject

// Para DataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: Repository,
    private val dataStore: DataStore<Preferences> // Inyectar DataStore
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val TAG = "AuthViewModel"

    // Clave para almacenar el ID del usuario en DataStore
    private val USER_ID_KEY = intPreferencesKey("user_id")

    init {
        // Intentar cargar la sesión del usuario al inicializar el ViewModel
        viewModelScope.launch {
            dataStore.data
                .map { preferences -> preferences[USER_ID_KEY] }
                .firstOrNull() // Obtener el primer valor o null si no existe
                ?.let { userId ->
                    Log.d(TAG, "init: Found user ID in DataStore: $userId. Loading user...")
                    // Cargar el usuario completo desde el repositorio
                    repository.getUserById(userId).collect {
                        _currentUser.value = it
                        if (it != null) {
                            _authState.value = AuthState.Success
                            Log.d(TAG, "init: User loaded successfully from DataStore: ${it.username}")
                        } else {
                            Log.e(TAG, "init: User not found in DB for ID: $userId. Clearing DataStore.")
                            clearUserSession()
                        }
                    }
                }
        }
    }

    fun getUserById(userId: String): Flow<User?> {
        return repository.getUserById(userId.toInt())
    }

    fun updateUserInterests(userId: Int, interests: List<String>) {
        viewModelScope.launch {
            Log.d(TAG, "updateUserInterests: Attempting to update for userId: $userId with interests: $interests")
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "updateUserInterests: AuthState set to Loading")

                // Obtener el usuario actual, actualizarlo y guardarlo
                val userToUpdate = _currentUser.value
                if (userToUpdate != null && userToUpdate.id == userId) {
                    val updatedUser = userToUpdate.copy(interests = interests)
                    repository.updateUser(updatedUser) // <-- LLAMADA CORREGIDA
                    _currentUser.value = updatedUser // Actualizar estado local
                    _authState.value = AuthState.Success
                    Log.d(TAG, "updateUserInterests: User updated successfully in DB and locally.")
                } else {
                    throw IllegalStateException("User not found or ID mismatch")
                }
                
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
                Log.e(TAG, "updateUserInterests: Error: ${e.message}", e)
            }
        }
    }

    fun updateProfileImage(userId: Int, imageUri: String) {
        viewModelScope.launch {
            try {
                repository.updateUserProfileImage(userId, imageUri)
                if (_currentUser.value?.id == userId) {
                    _currentUser.value = _currentUser.value?.copy(profileImageUri = imageUri)
                    Log.d(TAG, "updateProfileImage: Current user profile image updated locally. New URI: ${imageUri}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateProfileImage: Error: ${e.message}")
            }
        }
    }

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            Log.d(TAG, "Register attempt for: $email, $username")
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "Register: AuthState set to Loading")

                if (username.isBlank() || email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Todos los campos son obligatorios")
                    Log.d(TAG, "Register: Validation Error: ${_authState.value}")
                    return@launch
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("El formato del email no es válido")
                    Log.d(TAG, "Register: Validation Error: ${_authState.value}")
                    return@launch
                }
                if (password.length < 6) {
                    _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
                    Log.d(TAG, "Register: Validation Error: ${_authState.value}")
                    return@launch
                }

                val existingUser = repository.getUserByUsername(username)
                if (existingUser != null) {
                    _authState.value = AuthState.Error("El nombre de usuario ya está en uso")
                    Log.d(TAG, "Register: Validation Error: ${_authState.value}")
                    return@launch
                }
                 val existingUserEmail = repository.getUserByEmail(email)
                if (existingUserEmail != null) {
                    _authState.value = AuthState.Error("El email ya está registrado")
                    Log.d(TAG, "Register: Validation Error: ${_authState.value}")
                    return@launch
                }

                val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                Log.d(TAG, "Register: Password hashed.")

                val newUser = User(
                    username = username,
                    email = email,
                    password = hashedPassword
                )

                val userId = repository.registerUser(newUser)
                Log.d(TAG, "Register: User registered with ID: $userId")

                if (userId > 0) {
                    val registeredUser = newUser.copy(id = userId.toInt())
                    _currentUser.value = registeredUser
                    _authState.value = AuthState.Success
                    saveUserSession(registeredUser.id) // Guardar sesión
                    Log.d(TAG, "Register: AuthState set to Success. Current User: ${_currentUser.value?.username}")
                } else {
                    _authState.value = AuthState.Error("Error al registrar usuario")
                    Log.e(TAG, "Register: Failed to register user. AuthState: ${_authState.value}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
                Log.e(TAG, "Register: Exception: ${e.message}", e)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "Login attempt for: $email")
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "Login: AuthState set to Loading")

                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Email y contraseña son obligatorios")
                    Log.d(TAG, "Login: Validation Error: ${_authState.value}")
                    return@launch
                }
                 if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("El formato del email no es válido")
                    Log.d(TAG, "Login: Validation Error: ${_authState.value}")
                    return@launch
                }

                val user = repository.getUserByEmail(email)
                Log.d(TAG, "Login: User fetched from DB: ${user?.username ?: "null"}")

                if (user != null && BCrypt.verifyer().verify(password.toCharArray(), user.password.toCharArray()).verified) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success
                    saveUserSession(user.id) // Guardar sesión
                    Log.d(TAG, "Login: AuthState set to Success. Current User: ${_currentUser.value?.username}")
                } else {
                    _authState.value = AuthState.Error("Email o contraseña incorrectos")
                    Log.d(TAG, "Login: Failed. AuthState: ${_authState.value}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
                Log.e(TAG, "Login: Exception: ${e.message}", e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            clearUserSession() // Limpiar sesión de DataStore
            _currentUser.value = null
            _authState.value = AuthState.Idle
            Log.d(TAG, "Logout: Current User set to null. AuthState set to Idle")
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
        Log.d(TAG, "resetAuthState: AuthState reset to Idle")
    }

    private suspend fun saveUserSession(userId: Int) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            Log.d(TAG, "saveUserSession: User ID $userId saved to DataStore.")
        }
    }

    private suspend fun clearUserSession() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            Log.d(TAG, "clearUserSession: User session cleared from DataStore.")
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
