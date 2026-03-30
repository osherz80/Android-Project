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

    private val appContext = context.applicationContext
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
                            password = pass,
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
                                val existingByUid = userDao.getUserByUid(firebaseUser.uid)
                                val existingByEmail = userDao.getUserByEmail(email)

                                val existingToMerge = existingByUid ?: existingByEmail

                                Log.d("AuthRepository", "Merging user: googleUid=${firebaseUser.uid}, existingFound=${existingToMerge != null}")

                                val user = User(
                                    uid = firebaseUser.uid,
                                    email = email,
                                    displayName = existingToMerge?.displayName ?: firebaseUser.displayName,
                                    photoUrl = existingToMerge?.photoUrl ?: firebaseUser.photoUrl?.toString(),
                                    bio = existingToMerge?.bio,
                                    isLoggedIn = true
                                )

                                userDao.logoutAll()

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
        Log.d("AuthRepository", "Starting updatePhotoUrl local only for uid: $uid with url: $photoUrl")
        
        if (photoUrl.startsWith("http") || photoUrl.startsWith("https")) {
            Log.d("AuthRepository", "URL is already remote, skipping upload.")
            updateUserAndProfile(uid, photoUrl, onResult)
            return
        }

        val uri = try {
            android.net.Uri.parse(photoUrl)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to parse URI: $photoUrl", e)
            onResult(false)
            return
        }

        executor.execute {
            try {
                val context = appContext
                
                Log.d("AuthRepository", "Copying image to local app storage...")
                val localPath = "profile_pic_${uid}_${System.currentTimeMillis()}.jpg"
                val localFile = java.io.File(context?.filesDir, localPath)
                val inputStream = context?.contentResolver?.openInputStream(uri)
                val outputStream = java.io.FileOutputStream(localFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                val finalPath = localFile.absolutePath
                Log.d("AuthRepository", "Local file saved at: $finalPath")

                val user = userDao.getLoggedInUser()
                if (user != null && user.uid == uid) {
                    val updatedUser = user.copy(localImagePath = finalPath)
                    userDao.updateUser(updatedUser)
                    Log.d("AuthRepository", "Local DB updated with local image path")
                    mainHandler.post { onResult(true) }
                } else {
                    Log.e("AuthRepository", "Local user not found or UID mismatch")
                    mainHandler.post { onResult(false) }
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error getting or saving file", e)
                mainHandler.post { onResult(false) }
            }
        }
    }

    private fun updateUserAndProfile(uid: String, remoteUrl: String, onResult: (Boolean) -> Unit) {
        Log.d("AuthRepository", "Updating profile and DB with URL: $remoteUrl")
        
        val downloadUri = android.net.Uri.parse(remoteUrl)
        
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setPhotoUri(downloadUri)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
            Log.d("AuthRepository", "Firebase Profile update successful: ${profileTask.isSuccessful}")
        }

        executor.execute {
            try {
                val user = userDao.getLoggedInUser()
                if (user != null && user.uid == uid) {
                    userDao.updateUser(user.copy(photoUrl = remoteUrl))
                    Log.d("AuthRepository", "Local DB updated with remote URL")
                    mainHandler.post { onResult(true) }
                } else {
                    Log.e("AuthRepository", "Local user not found or UID mismatch: localUser=$user, expectedUid=$uid")
                    mainHandler.post { onResult(false) }
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Local DB update failed", e)
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
                        val existingByUid = userDao.getUserByUid(firebaseUser.uid)
                        val existingByEmail = userDao.getUserByEmail(email)
                        val existingToMerge = existingByUid ?: existingByEmail

                        user = User(
                            uid = firebaseUser.uid,
                            email = email,
                            displayName = existingToMerge?.displayName ?: firebaseUser.displayName,
                            photoUrl = existingToMerge?.photoUrl ?: firebaseUser.photoUrl?.toString(),
                            localImagePath = existingToMerge?.localImagePath,
                            bio = existingToMerge?.bio,
                            isLoggedIn = true
                        )
                        userDao.logoutAll()

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
