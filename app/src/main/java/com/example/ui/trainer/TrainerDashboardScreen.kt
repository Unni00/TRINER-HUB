package com.example.ui.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DailyFoodLogEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WorkoutLogEntity
import com.example.data.local.entity.ClientSessionEntity
import com.example.ui.CoachFitViewModel
import com.example.ui.components.CoachFitCard
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrainerDashboardScreen(
    viewModel: CoachFitViewModel,
    onNavigateToClientDetail: (UserEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allClients by viewModel.allClients.collectAsStateWithLifecycle()
    val foodLogs by viewModel.dashboardFilteredFoodLogs.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.dashboardFilteredWorkoutLogs.collectAsStateWithLifecycle()
    val sessions by viewModel.dashboardFilteredSessions.collectAsStateWithLifecycle()
    val selectedClientId by viewModel.trainerDashboardClientId.collectAsStateWithLifecycle()
    val allFoodItems by viewModel.allFoodItems.collectAsStateWithLifecycle()

    var dashboardTab by remember { mutableIntStateOf(0) } // 0: Nutritional Macros, 1: Workout Logs, 2: Client Health Stats, 3: Sessions & Attendance
    var showMacroAssignerForClient by remember { mutableStateOf<UserEntity?>(null) }
    var showPrescribeMealForClient by remember { mutableStateOf<UserEntity?>(null) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showEditCoachProfileDialog by remember { mutableStateOf(false) }
    var editCoachName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var editCoachProfession by remember { mutableStateOf(currentUser?.profession ?: "") }
    var editCoachPhotoUri by remember { mutableStateOf<String?>(currentUser?.profilePictureUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { editCoachPhotoUri = it.toString() }
    }

    // Role-based Access Guard: Strictly ensure only users with the 'Trainer' role can access this view
    if (currentUser?.isTrainer != true) {
        TrainerAccessDeniedScreen(onSwitchToDemoTrainer = { viewModel.loginAsDemoTrainer() })
        return
    }

    val selectedClient = allClients.firstOrNull { it.id == selectedClientId }

    // Aggregate statistics across assigned clients
    val totalCaloriesToday = remember(foodLogs) {
        foodLogs.sumOf { it.totalCalories.toDouble() }.toInt()
    }
    val totalWorkoutSets = remember(workoutLogs) {
        workoutLogs.sumOf { it.sets }
    }

    val dashboardScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(dashboardScrollState)
            .padding(16.dp)
    ) {
        // Top Header Banner with Role Verified Badge & Edit Profile Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Role verified",
                                tint = NeonGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TRAINER ACCESS ONLY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentUser?.fullName ?: "Trainer Hub",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = currentUser?.profession ?: "Elite Strength & Conditioning Coach",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9100)
                )
            }

            Button(
                onClick = {
                    editCoachName = currentUser?.fullName ?: ""
                    editCoachProfession = currentUser?.profession ?: ""
                    editCoachPhotoUri = currentUser?.profilePictureUri ?: ""
                    showEditCoachProfileDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceVariant,
                    contentColor = NeonGreen
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (showEditCoachProfileDialog) {
            Dialog(onDismissRequest = { showEditCoachProfileDialog = false }) {
                CoachFitCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkSurface,
                    cornerRadius = 16.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Edit Coach Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Profile Photo Picker / Preview
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(2.dp, NeonGreen, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!editCoachPhotoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = editCoachPhotoUri,
                                    contentDescription = "Trainer Photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Add Photo",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = {
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Select from Gallery", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                            }
                            if (!editCoachPhotoUri.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                TextButton(
                                    onClick = { editCoachPhotoUri = null }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editCoachName,
                            onValueChange = { editCoachName = it },
                            label = { Text("Coach Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editCoachProfession,
                            onValueChange = { editCoachProfession = it },
                            label = { Text("Profession / Specialty") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showEditCoachProfileDialog = false }) {
                                Text("Cancel", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateCoachProfile(
                                        name = editCoachName,
                                        profession = editCoachProfession,
                                        profilePictureUri = editCoachPhotoUri?.ifBlank { null }
                                    )
                                    showEditCoachProfileDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                            ) {
                                Text("Save Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // High-level KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardKpiCard(
                title = "Clients",
                value = "${allClients.size}",
                subtitle = "Active Assigned",
                accentColor = NeonGreen,
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
            DashboardKpiCard(
                title = "Macro Logs",
                value = "${foodLogs.size}",
                subtitle = "$totalCaloriesToday kcal logged",
                accentColor = AthleticOrange,
                icon = Icons.Default.Restaurant,
                modifier = Modifier.weight(1.1f)
            )
            DashboardKpiCard(
                title = "Workouts",
                value = "$totalWorkoutSets",
                subtitle = "Total Sets Done",
                accentColor = AthleticCyan,
                icon = Icons.Default.FitnessCenter,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // TRAINER DIET & MACRO CONTROL CENTER
        CoachFitCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trainer_diet_macro_controls"),
            backgroundColor = DarkSurface,
            cornerRadius = 14.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = AthleticOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Diet & Macros Control Center",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AthleticOrange.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("TRAINER ASSIGNER", fontSize = 9.sp, fontWeight = FontWeight.Black, color = AthleticOrange)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Set Target Macros
                    Button(
                        onClick = {
                            val clientToAssign = selectedClient ?: allClients.firstOrNull()
                            showMacroAssignerForClient = clientToAssign
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set Macros", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Prescribe Meal
                    Button(
                        onClick = {
                            val clientToPrescribe = selectedClient ?: allClients.firstOrNull()
                            showPrescribeMealForClient = clientToPrescribe
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prescribe Diet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Add Food to Catalog
                    Button(
                        onClick = { showAddFoodDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Food", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Client Selector Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Filter Assigned Clients:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            if (selectedClientId != null) {
                Text(
                    text = "Clear filter",
                    fontSize = 11.sp,
                    color = NeonGreen,
                    modifier = Modifier.clickable { viewModel.setTrainerDashboardClientId(null) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val isSelected = selectedClientId == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonGreen else DarkSurfaceVariant)
                        .clickable { viewModel.setTrainerDashboardClientId(null) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("filter_all_clients")
                ) {
                    Text(
                        text = "All Clients (${allClients.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else TextSecondary
                    )
                }
            }

            items(allClients, key = { it.id }) { client ->
                val isSelected = selectedClientId == client.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonGreen else DarkSurfaceVariant)
                        .clickable { viewModel.setTrainerDashboardClientId(client.id) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("filter_client_${client.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = client.fullName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${client.currentWeightKg}kg)",
                            fontSize = 10.sp,
                            color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Dashboard Tabs: 0: Nutritional Macro Logs, 1: Workout Logs, 2: Client Health & Feedback
        TabRow(
            selectedTabIndex = dashboardTab,
            containerColor = DarkSurface,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[dashboardTab]),
                    color = NeonGreen
                )
            }
        ) {
            Tab(
                selected = dashboardTab == 0,
                onClick = { dashboardTab = 0 },
                text = {
                    Text(
                        "Nutritional Macros (${foodLogs.size})",
                        fontSize = 12.sp,
                        fontWeight = if (dashboardTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_dashboard_macros")
            )

            Tab(
                selected = dashboardTab == 1,
                onClick = { dashboardTab = 1 },
                text = {
                    Text(
                        "Workout Logs (${workoutLogs.size})",
                        fontSize = 12.sp,
                        fontWeight = if (dashboardTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_dashboard_workouts")
            )

            Tab(
                selected = dashboardTab == 2,
                onClick = { dashboardTab = 2 },
                text = {
                    Text(
                        "Client Stats",
                        fontSize = 11.sp,
                        fontWeight = if (dashboardTab == 2) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag("tab_dashboard_clients")
            )

            Tab(
                selected = dashboardTab == 3,
                onClick = { dashboardTab = 3 },
                text = {
                    val attendingCount = sessions.count { it.status.contains("Reached", true) || it.status.contains("Attending", true) }
                    Text(
                        if (attendingCount > 0) "Sessions ($attendingCount 📍)" else "Sessions (${sessions.size})",
                        fontSize = 11.sp,
                        fontWeight = if (dashboardTab == 3) FontWeight.Bold else FontWeight.Normal,
                        color = if (attendingCount > 0) Color(0xFFFF9100) else Color.Unspecified
                    )
                },
                modifier = Modifier.testTag("tab_dashboard_sessions")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Activity Feed
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            when (dashboardTab) {
                0 -> {
                    // Nutritional Macro Logs Section
                    NutritionalMacroLogsFeed(
                        logs = foodLogs,
                        clients = allClients,
                        onOpenClient = { client -> onNavigateToClientDetail(client) },
                        onDeleteLog = { logId -> viewModel.deleteFoodLog(logId) }
                    )
                }
                1 -> {
                    // Workout Logs Section
                    WorkoutLogsFeed(
                        logs = workoutLogs,
                        clients = allClients,
                        onOpenClient = { client -> onNavigateToClientDetail(client) }
                    )
                }
                2 -> {
                    // Client Health & Feedback Summary Section
                    ClientHealthStatsFeed(
                        clients = if (selectedClient != null) listOf(selectedClient) else allClients,
                        workoutLogs = workoutLogs,
                        onOpenClient = { client -> onNavigateToClientDetail(client) },
                        onDeleteClient = { clientId -> viewModel.deleteClient(clientId) }
                    )
                }
                3 -> {
                    // Sessions & Live Attendance Section
                    SessionsAttendanceFeed(
                        sessions = sessions,
                        clients = allClients,
                        onOpenClient = { client -> onNavigateToClientDetail(client) },
                        onUpdateAttendance = { sessionId, status -> viewModel.updateSessionAttendance(sessionId, status) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // Macro Assigner Dialog
    showMacroAssignerForClient?.let { client ->
        MacroAssignerDialog(
            client = client,
            onDismiss = { showMacroAssignerForClient = null },
            onSaveMacros = { calories, protein, carbs, fats ->
                viewModel.updateClientMacros(client.id, calories, protein, carbs, fats) {
                    showMacroAssignerForClient = null
                }
            }
        )
    }

    // Prescribe Meal Dialog
    showPrescribeMealForClient?.let { client ->
        TrainerPrescribeMealDialog(
            clientName = client.fullName,
            foodItems = allFoodItems,
            clientTargetCalories = client.targetCalories,
            clientTargetProtein = client.targetProtein,
            clientTargetCarbs = client.targetCarbs,
            clientTargetFats = client.targetFats,
            onDismiss = { showPrescribeMealForClient = null },
            onSave = { items ->
                viewModel.trainerPrescribeMultipleFoodLogsForClient(client.id, items) {
                    showPrescribeMealForClient = null
                }
            }
        )
    }

    // Add Food to Catalog Dialog
    if (showAddFoodDialog) {
        TrainerAddFoodDialog(
            onDismiss = { showAddFoodDialog = false },
            onSaveFood = { name, calories, protein, carbs, fat, category ->
                viewModel.addCustomFoodItem(name, calories, protein, carbs, fat, category) {
                    showAddFoodDialog = false
                }
            }
        )
    }
}

@Composable
fun NutritionalMacroLogsFeed(
    logs: List<DailyFoodLogEntity>,
    clients: List<UserEntity>,
    onOpenClient: (UserEntity) -> Unit,
    onDeleteLog: ((Long) -> Unit)? = null
) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("No nutritional macro logs found for assigned client(s).", color = TextSecondary)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            logs.forEach { log ->
                val client = clients.firstOrNull { it.id == log.userId }
                CoachFitCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkSurface,
                    onClick = { client?.let { onOpenClient(it) } }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Client Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = client?.fullName ?: "Client #${log.userId}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = log.mealType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault()).format(Date(log.dateLogged)),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                if (onDeleteLog != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteLog(log.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove meal",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.foodName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${log.gramsConsumed.toInt()}g portion",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${log.totalCalories.toInt()} kcal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("P: ${log.totalProtein.toInt()}g", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                                    Text("•", fontSize = 11.sp, color = TextSecondary)
                                    Text("C: ${log.totalCarbs.toInt()}g", fontSize = 11.sp, color = AthleticOrange, fontWeight = FontWeight.SemiBold)
                                    Text("•", fontSize = 11.sp, color = TextSecondary)
                                    Text("F: ${log.totalFats.toInt()}g", fontSize = 11.sp, color = AthleticCyan, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutLogsFeed(
    logs: List<WorkoutLogEntity>,
    clients: List<UserEntity>,
    onOpenClient: (UserEntity) -> Unit
) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("No workout logs recorded for assigned client(s).", color = TextSecondary)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            logs.forEach { log ->
                val client = clients.firstOrNull { it.id == log.userId }
                val volume = (log.sets * log.reps * log.weightLifted).toInt()

                CoachFitCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkSurface,
                    onClick = { client?.let { onOpenClient(it) } }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AthleticOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = client?.fullName ?: "Client #${log.userId}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AthleticOrange
                                )
                            }

                            Text(
                                text = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault()).format(Date(log.dateLogged)),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = log.exerciseName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Volume: $volume kg",
                                    fontSize = 11.sp,
                                    color = AthleticCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${log.sets} sets × ${log.reps} reps",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "@ ${log.weightLifted} kg",
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientHealthStatsFeed(
    clients: List<UserEntity>,
    workoutLogs: List<WorkoutLogEntity>,
    onOpenClient: (UserEntity) -> Unit,
    onDeleteClient: (Long) -> Unit
) {
    var clientToDelete by remember { mutableStateOf<UserEntity?>(null) }

    if (clientToDelete != null) {
        Dialog(onDismissRequest = { clientToDelete = null }) {
            CoachFitCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface,
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Remove Client Profile?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Are you sure you want to remove ${clientToDelete?.fullName}? This action will permanently delete their account and logs.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { clientToDelete = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                clientToDelete?.let { onDeleteClient(it.id) }
                                clientToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White)
                        ) {
                            Text("Remove", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (clients.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.People, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("No clients found.", color = TextSecondary)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            clients.forEach { client ->
                val heightInM = client.heightCm / 100f
                val bmi = if (heightInM > 0) client.currentWeightKg / (heightInM * heightInM) else 0f
                val bmiFormatted = String.format(Locale.getDefault(), "%.1f", bmi)
                val clientWorkoutLogs = workoutLogs.filter { it.userId == client.id }
                val completedSessionDays = clientWorkoutLogs.map {
                    SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.dateLogged))
                }.distinct().size

                CoachFitCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkSurface,
                    onClick = { onOpenClient(client) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurfaceVariant)
                                        .border(1.dp, NeonGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(client.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(client.email, fontSize = 11.sp, color = TextSecondary)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { clientToDelete = client },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove Client",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Session Days Done Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Completed Session Days:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                            Text("$completedSessionDays Days (${clientWorkoutLogs.size} sets)", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Weight", fontSize = 10.sp, color = TextSecondary)
                                Text("${client.currentWeightKg} kg", fontSize = 12.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Height", fontSize = 10.sp, color = TextSecondary)
                                Text("${client.heightCm.toInt()} cm", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Age / Sex", fontSize = 10.sp, color = TextSecondary)
                                Text("${client.age}y • ${client.sex.take(1)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BMI", fontSize = 10.sp, color = TextSecondary)
                                Text(bmiFormatted, fontSize = 12.sp, fontWeight = FontWeight.Black, color = AthleticCyan)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Target", fontSize = 10.sp, color = TextSecondary)
                                Text("${client.targetCalories} kcal", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AthleticOrange)
                            }
                        }

                        if (client.healthInfo.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Health Info: ${client.healthInfo}",
                                fontSize = 11.sp,
                                color = Color(0xFFFF8A80),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        if (client.feedback.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Feedback: \"${client.feedback}\"",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardKpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    CoachFitCard(
        modifier = modifier,
        backgroundColor = DarkSurface
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = accentColor)
            Text(subtitle, fontSize = 9.sp, color = TextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun TrainerAccessDeniedScreen(
    onSwitchToDemoTrainer: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CoachFitCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface,
            cornerRadius = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Access Denied",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Trainer Access Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This screen displays nutritional macro logs and workout logs of assigned clients and is restricted exclusively to authorized Personal Trainers and Coaches.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onSwitchToDemoTrainer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Switch to Trainer Account (Coach Marcus)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SessionsAttendanceFeed(
    sessions: List<ClientSessionEntity>,
    clients: List<UserEntity>,
    onOpenClient: (UserEntity) -> Unit,
    onUpdateAttendance: (Long, String) -> Unit
) {
    if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("No training sessions scheduled for assigned client(s).", color = TextSecondary)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            sessions.forEach { session ->
                val client = clients.firstOrNull { it.id == session.userId }
                val isClientReached = session.status.contains("Reached", true) || session.status.contains("Attending", true)

                CoachFitCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isClientReached) Modifier.border(1.5.dp, Color(0xFFFF9100), RoundedCornerShape(14.dp)) else Modifier),
                    backgroundColor = DarkSurface,
                    onClick = { client?.let { onOpenClient(it) } }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (isClientReached) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF9100).copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📍", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                "CLIENT REACHED & ATTENDING NOW",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFFF9100)
                                            )
                                            session.clientCheckInTime?.let {
                                                Text(
                                                    "Checked in at ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it))}",
                                                    fontSize = 10.sp,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = { onUpdateAttendance(session.id, "Present") },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Confirm Present", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AthleticOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = client?.fullName ?: "Client #${session.userId}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AthleticOrange
                                )
                            }

                            Text(
                                text = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(session.sessionTimestamp)),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NeonGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "Session #${session.sessionNumber}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = session.sessionTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                if (session.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("Focus: ${session.notes}", fontSize = 11.sp, color = AthleticCyan)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1-Tap Attendance Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mark Attendance:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val statuses = listOf("Scheduled", "Present", "Absent", "Completed")
                                statuses.forEach { status ->
                                    val isSelected = session.status.equals(status, ignoreCase = true)
                                    val chipColor = when {
                                        status.contains("Present") || status.contains("Completed") -> NeonGreen
                                        status.contains("Absent") -> Color(0xFFFF5252)
                                        else -> AthleticCyan
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) chipColor else DarkSurfaceVariant)
                                            .clickable { onUpdateAttendance(session.id, status) }
                                            .padding(horizontal = 7.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
