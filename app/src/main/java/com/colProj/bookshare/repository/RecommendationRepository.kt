package com.colProj.bookshare.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.colProj.bookshare.data.AppDatabase
import com.colProj.bookshare.data.Recommendation
import java.util.concurrent.Executors

class RecommendationRepository private constructor(context: Context) {

    private val recommendationDao = AppDatabase.getDatabase(context).recommendationDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun saveRecommendation(
        recommendation: Recommendation,
        onResult: (Boolean, String?) -> Unit
    ) {
        executor.execute {
            try {
                recommendationDao.insertRecommendation(recommendation)
                mainHandler.post { onResult(true, null) }
            } catch (e: Exception) {
                mainHandler.post { onResult(false, e.message) }
            }
        }
    }

    fun getRecommendationsByUserId(
        userId: String,
        onResult: (List<Recommendation>) -> Unit
    ) {
        executor.execute {
            try {
                val recommendations = recommendationDao.getRecommendationsByUserId(userId)
                mainHandler.post { onResult(recommendations) }
            } catch (e: Exception) {
                mainHandler.post { onResult(emptyList()) }
            }
        }
    }

    fun getAllRecommendations(onResult: (List<Recommendation>) -> Unit) {
        executor.execute {
            try {
                val recommendations = recommendationDao.getAllRecommendations()
                mainHandler.post { onResult(recommendations) }
            } catch (e: Exception) {
                mainHandler.post { onResult(emptyList()) }
            }
        }
    }

    fun deleteRecommendation(
        recommendationId: Long,
        onResult: (Boolean) -> Unit
    ) {
        executor.execute {
            try {
                recommendationDao.deleteRecommendationById(recommendationId)
                mainHandler.post { onResult(true) }
            } catch (e: Exception) {
                mainHandler.post { onResult(false) }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RecommendationRepository? = null

        fun getInstance(context: Context): RecommendationRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = RecommendationRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
