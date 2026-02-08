package com.colProj.bookshare.data

import androidx.room.*

@Dao
interface RecommendationDao {
    @Insert
    fun insertRecommendation(recommendation: Recommendation): Long

    @Query("SELECT * FROM recommendations WHERE userId = :userId ORDER BY timestamp DESC")
    fun getRecommendationsByUserId(userId: String): List<Recommendation>

    @Query("SELECT * FROM recommendations ORDER BY timestamp DESC")
    fun getAllRecommendations(): List<Recommendation>

    @Delete
    fun deleteRecommendation(recommendation: Recommendation)

    @Query("DELETE FROM recommendations WHERE id = :id")
    fun deleteRecommendationById(id: Long)

    @Query("DELETE FROM recommendations")
    fun deleteAll()
}
