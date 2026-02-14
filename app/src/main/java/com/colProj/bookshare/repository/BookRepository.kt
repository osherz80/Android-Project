package com.colProj.bookshare.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.colProj.bookshare.data.AppDatabase
import com.colProj.bookshare.data.model.GoogleBookItem
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.data.remote.RetrofitClient
import com.colProj.bookshare.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookRepository(context: Context) {

    private val postDao = AppDatabase.getDatabase(context).postDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postsCollection = firestore.collection("posts")

    val allPosts: LiveData<List<Post>> = postDao.getAllPosts()

    fun getUserPosts(userId: String): LiveData<List<Post>> {
        return postDao.getUserPosts(userId)
    }

    fun getPostsByTitle(title: String): LiveData<List<Post>> {
        return postDao.getPostsByTitle(title)
    }

    fun searchPosts(query: String): LiveData<List<Post>> {
        return postDao.searchPosts(query)
    }

    suspend fun refreshPosts(): Resource<Unit> {
        return try {
            val snapshot = postsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val posts = snapshot.toObjects(Post::class.java)

            withContext(Dispatchers.IO) {
                postDao.clearAllPosts()
                postDao.insertPosts(posts)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch posts")
        }
    }

    suspend fun refreshUserPosts(userId: String): Resource<Unit> {
         return try {
             val snapshot = postsCollection
                 .whereEqualTo("userId", userId)
                 .get()
                 .await()
             val posts = snapshot.toObjects(Post::class.java)
             // The query above doesn't support ordering by default without a composite index.
             // We can sort in memory since the user's post count shouldn't be massive.
             val sortedPosts = posts.sortedByDescending { it.timestamp }

             withContext(Dispatchers.IO) {
                 postDao.deleteUserPosts(userId)
                 postDao.insertPosts(sortedPosts)
             }
             Resource.Success(Unit)
         } catch (e: Exception) {
             Resource.Error(e.message ?: "Failed to fetch user posts")
         }
    }

    suspend fun addPost(post: Post): Resource<Unit> {
        return try {
            val document = postsCollection.document()
            val newPost = post.copy(id = document.id)
            
            // 1. Firestore (Async operation with timeout)
            document.set(newPost)

            // 2. Room (Blocking IO operation)
            withContext(Dispatchers.IO) {
                postDao.insertPost(newPost)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add post")
        }
    }
    
    private val userDao = AppDatabase.getDatabase(context).userDao()

    suspend fun getCurrentUserId(): String? {
        val firebaseUid = auth.currentUser?.uid
        if (firebaseUid != null) return firebaseUid

        return withContext(Dispatchers.IO) {
            val localUid = userDao.getLoggedInUser()?.uid
            localUid
        }
    }

    suspend fun getCurrentUserName(): String? {
        val firebaseName = auth.currentUser?.displayName
        if (!firebaseName.isNullOrBlank()) return firebaseName

        return withContext(Dispatchers.IO) {
            userDao.getLoggedInUser()?.displayName
        }
    }
    
    suspend fun searchBooks(query: String): Resource<List<GoogleBookItem>> {
        return try {
            // Note: API Key should be in BuildConfig.GOOGLE_BOOKS_API_KEY if configured
            // Since we can't easily access BuildConfig here without import, we'll try to find it or pass null
            // For now, let's assume we pass null if not available.
            // Ideally, pass BuildConfig.GOOGLE_BOOKS_API_KEY
            
            // To access BuildConfig, we need to import it. It's usually in the app package.
            // com.colProj.bookshare.BuildConfig
            
            val apiKey = try {
                val buildConfigClass = Class.forName("com.colProj.bookshare.BuildConfig")
                val field = buildConfigClass.getField("GOOGLE_BOOKS_API_KEY")
                val key = field.get(null) as? String
                if (key.isNullOrBlank()) null else key
            } catch (e: Exception) {
                null
            }

            val response = RetrofitClient.instance.searchBooks(query, apiKey)
            Resource.Success(response.items ?: emptyList())
        } catch (e: retrofit2.HttpException) {
             if (e.code() == 429) {
                 Resource.Error("Search quota exceeded. Please try again tomorrow or add an API Key.")
             } else {
                 Resource.Error("Network error: ${e.message}")
             }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to search books")
        }
    }
}
