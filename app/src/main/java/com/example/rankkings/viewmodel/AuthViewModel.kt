package com.example.rankkings.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rankkings.model.LoginRequest
import com.example.rankkings.model.SignupRequest
import com.example.rankkings.model.User
import com.example.rankkings.network.ApiService
import com.example.rankkings.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

// ================= ADMIN TEMPORAL =================
private const val ADMIN_EMAIL = "admin@rankkings.com"
private const val ADMIN_PASSWORD = "Admin1234"
private const val ADMIN_TOKEN = "ADMIN_LOCAL_TOKEN"
// =================================================

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: Repository,
    @Named("AuthApiService") private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val USER_ID_KEY = intPreferencesKey("user_id")
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    private val WELCOME_SHOWN_KEY = stringPreferencesKey("welcome_shown_user")

    init {
        viewModelScope.launch {
            val userId = dataStore.data.map { it[USER_ID_KEY] }.firstOrNull()
            val authToken = dataStore.data.map { it[AUTH_TOKEN_KEY] }.firstOrNull()

            // 👑 ADMIN LOCAL
            if (authToken == ADMIN_TOKEN) {
                _currentUser.value = User(
                    id = -1,
                    name = "Administrador",
                    email = ADMIN_EMAIL,
                    passwordHash = ""
                )
                _authState.value = AuthState.Success
                return@launch
            }

            // 🔐 USUARIO NORMAL (XANO)
            if (userId != null && authToken != null) {
                try {
                    val response = apiService.getAuthUser()
                    if (response.isSuccessful) {
                        response.body()?.let {
                            _currentUser.value = User(
                                id = it.id,
                                name = it.name,
                                email = it.email,
                                passwordHash = ""
                            )
                            _authState.value = AuthState.Success
                        }
                    } else {
                        clearUserSession()
                        _authState.value = AuthState.Idle
                    }
                } catch (e: Exception) {
                    clearUserSession()
                    _authState.value = AuthState.Idle
                }
            }
        }
    }

    // 👑 ADMIN CHECK
    fun isAdmin(): Boolean =
        _currentUser.value?.email == ADMIN_EMAIL

    // 🎉 WELCOME POPUP
    suspend fun shouldShowWelcome(): Boolean {
        val email = _currentUser.value?.email ?: return false
        val shownFor = dataStore.data.map { it[WELCOME_SHOWN_KEY] }.firstOrNull()
        return shownFor != email
    }

    suspend fun markWelcomeShown() {
        val email = _currentUser.value?.email ?: return
        dataStore.edit { it[WELCOME_SHOWN_KEY] = email }
    }

    // ===============================
    // 🔐 AUTH
    // ===============================
    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (email.isBlank() || password.isBlank() || name.isBlank()) {
                _authState.value = AuthState.Error("Todos los campos son obligatorios")
                return@launch
            }

            val response = apiService.signup(SignupRequest(name, email, password))
            if (response.isSuccessful) {
                login(email, password)
            } else {
                _authState.value = AuthState.Error("Error de registro")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {

            // 👑 LOGIN ADMIN
            if (email == ADMIN_EMAIL && password == ADMIN_PASSWORD) {
                val admin = User(
                    id = -1,
                    name = "Administrador",
                    email = ADMIN_EMAIL,
                    passwordHash = ""
                )
                _currentUser.value = admin
                saveUserSession(admin.id, ADMIN_TOKEN)
                _authState.value = AuthState.Success
                return@launch
            }

            _authState.value = AuthState.Loading

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                _authState.value = AuthState.Error("Email inválido")
                return@launch
            }

            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val auth = response.body() ?: return@launch
                    val userResponse = apiService.getAuthUser("Bearer ${auth.authToken}")

                    if (userResponse.isSuccessful) {
                        userResponse.body()?.let {
                            val user = User(
                                id = it.id,
                                name = it.name,
                                email = it.email,
                                passwordHash = ""
                            )
                            _currentUser.value = user
                            saveUserSession(user.id, auth.authToken)
                            _authState.value = AuthState.Success
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Credenciales incorrectas")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            clearUserSession()
            _currentUser.value = null
            _authState.value = AuthState.Idle
        }
    }

    // ===============================
    // 🎵 INTERESES (FIX FINAL)
    // ===============================
    fun updateUserInterests(userId: Int, interests: List<String>) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val user = _currentUser.value
                if (user == null || user.id != userId) {
                    _authState.value = AuthState.Error("Usuario inválido")
                    return@launch
                }

                val updatedUser = user.copy(interests = interests)
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "Error al guardar intereses"
                )
            }
        }
    }

    // ===============================
    // 💾 DATASTORE
    // ===============================
    private suspend fun saveUserSession(userId: Int, token: String) {
        dataStore.edit {
            it[USER_ID_KEY] = userId
            it[AUTH_TOKEN_KEY] = token
        }
    }

    private suspend fun clearUserSession() {
        dataStore.edit {
            it.remove(USER_ID_KEY)
            it.remove(AUTH_TOKEN_KEY)
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
