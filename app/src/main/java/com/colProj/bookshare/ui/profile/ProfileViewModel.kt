package com.colProj.bookshare.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.colProj.bookshare.data.User
import com.colProj.bookshare.repository.AuthRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository.getInstance(application)

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _updateStatus = MutableLiveData<Boolean>()
    val updateStatus: LiveData<Boolean> = _updateStatus

    fun fetchUser() {
        authRepository.getLoggedInUser {
            _user.value = it
        }
    }

    fun updateProfilePicture(uri: String) {
        val currentUser = _user.value
        if (currentUser != null) {
            authRepository.updatePhotoUrl(currentUser.uid, uri) { success ->
                if (success) {
                    fetchUser() // Refresh user data
                }
                _updateStatus.value = success
            }
        }
    }
}
