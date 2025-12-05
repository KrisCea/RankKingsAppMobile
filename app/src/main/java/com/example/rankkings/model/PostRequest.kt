package com.example.rankkings.model

import com.google.gson.annotations.SerializedName

data class PostRequest(
    @SerializedName("userId")
    val userId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String
    // Otros campos como albums se añadirán en un paso posterior si Xano lo requiere directamente en el endpoint /post
)
