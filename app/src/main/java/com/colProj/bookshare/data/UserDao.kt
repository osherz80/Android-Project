package com.colProj.bookshare.data

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getLoggedInUser(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User)

    @Query("DELETE FROM users")
    fun deleteUser()
}
