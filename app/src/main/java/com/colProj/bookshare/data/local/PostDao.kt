package com.colProj.bookshare.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colProj.bookshare.data.model.Post

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE bookTitle = :title ORDER BY timestamp DESC")
    fun getPostsByTitle(title: String): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserPosts(userId: String): LiveData<List<Post>>

    @Query("SELECT * FROM posts WHERE bookTitle LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR userName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchPosts(query: String): LiveData<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()
}
