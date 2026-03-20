package com.colProj.bookshare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    @PropertyName("id") val id: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("userName") val userName: String = "",
    @PropertyName("bookTitle") val bookTitle: String = "",
    @PropertyName("bookSummary") val bookSummary: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("author") val author: String = "",
    @PropertyName("rating") val rating: Float = 0f,
    @PropertyName("imageUrl") val imageUrl: String = "",
    @PropertyName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @PropertyName("localImagePath") val localImagePath: String? = null
)
