package com.colProj.bookshare.data

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun getUserByUid(uid: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    fun loginLocal(email: String, password: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Update
    fun updateUser(user: User)

    @Query("UPDATE users SET isLoggedIn = 0")
    fun logoutAll()

    @Query("DELETE FROM users")
    fun deleteAll()
}
