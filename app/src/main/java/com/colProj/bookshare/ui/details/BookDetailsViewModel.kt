package com.colProj.bookshare.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.repository.BookRepository

class BookDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookRepository(application)
    private val _bookTitle = MutableLiveData<String>()

    val posts: LiveData<List<Post>> = _bookTitle.switchMap { title ->
        repository.getPostsByTitle(title)
    }
    
    fun setBookTitle(title: String) {
        if (_bookTitle.value != title) {
            _bookTitle.value = title
        }
    }
}
