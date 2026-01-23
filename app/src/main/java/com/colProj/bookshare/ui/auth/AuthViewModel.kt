package com.colProj.bookshare.ui.auth

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.colProj.bookshare.repository.AuthRepository
import com.colProj.bookshare.utils.Resource

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository.getInstance(application)

    private val _authState = MutableLiveData<Resource<Boolean>>()
    val authState: LiveData<Resource<Boolean>> = _authState

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = Resource.Error("Please fill all fields")
            return
        }

        if (!isValidEmail(email)) {
            _authState.value = Resource.Error("Invalid email format")
            return
        }

        _authState.value = Resource.Loading()
        repository.loginLocal(email, pass) { success, error ->
            if (success) {
                _authState.value = Resource.Success(true)
            } else {
                _authState.value = Resource.Error(error ?: "Invalid credentials")
            }
        }
    }

    fun signup(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = Resource.Error("Please fill all fields")
            return
        }

        if (!isValidEmail(email)) {
            _authState.value = Resource.Error("Invalid email format")
            return
        }

        if (pass.length < 6) {
            _authState.value = Resource.Error("Password must be at least 6 characters")
            return
        }

        _authState.value = Resource.Loading()
        repository.registerLocal(email, pass) { success, error ->
            if (success) {
                _authState.value = Resource.Success(true)
            } else {
                _authState.value = Resource.Error(error ?: "Signup failed")
            }
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
