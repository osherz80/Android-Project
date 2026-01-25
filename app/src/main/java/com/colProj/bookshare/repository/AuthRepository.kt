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
                        executor.execute {
                            try {
                                val email = firebaseUser.email ?: ""
                                // Try to find existing user to preserve bio
                                val existingUser = userDao.getUserByUid(firebaseUser.uid) 
                                    ?: userDao.getUserByEmail(email)

                                android.util.Log.d("AuthRepository", "Google Login: displayName=${firebaseUser.displayName}, photoUrl=${firebaseUser.photoUrl}")

                                val user = User(
                                    uid = firebaseUser.uid,
                                    email = email,
                                    // Prioritize Google data, fallback to existing local data
                                    displayName = firebaseUser.displayName ?: existingUser?.displayName,
                                    photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser?.photoUrl,
                                    bio = existingUser?.bio,
                                    isLoggedIn = true
                                )
                                
                                android.util.Log.d("AuthRepository", "Saving user to DB: $user")
                                
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

    fun syncUserWithFirebase(onComplete: (User?) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            executor.execute {
                try {
                    val email = firebaseUser.email ?: ""
                    var user = userDao.getLoggedInUser()
                    
                    if (user == null || user.uid != firebaseUser.uid) {
                        // Restore existing local bio/data if found by UID
                        val existing = userDao.getUserByUid(firebaseUser.uid)
                        
                        user = User(
                            uid = firebaseUser.uid,
                            email = email,
                            displayName = firebaseUser.displayName ?: existing?.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString() ?: existing?.photoUrl,
                            bio = existing?.bio,
                            isLoggedIn = true
                        )
                        userDao.logoutAll()
                        userDao.insertUser(user)
                    }
                    mainHandler.post { onComplete(user) }
                } catch (e: Exception) {
                    mainHandler.post { onComplete(null) }
                }
            }
        } else {
            onComplete(null)
        }
    }

    fun updateProfile(uid: String, displayName: String, bio: String, onResult: (Boolean) -> Unit) {
        executor.execute {
            try {
                val user = userDao.getLoggedInUser()
                if (user != null && user.uid == uid) {
                    userDao.updateUser(user.copy(displayName = displayName, bio = bio))
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
