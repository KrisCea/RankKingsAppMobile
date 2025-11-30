package com.example.rankkings.repository

import androidx.room.*
import com.example.rankkings.model.User
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de usuarios
 */
@Dao
interface UserDao {

    // Insertar un usuario
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    // Obtener usuario por email
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // Obtener usuario por nombre
    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): User?

    // Obtener usuario por ID
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): Flow<User?>

    // Obtener todos los usuarios
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    // Actualizar solo la imagen de perfil de un usuario
    @Query("UPDATE users SET profileImageUri = :imageUri WHERE id = :userId")
    suspend fun updateUserProfileImage(userId: Int, imageUri: String)

    // Actualizar usuario
    @Update
    suspend fun updateUser(user: User)

    // Eliminar usuario
    @Delete
    suspend fun deleteUser(user: User)
}
