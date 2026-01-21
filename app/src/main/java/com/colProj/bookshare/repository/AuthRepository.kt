package com.colProj.bookshare.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.colProj.bookshare.data.AppDatabase
import com.colProj.bookshare.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.Executors

class AuthRepository private constructor(context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun loginWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val user = User(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email,
                            displayName = firebaseUser.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString()
                        )
                        // Save to Room on background thread
                        executor.execute {
                            try {
                                userDao.insertUser(user)
                                mainHandler.post {
                                    onResult(true, null)
                                }
                            } catch (e: Exception) {
                                mainHandler.post {
                                    onResult(false, "Local storage error: ${e.message}")
                                }
                            }
                        }
                    } else {
                        onResult(false, "Unknown error")
                    }
                } else {
                    onResult(false, task.exception?.message ?: "Login failed")
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
                            email = firebaseUser.email,
                            displayName = firebaseUser.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString()
                        )
                        executor.execute {
                            try {
                                userDao.insertUser(user)
                                mainHandler.post {
                                    onResult(true, null)
                                }
                            } catch (e: Exception) {
                                mainHandler.post {
                                    onResult(false, "Local storage error: ${e.message}")
                                }
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

    fun logout(onComplete: () -> Unit) {
        auth.signOut()
        executor.execute {
            userDao.deleteUser()
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
