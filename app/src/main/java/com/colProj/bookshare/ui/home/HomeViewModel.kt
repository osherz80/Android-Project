package com.colProj.bookshare.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.repository.BookRepository

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository(application)
    val posts: LiveData<List<Post>> = repository.allPosts

    init {
        viewModelScope.launch {
            repository.syncPostsFromFirestore()
        }
    }
}
