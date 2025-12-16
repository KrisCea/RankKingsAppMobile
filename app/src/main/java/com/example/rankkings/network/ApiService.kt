package com.example.rankkings.network

import com.example.rankkings.model.AuthResponse
import com.example.rankkings.model.LoginRequest
import com.example.rankkings.model.Post
import com.example.rankkings.model.PostRequest
import com.example.rankkings.model.SignupRequest
import com.example.rankkings.model.UserDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth Endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    // Endpoint para obtener datos del usuario autenticado (usando el token del interceptor)
    @GET("auth/me")
    suspend fun getAuthUser(): Response<UserDto>

    // User Endpoints (Estos ahora requerirán autenticación)
    @POST("user")
    suspend fun createUser(@Body user: UserDto): Response<UserDto>

    @GET("user")
    suspend fun getAllUsers(): Response<List<UserDto>>

    @PUT("user/{user_id}")
    suspend fun updateUser(@Path("user_id") userId: Int, @Body user: UserDto): Response<UserDto>

    @GET("user/{user_id}")
    suspend fun getUserById(@Path("user_id") userId: Int): Response<UserDto>

    @DELETE("user/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int): Response<Unit>

    @PATCH("user/{user_id}")
    suspend fun editUser(@Path("user_id") userId: Int, @Body user: UserDto): Response<UserDto>

    // Post Endpoints
    @GET("post")
    suspend fun getPosts(): Response<List<Post>>

    @POST("post")
    suspend fun createPost(@Body request: PostRequest): Response<Post>
}
