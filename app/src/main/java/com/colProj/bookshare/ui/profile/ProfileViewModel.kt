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
        authRepository.getLoggedInUser { user ->
            android.util.Log.d("ProfileViewModel", "Fetched local user: $user")
            if (user != null) {
                _user.value = user
            } else {
                // Try to recover from Firebase session if local DB record is missing
                authRepository.syncUserWithFirebase { recoveredUser ->
                    android.util.Log.d("ProfileViewModel", "Recovered user from Firebase: $recoveredUser")
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
                    fetchUser() // Refresh user data
                }
                _updateStatus.value = success
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
                _updateStatus.value = success
            }
        }
    }
}
