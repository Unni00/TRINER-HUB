package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val password: String,
    val fullName: String,
    val profilePictureUri: String? = null,
    val isTrainer: Boolean = false,
    val profession: String = "Certified Strength & Conditioning Specialist (CSCS)",
    val targetCalories: Int = 2200,
    val targetProtein: Int = 150,
    val targetCarbs: Int = 250,
    val targetFats: Int = 60,
    val waterIntakeMl: Int = 0,
    val currentWeightKg: Float = 78.0f,
    val heightCm: Float = 175.0f,
    val age: Int = 26,
    val sex: String = "Male",
    val healthInfo: String = "No known medical issues, cleared for training",
    val feedback: String = "Recovering well from leg workouts, feeling motivated and keeping hydration consistent.",
    val totalPurchasedSessions: Int = 12
)
