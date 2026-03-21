package com.colProj.bookshare.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.repository.BookRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository(application)
    val posts: LiveData<List<Post>> = repository.allPosts
}
