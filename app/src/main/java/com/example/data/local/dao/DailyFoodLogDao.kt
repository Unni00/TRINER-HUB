package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DailyFoodLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyFoodLogDao {
    @Query("SELECT * FROM daily_food_logs WHERE userId = :userId ORDER BY dateLogged DESC")
    fun getLogsForUser(userId: Long): Flow<List<DailyFoodLogEntity>>

    @Query("SELECT * FROM daily_food_logs ORDER BY dateLogged DESC")
    fun getAllFoodLogs(): Flow<List<DailyFoodLogEntity>>

    @Query("SELECT * FROM daily_food_logs WHERE userId = :userId AND dateLogged >= :startOfDay ORDER BY dateLogged DESC")
    fun getTodayLogsForUser(userId: Long, startOfDay: Long): Flow<List<DailyFoodLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyFoodLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<DailyFoodLogEntity>)

    @Query("DELETE FROM daily_food_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("SELECT COUNT(*) FROM daily_food_logs")
    suspend fun getCount(): Int
}
