package com.example.ui.trainer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.FoodItemEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.CoachFitViewModel
import com.example.ui.client.screens.MiniPhotoBadge
import com.example.ui.components.CoachFitCard
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    client: UserEntity,
    viewModel: CoachFitViewModel,
    onBackToRoster: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToRoster() }

    var selectedListIndex by remember { mutableIntStateOf(0) } // 0: Food Logs, 1: Workout Logs, 2: Check-ins
    val foodLogs by viewModel.selectedClientFoodLogs.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.selectedClientWorkoutLogs.collectAsStateWithLifecycle()
    val checkins by viewModel.selectedClientCheckins.collectAsStateWithLifecycle()
    val sessions by viewModel.selectedClientSessions.collectAsStateWithLifecycle()
    val allFoodItems by viewModel.allFoodItems.collectAsStateWithLifecycle()
    val isMacroModalOpen by viewModel.isMacroAssignerOpen.collectAsStateWithLifecycle()

    var showTrainerAddWorkoutDialog by remember { mutableStateOf(false) }
    var showTrainerPrescribeFoodDialog by remember { mutableStateOf(false) }
    var showTrainerAddFoodDialog by remember { mutableStateOf(false) }
    var showDeleteClientDialog by remember { mutableStateOf(false) }
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var showEditQuotaDialog by remember { mutableStateOf(false) }
    var newQuotaInput by remember(client.totalPurchasedSessions) { mutableStateOf(client.totalPurchasedSessions.toString()) }
    var newSessionTitle by remember { mutableStateOf("") }
    var newSessionDaysAhead by remember { mutableStateOf("1") }
    var newSessionNotes by remember { mutableStateOf("") }
    var editingWorkoutLog by remember { mutableStateOf<com.example.data.local.entity.WorkoutLogEntity?>(null) }
    var editingSession by remember { mutableStateOf<com.example.data.local.entity.ClientSessionEntity?>(null) }

    if (showEditQuotaDialog) {
        Dialog(onDismissRequest = { showEditQuotaDialog = false }) {
            CoachFitCard(modifier = Modifier.fillMaxWidth(), backgroundColor = DarkSurface, cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Total Package Sessions Quota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Set total number of training sessions booked/purchased for ${client.fullName}:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newQuotaInput,
                        onValueChange = { newQuotaInput = it },
                        label = { Text("Total Sessions (e.g. 10, 12, 20, 30)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditQuotaDialog = false }) { Text("Cancel", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val q = newQuotaInput.toIntOrNull() ?: client.totalPurchasedSessions
                                viewModel.updateTotalPurchasedSessions(client.id, q)
                                showEditQuotaDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                        ) {
                            Text("Save Quota", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    editingWorkoutLog?.let { log ->
        var editExercise by remember { mutableStateOf(log.exerciseName) }
        var editSets by remember { mutableStateOf(log.sets.toString()) }
        var editReps by remember { mutableStateOf(log.reps.toString()) }
        var editWeight by remember { mutableStateOf(log.weightLifted.toString()) }

        Dialog(onDismissRequest = { editingWorkoutLog = null }) {
            CoachFitCard(modifier = Modifier.fillMaxWidth(), backgroundColor = DarkSurface, cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Edit Workout Set", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editExercise,
                        onValueChange = { editExercise = it },
                        label = { Text("Exercise Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editSets,
                            onValueChange = { editSets = it },
                            label = { Text("Sets") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editReps,
                            onValueChange = { editReps = it },
                            label = { Text("Reps") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editWeight,
                            onValueChange = { editWeight = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingWorkoutLog = null }) { Text("Cancel", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val s = editSets.toIntOrNull() ?: log.sets
                                val r = editReps.toIntOrNull() ?: log.reps
                                val w = editWeight.toFloatOrNull() ?: log.weightLifted
                                if (editExercise.isNotBlank()) {
                                    viewModel.updateWorkoutLog(log.id, client.id, editExercise.trim(), s, r, w, log.dateLogged)
                                    editingWorkoutLog = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    editingSession?.let { session ->
        var editTitle by remember { mutableStateOf(session.sessionTitle) }
        var editNum by remember { mutableStateOf(session.sessionNumber.toString()) }
        var editNotes by remember { mutableStateOf(session.notes) }
        var editStatus by remember { mutableStateOf(session.status) }

        Dialog(onDismissRequest = { editingSession = null }) {
            CoachFitCard(modifier = Modifier.fillMaxWidth(), backgroundColor = DarkSurface, cornerRadius = 16.dp) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Edit Training Session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editNum,
                            onValueChange = { editNum = it },
                            label = { Text("Session #") },
                            modifier = Modifier.width(90.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Session Title") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Focus / Coach Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Put / Update Attendance:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val statuses = listOf("Scheduled", "Reached / Attending", "Present", "Absent", "Completed")
                        statuses.forEach { st ->
                            val isSel = editStatus.equals(st, ignoreCase = true)
                            val chipCol = when {
                                st.contains("Present") || st.contains("Completed") -> NeonGreen
                                st.contains("Absent") -> Color(0xFFFF5252)
                                st.contains("Reached") || st.contains("Attending") -> Color(0xFFFF9100)
                                else -> AthleticCyan
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) chipCol else DarkSurfaceVariant)
                                    .clickable { editStatus = st }
                                    .padding(horizontal = 6.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    when (st) {
                                        "Reached / Attending" -> "Attending"
                                        else -> st
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else TextSecondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingSession = null }) { Text("Cancel", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editTitle.isNotBlank()) {
                                    val num = editNum.toIntOrNull() ?: session.sessionNumber
                                    viewModel.updateSession(
                                        sessionId = session.id,
                                        userId = client.id,
                                        sessionNumber = num,
                                        sessionTitle = editTitle.trim(),
                                        sessionTimestamp = session.sessionTimestamp,
                                        status = editStatus,
                                        notes = editNotes.trim(),
                                        clientCheckInTime = session.clientCheckInTime
                                    )
                                    editingSession = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                        ) {
                            Text("Update Session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showAddSessionDialog) {
        Dialog(onDismissRequest = { showAddSessionDialog = false }) {
            CoachFitCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface,
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Schedule Day Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newSessionTitle,
                        onValueChange = { newSessionTitle = it },
                        label = { Text("Session Title (e.g. Day ${sessions.size + 1}: Leg Hypertrophy)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newSessionDaysAhead,
                        onValueChange = { newSessionDaysAhead = it },
                        label = { Text("Days from Today (0 = Today, 1 = Tomorrow, 2 = Day After)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newSessionNotes,
                        onValueChange = { newSessionNotes = it },
                        label = { Text("Coach Notes / Workout Focus (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddSessionDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newSessionTitle.isNotBlank()) {
                                    val days = newSessionDaysAhead.toLongOrNull() ?: 1L
                                    val targetMillis = System.currentTimeMillis() + (days * 24L * 60L * 60L * 1000L)
                                    viewModel.addClientSession(
                                        userId = client.id,
                                        sessionNumber = sessions.size + 1,
                                        title = newSessionTitle.trim(),
                                        timestamp = targetMillis,
                                        notes = newSessionNotes.trim()
                                    )
                                    newSessionTitle = ""
                                    newSessionNotes = ""
                                    showAddSessionDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                        ) {
                            Text("Schedule Session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteClientDialog) {
        Dialog(onDismissRequest = { showDeleteClientDialog = false }) {
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
                        text = "Are you sure you want to remove ${client.fullName}? This will permanently delete their account and history.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDeleteClientDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.deleteClient(client.id) {
                                    showDeleteClientDialog = false
                                    onBackToRoster()
                                }
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

    val heightInMeters = client.heightCm / 100f
    val bmi = if (heightInMeters > 0) client.currentWeightKg / (heightInMeters * heightInMeters) else 0f
    val bmiFormatted = String.format(Locale.getDefault(), "%.1f", bmi)
    val completedSessionDays = workoutLogs.map {
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.dateLogged))
    }.distinct().size

    val startDateStr = workoutLogs.minOfOrNull { it.dateLogged }?.let {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
    } ?: "N/A"
    val endDateStr = workoutLogs.maxOfOrNull { it.dateLogged }?.let {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
    } ?: "N/A"
    val sessionDateRange = if (workoutLogs.isNotEmpty()) "$startDateStr → $endDateStr" else "No sessions yet"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Client Hub: ${client.fullName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToRoster, modifier = Modifier.testTag("back_to_roster_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteClientDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove Client",
                            tint = Color(0xFFFF5252)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { viewModel.setMacroAssignerOpen(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("open_macro_assigner_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign Macros", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
                .verticalScroll(rememberScrollState())
        ) {
            // Client Summary Banner with Weight, Height, Age, BMI, and Feedback
            CoachFitCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                backgroundColor = DarkSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                                    .border(1.5.dp, NeonGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = client.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = client.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onOpenChat,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("chat_with_client_button")
                        ) {
                            Icon(Icons.Default.ChatBubble, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chat", color = NeonGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Session Days Done Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Completed Session Days:", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Text("$completedSessionDays Days (${workoutLogs.size} sets)", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Session Date Range (Start to End) Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = AthleticCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Active Session Dates:", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Text(sessionDateRange, fontSize = 11.sp, color = AthleticCyan, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Client's Physical Stats Grid: Weight, Height, Age, BMI
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Client Weight", fontSize = 10.sp, color = TextSecondary)
                            Text("${client.currentWeightKg} kg", fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                        }
                        Box(modifier = Modifier.width(1.dp).height(26.dp).background(Color(0xFF383838)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Height", fontSize = 10.sp, color = TextSecondary)
                            Text("${client.heightCm.toInt()} cm", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        }
                        Box(modifier = Modifier.width(1.dp).height(26.dp).background(Color(0xFF383838)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Age / Sex", fontSize = 10.sp, color = TextSecondary)
                            Text("${client.age}y • ${client.sex.take(1)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        }
                        Box(modifier = Modifier.width(1.dp).height(26.dp).background(Color(0xFF383838)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BMI", fontSize = 10.sp, color = TextSecondary)
                            Text(bmiFormatted, fontSize = 13.sp, fontWeight = FontWeight.Black, color = AthleticCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Client's Health & Medical Info Banner
                    if (client.healthInfo.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFF5252).copy(alpha = 0.12f))
                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Client Medical & Health Info:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5252)
                                    )
                                    Text(
                                        text = client.healthInfo,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Client's Feedback Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Feedback, contentDescription = null, tint = Color(0xFFFF9100), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Client's Latest Feedback & Notes:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9100)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (client.feedback.isNotBlank()) "\"${client.feedback}\"" else "No feedback reported by client yet.",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Macro Targets row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF161616))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TargetPill("Target Cals", "${client.targetCalories} kcal", NeonGreen)
                        TargetPill("Protein", "${client.targetProtein}g", NeonGreen)
                        TargetPill("Carbs", "${client.targetCarbs}g", AthleticOrange)
                        TargetPill("Fats", "${client.targetFats}g", AthleticCyan)
                    }
                }
            }

            // Segmented Lists Tab Row
            TabRow(
                selectedTabIndex = selectedListIndex,
                containerColor = DarkSurface,
                contentColor = NeonGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedListIndex]),
                        color = NeonGreen
                    )
                }
            ) {
                Tab(
                    selected = selectedListIndex == 0,
                    onClick = { selectedListIndex = 0 },
                    text = {
                        Text(
                            "Diet Logs (${foodLogs.size})",
                            fontWeight = if (selectedListIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedListIndex == 1,
                    onClick = { selectedListIndex = 1 },
                    text = {
                        Text(
                            "Workouts (${workoutLogs.size})",
                            fontWeight = if (selectedListIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedListIndex == 2,
                    onClick = { selectedListIndex = 2 },
                    text = {
                        Text(
                            "Feedbacks & Reports (${checkins.size})",
                            fontWeight = if (selectedListIndex == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedListIndex == 3,
                    onClick = { selectedListIndex = 3 },
                    text = {
                        Text(
                            "Sessions (${sessions.size})",
                            fontWeight = if (selectedListIndex == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-actions and list view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when (selectedListIndex) {
                    0 -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Diet & Nutrition Prescribed", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = { showTrainerAddFoodDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp).testTag("trainer_add_new_food_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("+ Food Item", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { showTrainerPrescribeFoodDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp).testTag("prescribe_meal_button")
                                    ) {
                                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Prescribe Meal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FoodLogsList(
                                logs = foodLogs,
                                onDeleteLog = { logId -> viewModel.deleteFoodLog(logId) }
                            )
                        }
                    }
                    1 -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Assigned Workout Sets", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Button(
                                    onClick = { showTrainerAddWorkoutDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp).testTag("trainer_add_workout_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Log Workout Set", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            WorkoutLogsList(
                                logs = workoutLogs,
                                onEditLog = { log -> editingWorkoutLog = log },
                                onDeleteLog = { logId -> viewModel.deleteWorkoutLog(logId) }
                            )
                        }
                    }
                    2 -> CheckinsList(checkins)
                    3 -> SessionsManagementSection(
                        client = client,
                        sessions = sessions,
                        onUpdateAttendance = { id, status -> viewModel.updateSessionAttendance(id, status) },
                        onEditSession = { session -> editingSession = session },
                        onDeleteSession = { id -> viewModel.deleteSession(id) },
                        onAddSessionClick = { showAddSessionDialog = true },
                        onEditQuotaClick = { showEditQuotaDialog = true }
                    )
                }
            }
        }
    }

    // Macro Assigner Modal
    if (isMacroModalOpen) {
        MacroAssignerDialog(
            client = client,
            onDismiss = { viewModel.setMacroAssignerOpen(false) },
            onSaveMacros = { calories, protein, carbs, fats ->
                viewModel.updateClientMacros(client.id, calories, protein, carbs, fats) {
                    viewModel.setMacroAssignerOpen(false)
                }
            }
        )
    }

    // Trainer Log Workout Dialog
    if (showTrainerAddWorkoutDialog) {
        TrainerAddWorkoutDialog(
            clientName = client.fullName,
            onDismiss = { showTrainerAddWorkoutDialog = false },
            onSave = { exercise, sets, reps, weight ->
                viewModel.trainerLogWorkoutForClient(client.id, exercise, sets, reps, weight) {
                    showTrainerAddWorkoutDialog = false
                }
            }
        )
    }

    // Trainer Prescribe Meal Dialog
    if (showTrainerPrescribeFoodDialog) {
        TrainerPrescribeMealDialog(
            clientName = client.fullName,
            foodItems = allFoodItems,
            clientTargetCalories = client.targetCalories,
            clientTargetProtein = client.targetProtein,
            clientTargetCarbs = client.targetCarbs,
            clientTargetFats = client.targetFats,
            onDismiss = { showTrainerPrescribeFoodDialog = false },
            onSave = { items ->
                viewModel.trainerPrescribeMultipleFoodLogsForClient(client.id, items) {
                    showTrainerPrescribeFoodDialog = false
                }
            }
        )
    }

    // Trainer Add Food to Catalog Dialog
    if (showTrainerAddFoodDialog) {
        TrainerAddFoodDialog(
            onDismiss = { showTrainerAddFoodDialog = false },
            onSaveFood = { name, calories, protein, carbs, fat, category ->
                viewModel.addCustomFoodItem(name, calories, protein, carbs, fat, category) {
                    showTrainerAddFoodDialog = false
                }
            }
        )
    }
}

@Composable
fun TrainerAddWorkoutDialog(
    clientName: String,
    onDismiss: () -> Unit,
    onSave: (exercise: String, sets: Int, reps: Int, weight: Float) -> Unit
) {
    var exercise by remember { mutableStateOf("Barbell Back Squat") }
    var sets by remember { mutableStateOf("4") }
    var reps by remember { mutableStateOf("8") }
    var weight by remember { mutableStateOf("100") }

    val presets = listOf("Barbell Back Squat", "Romanian Deadlift", "Barbell Bench Press", "Overhead Press", "Pull-Ups")

    Dialog(onDismissRequest = onDismiss) {
        CoachFitCard(modifier = Modifier.fillMaxWidth(), backgroundColor = DarkSurface, cornerRadius = 16.dp) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Log Workout for $clientName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presets) { preset ->
                        val isSelected = exercise == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AthleticOrange else DarkSurfaceVariant)
                                .clickable { exercise = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(preset, fontSize = 11.sp, color = if (isSelected) Color.Black else TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = exercise,
                    onValueChange = { exercise = it },
                    label = { Text("Exercise Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text("Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (exercise.isNotBlank()) {
                                onSave(exercise, sets.toIntOrNull() ?: 1, reps.toIntOrNull() ?: 1, weight.toFloatOrNull() ?: 0f)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Log Workout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class StagedPrescribedMeal(
    val id: String = java.util.UUID.randomUUID().toString(),
    val foodItem: FoodItemEntity,
    val mealType: String,
    val grams: Float
) {
    val factor: Float get() = (grams / 100f).coerceAtLeast(0f)
    val calories: Float get() = foodItem.calPer100g * factor
    val protein: Float get() = foodItem.proteinPer100g * factor
    val carbs: Float get() = foodItem.carbsPer100g * factor
    val fat: Float get() = foodItem.fatPer100g * factor
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerPrescribeMealDialog(
    clientName: String,
    foodItems: List<FoodItemEntity>,
    clientTargetCalories: Int = 0,
    clientTargetProtein: Int = 0,
    clientTargetCarbs: Int = 0,
    clientTargetFats: Int = 0,
    onDismiss: () -> Unit,
    onSave: (items: List<Triple<FoodItemEntity, String, Float>>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedFood by remember { mutableStateOf(foodItems.firstOrNull()) }
    var selectedMealType by remember { mutableStateOf("Lunch") }
    var gramsInput by remember { mutableStateOf("150") }
    var expandedFoodMenu by remember { mutableStateOf(false) }

    val stagedItems = remember { mutableStateListOf<StagedPrescribedMeal>() }

    val categories = remember(foodItems) {
        listOf("All") + foodItems.map { it.category }.distinct().filter { it.isNotBlank() }
    }

    val filteredFoods = remember(foodItems, searchQuery, selectedCategory) {
        foodItems.filter { item ->
            (selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isBlank() || item.foodName.contains(searchQuery, ignoreCase = true))
        }
    }

    val grams = gramsInput.toFloatOrNull() ?: 100f
    val factor = (grams / 100f).coerceAtLeast(0f)

    val currentFood = selectedFood ?: foodItems.firstOrNull()
    val calculatedCal = (currentFood?.calPer100g ?: 0f) * factor
    val calculatedProtein = (currentFood?.proteinPer100g ?: 0f) * factor
    val calculatedCarbs = (currentFood?.carbsPer100g ?: 0f) * factor
    val calculatedFat = (currentFood?.fatPer100g ?: 0f) * factor

    // Macro calorie distribution for the currently selected food
    val proteinCal = calculatedProtein * 4f
    val carbsCal = calculatedCarbs * 4f
    val fatCal = calculatedFat * 9f
    val totalMacroCal = (proteinCal + carbsCal + fatCal).coerceAtLeast(1f)
    val proteinRatio = (proteinCal / totalMacroCal).coerceIn(0f, 1f)
    val carbsRatio = (carbsCal / totalMacroCal).coerceIn(0f, 1f)
    val fatRatio = (fatCal / totalMacroCal).coerceIn(0f, 1f)

    Dialog(onDismissRequest = onDismiss) {
        CoachFitCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f),
            backgroundColor = DarkSurface,
            cornerRadius = 18.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Prescribe Diet Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Client: $clientName",
                            style = MaterialTheme.typography.labelSmall,
                            color = AthleticOrange
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AthleticOrange.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("COACH ASSIGNED", fontSize = 9.sp, fontWeight = FontWeight.Black, color = AthleticOrange)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Body for all food details and staged meals
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // STAGED MEAL ITEMS LIST (Add & Remove meals in this prescription)
                    if (stagedItems.isNotEmpty()) {
                        val sumCal = stagedItems.sumOf { it.calories.toDouble() }.toFloat()
                        val sumP = stagedItems.sumOf { it.protein.toDouble() }.toFloat()
                        val sumC = stagedItems.sumOf { it.carbs.toDouble() }.toFloat()
                        val sumF = stagedItems.sumOf { it.fat.toDouble() }.toFloat()

                        CoachFitCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0xFF162316),
                            borderColor = NeonGreen.copy(alpha = 0.5f),
                            cornerRadius = 12.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Prescribed Meal Items (${stagedItems.size}):",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = NeonGreen
                                        )
                                    }
                                    TextButton(
                                        onClick = { stagedItems.clear() },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("Clear All", fontSize = 10.sp, color = Color(0xFFFF5252))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    stagedItems.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DarkSurface)
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(DarkSurfaceVariant)
                                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(item.mealType, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(item.foodItem.foodName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                                    Text(" • ${item.grams.toInt()}g", fontSize = 11.sp, color = TextSecondary)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    "${item.calories.toInt()} kcal | P: ${String.format(Locale.getDefault(), "%.1f", item.protein)}g | C: ${String.format(Locale.getDefault(), "%.1f", item.carbs)}g | F: ${String.format(Locale.getDefault(), "%.1f", item.fat)}g",
                                                    fontSize = 10.sp,
                                                    color = TextSecondary
                                                )
                                            }

                                            // Option to remove this meal item from prescription
                                            IconButton(
                                                onClick = { stagedItems.remove(item) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove item",
                                                    tint = Color(0xFFFF5252),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Combined Prescribed Total:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Text(
                                        "${sumCal.toInt()} kcal | P: ${sumP.toInt()}g | C: ${sumC.toInt()}g | F: ${sumF.toInt()}g",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen
                                    )
                                }
                            }
                        }
                    }

                    // Category Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            val isSel = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) NeonGreen else DarkSurfaceVariant)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else TextSecondary
                                )
                            }
                        }
                    }

                    // Food Selector Dropdown with Macro Details in Candidates
                    ExposedDropdownMenuBox(
                        expanded = expandedFoodMenu,
                        onExpandedChange = { expandedFoodMenu = it }
                    ) {
                        OutlinedTextField(
                            value = currentFood?.let { "${it.foodName} (${it.category})" } ?: "Select food item",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Food Item from Catalog") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFoodMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                focusedLabelColor = NeonGreen
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFoodMenu,
                            onDismissRequest = { expandedFoodMenu = false },
                            modifier = Modifier
                                .background(DarkSurfaceVariant)
                                .heightIn(max = 280.dp)
                        ) {
                            filteredFoods.forEach { item ->
                                DropdownMenuItem(
                                    text = {
                                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(item.foodName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(AthleticOrange.copy(alpha = 0.2f))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(item.category, fontSize = 9.sp, color = AthleticOrange, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                            Text(
                                                "100g: ${item.calPer100g.toInt()} kcal | P: ${String.format(Locale.getDefault(), "%.1f", item.proteinPer100g)}g | C: ${String.format(Locale.getDefault(), "%.1f", item.carbsPer100g)}g | F: ${String.format(Locale.getDefault(), "%.1f", item.fatPer100g)}g",
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedFood = item
                                        expandedFoodMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // COMPREHENSIVE FOOD DETAILS CARD FOR SELECTED ITEM
                    currentFood?.let { food ->
                        CoachFitCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = DarkSurfaceVariant,
                            cornerRadius = 12.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = AthleticOrange, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(food.foodName, fontWeight = FontWeight.Black, fontSize = 14.sp, color = TextPrimary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(food.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AthleticCyan)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Base Nutrition per 100g
                                Text("Standard Nutritional Profile (Per 100g):", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    NutritionMetricBadge("Calories", "${food.calPer100g.toInt()}", "kcal", Color.White)
                                    NutritionMetricBadge("Protein", String.format(Locale.getDefault(), "%.1f", food.proteinPer100g), "g", NeonGreen)
                                    NutritionMetricBadge("Carbs", String.format(Locale.getDefault(), "%.1f", food.carbsPer100g), "g", AthleticOrange)
                                    NutritionMetricBadge("Fat", String.format(Locale.getDefault(), "%.1f", food.fatPer100g), "g", AthleticCyan)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Calorie Macro Energy Split
                                Text("Macro Energy Split (% of Calories):", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                // Segmented Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                ) {
                                    if (proteinRatio > 0f) {
                                        Box(modifier = Modifier.weight(proteinRatio).fillMaxHeight().background(NeonGreen))
                                    }
                                    if (carbsRatio > 0f) {
                                        Box(modifier = Modifier.weight(carbsRatio).fillMaxHeight().background(AthleticOrange))
                                    }
                                    if (fatRatio > 0f) {
                                        Box(modifier = Modifier.weight(fatRatio).fillMaxHeight().background(AthleticCyan))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Protein ${(proteinRatio * 100).toInt()}%", fontSize = 9.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                                    Text("Carbs ${(carbsRatio * 100).toInt()}%", fontSize = 9.sp, color = AthleticOrange, fontWeight = FontWeight.Bold)
                                    Text("Fat ${(fatRatio * 100).toInt()}%", fontSize = 9.sp, color = AthleticCyan, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Meal Timing
                    Column {
                        Text("Prescribed Meal Timing", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                                val isSelected = selectedMealType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonGreen else DarkSurfaceVariant)
                                        .clickable { selectedMealType = type }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        type,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Serving Size / Grams Selection
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Serving Size (Grams)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("Presets:", fontSize = 10.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Quick presets
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("50", "100", "150", "200", "250", "300").forEach { pGrams ->
                                val isP = gramsInput == pGrams
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isP) AthleticOrange else DarkSurfaceVariant)
                                        .clickable { gramsInput = pGrams }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${pGrams}g",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isP) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = gramsInput,
                            onValueChange = { gramsInput = it },
                            label = { Text("Exact Grams to Prescribe") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // LIVE PRESCRIBED PORTION IMPACT CARD
                    CoachFitCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFF161F16),
                        borderColor = NeonGreen.copy(alpha = 0.5f),
                        cornerRadius = 12.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Assessment, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Portion Output (${grams.toInt()}g):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                                }
                                Text(
                                    "${calculatedCal.toInt()} kcal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PrescribedNutrientPill("Protein", "${String.format(Locale.getDefault(), "%.1f", calculatedProtein)}g", NeonGreen, if (clientTargetProtein > 0) ((calculatedProtein / clientTargetProtein) * 100).toInt() else null)
                                PrescribedNutrientPill("Carbs", "${String.format(Locale.getDefault(), "%.1f", calculatedCarbs)}g", AthleticOrange, if (clientTargetCarbs > 0) ((calculatedCarbs / clientTargetCarbs) * 100).toInt() else null)
                                PrescribedNutrientPill("Fats", "${String.format(Locale.getDefault(), "%.1f", calculatedFat)}g", AthleticCyan, if (clientTargetFats > 0) ((calculatedFat / clientTargetFats) * 100).toInt() else null)
                            }
                        }
                    }

                    // BUTTON TO ADD FOOD TO MEAL PLAN
                    Button(
                        onClick = {
                            currentFood?.let { food ->
                                stagedItems.add(
                                    StagedPrescribedMeal(
                                        foodItem = food,
                                        mealType = selectedMealType,
                                        grams = grams
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add to Prescribed Meal Plan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (stagedItems.isNotEmpty()) {
                                onSave(stagedItems.map { Triple(it.foodItem, it.mealType, it.grams) })
                            } else {
                                currentFood?.let { food ->
                                    onSave(listOf(Triple(food, selectedMealType, grams)))
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        if (stagedItems.isNotEmpty()) {
                            val totalCal = stagedItems.sumOf { it.calories.toDouble() }.toInt()
                            Text("Prescribe (${stagedItems.size} items • $totalCal kcal)", fontWeight = FontWeight.Bold)
                        } else {
                            Text("Prescribe to $clientName", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionMetricBadge(label: String, value: String, unit: String, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 9.sp, color = TextSecondary)
        Text("$value $unit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun PrescribedNutrientPill(label: String, value: String, color: Color, targetPercent: Int? = null) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 9.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
        if (targetPercent != null) {
            Text("$targetPercent% target", fontSize = 8.sp, color = color.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TargetPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun FoodLogsList(
    logs: List<com.example.data.local.entity.DailyFoodLogEntity>,
    onDeleteLog: ((Long) -> Unit)? = null
) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No diet logs for this client. Tap 'Prescribe Meal' above.", color = TextSecondary, fontSize = 12.sp)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            logs.forEach { log ->
                CoachFitCard(modifier = Modifier.fillMaxWidth(), backgroundColor = DarkSurface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(log.mealType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(log.dateLogged)),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.foodName, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${log.gramsConsumed.toInt()}g portion", fontSize = 12.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${log.totalCalories.toInt()} kcal", fontWeight = FontWeight.Black, color = NeonGreen)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("P: ${log.totalProtein.toInt()}g", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                                    Text("•", fontSize = 11.sp, color = TextSecondary)
                                    Text("C: ${log.totalCarbs.toInt()}g", fontSize = 11.sp, color = AthleticOrange, fontWeight = FontWeight.SemiBold)
                                    Text("•", fontSize = 11.sp, color = TextSecondary)
                                    Text("F: ${log.totalFats.toInt()}g", fontSize = 11.sp, color = AthleticCyan, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (onDeleteLog != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onDeleteLog(log.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remove prescribed meal",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(18.dp)
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

@Composable
fun WorkoutLogsList(
    logs: List<com.example.data.local.entity.WorkoutLogEntity>,
    onEditLog: (com.example.data.local.entity.WorkoutLogEntity) -> Unit = {},
    onDeleteLog: (Long) -> Unit = {}
) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No workout logs for this client. Tap 'Log Workout Set' above.", color = TextSecondary, fontSize = 12.sp)
        }
    } else {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val groupedLogs = logs.groupBy { dateFormat.format(Date(it.dateLogged)) }
            .toSortedMap(reverseOrder())

        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            groupedLogs.forEach { (dateKey, dayLogs) ->
                val dayTitle = dayLogs.firstOrNull()?.let { displayDateFormat.format(Date(it.dateLogged)) } ?: dateKey

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Day Header Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dayTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${dayLogs.size} sets",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    // Exercises for this day
                    dayLogs.forEach { log ->
                        CoachFitCard(modifier = Modifier.fillMaxWidth(), backgroundColor = DarkSurface) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.exerciseName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.dateLogged)),
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${log.sets} sets × ${log.reps} reps @ ${log.weightLifted} kg", fontSize = 12.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onEditLog(log) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Workout Set",
                                            tint = AthleticCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteLog(log.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Workout Set",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(18.dp)
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

@Composable
fun CheckinsList(checkins: List<com.example.data.local.entity.WeeklyCheckinEntity>) {
    if (checkins.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No reports submitted by this client yet.", color = TextSecondary)
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            checkins.forEach { checkin ->
                CoachFitCard(modifier = Modifier.fillMaxWidth(), backgroundColor = DarkSurface) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(checkin.date)),
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text("${checkin.currentWeight} kg", fontWeight = FontWeight.Black, color = NeonGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Sleep: ${checkin.sleepQuality}/5 ★", fontSize = 12.sp, color = AthleticCyan)
                            Text("Energy: ${checkin.energyLevel}/5 ★", fontSize = 12.sp, color = NeonGreen)
                        }
                        if (!checkin.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Client Feedback: \"${checkin.notes}\"",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiniPhotoBadge("Front", checkin.frontPhotoUri, Modifier.weight(1f))
                            MiniPhotoBadge("Side", checkin.sidePhotoUri, Modifier.weight(1f))
                            MiniPhotoBadge("Back", checkin.backPhotoUri, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SessionsManagementSection(
    client: com.example.data.local.entity.UserEntity,
    sessions: List<com.example.data.local.entity.ClientSessionEntity>,
    onUpdateAttendance: (Long, String) -> Unit,
    onEditSession: (com.example.data.local.entity.ClientSessionEntity) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onAddSessionClick: () -> Unit,
    onEditQuotaClick: () -> Unit
) {
    val totalQuota = client.totalPurchasedSessions
    val completedOrPresent = sessions.count { it.status.equals("Present", true) || it.status.equals("Completed", true) }
    val absentCount = sessions.count { it.status.equals("Absent", true) }
    val attendingNowCount = sessions.count { it.status.contains("Reached", true) || it.status.contains("Attending", true) }
    val remainingQuota = (totalQuota - completedOrPresent).coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Session Package & Quota Overview Card
        CoachFitCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface,
            cornerRadius = 14.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Session Package & Attendance", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text("$completedOrPresent of $totalQuota Sessions Completed ($remainingQuota Remaining)", fontSize = 11.sp, color = NeonGreen)
                    }
                    OutlinedButton(
                        onClick = onEditQuotaClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = AthleticCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Edit Quota", fontSize = 10.sp, color = AthleticCyan, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF161616))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TargetPill("Total Quota", "$totalQuota", NeonGreen)
                    TargetPill("Attended", "$completedOrPresent", AthleticCyan)
                    TargetPill("Remaining", "$remainingQuota", AthleticOrange)
                    TargetPill("Absent", "$absentCount", Color(0xFFFF5252))
                }

                if (attendingNowCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFF9100).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFFF9100), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color(0xFFFF9100), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "$attendingNowCount client session currently active / checked-in!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9100)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Schedule Session Button Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Day Sessions Schedule (${sessions.size})", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
            Button(
                onClick = onAddSessionClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp).testTag("trainer_add_session_schedule_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("+ Schedule Session", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No training sessions scheduled for this client yet. Tap '+ Schedule Session' above.", color = TextSecondary, fontSize = 12.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                sessions.forEach { session ->
                    val isClientReached = session.status.contains("Reached", true) || session.status.contains("Attending", true)

                    CoachFitCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isClientReached) Modifier.border(1.5.dp, Color(0xFFFF9100), RoundedCornerShape(14.dp)) else Modifier),
                        backgroundColor = DarkSurface
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Client Reached / Check-In Banner if active
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
                                                    "CLIENT HAS REACHED & ATTENDING",
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
                                        Text(session.sessionTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(session.sessionTimestamp)),
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    if (session.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text("Focus: ${session.notes}", fontSize = 11.sp, color = AthleticCyan, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onEditSession(session) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Session",
                                            tint = AthleticCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { onDeleteSession(session.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Session",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Trainer Attendance Status Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Trainer Attendance:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val statuses = listOf("Scheduled", "Reached / Attending", "Present", "Absent", "Completed")
                                    statuses.forEach { status ->
                                        val isSelected = session.status.equals(status, ignoreCase = true)
                                        val chipColor = when {
                                            status.contains("Present") || status.contains("Completed") -> NeonGreen
                                            status.contains("Absent") -> Color(0xFFFF5252)
                                            status.contains("Reached") || status.contains("Attending") -> Color(0xFFFF9100)
                                            else -> AthleticCyan
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) chipColor else DarkSurfaceVariant)
                                                .clickable { onUpdateAttendance(session.id, status) }
                                                .padding(horizontal = 7.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = when (status) {
                                                    "Reached / Attending" -> "Attending"
                                                    else -> status
                                                },
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
}
