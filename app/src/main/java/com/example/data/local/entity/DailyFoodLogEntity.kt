package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_food_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FoodItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("foodItemId")
    ]
)
data class DailyFoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val foodItemId: Long,
    val foodName: String,
    val dateLogged: Long = System.currentTimeMillis(),
    val mealType: String = "Lunch", // Breakfast, Lunch, Dinner, Snack
    val gramsConsumed: Float,
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFats: Float
)
