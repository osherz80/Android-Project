package com.colProj.bookshare.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colProj.bookshare.data.model.Post

@Dao
interface PostDao {
    @Query("""
        SELECT posts.*, COALESCE(users.displayName, posts.userName) as userName 
        FROM posts 
        LEFT JOIN users ON posts.userId = users.uid 
        WHERE 
            (:userId IS NULL OR posts.userId = :userId) AND 
            (:title IS NULL OR posts.bookTitle = :title) AND 
            (:query IS NULL OR (
                posts.bookTitle LIKE '%' || :query || '%' OR 
                posts.author LIKE '%' || :query || '%' OR 
                COALESCE(users.displayName, posts.userName) LIKE '%' || :query || '%'
            ))
        ORDER BY timestamp DESC
    """)
    fun getPosts(
        query: String? = null,
        userId: String? = null,
        title: String? = null
    ): LiveData<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()

    @Query("DELETE FROM posts WHERE userId = :userId")
    suspend fun deleteUserPosts(userId: String)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)
}
