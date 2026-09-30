package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "client_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class ClientSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val sessionNumber: Int = 1,
    val sessionTitle: String,
    val sessionTimestamp: Long, // scheduled date/time
    val status: String = "Scheduled", // "Scheduled", "Reached / Attending", "Present", "Absent", "Completed"
    val clientCheckInTime: Long? = null,
    val notes: String = ""
)
