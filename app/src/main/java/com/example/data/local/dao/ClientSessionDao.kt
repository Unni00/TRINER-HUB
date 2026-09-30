package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ClientSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientSessionDao {
    @Query("SELECT * FROM client_sessions WHERE userId = :userId ORDER BY sessionTimestamp ASC")
    fun getSessionsForUser(userId: Long): Flow<List<ClientSessionEntity>>

    @Query("SELECT * FROM client_sessions ORDER BY sessionTimestamp ASC")
    fun getAllSessions(): Flow<List<ClientSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ClientSessionEntity): Long

    @Update
    suspend fun updateSession(session: ClientSessionEntity)

    @Query("UPDATE client_sessions SET status = :status WHERE id = :sessionId")
    suspend fun updateAttendance(sessionId: Long, status: String)

    @Query("UPDATE client_sessions SET status = :status, clientCheckInTime = :checkInTime WHERE id = :sessionId")
    suspend fun clientCheckIn(sessionId: Long, status: String, checkInTime: Long)

    @Query("DELETE FROM client_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}
