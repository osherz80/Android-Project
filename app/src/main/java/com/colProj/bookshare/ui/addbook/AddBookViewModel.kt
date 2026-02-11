package com.colProj.bookshare.ui.addbook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.repository.BookRepository
import com.colProj.bookshare.utils.Resource
import kotlinx.coroutines.launch

class AddBookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookRepository(application)

    private val _addPostStatus = MutableLiveData<Resource<Unit>>()
    private val _searchResults = MutableLiveData<Resource<List<com.colProj.bookshare.data.model.GoogleBookItem>>>()

    val addPostStatus: LiveData<Resource<Unit>> = _addPostStatus

    fun addPost(title: String, bookSummary: String, recommendation: String, rating: Float, imageUrl: String, author: String) {

        viewModelScope.launch {
            val userId = repository.getCurrentUserId()
            if (userId == null) {
                _addPostStatus.value = Resource.Error("User not logged in")
                return@launch
            }

            val userName = repository.getCurrentUserName() ?: "Anonymous"

            val post = Post(
                userId = userId,
                userName = userName,
                bookTitle = title,
                bookSummary = bookSummary,
                description = recommendation,
                author = author,
                rating = rating,
                imageUrl = imageUrl
            )

            _addPostStatus.value = Resource.Loading()
            
            val result = repository.addPost(post)
            _addPostStatus.value = result
        }
    }
    
    val searchResults: LiveData<Resource<List<com.colProj.bookshare.data.model.GoogleBookItem>>> = _searchResults

    fun searchBooks(query: String) {
        _searchResults.value = Resource.Loading()
        viewModelScope.launch {
            _searchResults.value = repository.searchBooks(query)
        }
    }
}
