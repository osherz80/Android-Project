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

    fun hasLoggedInUser(onResult: (Boolean) -> Unit) {
        executor.execute {
            val user = userDao.getLoggedInUser()
            mainHandler.post { onResult(user != null) }
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
                                // Try to find existing user by UID or Email
                                val existingByUid = userDao.getUserByUid(firebaseUser.uid)
                                val existingByEmail = userDao.getUserByEmail(email)
                                
                                val existingToMerge = existingByUid ?: existingByEmail

                                android.util.Log.d("AuthRepository", "Merging user: googleUid=${firebaseUser.uid}, existingFound=${existingToMerge != null}")

                                val user = User(
                                    uid = firebaseUser.uid,
                                    email = email,
                                    // Prioritize existing local data, fallback to Google
                                    displayName = existingToMerge?.displayName ?: firebaseUser.displayName,
                                    photoUrl = existingToMerge?.photoUrl ?: firebaseUser.photoUrl?.toString(),
                                    bio = existingToMerge?.bio,
                                    isLoggedIn = true
                                )
                                
                                userDao.logoutAll()
                                
                                // If we found an existing record with a DIFFERENT UID (local user becoming Google user)
                                // delete the old record to avoid duplicates of the same email
                                if (existingByEmail != null && existingByEmail.uid != firebaseUser.uid) {
                                    userDao.deleteUserByUid(existingByEmail.uid)
                                    android.util.Log.d("AuthRepository", "Deleted older local record for email: $email")
                                }

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
                        // Try to find existing user by UID or Email to restore bio/photo
                        val existingByUid = userDao.getUserByUid(firebaseUser.uid)
                        val existingByEmail = userDao.getUserByEmail(email)
                        val existingToMerge = existingByUid ?: existingByEmail
                        
                        user = User(
                            uid = firebaseUser.uid,
                            email = email,
                            displayName = existingToMerge?.displayName ?: firebaseUser.displayName,
                            photoUrl = existingToMerge?.photoUrl ?: firebaseUser.photoUrl?.toString(),
                            bio = existingToMerge?.bio,
                            isLoggedIn = true
                        )
                        userDao.logoutAll()
                        
                        // Deduplicate if needed
                        if (existingByEmail != null && existingByEmail.uid != firebaseUser.uid) {
                            userDao.deleteUserByUid(existingByEmail.uid)
                        }
                        
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
