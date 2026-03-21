package com.colProj.bookshare.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.repository.BookRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import com.colProj.bookshare.utils.Resource

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository(application)
    val posts: LiveData<List<Post>> = repository.allPosts

    private val _syncStatus = MutableLiveData<Resource<Unit>>()
    val syncStatus: LiveData<Resource<Unit>> = _syncStatus

    init {
        viewModelScope.launch {
            _syncStatus.value = Resource.Loading()
            val result = repository.syncPostsFromFirestore()
            _syncStatus.value = result
        }
    }
}
