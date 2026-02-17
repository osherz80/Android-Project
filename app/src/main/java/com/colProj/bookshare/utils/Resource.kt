package com.colProj.bookshare.utils

enum class StatusResource{
    SUCCESS,
    ERROR,
    LOADING
}

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val status: StatusResource = StatusResource.LOADING
) {
    class Success<T>(data: T) : Resource<T>(data, status = StatusResource.SUCCESS)
    class Error<T>(message: String, data: T? = null)
        : Resource<T>(data, message, status = StatusResource.ERROR)
    class Loading<T> : Resource<T>(status = StatusResource.LOADING)
}
