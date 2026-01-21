package com.colProj.bookshare.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.colProj.bookshare.repository.AuthRepository
import com.colProj.bookshare.utils.Resource

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository.getInstance(application)

    private val _authState = MutableLiveData<Resource<Boolean>>()
    val authState: LiveData<Resource<Boolean>> = _authState

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = Resource.Error("Please fill all fields")
            return
        }

        _authState.value = Resource.Loading()
        repository.loginWithEmail(email, pass) { success, error ->
            if (success) {
                _authState.value = Resource.Success(true)
            } else {
                _authState.value = Resource.Error(error ?: "Authentication failed")
            }
        }
    fun handleGoogleIdToken(idToken: String) {
        _authState.value = Resource.Loading()
        repository.signInWithGoogle(idToken) { success, error ->
            if (success) {
                _authState.value = Resource.Success(true)
            } else {
                _authState.value = Resource.Error(error ?: "Google Sign-In failed")
            }
        }
    }
}
