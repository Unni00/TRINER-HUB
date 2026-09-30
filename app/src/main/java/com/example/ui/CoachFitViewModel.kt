package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseProfileService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ClientSessionEntity
import com.example.data.local.entity.DailyFoodLogEntity
import com.example.data.local.entity.FoodItemEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WeeklyCheckinEntity
import com.example.data.local.entity.WorkoutLogEntity
import com.example.data.repository.CoachFitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ClientTab {
    HOME, MEALS, WORKOUTS, CHECKIN, CHAT, BIOMETRICS
}

enum class TrainerTab {
    DASHBOARD, ROSTER, INBOX
}

@OptIn(ExperimentalCoroutinesApi::class)
class CoachFitViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val firebaseProfileService = FirebaseProfileService(application)
    val repository = CoachFitRepository(database, firebaseProfileService)

    // Current Logged In User
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Navigation State
    private val _clientTab = MutableStateFlow(ClientTab.HOME)
    val clientTab: StateFlow<ClientTab> = _clientTab.asStateFlow()

    private val _trainerTab = MutableStateFlow(TrainerTab.DASHBOARD)
    val trainerTab: StateFlow<TrainerTab> = _trainerTab.asStateFlow()

    // Trainer Dashboard client filter (null = all assigned clients)
    private val _trainerDashboardClientId = MutableStateFlow<Long?>(null)
    val trainerDashboardClientId: StateFlow<Long?> = _trainerDashboardClientId.asStateFlow()

    // Trainer's selected client for detail view
    private val _selectedClient = MutableStateFlow<UserEntity?>(null)
    val selectedClient: StateFlow<UserEntity?> = _selectedClient.asStateFlow()

    // Macro Assigner Modal
    private val _isMacroAssignerOpen = MutableStateFlow(false)
    val isMacroAssignerOpen: StateFlow<Boolean> = _isMacroAssignerOpen.asStateFlow()

    // Food item search query
    private val _foodSearchQuery = MutableStateFlow("")
    val foodSearchQuery: StateFlow<String> = _foodSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Data streams from repository
    val allClients: StateFlow<List<UserEntity>> = repository.clients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrainers: StateFlow<List<UserEntity>> = repository.trainers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFoodItems: StateFlow<List<FoodItemEntity>> = repository.allFoodItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered food items based on search and category
    val filteredFoodItems: StateFlow<List<FoodItemEntity>> = combine(
        allFoodItems,
        _foodSearchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            val matchesQuery = query.isBlank() || item.foodName.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || item.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current client logs
    val clientFoodLogs: StateFlow<List<DailyFoodLogEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && !user.isTrainer) {
            repository.getFoodLogsForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clientWorkoutLogs: StateFlow<List<WorkoutLogEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && !user.isTrainer) {
            repository.getWorkoutLogsForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clientCheckins: StateFlow<List<WeeklyCheckinEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && !user.isTrainer) {
            repository.getCheckinsForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clientSessions: StateFlow<List<ClientSessionEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && !user.isTrainer) {
            repository.getSessionsForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedClientSessions: StateFlow<List<ClientSessionEntity>> = _selectedClient.flatMapLatest { client ->
        if (client != null) {
            repository.getSessionsForUser(client.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current client chat with coach (first trainer found)
    val clientMessages: StateFlow<List<MessageEntity>> = combine(_currentUser, allTrainers) { user, trainers ->
        Pair(user, trainers)
    }.flatMapLatest { (user, trainers) ->
        val trainer = trainers.firstOrNull()
        if (user != null && trainer != null) {
            repository.getMessagesBetween(user.id, trainer.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected client's data when viewed by trainer
    val selectedClientFoodLogs: StateFlow<List<DailyFoodLogEntity>> = _selectedClient.flatMapLatest { client ->
        if (client != null) repository.getFoodLogsForUser(client.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedClientWorkoutLogs: StateFlow<List<WorkoutLogEntity>> = _selectedClient.flatMapLatest { client ->
        if (client != null) repository.getWorkoutLogsForUser(client.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedClientCheckins: StateFlow<List<WeeklyCheckinEntity>> = _selectedClient.flatMapLatest { client ->
        if (client != null) repository.getCheckinsForUser(client.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedClientMessages: StateFlow<List<MessageEntity>> = combine(_currentUser, _selectedClient) { trainer, client ->
        Pair(trainer, client)
    }.flatMapLatest { (trainer, client) ->
        if (trainer != null && client != null) {
            repository.getMessagesBetween(trainer.id, client.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trainer Inbox: all messages for the trainer
    val trainerAllMessages: StateFlow<List<MessageEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && user.isTrainer) {
            repository.getAllMessagesForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trainer Dashboard: All food and workout logs from assigned clients
    val allAssignedFoodLogs: StateFlow<List<DailyFoodLogEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && user.isTrainer) {
            repository.allFoodLogs
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignedWorkoutLogs: StateFlow<List<WorkoutLogEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && user.isTrainer) {
            repository.allWorkoutLogs
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard filtered logs (filtered by selected client if set, or all assigned clients)
    val dashboardFilteredFoodLogs: StateFlow<List<DailyFoodLogEntity>> = combine(
        allAssignedFoodLogs,
        _trainerDashboardClientId
    ) { logs, selectedId ->
        if (selectedId == null) logs else logs.filter { it.userId == selectedId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardFilteredWorkoutLogs: StateFlow<List<WorkoutLogEntity>> = combine(
        allAssignedWorkoutLogs,
        _trainerDashboardClientId
    ) { logs, selectedId ->
        if (selectedId == null) logs else logs.filter { it.userId == selectedId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignedSessions: StateFlow<List<ClientSessionEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null && user.isTrainer) {
            repository.allSessions
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardFilteredSessions: StateFlow<List<ClientSessionEntity>> = combine(
        allAssignedSessions,
        _trainerDashboardClientId
    ) { sessions, selectedId ->
        if (selectedId == null) sessions else sessions.filter { it.userId == selectedId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTrainerDashboardClientId(clientId: Long?) {
        _trainerDashboardClientId.value = clientId
    }

    init {
        viewModelScope.launch {
            repository.ensureDatabaseSeeded()
            // Default login to demo client so app opens ready to explore
            loginAsDemoClient()
        }
    }

    fun setClientTab(tab: ClientTab) {
        _clientTab.value = tab
    }

    fun setTrainerTab(tab: TrainerTab) {
        _trainerTab.value = tab
    }

    fun setSelectedClient(client: UserEntity?) {
        _selectedClient.value = client
    }

    fun setMacroAssignerOpen(isOpen: Boolean) {
        _isMacroAssignerOpen.value = isOpen
    }

    fun setFoodSearchQuery(query: String) {
        _foodSearchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // --- Authentication Actions ---
    fun login(email: String, password: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email.trim().lowercase())
            if (user != null && user.password == password) {
                _currentUser.value = user
                _selectedClient.value = null
            } else {
                onError("Invalid email or password.")
            }
        }
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        isTrainer: Boolean,
        age: Int = 26,
        sex: String = "Male",
        heightCm: Float = 175f,
        weightKg: Float = 78f,
        healthInfo: String = "No known medical issues",
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email.trim().lowercase())
            if (existing != null) {
                onError("An account with this email already exists.")
                return@launch
            }
            val newUser = UserEntity(
                email = email.trim().lowercase(),
                password = password,
                fullName = name.trim(),
                isTrainer = isTrainer,
                age = age,
                sex = sex,
                heightCm = heightCm,
                currentWeightKg = weightKg,
                healthInfo = healthInfo,
                targetCalories = if (isTrainer) 2600 else 2100,
                targetProtein = if (isTrainer) 180 else 145,
                targetCarbs = if (isTrainer) 280 else 225,
                targetFats = if (isTrainer) 70 else 55,
                waterIntakeMl = 0
            )
            val id = repository.insertUser(newUser)
            val insertedUser = newUser.copy(id = id)
            _currentUser.value = insertedUser
            _selectedClient.value = null

            // Sync client initial biometrics to Firebase Cloud
            if (!isTrainer) {
                firebaseProfileService.saveUserBiometrics(
                    user = insertedUser,
                    age = age,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    sex = sex,
                    healthInfo = healthInfo
                )
            }
        }
    }

    fun clientLoginWithBiometrics(
        email: String,
        age: Int,
        sex: String,
        heightCm: Float,
        weightKg: Float,
        healthInfo: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val targetEmail = email.trim().lowercase()
            val existing = repository.getUserByEmail(targetEmail)
            if (existing != null) {
                // Update client's biometrics & health info upon login
                repository.updateClientBiometricsAndHealth(
                    userId = existing.id,
                    weight = weightKg,
                    height = heightCm,
                    age = age,
                    sex = sex,
                    healthInfo = healthInfo,
                    feedback = existing.feedback
                )
                val updated = existing.copy(
                    age = age,
                    sex = sex,
                    heightCm = heightCm,
                    currentWeightKg = weightKg,
                    healthInfo = healthInfo
                )
                _currentUser.value = updated
                _clientTab.value = ClientTab.HOME
                firebaseProfileService.saveUserBiometrics(
                    user = updated,
                    age = age,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    sex = sex,
                    healthInfo = healthInfo
                )
                onSuccess()
            } else {
                // Register and log in client with these biometrics
                val newUser = UserEntity(
                    email = targetEmail,
                    password = "password123",
                    fullName = targetEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                    isTrainer = false,
                    age = age,
                    sex = sex,
                    heightCm = heightCm,
                    currentWeightKg = weightKg,
                    healthInfo = healthInfo,
                    targetCalories = 2100,
                    targetProtein = 145,
                    targetCarbs = 225,
                    targetFats = 55
                )
                val id = repository.insertUser(newUser)
                val inserted = newUser.copy(id = id)
                _currentUser.value = inserted
                _clientTab.value = ClientTab.HOME
                firebaseProfileService.saveUserBiometrics(
                    user = inserted,
                    age = age,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    sex = sex,
                    healthInfo = healthInfo
                )
                onSuccess()
            }
        }
    }

    fun loginAsDemoClient() {
        viewModelScope.launch {
            val user = repository.getUserByEmail("alex@example.com")
            if (user != null) {
                _currentUser.value = user
                _selectedClient.value = null
                _clientTab.value = ClientTab.HOME
            }
        }
    }

    fun loginAsDemoTrainer() {
        viewModelScope.launch {
            val user = repository.getUserByEmail("coach@coachfit.com")
            if (user != null) {
                _currentUser.value = user
                _selectedClient.value = null
                _trainerTab.value = TrainerTab.ROSTER
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _selectedClient.value = null
    }

    // Switch between Client & Trainer for quick testing/demo
    fun switchRoleDemo() {
        val current = _currentUser.value
        if (current?.isTrainer == true) {
            loginAsDemoClient()
        } else {
            loginAsDemoTrainer()
        }
    }

    // --- Water Tracker Action ---
    fun updateWaterIntake(amountMl: Int) {
        val user = _currentUser.value ?: return
        val newAmount = (user.waterIntakeMl + amountMl).coerceAtLeast(0)
        viewModelScope.launch {
            repository.updateWaterIntake(user.id, newAmount)
            _currentUser.value = user.copy(waterIntakeMl = newAmount)
        }
    }

    fun setExactWaterIntake(amountMl: Int) {
        val user = _currentUser.value ?: return
        val newAmount = amountMl.coerceAtLeast(0)
        viewModelScope.launch {
            repository.updateWaterIntake(user.id, newAmount)
            _currentUser.value = user.copy(waterIntakeMl = newAmount)
        }
    }

    // --- Food Actions ---
    fun logMeal(
        foodItem: FoodItemEntity,
        mealType: String,
        grams: Float,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.logMeal(
                userId = user.id,
                foodItemId = foodItem.id,
                foodName = foodItem.foodName,
                mealType = mealType,
                gramsConsumed = grams,
                calPer100g = foodItem.calPer100g,
                proteinPer100g = foodItem.proteinPer100g,
                carbsPer100g = foodItem.carbsPer100g,
                fatPer100g = foodItem.fatPer100g
            )
            onSuccess()
        }
    }

    fun addCustomFoodItem(
        name: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fat: Float,
        category: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.addFoodItem(
                FoodItemEntity(
                    foodName = name.trim(),
                    calPer100g = calories,
                    proteinPer100g = protein,
                    carbsPer100g = carbs,
                    fatPer100g = fat,
                    category = category.trim().ifEmpty { "General" }
                )
            )
            onSuccess()
        }
    }

    fun deleteFoodLog(id: Long) {
        viewModelScope.launch {
            repository.deleteFoodLog(id)
        }
    }

    // --- Workout Actions ---
    fun logWorkout(
        exerciseName: String,
        sets: Int,
        reps: Int,
        weight: Float,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.logWorkout(
                userId = user.id,
                exerciseName = exerciseName.trim(),
                sets = sets,
                reps = reps,
                weightLifted = weight
            )
            onSuccess()
        }
    }

    fun deleteWorkoutLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(id)
        }
    }

    // --- Weekly Check-in Actions ---
    fun submitWeeklyCheckin(
        weight: Float,
        sleepQuality: Int,
        energyLevel: Int,
        frontPhotoUri: String?,
        sidePhotoUri: String?,
        backPhotoUri: String?,
        notes: String?,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.submitCheckin(
                userId = user.id,
                currentWeight = weight,
                sleepQuality = sleepQuality,
                energyLevel = energyLevel,
                frontPhotoUri = frontPhotoUri,
                sidePhotoUri = sidePhotoUri,
                backPhotoUri = backPhotoUri,
                notes = notes
            )
            onSuccess()
        }
    }

    fun deleteCheckin(id: Long) {
        viewModelScope.launch {
            repository.deleteCheckin(id)
        }
    }

    // --- Messaging Actions ---
    fun sendClientMessage(content: String) {
        val user = _currentUser.value ?: return
        val trainer = allTrainers.value.firstOrNull() ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                senderId = user.id,
                receiverId = trainer.id,
                messageContent = content.trim()
            )
        }
    }

    fun sendTrainerMessage(recipientClientId: Long, content: String) {
        val trainer = _currentUser.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                senderId = trainer.id,
                receiverId = recipientClientId,
                messageContent = content.trim()
            )
        }
    }

    // --- Client Physical Stats & Feedback ---
    fun updateClientPhysicalStatsAndFeedback(
        weight: Float,
        height: Float,
        age: Int,
        feedback: String,
        onSuccess: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateClientStatsAndFeedback(user.id, weight, height, age, feedback)
            // Also log to weekly checkins so progress history is tracked
            repository.submitCheckin(
                userId = user.id,
                currentWeight = weight,
                sleepQuality = 4,
                energyLevel = 4,
                frontPhotoUri = null,
                sidePhotoUri = null,
                backPhotoUri = null,
                notes = feedback
            )
            _currentUser.value = user.copy(
                currentWeightKg = weight,
                heightCm = height,
                age = age,
                feedback = feedback
            )
            onSuccess()
        }
    }

    // --- Trainer Logging for Client ---
    fun trainerLogWorkoutForClient(
        clientId: Long,
        exerciseName: String,
        sets: Int,
        reps: Int,
        weight: Float,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.logWorkout(
                userId = clientId,
                exerciseName = exerciseName.trim(),
                sets = sets,
                reps = reps,
                weightLifted = weight
            )
            onSuccess()
        }
    }

    fun trainerPrescribeFoodLogForClient(
        clientId: Long,
        foodItem: FoodItemEntity,
        mealType: String,
        grams: Float,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.logMeal(
                userId = clientId,
                foodItemId = foodItem.id,
                foodName = foodItem.foodName,
                mealType = mealType,
                gramsConsumed = grams,
                calPer100g = foodItem.calPer100g,
                proteinPer100g = foodItem.proteinPer100g,
                carbsPer100g = foodItem.carbsPer100g,
                fatPer100g = foodItem.fatPer100g
            )
            onSuccess()
        }
    }

    fun trainerPrescribeMultipleFoodLogsForClient(
        clientId: Long,
        items: List<Triple<FoodItemEntity, String, Float>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            items.forEach { (foodItem, mealType, grams) ->
                repository.logMeal(
                    userId = clientId,
                    foodItemId = foodItem.id,
                    foodName = foodItem.foodName,
                    mealType = mealType,
                    gramsConsumed = grams,
                    calPer100g = foodItem.calPer100g,
                    proteinPer100g = foodItem.proteinPer100g,
                    carbsPer100g = foodItem.carbsPer100g,
                    fatPer100g = foodItem.fatPer100g
                )
            }
            onSuccess()
        }
    }

    // --- Macro Assigner (Trainer) ---
    fun updateClientMacros(
        clientId: Long,
        calories: Int,
        protein: Int,
        carbs: Int,
        fats: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateMacros(clientId, calories, protein, carbs, fats)
            // Update selected client state if matched
            _selectedClient.value?.let { currentSelected ->
                if (currentSelected.id == clientId) {
                    _selectedClient.value = currentSelected.copy(
                        targetCalories = calories,
                        targetProtein = protein,
                        targetCarbs = carbs,
                        targetFats = fats
                    )
                }
            }
            onSuccess()
        }
    }

    // --- Firebase Client Biometrics Sync ---
    fun saveClientBiometricsToFirebase(
        age: Int,
        height: Float,
        weight: Float,
        sex: String = "Male",
        healthInfo: String = "No known medical issues",
        feedback: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val (success, message) = repository.saveClientBiometrics(
                user = user,
                age = age,
                heightCm = height,
                weightKg = weight,
                sex = sex,
                healthInfo = healthInfo,
                feedback = feedback
            )
            _currentUser.value = user.copy(
                age = age,
                heightCm = height,
                currentWeightKg = weight,
                sex = sex,
                healthInfo = healthInfo,
                feedback = feedback
            )
            onResult(success, message)
        }
    }

    fun updateCoachProfile(name: String, profession: String, profilePictureUri: String?) {
        val user = _currentUser.value ?: return
        if (!user.isTrainer) return
        viewModelScope.launch {
            val updated = user.copy(
                fullName = name.trim(),
                profession = profession.trim(),
                profilePictureUri = profilePictureUri
            )
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    fun updateClientProfile(name: String, profilePictureUri: String?) {
        val user = _currentUser.value ?: return
        if (user.isTrainer) return
        viewModelScope.launch {
            val updated = user.copy(
                fullName = name.trim(),
                profilePictureUri = profilePictureUri
            )
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    fun deleteClient(userId: Long, onComplete: () -> Unit = {}) {
        val user = _currentUser.value ?: return
        if (!user.isTrainer) return
        viewModelScope.launch {
            repository.deleteUser(userId)
            if (_trainerDashboardClientId.value == userId) {
                _trainerDashboardClientId.value = null
            }
            if (_selectedClient.value?.id == userId) {
                _selectedClient.value = null
            }
            onComplete()
        }
    }

    fun updateTotalPurchasedSessions(userId: Long, totalSessions: Int) {
        viewModelScope.launch {
            repository.updateTotalPurchasedSessions(userId, totalSessions)
            _selectedClient.value?.let { current ->
                if (current.id == userId) {
                    _selectedClient.value = current.copy(totalPurchasedSessions = totalSessions)
                }
            }
        }
    }

    fun addClientSession(userId: Long, sessionNumber: Int, title: String, timestamp: Long, notes: String = "") {
        viewModelScope.launch {
            repository.insertSession(userId, sessionNumber, title, timestamp, notes)
        }
    }

    fun updateSessionAttendance(sessionId: Long, status: String) {
        viewModelScope.launch {
            repository.updateSessionAttendance(sessionId, status)
        }
    }

    fun clientCheckInSession(sessionId: Long) {
        viewModelScope.launch {
            repository.clientCheckInSession(sessionId)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun updateWorkoutLog(id: Long, userId: Long, exerciseName: String, sets: Int, reps: Int, weightLifted: Float, dateLogged: Long) {
        viewModelScope.launch {
            repository.updateWorkoutLog(id, userId, exerciseName, sets, reps, weightLifted, dateLogged)
        }
    }

    fun updateSession(
        sessionId: Long,
        userId: Long,
        sessionNumber: Int,
        sessionTitle: String,
        sessionTimestamp: Long,
        status: String,
        notes: String = "",
        clientCheckInTime: Long? = null
    ) {
        viewModelScope.launch {
            repository.updateSession(sessionId, userId, sessionNumber, sessionTitle, sessionTimestamp, status, notes, clientCheckInTime)
        }
    }
}
