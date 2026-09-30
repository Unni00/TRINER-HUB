package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weekly_checkins",
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
data class WeeklyCheckinEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: Long = System.currentTimeMillis(),
    val currentWeight: Float, // kg
    val sleepQuality: Int, // 1 to 5
    val energyLevel: Int, // 1 to 5
    val frontPhotoUri: String? = null,
    val sidePhotoUri: String? = null,
    val backPhotoUri: String? = null,
    val notes: String? = null
)
