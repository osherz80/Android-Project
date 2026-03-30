package com.colProj.bookshare.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.colProj.bookshare.data.User
import com.colProj.bookshare.data.model.Post
import com.colProj.bookshare.repository.AuthRepository
import com.colProj.bookshare.repository.BookRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository.getInstance(application)
    private val bookRepository = BookRepository(application)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _userPostsCount = MediatorLiveData<Int>()
    val userPostsCount: LiveData<Int> = _userPostsCount
    private var _currentPostsSource: LiveData<List<Post>>? = null

    init {
        _userPostsCount.addSource(_user) { user ->
            _currentPostsSource?.let { _userPostsCount.removeSource(it) }
            if (user != null) {
                val newSource = bookRepository.getUserPosts(user.uid)
                _currentPostsSource = newSource
                _userPostsCount.addSource(newSource) { posts ->
                    _userPostsCount.value = posts.size
                }
            } else {
                _userPostsCount.value = 0
            }
        }
    }

    private val _imageUpdateStatus = MutableLiveData<Boolean?>()
    val imageUpdateStatus: LiveData<Boolean?> = _imageUpdateStatus

    private val _profileUpdateStatus = MutableLiveData<Boolean?>()
    val profileUpdateStatus: LiveData<Boolean?> = _profileUpdateStatus

    fun clearImageStatus() { _imageUpdateStatus.value = null }
    fun clearProfileStatus() { _profileUpdateStatus.value = null }


    fun fetchUser() {
        authRepository.getLoggedInUser { user ->
            Log.d("ProfileViewModel", "Fetched local user: $user")
            if (user != null) {
                _user.value = user
            } else {
                authRepository.syncUserWithFirebase { recoveredUser ->
                    Log.d("ProfileViewModel", "Recovered user from Firebase: $recoveredUser")
                    _user.value = recoveredUser
                }
            }
        }
    }

    fun updateProfilePicture(uri: String) {
        val currentUser = _user.value
        if (currentUser != null) {
            authRepository.updatePhotoUrl(currentUser.uid, uri) { success ->
                if (success) {
                    fetchUser() 
                }
                _imageUpdateStatus.value = success
            }
        }
    }

    fun updateProfile(displayName: String, bio: String) {
        val currentUser = _user.value
        if (currentUser != null) {
            authRepository.updateProfile(currentUser.uid, displayName, bio) { success ->
                if (success) {
                    fetchUser()
                }
                _profileUpdateStatus.value = success
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        authRepository.logout(onComplete)
    }
}
