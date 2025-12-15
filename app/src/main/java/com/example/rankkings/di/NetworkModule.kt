package com.example.rankkings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.rankkings.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton
import android.util.Log

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val AUTH_BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:z5Tqs_nA/"
    private const val USER_BASE_URL = "https://x8ki-letl-twmt.n7.xano.io/api:UMs5_miE/"

    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

    @Provides
    @Singleton
    fun provideOkHttpClient(dataStore: DataStore<Preferences>): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor {
            chain ->
            val originalRequest = chain.request()
            val token = runBlocking { dataStore.data.first()[AUTH_TOKEN_KEY] }

            val requestBuilder = originalRequest.newBuilder()
            token?.let {
                Log.d("AuthInterceptor", "Token found: $it")
                requestBuilder.header("Authorization", "Bearer $it")
            } ?: run {
                Log.d("AuthInterceptor", "No token found in DataStore.")
            }
            chain.proceed(requestBuilder.build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideAuthRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("UserRetrofit")
    fun provideUserRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(USER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthApiService")
    fun provideAuthApiService(@Named("AuthRetrofit") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("UserApiService")
    fun provideUserApiService(@Named("UserRetrofit") retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
