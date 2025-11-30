package com.example.rankkings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val profileImageUri: String? = null,
    val interests: List<String>? = null // Añadido para los intereses del usuario
)
