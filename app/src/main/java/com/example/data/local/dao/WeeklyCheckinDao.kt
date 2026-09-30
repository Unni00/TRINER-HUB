package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.WeeklyCheckinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeeklyCheckinDao {
    @Query("SELECT * FROM weekly_checkins WHERE userId = :userId ORDER BY date DESC")
    fun getCheckinsForUser(userId: Long): Flow<List<WeeklyCheckinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckin(checkin: WeeklyCheckinEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckins(checkins: List<WeeklyCheckinEntity>)

    @Query("DELETE FROM weekly_checkins WHERE id = :id")
    suspend fun deleteCheckinById(id: Long)

    @Query("SELECT COUNT(*) FROM weekly_checkins")
    suspend fun getCount(): Int
}
