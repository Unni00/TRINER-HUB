package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isTrainer = 0 ORDER BY fullName ASC")
    fun getClients(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isTrainer = 1 ORDER BY fullName ASC")
    fun getTrainers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdDirect(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET waterIntakeMl = :waterMl WHERE id = :userId")
    suspend fun updateWaterIntake(userId: Long, waterMl: Int)

    @Query("UPDATE users SET targetCalories = :calories, targetProtein = :protein, targetCarbs = :carbs, targetFats = :fats WHERE id = :userId")
    suspend fun updateMacros(userId: Long, calories: Int, protein: Int, carbs: Int, fats: Int)

    @Query("UPDATE users SET currentWeightKg = :weight, heightCm = :height, age = :age, feedback = :feedback WHERE id = :userId")
    suspend fun updateClientStatsAndFeedback(userId: Long, weight: Float, height: Float, age: Int, feedback: String)

    @Query("UPDATE users SET currentWeightKg = :weight, heightCm = :height, age = :age, sex = :sex, healthInfo = :healthInfo, feedback = :feedback WHERE id = :userId")
    suspend fun updateClientBiometricsAndHealth(userId: Long, weight: Float, height: Float, age: Int, sex: String, healthInfo: String, feedback: String)

    @Query("UPDATE users SET totalPurchasedSessions = :totalSessions WHERE id = :userId")
    suspend fun updateTotalPurchasedSessions(userId: Long, totalSessions: Int)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)
}
