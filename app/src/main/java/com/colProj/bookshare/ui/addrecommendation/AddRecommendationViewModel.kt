package com.colProj.bookshare.ui.addrecommendation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.colProj.bookshare.data.Recommendation
import com.colProj.bookshare.repository.AuthRepository
import com.colProj.bookshare.repository.RecommendationRepository
import com.colProj.bookshare.utils.Resource

class AddRecommendationViewModel(application: Application) : AndroidViewModel(application) {

    private val recommendationRepository = RecommendationRepository.getInstance(application)
    private val authRepository = AuthRepository.getInstance(application)

    private val _saveStatus = MutableLiveData<Resource<Boolean>>()
    val saveStatus: LiveData<Resource<Boolean>> = _saveStatus

    private val _selectedImageUri = MutableLiveData<Uri?>()
    val selectedImageUri: LiveData<Uri?> = _selectedImageUri

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun submitRecommendation(
        title: String,
        author: String,
        rating: Int,
        recommendationText: String
    ) {
        if (title.isBlank() || author.isBlank() || recommendationText.isBlank() || rating == 0) {
            _saveStatus.value = Resource.Error("VAL_ERROR")
            return
        }

        _saveStatus.value = Resource.Loading()

        authRepository.getLoggedInUser { user ->
            if (user != null) {
                val recommendation = Recommendation(
                    userId = user.uid,
                    bookTitle = title,
                    bookAuthor = author,
                    bookCoverUri = _selectedImageUri.value?.toString(),
                    rating = rating,
                    recommendationText = recommendationText
                )

                recommendationRepository.saveRecommendation(recommendation) { success, error ->
                    if (success) {
                        _saveStatus.value = Resource.Success(true)
                    } else {
                        _saveStatus.value = Resource.Error(error ?: "Unknown error occurred")
                    }
                }
            } else {
                _saveStatus.value = Resource.Error("User not logged in")
            }
        }
    }

    fun resetSaveStatus() {
        _saveStatus.value = null
    }
}
