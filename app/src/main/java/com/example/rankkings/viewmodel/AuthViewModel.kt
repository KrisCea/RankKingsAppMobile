package com.example.rankkings.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rankkings.model.LoginRequest
import com.example.rankkings.model.SignupRequest
import com.example.rankkings.model.User
import com.example.rankkings.model.UserDto
import com.example.rankkings.network.ApiService
import com.example.rankkings.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import at.favre.lib.crypto.bcrypt.BCrypt
import javax.inject.Inject
import javax.inject.Named // Importar @Named

// Para DataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: Repository, // Para operaciones locales de usuario (Room)
    @Named("AuthApiService") private val apiService: ApiService, // Para operaciones de autenticación con Xano
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null) // Sigue siendo la entidad local de Room
    val currentUser: StateFlow<User?> = _currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val TAG = "AuthViewModel"

    // Claves para almacenar en DataStore
    private val USER_ID_KEY = intPreferencesKey("user_id")
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token") // Nueva clave para el token

    init {
        viewModelScope.launch {
            val userId = dataStore.data.map { preferences -> preferences[USER_ID_KEY] }.firstOrNull()
            val authToken = dataStore.data.map { preferences -> preferences[AUTH_TOKEN_KEY] }.firstOrNull()

            if (userId != null && authToken != null) {
                Log.d(TAG, "init: Found user ID and Auth Token in DataStore. Loading user...")
                // Usar auth/me para obtener los datos del usuario de Xano
                try {
                    // Esta llamada todavía depende del interceptor, ya que es al inicio de la app
                    val response = apiService.getAuthUser()
                    if (response.isSuccessful) {
                        val xanoUser = response.body()
                        if (xanoUser != null) {
                            // Mapear UserDto a User (entidad de Room) para compatibilidad local
                            val localUser = User(id = xanoUser.id, name = xanoUser.name, email = xanoUser.email, passwordHash = "", profileImageUri = null, interests = null) // TODO: Ajustar mapeo completo
                            _currentUser.value = localUser
                            _authState.value = AuthState.Success
                            Log.d(TAG, "init: User loaded successfully from Xano: ${xanoUser.name}")
                        } else {
                            Log.e(TAG, "init: Xano user data is null. Clearing session.")
                            clearUserSession()
                        }
                    } else {
                        Log.e(TAG, "init: Xano auth/me failed: ${response.code()} ${response.message()}. Clearing session.")
                        clearUserSession()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "init: Exception calling auth/me: ${e.message}. Clearing session.", e)
                    clearUserSession()
                }
            } else {
                Log.d(TAG, "init: No user ID or Auth Token found in DataStore.")
                _authState.value = AuthState.Idle
            }
        }
    }

    fun getUserById(userId: String): Flow<User?> {
        // Esta función podría necesitar ser adaptada para obtener datos de Xano también, si no están en Room
        return repository.getUserById(userId.toInt())
    }

    fun updateUserInterests(userId: Int, interests: List<String>) {
        viewModelScope.launch {
            Log.d(TAG, "updateUserInterests: Attempting to update for userId: $userId with interests: $interests")
            try {
                _authState.value = AuthState.Loading
                Log.d(TAG, "updateUserInterests: AuthState set to Loading")

                val userToUpdate = _currentUser.value
                if (userToUpdate != null && userToUpdate.id == userId) {
                    val updatedUser = userToUpdate.copy(interests = interests)
                    repository.updateUser(updatedUser)
                    _currentUser.value = updatedUser
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

    fun register(email: String, password: String, name: String) { // Ahora name en lugar de username
        viewModelScope.launch {
            Log.d(TAG, "Register attempt for: $email, $name")
            try {
                _authState.value = AuthState.Loading

                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Todos los campos son obligatorios")
                    return@launch
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("El formato del email no es válido")
                    return@launch
                }
                if (password.length < 6) {
                    _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
                    return@launch
                }

                // === Llamada a la API de Xano para el registro ===
                val request = SignupRequest(name = name, email = email, password = password)
                val response = apiService.signup(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        val xanoUserId = authResponse.userId
                        val authToken = authResponse.authToken

                        // Obtener los detalles completos del usuario de Xano usando el token recién adquirido
                        val userDetailsResponse = apiService.getAuthUser("Bearer $authToken")
                        if (userDetailsResponse.isSuccessful) {
                            val xanoUser = userDetailsResponse.body()
                            if (xanoUser != null) {
                                // Mapear UserDto a User (entidad de Room) para compatibilidad local
                                val localUser = User(id = xanoUser.id, name = xanoUser.name, email = xanoUser.email, passwordHash = "", profileImageUri = null, interests = null) // TODO: Ajustar mapeo completo
                                
                                // Guardar en Room si es necesario (ej. para caché offline o datos adicionales)
                                val userId = repository.registerUser(localUser)
                                Log.d(TAG, "Register: User registered locally with ID: $userId")

                                _currentUser.value = localUser.copy(id = userId.toInt()) // Actualizar el ID si Room lo autogeneró
                                _authState.value = AuthState.Success
                                saveUserSession(_currentUser.value!!.id, authToken) // Guardar sesión y token
                                Log.d(TAG, "Register: AuthState set to Success. Current User: ${_currentUser.value?.name}")
                            } else {
                                _authState.value = AuthState.Error("Detalles de usuario de Xano nulos después del registro.")
                                Log.e(TAG, "Register: Xano user details are null after signup.")
                                clearUserSession()
                            }
                        } else {
                            _authState.value = AuthState.Error("Error al obtener detalles del usuario de Xano después del registro.")
                            Log.e(TAG, "Register: Failed to get user details from Xano after signup: ${userDetailsResponse.code()} ${userDetailsResponse.message()}")
                            clearUserSession()
                        }
                    } else {
                        _authState.value = AuthState.Error("Respuesta de registro vacía")
                    }
                } else {
                    _authState.value = AuthState.Error(response.errorBody()?.string() ?: "Error de registro")
                    Log.e(TAG, "Register: Xano signup failed: ${response.code()} ${response.errorBody()?.string()}")
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

                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Email y contraseña son obligatorios")
                    return@launch
                }
                 if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("El formato del email no es válido")
                    return@launch
                }

                // === Llamada a la API de Xano para el login ===
                val request = LoginRequest(email = email, password = password)
                val response = apiService.login(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        val xanoUserId = authResponse.userId
                        val authToken = authResponse.authToken

                        // Obtener los detalles completos del usuario de Xano usando el token recién adquirido
                        val userDetailsResponse = apiService.getAuthUser("Bearer $authToken")
                        if (userDetailsResponse.isSuccessful) {
                            val xanoUser = userDetailsResponse.body()
                            if (xanoUser != null) {
                                // Mapear UserDto a User (entidad de Room) para compatibilidad local
                                val localUser = User(id = xanoUser.id, name = xanoUser.name, email = xanoUser.email, passwordHash = "", profileImageUri = null, interests = null) // TODO: Ajustar mapeo completo

                                // Opcional: Actualizar usuario en Room si ya existe, o insertarlo
                                // Podrías tener una función upsertUser en tu DAO
                                val existingLocalUser = repository.getUserByEmail(email)
                                if (existingLocalUser != null) {
                                    repository.updateUser(localUser.copy(id = existingLocalUser.id))
                                    _currentUser.value = localUser.copy(id = existingLocalUser.id)
                                } else {
                                    val userId = repository.registerUser(localUser)
                                    _currentUser.value = localUser.copy(id = userId.toInt())
                                }

                                _authState.value = AuthState.Success
                                saveUserSession(_currentUser.value!!.id, authToken) // Guardar sesión y token
                                Log.d(TAG, "Login: AuthState set to Success. Current User: ${_currentUser.value?.name}")
                            } else {
                                _authState.value = AuthState.Error("Detalles de usuario de Xano nulos después del login.")
                                Log.e(TAG, "Login: Xano user details are null after login.")
                                clearUserSession()
                            }
                        } else {
                            _authState.value = AuthState.Error("Error al obtener detalles del usuario de Xano después del login.")
                            Log.e(TAG, "Login: Failed to get user details from Xano after login: ${userDetailsResponse.code()} ${userDetailsResponse.message()}")
                            clearUserSession()
                        }
                    } else {
                        _authState.value = AuthState.Error("Respuesta de login vacía")
                    }
                } else {
                    _authState.value = AuthState.Error(response.errorBody()?.string() ?: "Error de login")
                    Log.e(TAG, "Login: Xano login failed: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
                Log.e(TAG, "Login: Exception: ${e.message}", e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            clearUserSession() // Limpiar sesión de DataStore y token
            _currentUser.value = null
            _authState.value = AuthState.Idle
            Log.d(TAG, "Logout: Current User set to null. AuthState set to Idle")
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
        Log.d(TAG, "resetAuthState: AuthState reset to Idle")
    }

    private suspend fun saveUserSession(userId: Int, authToken: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[AUTH_TOKEN_KEY] = authToken
            Log.d(TAG, "saveUserSession: User ID $userId and Auth Token saved to DataStore.")
        }
    }

    private suspend fun clearUserSession() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(AUTH_TOKEN_KEY)
            Log.d(TAG, "clearUserSession: User session and Auth Token cleared from DataStore.")
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
