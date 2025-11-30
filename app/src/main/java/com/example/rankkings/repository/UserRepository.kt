package com.example.rankkings.repository

import com.example.rankkings.model.UserDto
import com.example.rankkings.network.ApiService
import javax.inject.Inject
import javax.inject.Named // Importar @Named

class UserRepository @Inject constructor(@Named("UserApiService") private val apiService: ApiService) {

    suspend fun createUser(user: UserDto) = apiService.createUser(user)

    suspend fun getAllUsers() = apiService.getAllUsers()

    suspend fun updateUser(userId: Int, user: UserDto) = apiService.updateUser(userId, user)

    suspend fun getUserById(userId: Int) = apiService.getUserById(userId)

    suspend fun deleteUser(userId: Int) = apiService.deleteUser(userId)

    suspend fun editUser(userId: Int, user: UserDto) = apiService.editUser(userId, user)
}
