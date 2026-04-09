package calmconnectapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import calmconnectapplication.db.entity.UserProfile

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile)

    @Update
    suspend fun update(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getProfile(userId: String): LiveData<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getProfileSync(userId: String): UserProfile?
}
