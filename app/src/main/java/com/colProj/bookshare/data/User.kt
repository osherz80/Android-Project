package com.colProj.bookshare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val email: String,
    val password: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val bio: String? = null,
    val isLoggedIn: Boolean = false,
    val localImagePath: String? = null
)
