package com.example.rankkings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.rankkings.repository.AppDatabase
import com.example.rankkings.repository.Repository
import com.example.rankkings.repository.AlbumDao
import com.example.rankkings.repository.CommentDao
import com.example.rankkings.repository.PostDao
import com.example.rankkings.repository.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Instancia de DataStore a nivel de archivo para ser utilizada por Hilt
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "rankkings-db"
    ).build()

    @Singleton
    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Singleton
    @Provides
    fun providePostDao(db: AppDatabase): PostDao = db.postDao()

    @Singleton
    @Provides
    fun provideAlbumDao(db: AppDatabase): AlbumDao = db.albumDao()

    @Singleton
    @Provides
    fun provideCommentDao(db: AppDatabase): CommentDao = db.commentDao()

    @Singleton
    @Provides
    fun provideRepository(userDao: UserDao, postDao: PostDao, albumDao: AlbumDao, commentDao: CommentDao): Repository = Repository(userDao, postDao, albumDao, commentDao)

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
