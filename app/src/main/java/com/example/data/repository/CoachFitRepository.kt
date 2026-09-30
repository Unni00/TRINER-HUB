package com.example.data.repository

import com.example.data.firebase.FirebaseProfileService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ClientSessionEntity
import com.example.data.local.entity.DailyFoodLogEntity
import com.example.data.local.entity.FoodItemEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WeeklyCheckinEntity
import com.example.data.local.entity.WorkoutLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CoachFitRepository(
    private val database: AppDatabase,
    private val firebaseProfileService: FirebaseProfileService? = null
) {
    private val userDao = database.userDao()
    private val foodItemDao = database.foodItemDao()
    private val dailyFoodLogDao = database.dailyFoodLogDao()
    private val workoutLogDao = database.workoutLogDao()
    private val weeklyCheckinDao = database.weeklyCheckinDao()
    private val messageDao = database.messageDao()
    private val clientSessionDao = database.clientSessionDao()

    suspend fun ensureDatabaseSeeded() = withContext(Dispatchers.IO) {
        AppDatabase.seedDatabase(database)
    }

    // --- Users ---
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val clients: Flow<List<UserEntity>> = userDao.getClients()
    val trainers: Flow<List<UserEntity>> = userDao.getTrainers()

    fun getUserById(id: Long): Flow<UserEntity?> = userDao.getUserById(id)

    suspend fun getUserByIdDirect(id: Long): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByIdDirect(id)
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    suspend fun insertUser(user: UserEntity): Long = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(userId: Long) = withContext(Dispatchers.IO) {
        userDao.deleteUser(userId)
    }

    suspend fun updateWaterIntake(userId: Long, waterMl: Int) = withContext(Dispatchers.IO) {
        userDao.updateWaterIntake(userId, waterMl)
    }

    suspend fun updateMacros(userId: Long, calories: Int, protein: Int, carbs: Int, fats: Int) =
        withContext(Dispatchers.IO) {
            userDao.updateMacros(userId, calories, protein, carbs, fats)
        }

    suspend fun updateClientStatsAndFeedback(userId: Long, weight: Float, height: Float, age: Int, feedback: String) =
        withContext(Dispatchers.IO) {
            userDao.updateClientStatsAndFeedback(userId, weight, height, age, feedback)
        }

    suspend fun updateClientBiometricsAndHealth(
        userId: Long,
        weight: Float,
        height: Float,
        age: Int,
        sex: String,
        healthInfo: String,
        feedback: String
    ) = withContext(Dispatchers.IO) {
        userDao.updateClientBiometricsAndHealth(userId, weight, height, age, sex, healthInfo, feedback)
    }

    suspend fun saveClientBiometrics(
        user: UserEntity,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        sex: String = "Male",
        healthInfo: String = "No known medical issues",
        feedback: String = ""
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        // 1. Update local database profile
        userDao.updateClientBiometricsAndHealth(user.id, weightKg, heightCm, age, sex, healthInfo, feedback)
        
        // 2. Track in weekly check-ins history
        weeklyCheckinDao.insertCheckin(
            WeeklyCheckinEntity(
                userId = user.id,
                date = System.currentTimeMillis(),
                currentWeight = weightKg,
                sleepQuality = 4,
                energyLevel = 4,
                frontPhotoUri = null,
                sidePhotoUri = null,
                backPhotoUri = null,
                notes = feedback
            )
        )

        // 3. Save to Firebase User Profile
        firebaseProfileService?.saveUserBiometrics(user, age, heightCm, weightKg, sex, healthInfo, feedback)
            ?: Pair(true, "Biometrics saved to profile")
    }

    // --- Food Items ---
    val allFoodItems: Flow<List<FoodItemEntity>> = foodItemDao.getAllFoodItems()

    fun searchFoodItems(query: String): Flow<List<FoodItemEntity>> =
        foodItemDao.searchFoodItems(query)

    suspend fun addFoodItem(item: FoodItemEntity): Long = withContext(Dispatchers.IO) {
        foodItemDao.insertFoodItem(item)
    }

    // --- Daily Food Logs ---
    val allFoodLogs: Flow<List<DailyFoodLogEntity>> = dailyFoodLogDao.getAllFoodLogs()

    fun getFoodLogsForUser(userId: Long): Flow<List<DailyFoodLogEntity>> =
        dailyFoodLogDao.getLogsForUser(userId)

    fun getTodayFoodLogsForUser(userId: Long, startOfDayMillis: Long): Flow<List<DailyFoodLogEntity>> =
        dailyFoodLogDao.getTodayLogsForUser(userId, startOfDayMillis)

    suspend fun logMeal(
        userId: Long,
        foodItemId: Long,
        foodName: String,
        mealType: String,
        gramsConsumed: Float,
        calPer100g: Float,
        proteinPer100g: Float,
        carbsPer100g: Float,
        fatPer100g: Float
    ): Long = withContext(Dispatchers.IO) {
        val multiplier = gramsConsumed / 100f
        val log = DailyFoodLogEntity(
            userId = userId,
            foodItemId = foodItemId,
            foodName = foodName,
            dateLogged = System.currentTimeMillis(),
            mealType = mealType,
            gramsConsumed = gramsConsumed,
            totalCalories = multiplier * calPer100g,
            totalProtein = multiplier * proteinPer100g,
            totalCarbs = multiplier * carbsPer100g,
            totalFats = multiplier * fatPer100g
        )
        dailyFoodLogDao.insertLog(log)
    }

    suspend fun deleteFoodLog(id: Long) = withContext(Dispatchers.IO) {
        dailyFoodLogDao.deleteLogById(id)
    }

    // --- Workout Logs ---
    val allWorkoutLogs: Flow<List<WorkoutLogEntity>> = workoutLogDao.getAllWorkoutLogs()

    fun getWorkoutLogsForUser(userId: Long): Flow<List<WorkoutLogEntity>> =
        workoutLogDao.getLogsForUser(userId)

    suspend fun logWorkout(
        userId: Long,
        exerciseName: String,
        sets: Int,
        reps: Int,
        weightLifted: Float
    ): Long = withContext(Dispatchers.IO) {
        val log = WorkoutLogEntity(
            userId = userId,
            dateLogged = System.currentTimeMillis(),
            exerciseName = exerciseName,
            sets = sets,
            reps = reps,
            weightLifted = weightLifted
        )
        workoutLogDao.insertLog(log)
    }

    suspend fun deleteWorkoutLog(id: Long) = withContext(Dispatchers.IO) {
        workoutLogDao.deleteLogById(id)
    }

    // --- Weekly Check-ins ---
    fun getCheckinsForUser(userId: Long): Flow<List<WeeklyCheckinEntity>> =
        weeklyCheckinDao.getCheckinsForUser(userId)

    suspend fun submitCheckin(
        userId: Long,
        currentWeight: Float,
        sleepQuality: Int,
        energyLevel: Int,
        frontPhotoUri: String?,
        sidePhotoUri: String?,
        backPhotoUri: String?,
        notes: String?
    ): Long = withContext(Dispatchers.IO) {
        val checkin = WeeklyCheckinEntity(
            userId = userId,
            date = System.currentTimeMillis(),
            currentWeight = currentWeight,
            sleepQuality = sleepQuality,
            energyLevel = energyLevel,
            frontPhotoUri = frontPhotoUri,
            sidePhotoUri = sidePhotoUri,
            backPhotoUri = backPhotoUri,
            notes = notes
        )
        weeklyCheckinDao.insertCheckin(checkin)
    }

    suspend fun deleteCheckin(id: Long) = withContext(Dispatchers.IO) {
        weeklyCheckinDao.deleteCheckinById(id)
    }

    // --- Messages ---
    fun getMessagesBetween(userA: Long, userB: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesBetween(userA, userB)

    fun getAllMessagesForUser(userId: Long): Flow<List<MessageEntity>> =
        messageDao.getAllMessagesForUser(userId)

    suspend fun sendMessage(
        senderId: Long,
        receiverId: Long,
        messageContent: String
    ): Long = withContext(Dispatchers.IO) {
        val message = MessageEntity(
            senderId = senderId,
            receiverId = receiverId,
            messageContent = messageContent,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(message)
    }

    // --- Client Sessions & Attendance ---
    suspend fun updateTotalPurchasedSessions(userId: Long, totalSessions: Int) =
        withContext(Dispatchers.IO) {
            userDao.updateTotalPurchasedSessions(userId, totalSessions)
        }

    fun getSessionsForUser(userId: Long): Flow<List<ClientSessionEntity>> =
        clientSessionDao.getSessionsForUser(userId)

    val allSessions: Flow<List<ClientSessionEntity>> = clientSessionDao.getAllSessions()

    suspend fun insertSession(userId: Long, sessionNumber: Int, sessionTitle: String, sessionTimestamp: Long, notes: String = ""): Long =
        withContext(Dispatchers.IO) {
            clientSessionDao.insertSession(
                ClientSessionEntity(
                    userId = userId,
                    sessionNumber = sessionNumber,
                    sessionTitle = sessionTitle,
                    sessionTimestamp = sessionTimestamp,
                    status = "Scheduled",
                    notes = notes
                )
            )
        }

    suspend fun updateSessionAttendance(sessionId: Long, status: String) =
        withContext(Dispatchers.IO) {
            clientSessionDao.updateAttendance(sessionId, status)
        }

    suspend fun clientCheckInSession(sessionId: Long) =
        withContext(Dispatchers.IO) {
            clientSessionDao.clientCheckIn(sessionId, "Reached / Attending", System.currentTimeMillis())
        }

    suspend fun updateSession(
        sessionId: Long,
        userId: Long,
        sessionNumber: Int,
        sessionTitle: String,
        sessionTimestamp: Long,
        status: String,
        notes: String = "",
        clientCheckInTime: Long? = null
    ) = withContext(Dispatchers.IO) {
        clientSessionDao.updateSession(
            ClientSessionEntity(
                id = sessionId,
                userId = userId,
                sessionNumber = sessionNumber,
                sessionTitle = sessionTitle,
                sessionTimestamp = sessionTimestamp,
                status = status,
                clientCheckInTime = clientCheckInTime,
                notes = notes
            )
        )
    }

    suspend fun deleteSession(sessionId: Long) =
        withContext(Dispatchers.IO) {
            clientSessionDao.deleteSession(sessionId)
        }

    suspend fun updateWorkoutLog(id: Long, userId: Long, exerciseName: String, sets: Int, reps: Int, weightLifted: Float, dateLogged: Long) =
        withContext(Dispatchers.IO) {
            workoutLogDao.updateLog(
                WorkoutLogEntity(
                    id = id,
                    userId = userId,
                    exerciseName = exerciseName,
                    sets = sets,
                    reps = reps,
                    weightLifted = weightLifted,
                    dateLogged = dateLogged
                )
            )
        }
}
