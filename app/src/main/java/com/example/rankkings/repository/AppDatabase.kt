package com.example.rankkings.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rankkings.model.Album
import com.example.rankkings.model.Comment
import com.example.rankkings.model.Post
import com.example.rankkings.model.User

/**
 * Base de datos principal de la aplicación
 * Contiene todas las entidades y proporciona acceso a los DAOs
 */
@Database(
    entities = [User::class, Post::class, Album::class, Comment::class],
    version = 5, // <-- VERSIÓN INCREMENTADA
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // DAOs abstractos
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun albumDao(): AlbumDao
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos (Singleton)
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rankkings_database"
                )
                    .fallbackToDestructiveMigration() // En desarrollo, recrea BD si cambia versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
