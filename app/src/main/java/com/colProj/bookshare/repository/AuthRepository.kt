package com.colProj.bookshare.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.colProj.bookshare.data.AppDatabase
import com.colProj.bookshare.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.*
import java.util.concurrent.Executors

class AuthRepository private constructor(context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun registerLocal(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        executor.execute {
            try {
                val existing = userDao.getUserByEmail(email)
                if (existing != null) {
                    mainHandler.post { onResult(false, "User already exists") }
                    return@execute
                }

                val newUser = User(
                    uid = UUID.randomUUID().toString(),
                    email = email,
                    password = pass,
                    isLoggedIn = true
                )
                userDao.insertUser(newUser)
                mainHandler.post { onResult(true, null) }
            } catch (e: Exception) {
                mainHandler.post { onResult(false, e.message) }
            }
        }
    }

    fun loginLocal(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        executor.execute {
            try {
                val user = userDao.loginLocal(email, pass)
                if (user != null) {
                    userDao.logoutAll()
                    userDao.updateUser(user.copy(isLoggedIn = true))
                    mainHandler.post { onResult(true, null) }
                } else {
                    mainHandler.post { onResult(false, "Invalid email or password") }
                }
            } catch (e: Exception) {
                mainHandler.post { onResult(false, e.message) }
            }
        }
    }

    fun signInWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val user = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            displayName = firebaseUser.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString(),
                            isLoggedIn = true
                        )
                        executor.execute {
                            try {
                                userDao.logoutAll()
                                userDao.insertUser(user)
                                mainHandler.post { onResult(true, null) }
                            } catch (e: Exception) {
                                mainHandler.post { onResult(false, "Local storage error: ${e.message}") }
                            }
                        }
                    } else {
                        onResult(false, "Google Auth Success, but user is null")
                    }
                } else {
                    onResult(false, task.exception?.message ?: "Google Sign-In failed")
                }
            }
    }

    fun getLoggedInUser(onResult: (User?) -> Unit) {
        executor.execute {
            val user = userDao.getLoggedInUser()
            mainHandler.post { onResult(user) }
        }
    }

    fun updatePhotoUrl(uid: String, photoUrl: String, onResult: (Boolean) -> Unit) {
        executor.execute {
            try {
                val user = userDao.getLoggedInUser()
                if (user != null && user.uid == uid) {
                    userDao.updateUser(user.copy(photoUrl = photoUrl))
                    mainHandler.post { onResult(true) }
                } else {
                    mainHandler.post { onResult(false) }
                }
            } catch (e: Exception) {
                mainHandler.post { onResult(false) }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        auth.signOut()
        executor.execute {
            userDao.logoutAll()
            mainHandler.post {
                onComplete()
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
