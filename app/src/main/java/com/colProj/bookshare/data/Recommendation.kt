package com.colProj.bookshare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendations")
data class Recommendation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val bookTitle: String,
    val bookAuthor: String,
    val bookCoverUri: String? = null,
    val rating: Int, // 1-5
    val recommendationText: String,
    val timestamp: Long = System.currentTimeMillis()
)
