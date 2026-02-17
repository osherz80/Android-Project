package com.colProj.bookshare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.colProj.bookshare.data.local.PostDao
import com.colProj.bookshare.data.model.Post

@Database(entities = [Post::class, User::class], version = 6) // Incremented version
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bookshare_db"
                )
                .fallbackToDestructiveMigration() // Handle schema changes during dev
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
