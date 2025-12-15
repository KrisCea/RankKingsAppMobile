package com.example.rankkings.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("authToken")
    val authToken: String,
    @SerializedName("user_id") // Cambiado de "user" a "user_id"
    val userId: Int // Cambiado de UserDto a Int
)
