package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("""
        SELECT * FROM messages 
        WHERE (senderId = :userA AND receiverId = :userB) 
           OR (senderId = :userB AND receiverId = :userA)
        ORDER BY timestamp ASC
    """)
    fun getMessagesBetween(userA: Long, userB: Long): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE senderId = :userId OR receiverId = :userId 
        ORDER BY timestamp DESC
    """)
    fun getAllMessagesForUser(userId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getCount(): Int
}
