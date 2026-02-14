package com.colProj.bookshare.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
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

    fun registerLocal(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val newUser = User(
                            uid = firebaseUser.uid,
                            email = email,
                            password = pass, // Ideally verify if we want to store password locally. For now keeping consistent.
                            isLoggedIn = true
                        )
                        executor.execute {
                            userDao.insertUser(newUser)
                            mainHandler.post { onResult(true, null) }
                        }
                    } else {
                        onResult(false, "Registration success but user is null")
                    }
                } else {
                    onResult(false, task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun loginLocal(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        executor.execute {
                            // Update local generic user state if needed, or just sync
                            val existingUser = userDao.getUserByEmail(email) ?: User(
                                uid = firebaseUser.uid,
                                email = email,
                                isLoggedIn = true
                            )
                            userDao.logoutAll()
                            userDao.insertUser(existingUser.copy(isLoggedIn = true, uid = firebaseUser.uid))
                            mainHandler.post { onResult(true, null) }
                        }
                    } else {
                        onResult(false, "Login success but user is null")
                    }
                } else {
                    onResult(false, task.exception?.message ?: "Login failed")
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

                                Log.d("AuthRepository", "Merging user: googleUid=${firebaseUser.uid}, existingFound=${existingToMerge != null}")

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
                                    Log.d("AuthRepository", "Deleted older local record for email: $email")
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
        val firebaseUser = auth.currentUser
        if (firebaseUser != null && firebaseUser.uid == uid) {
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
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
                    } else {
                        onResult(false)
                    }
                }
        } else {
            // Fallback to local only if firebase user is missing (should generally not happen if logged in)
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
