package com.colProj.bookshare.ui.myposts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.repository.BookRepository
import com.colProj.bookshare.utils.Resource
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class MyPostsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository(application)
    
    private val _refreshStatus = MutableLiveData<Resource<Unit>>()
    val refreshStatus: LiveData<Resource<Unit>> = _refreshStatus

    val myPosts: LiveData<List<Post>> = liveData {
        val userId = repository.getCurrentUserId() ?: ""
        emitSource(repository.getUserPosts(userId))
        
        // Trigger refresh on init
        if (userId.isNotEmpty()) {
            refresh(userId)
        }
    }

    fun refresh(userId: String = "") {
        viewModelScope.launch {
             _refreshStatus.value = Resource.Loading()
             val id = if (userId.isNotBlank()) userId else repository.getCurrentUserId() ?: ""
             if (id.isNotBlank()) {
                 _refreshStatus.value = repository.refreshUserPosts(id)
             } else {
                 _refreshStatus.value = Resource.Error("User not found")
             }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _refreshStatus.value = Resource.Loading()
            val result = repository.deletePost(postId)
            _refreshStatus.value = result
            if (result is Resource.Success) {
                // Refresh list if needed, but SnapshotListener should handle it
            }
        }
    }
}
