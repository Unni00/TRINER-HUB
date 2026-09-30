package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DailyFoodLogDao
import com.example.data.local.dao.FoodItemDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WeeklyCheckinDao
import com.example.data.local.dao.WorkoutLogDao
import com.example.data.local.dao.ClientSessionDao
import com.example.data.local.entity.DailyFoodLogEntity
import com.example.data.local.entity.FoodItemEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WeeklyCheckinEntity
import com.example.data.local.entity.WorkoutLogEntity
import com.example.data.local.entity.ClientSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        FoodItemEntity::class,
        DailyFoodLogEntity::class,
        WorkoutLogEntity::class,
        WeeklyCheckinEntity::class,
        MessageEntity::class,
        ClientSessionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun dailyFoodLogDao(): DailyFoodLogDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun weeklyCheckinDao(): WeeklyCheckinDao
    abstract fun messageDao(): MessageDao
    abstract fun clientSessionDao(): ClientSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coachfit_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }
        }

        suspend fun seedDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            val foodItemDao = database.foodItemDao()
            val foodLogDao = database.dailyFoodLogDao()
            val workoutDao = database.workoutLogDao()
            val checkinDao = database.weeklyCheckinDao()
            val messageDao = database.messageDao()

            if (userDao.getUserCount() > 0) return

            // 1. Seed Trainer
            val trainerId = userDao.insertUser(
                UserEntity(
                    id = 1,
                    email = "coach@coachfit.com",
                    password = "password123",
                    fullName = "Coach Marcus Vance",
                    profession = "Elite Strength & Conditioning Coach & Sports Nutritionist",
                    profilePictureUri = null,
                    isTrainer = true,
                    targetCalories = 2600,
                    targetProtein = 180,
                    targetCarbs = 280,
                    targetFats = 70,
                    waterIntakeMl = 3200
                )
            )

            // 2. Seed Clients
            val client1Id = userDao.insertUser(
                UserEntity(
                    id = 2,
                    email = "alex@example.com",
                    password = "password123",
                    fullName = "Alex Rivera",
                    profilePictureUri = null,
                    isTrainer = false,
                    targetCalories = 2200,
                    targetProtein = 155,
                    targetCarbs = 230,
                    targetFats = 60,
                    waterIntakeMl = 1750,
                    currentWeightKg = 78.4f,
                    heightCm = 178.0f,
                    age = 26,
                    sex = "Male",
                    healthInfo = "No injuries, cleared for heavy lifting, mild asthma in high pollen",
                    feedback = "Energy is solid this week! Ready for progressive overload on squats. Digestion is great with the current rice and chicken meals."
                )
            )

            val client2Id = userDao.insertUser(
                UserEntity(
                    id = 3,
                    email = "priya@example.com",
                    password = "password123",
                    fullName = "Priya Nair",
                    profilePictureUri = null,
                    isTrainer = false,
                    targetCalories = 1850,
                    targetProtein = 125,
                    targetCarbs = 205,
                    targetFats = 50,
                    waterIntakeMl = 2250,
                    currentWeightKg = 61.2f,
                    heightCm = 164.0f,
                    age = 28,
                    sex = "Female",
                    healthInfo = "Previous right wrist sprain (fully healed), vegetarian diet preferred",
                    feedback = "Loving the Thoran and Aviyal in the meal plan. Feeling consistent and sleeping 7+ hours nightly."
                )
            )

            // 3. Seed Food Items (Phase 1 mandatory list + popular additions)
            val foods = listOf(
                FoodItemEntity(id = 1, foodName = "Chicken Breast", calPer100g = 165f, proteinPer100g = 31f, carbsPer100g = 0f, fatPer100g = 3.6f, category = "Poultry"),
                FoodItemEntity(id = 2, foodName = "Whey", calPer100g = 390f, proteinPer100g = 78f, carbsPer100g = 8f, fatPer100g = 5f, category = "Supplements"),
                FoodItemEntity(id = 3, foodName = "Matta Rice", calPer100g = 130f, proteinPer100g = 2.7f, carbsPer100g = 28f, fatPer100g = 0.3f, category = "Grains"),
                FoodItemEntity(id = 4, foodName = "Aviyal", calPer100g = 115f, proteinPer100g = 2.5f, carbsPer100g = 11f, fatPer100g = 7f, category = "Vegetables"),
                FoodItemEntity(id = 5, foodName = "Thoran", calPer100g = 105f, proteinPer100g = 3f, carbsPer100g = 9f, fatPer100g = 6.5f, category = "Vegetables"),
                FoodItemEntity(id = 6, foodName = "Fish Curry", calPer100g = 140f, proteinPer100g = 18f, carbsPer100g = 3f, fatPer100g = 6.5f, category = "Seafood"),
                // Extra staples
                FoodItemEntity(id = 7, foodName = "Boiled Eggs (2 large)", calPer100g = 155f, proteinPer100g = 13f, carbsPer100g = 1.1f, fatPer100g = 11f, category = "Protein"),
                FoodItemEntity(id = 8, foodName = "Rolled Oats", calPer100g = 389f, proteinPer100g = 16.9f, carbsPer100g = 66f, fatPer100g = 6.9f, category = "Grains"),
                FoodItemEntity(id = 9, foodName = "Greek Yogurt", calPer100g = 59f, proteinPer100g = 10f, carbsPer100g = 3.6f, fatPer100g = 0.4f, category = "Dairy"),
                FoodItemEntity(id = 10, foodName = "Peanut Butter", calPer100g = 588f, proteinPer100g = 25f, carbsPer100g = 20f, fatPer100g = 50f, category = "Healthy Fats"),
                FoodItemEntity(id = 11, foodName = "Paneer", calPer100g = 265f, proteinPer100g = 18.3f, carbsPer100g = 1.2f, fatPer100g = 20.8f, category = "Dairy"),
                FoodItemEntity(id = 12, foodName = "Banana", calPer100g = 89f, proteinPer100g = 1.1f, carbsPer100g = 23f, fatPer100g = 0.3f, category = "Fruits")
            )
            foodItemDao.insertAll(foods)

            // 4. Seed initial Daily Food Logs for Alex
            val now = System.currentTimeMillis()
            val twoHoursAgo = now - 2 * 3600 * 1000L
            val fiveHoursAgo = now - 5 * 3600 * 1000L

            foodLogDao.insertLogs(
                listOf(
                    DailyFoodLogEntity(
                        userId = client1Id,
                        foodItemId = 8,
                        foodName = "Rolled Oats",
                        dateLogged = fiveHoursAgo,
                        mealType = "Breakfast",
                        gramsConsumed = 80f,
                        totalCalories = (80f / 100f) * 389f,
                        totalProtein = (80f / 100f) * 16.9f,
                        totalCarbs = (80f / 100f) * 66f,
                        totalFats = (80f / 100f) * 6.9f
                    ),
                    DailyFoodLogEntity(
                        userId = client1Id,
                        foodItemId = 2,
                        foodName = "Whey",
                        dateLogged = fiveHoursAgo,
                        mealType = "Breakfast",
                        gramsConsumed = 35f,
                        totalCalories = (35f / 100f) * 390f,
                        totalProtein = (35f / 100f) * 78f,
                        totalCarbs = (35f / 100f) * 8f,
                        totalFats = (35f / 100f) * 5f
                    ),
                    DailyFoodLogEntity(
                        userId = client1Id,
                        foodItemId = 1,
                        foodName = "Chicken Breast",
                        dateLogged = twoHoursAgo,
                        mealType = "Lunch",
                        gramsConsumed = 200f,
                        totalCalories = (200f / 100f) * 165f,
                        totalProtein = (200f / 100f) * 31f,
                        totalCarbs = 0f,
                        totalFats = (200f / 100f) * 3.6f
                    ),
                    DailyFoodLogEntity(
                        userId = client1Id,
                        foodItemId = 3,
                        foodName = "Matta Rice",
                        dateLogged = twoHoursAgo,
                        mealType = "Lunch",
                        gramsConsumed = 150f,
                        totalCalories = (150f / 100f) * 130f,
                        totalProtein = (150f / 100f) * 2.7f,
                        totalCarbs = (150f / 100f) * 28f,
                        totalFats = (150f / 100f) * 0.3f
                    ),
                    DailyFoodLogEntity(
                        userId = client1Id,
                        foodItemId = 4,
                        foodName = "Aviyal",
                        dateLogged = twoHoursAgo,
                        mealType = "Lunch",
                        gramsConsumed = 100f,
                        totalCalories = 115f,
                        totalProtein = 2.5f,
                        totalCarbs = 11f,
                        totalFats = 7f
                    )
                )
            )

            // 5. Seed Workout Logs for Alex
            val yesterday = now - 24 * 3600 * 1000L
            val threeDaysAgo = now - 3 * 24 * 3600 * 1000L

            workoutDao.insertLogs(
                listOf(
                    WorkoutLogEntity(
                        userId = client1Id,
                        dateLogged = yesterday,
                        exerciseName = "Barbell Back Squat",
                        sets = 4,
                        reps = 8,
                        weightLifted = 100f
                    ),
                    WorkoutLogEntity(
                        userId = client1Id,
                        dateLogged = yesterday,
                        exerciseName = "Romanian Deadlift",
                        sets = 3,
                        reps = 10,
                        weightLifted = 85f
                    ),
                    WorkoutLogEntity(
                        userId = client1Id,
                        dateLogged = threeDaysAgo,
                        exerciseName = "Incline Dumbbell Bench Press",
                        sets = 4,
                        reps = 10,
                        weightLifted = 32f
                    )
                )
            )

            // 6. Seed Weekly Check-in for Alex
            val sevenDaysAgo = now - 7 * 24 * 3600 * 1000L
            checkinDao.insertCheckins(
                listOf(
                    WeeklyCheckinEntity(
                        userId = client1Id,
                        date = sevenDaysAgo,
                        currentWeight = 78.4f,
                        sleepQuality = 4,
                        energyLevel = 4,
                        frontPhotoUri = "preset:front_male",
                        sidePhotoUri = "preset:side_male",
                        backPhotoUri = "preset:back_male",
                        notes = "Feeling strong on heavy squat days. Energy remained high throughout week."
                    ),
                    WeeklyCheckinEntity(
                        userId = client1Id,
                        date = now - 14 * 24 * 3600 * 1000L,
                        currentWeight = 79.1f,
                        sleepQuality = 3,
                        energyLevel = 3,
                        frontPhotoUri = "preset:front_male",
                        sidePhotoUri = null,
                        backPhotoUri = null,
                        notes = "Starting phase. Soreness after leg workout was intense."
                    )
                )
            )

            // 7. Seed Messages between Client Alex and Coach Marcus
            messageDao.insertMessages(
                listOf(
                    MessageEntity(
                        senderId = trainerId,
                        receiverId = client1Id,
                        messageContent = "Hey Alex! Great job hitting your protein target yesterday. How are you feeling for today's leg day?",
                        timestamp = now - 6 * 3600 * 1000L
                    ),
                    MessageEntity(
                        senderId = client1Id,
                        receiverId = trainerId,
                        messageContent = "Feeling energetic Coach! Had my pre-workout meal with oats and whey.",
                        timestamp = now - 5 * 3600 * 1000L
                    ),
                    MessageEntity(
                        senderId = trainerId,
                        receiverId = client1Id,
                        messageContent = "Awesome! Focus on depth and control during your top squat sets.",
                        timestamp = now - 4 * 3600 * 1000L
                    )
                )
            )

            // 8. Seed Initial Sessions for Alex
            val sessionDao = database.clientSessionDao()
            val todayStart = now - (now % (24 * 3600 * 1000L)) + (10 * 3600 * 1000L) // 10:00 AM today
            sessionDao.insertSession(
                ClientSessionEntity(
                    userId = client1Id,
                    sessionNumber = 1,
                    sessionTitle = "Day 1: Upper Body Chest & Triceps Hypertrophy",
                    sessionTimestamp = todayStart - 24 * 3600 * 1000L,
                    status = "Completed",
                    notes = "Great intensity, hit bench PR of 80kg for 8 reps."
                )
            )
            sessionDao.insertSession(
                ClientSessionEntity(
                    userId = client1Id,
                    sessionNumber = 2,
                    sessionTitle = "Day 2: Lower Body Squat & Hamstring Power",
                    sessionTimestamp = todayStart,
                    status = "Scheduled",
                    notes = "Focus on form and deep knee flexion."
                )
            )
            sessionDao.insertSession(
                ClientSessionEntity(
                    userId = client1Id,
                    sessionNumber = 3,
                    sessionTitle = "Day 3: Back, Biceps & Core Conditioning",
                    sessionTimestamp = todayStart + 48 * 3600 * 1000L,
                    status = "Scheduled",
                    notes = "Include weighted pull-ups and planks."
                )
            )
        }
    }
}
