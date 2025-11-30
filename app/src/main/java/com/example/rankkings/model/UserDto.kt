package com.example.rankkings.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String
    // Agrega aquí otros campos que pueda tener tu objeto User
)
