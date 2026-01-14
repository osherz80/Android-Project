package com.example.bookshare.model

data class Student(
    val id: String,
    val name: String,
    val checkStatus: Boolean,
    val birthDate: String = "",
    val avatarUrl: String = ""
)
