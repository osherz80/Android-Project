package com.colProj.bookshare.data.remote

import com.colProj.bookshare.data.model.GoogleBooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("key") apiKey: String?,
        @Query("maxResults") maxResults: Int = 10
    ): GoogleBooksResponse
}
