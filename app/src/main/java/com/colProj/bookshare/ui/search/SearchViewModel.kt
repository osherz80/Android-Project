package com.colProj.bookshare.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.repository.BookRepository
import com.colProj.bookshare.utils.Resource
import kotlinx.coroutines.launch

import androidx.lifecycle.switchMap

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookRepository(application)
    
    private val _searchQuery = MutableLiveData("")
    
    val posts: LiveData<List<Post>> = _searchQuery.switchMap { query ->
        if (query.isNullOrBlank()) {
            repository.allPosts
        } else {
            repository.searchPosts(query)
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    private val _refreshStatus = MutableLiveData<Resource<Unit>>()

    init {
        refreshPosts()
    }

    fun refreshPosts() {
        _refreshStatus.value = Resource.Loading()
        viewModelScope.launch {
            _refreshStatus.value = repository.refreshPosts()
        }
    }
}
