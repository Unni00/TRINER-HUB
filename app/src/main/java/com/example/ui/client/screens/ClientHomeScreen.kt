package com.example.ui.client.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Cloud
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ClientTab
import com.example.ui.CoachFitViewModel
import com.example.ui.components.CoachFitCard
import com.example.ui.components.MacroProgressBar
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DividerDark
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClientHomeScreen(
    viewModel: CoachFitViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val foodLogs by viewModel.clientFoodLogs.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.clientWorkoutLogs.collectAsStateWithLifecycle()
    val sessions by viewModel.clientSessions.collectAsStateWithLifecycle()
    val trainers by viewModel.allTrainers.collectAsStateWithLifecycle()
    val coachName = trainers.firstOrNull()?.fullName ?: "Coach Marcus Vance"
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.runtime.LaunchedEffect(sessions) {
        val now = System.currentTimeMillis()
        sessions.firstOrNull { it.sessionTimestamp > now && it.sessionTimestamp - now < 24L * 60L * 60L * 1000L && it.status == "Scheduled" }?.let { upcoming ->
            val timeStr = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(upcoming.sessionTimestamp))
            com.example.ui.components.NotificationHelper.showSessionReminder(context, upcoming.sessionTitle, timeStr)
        }
    }

    val user = currentUser ?: return

    // Calculate Today's Totals from Daily Food Logs
    val todayStartMillis = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    val todayLogs = foodLogs.filter { it.dateLogged >= todayStartMillis }

    val totalCaloriesConsumed = todayLogs.sumOf { it.totalCalories.toDouble() }.toFloat()
    val totalProteinConsumed = todayLogs.sumOf { it.totalProtein.toDouble() }.toFloat()
    val totalCarbsConsumed = todayLogs.sumOf { it.totalCarbs.toDouble() }.toFloat()
    val totalFatsConsumed = todayLogs.sumOf { it.totalFats.toDouble() }.toFloat()

    val calorieProgress = if (user.targetCalories > 0) {
        (totalCaloriesConsumed / user.targetCalories).coerceIn(0f, 1f)
    } else 0f
    val animatedCalorieProgress by animateFloatAsState(targetValue = calorieProgress, label = "calProgress")
    val caloriesRemaining = (user.targetCalories - totalCaloriesConsumed).coerceAtLeast(0f)

    var showWaterEditDialog by remember { mutableStateOf(false) }
    var showStatsEditDialog by remember { mutableStateOf(false) }
    var showEditClientProfileDialog by remember { mutableStateOf(false) }
    var showPalettePreviewDialog by remember { mutableStateOf(false) }
    var editClientName by remember { mutableStateOf(user.fullName) }
    var editClientPhotoUri by remember { mutableStateOf<String?>(user.profilePictureUri) }

    val clientPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { editClientPhotoUri = it.toString() }
    }

    // BMI Calculation
    val heightInMeters = user.heightCm / 100f
    val bmi = if (heightInMeters > 0) user.currentWeightKg / (heightInMeters * heightInMeters) else 0f
    val bmiFormatted = String.format(Locale.getDefault(), "%.1f", bmi)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Greeting Banner with Client Avatar & Edit Profile Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()),
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Welcome, ${user.fullName.split(" ").firstOrNull() ?: "Athlete"}! 💪",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Theme Palette Preview Icon Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .border(1.dp, NeonGreen.copy(alpha = 0.5f), CircleShape)
                        .clickable { showPalettePreviewDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme Color Palette Preview",
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .border(2.dp, NeonGreen, CircleShape)
                        .clickable {
                            editClientName = user.fullName
                            editClientPhotoUri = user.profilePictureUri
                            showEditClientProfileDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.profilePictureUri.isNullOrBlank()) {
                        AsyncImage(
                            model = user.profilePictureUri,
                            contentDescription = "Client Profile",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Edit Profile",
                            tint = NeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (showPalettePreviewDialog) {
            Dialog(onDismissRequest = { showPalettePreviewDialog = false }) {
                CoachFitCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkSurface,
                    cornerRadius = 18.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Black, Light Charcoal & White",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, CharcoalBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("MONOCHROME", fontSize = 9.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        val swatches = listOf(
                            Triple("Pure White (Primary CTA)", "#FFFFFF", NeonGreen),
                            Triple("Light Charcoal Surface", "#22232A", DarkSurface),
                            Triple("Elevated Container / Pill", "#2C2E38", DarkSurfaceVariant),
                            Triple("Crisp Charcoal Border", "#3E4150", DividerDark),
                            Triple("Pure Black Canvas", "#000000", DarkBackground),
                            Triple("White Type (Primary)", "#FFFFFF", TextPrimary),
                            Triple("Light Silver Type (Secondary)", "#CBD5E1", TextSecondary)
                        )

                        swatches.forEach { (name, hex, col) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(name, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                }
                                Text(hex, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showPalettePreviewDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close Preview", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showEditClientProfileDialog) {
            Dialog(onDismissRequest = { showEditClientProfileDialog = false }) {
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
                            text = "Edit My Profile",
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
                                    clientPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!editClientPhotoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = editClientPhotoUri,
                                    contentDescription = "Client Photo",
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
                                    clientPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Select from Gallery", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
                            }
                            if (!editClientPhotoUri.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                TextButton(
                                    onClick = { editClientPhotoUri = null }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Remove", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editClientName,
                            onValueChange = { editClientName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showEditClientProfileDialog = false }) {
                                Text("Cancel", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateClientProfile(
                                        name = editClientName,
                                        profilePictureUri = editClientPhotoUri?.ifBlank { null }
                                    )
                                    showEditClientProfileDialog = false
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

        // Assigned Coach Card (Displaying Photo, Name, and Profession)
        val coach = trainers.firstOrNull()
        val coachName = coach?.fullName ?: "Coach Marcus Vance"
        val coachProfession = coach?.profession ?: "Elite Strength & Conditioning Coach & Sports Nutritionist"

        CoachFitCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface,
            cornerRadius = 14.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFFF9100),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9100))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Assigned Coach",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9100)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = coachName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = coachProfession,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Button(
                    onClick = { viewModel.setClientTab(ClientTab.CHAT) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = Color(0xFFFF9100)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // My Training Sessions & Attendance Card (Package Quota + Client Check-in)
        val totalQuota = user.totalPurchasedSessions
        val attendedCount = sessions.count { it.status.equals("Present", true) || it.status.equals("Completed", true) }
        val remainingCount = (totalQuota - attendedCount).coerceAtLeast(0)
        val quotaProgress = if (totalQuota > 0) (attendedCount.toFloat() / totalQuota).coerceIn(0f, 1f) else 0f

        CoachFitCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface,
            cornerRadius = 16.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("My Training Sessions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("$attendedCount of $totalQuota Sessions Completed", fontSize = 11.sp, color = NeonGreen)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AthleticOrange.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "$remainingCount Left",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AthleticOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quota Progress Bar
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { quotaProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonGreen,
                    trackColor = DarkSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (sessions.isEmpty()) {
                    Text("No training sessions scheduled by your coach yet.", fontSize = 12.sp, color = TextSecondary)
                } else {
                    // Check for Active / Next Upcoming Session to highlight check-in
                    val activeOrNextSession = sessions.firstOrNull { it.status.equals("Scheduled", true) || it.status.contains("Reached", true) || it.status.contains("Attending", true) }
                    
                    if (activeOrNextSession != null) {
                        val isAttending = activeOrNextSession.status.contains("Reached", true) || activeOrNextSession.status.contains("Attending", true)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAttending) Color(0xFFFF9100).copy(alpha = 0.15f) else DarkSurfaceHigh)
                                .border(1.dp, if (isAttending) Color(0xFFFF9100) else NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (isAttending) "📍 CURRENT SESSION IN PROGRESS" else "⚡ UPCOMING SESSION CHECK-IN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isAttending) Color(0xFFFF9100) else NeonGreen
                                    )
                                    Text(
                                        "Session #${activeOrNextSession.sessionNumber}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(activeOrNextSession.sessionTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(
                                    SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(activeOrNextSession.sessionTimestamp)),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )

                                if (activeOrNextSession.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("Coach Notes: ${activeOrNextSession.notes}", fontSize = 11.sp, color = AthleticCyan, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (isAttending) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFF9100).copy(alpha = 0.25f))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🏃", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "You are currently attending this session!",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF9100)
                                            )
                                            activeOrNextSession.clientCheckInTime?.let {
                                                Text(
                                                    "Checked in at ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it))}. Coach is notified.",
                                                    fontSize = 10.sp,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Client Check In Button
                                    Button(
                                        onClick = {
                                            viewModel.clientCheckInSession(activeOrNextSession.id)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NeonGreen,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .testTag("client_checkin_button")
                                    ) {
                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("📍 I've Reached Gym / Attending Session", fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text("All Assigned Day Sessions (${sessions.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sessions.forEach { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Session #${session.sessionNumber} • ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                                        Text(session.sessionTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                    }
                                    Text(
                                        SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(session.sessionTimestamp)),
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    if (session.notes.isNotBlank()) {
                                        Text("Focus: ${session.notes}", fontSize = 10.sp, color = AthleticCyan)
                                    }
                                }
                                val statusColor = when {
                                    session.status.contains("Present", true) || session.status.contains("Completed", true) -> NeonGreen
                                    session.status.contains("Absent", true) -> Color(0xFFFF5252)
                                    session.status.contains("Reached", true) || session.status.contains("Attending", true) -> Color(0xFFFF9100)
                                    else -> AthleticCyan
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.2f))
                                        .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when {
                                            session.status.contains("Reached", true) || session.status.contains("Attending", true) -> "Attending 🏃"
                                            session.status.contains("Present", true) -> "Present ✅"
                                            session.status.contains("Completed", true) -> "Completed ✅"
                                            session.status.contains("Absent", true) -> "Absent ❌"
                                            else -> "Scheduled ⏳"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. CLIENT PHYSICAL STATS & FEEDBACK CARD (Requested: client can add weight, height, age, feedbacks)
        CoachFitCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface,
            cornerRadius = 16.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "My Physical Stats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Button(
                        onClick = { viewModel.setClientTab(ClientTab.BIOMETRICS) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkSurfaceVariant,
                            contentColor = NeonGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("open_biometrics_screen_button")
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Biometrics (Firebase)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Grid: Weight, Height, Age, BMI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(vertical = 12.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Weight", fontSize = 11.sp, color = TextSecondary)
                        Text("${user.currentWeightKg} kg", fontSize = 15.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF383838)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Height", fontSize = 11.sp, color = TextSecondary)
                        Text("${user.heightCm.toInt()} cm", fontSize = 15.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF383838)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Age / Sex", fontSize = 11.sp, color = TextSecondary)
                        Text("${user.age}y • ${user.sex.take(1)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF383838)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BMI", fontSize = 11.sp, color = TextSecondary)
                        Text(bmiFormatted, fontSize = 14.sp, fontWeight = FontWeight.Black, color = AthleticCyan)
                    }
                }

                if (user.healthInfo.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFF5252).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Health & Medical Info (Shared with Coach):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8A80)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user.healthInfo,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Client Feedback Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Feedback,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "My Current Feedback for Coach:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (user.feedback.isNotBlank()) "\"${user.feedback}\"" else "No feedback submitted yet. Tap 'Update Stats' to submit feedback.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (user.feedback.isNotBlank()) TextPrimary else TextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. COACH-ASSIGNED NUTRITION & DIET MACROS CARD
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
                    Text(
                        text = "Diet & Macro Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${caloriesRemaining.toInt()} kcal left",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (caloriesRemaining > 0) NeonGreen else AthleticOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Restored Circular Calorie Graph & Protein, Carbs, Fats Macro Graphs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Calorie Circular Ring Graph
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .testTag("calorie_circular_graph"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(92.dp)) {
                            val strokeWidth = 8.dp.toPx()
                            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            // Background ring track
                            drawArc(
                                color = DarkSurfaceVariant,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = stroke
                            )
                            // Animated Calorie Progress Ring
                            if (animatedCalorieProgress > 0f) {
                                drawArc(
                                    color = if (caloriesRemaining > 0) NeonGreen else AthleticOrange,
                                    startAngle = -90f,
                                    sweepAngle = (animatedCalorieProgress * 360f).coerceIn(0f, 360f),
                                    useCenter = false,
                                    style = stroke
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${totalCaloriesConsumed.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "/ ${user.targetCalories}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "kcal",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (caloriesRemaining > 0) NeonGreen else AthleticOrange
                            )
                        }
                    }

                    // Macro Progress Graphs for Protein, Carbs, Fats
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroProgressBar(
                            label = "Protein",
                            current = totalProteinConsumed,
                            target = user.targetProtein.toFloat(),
                            unit = "g",
                            accentColor = NeonGreen
                        )
                        MacroProgressBar(
                            label = "Carbs",
                            current = totalCarbsConsumed,
                            target = user.targetCarbs.toFloat(),
                            unit = "g",
                            accentColor = AthleticOrange
                        )
                        MacroProgressBar(
                            label = "Fats",
                            current = totalFatsConsumed,
                            target = user.targetFats.toFloat(),
                            unit = "g",
                            accentColor = AthleticCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. WATER COUNTER CARD
        CoachFitCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = "Water",
                            tint = AthleticCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Hydration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Edit Direct",
                        style = MaterialTheme.typography.labelMedium,
                        color = AthleticCyan,
                        modifier = Modifier
                            .clickable { showWaterEditDialog = true }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${user.waterIntakeMl}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = AthleticCyan
                            )
                            Text(
                                text = " / 3000 ml",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }
                        val glasses = (user.waterIntakeMl / 250)
                        Text(
                            text = "≈ $glasses glasses (250ml each)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Quick Increment/Decrement Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .clickable { viewModel.updateWaterIntake(-250) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease water", tint = TextPrimary)
                        }

                        Button(
                            onClick = { viewModel.updateWaterIntake(250) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AthleticCyan.copy(alpha = 0.2f),
                                contentColor = AthleticCyan
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+250ml", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.updateWaterIntake(500) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AthleticCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("+500ml", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. WORKOUT SUMMARY PRESCRIBED BY COACH
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Workout Logs (${workoutLogs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Managed by Coach",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFF9100)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (workoutLogs.isEmpty()) {
            CoachFitCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurfaceVariant
            ) {
                Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Your Coach has not logged any workout sets yet.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                workoutLogs.take(3).forEach { log ->
                    CoachFitCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AthleticOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = AthleticOrange, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(log.exerciseName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("${log.sets} sets × ${log.reps} reps", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                            Text("@ ${log.weightLifted} kg", fontWeight = FontWeight.Black, color = NeonGreen)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Bottom Action Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.setClientTab(ClientTab.CHECKIN) },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send Feedback", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.setClientTab(ClientTab.CHAT) },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurfaceHigh,
                    contentColor = TextPrimary
                )
            ) {
                Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chat Coach", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal Dialog to Update Weight, Height, Age, Feedback
    if (showStatsEditDialog) {
        ClientStatsEditDialog(
            currentWeight = user.currentWeightKg,
            currentHeight = user.heightCm,
            currentAge = user.age,
            currentFeedback = user.feedback,
            onDismiss = { showStatsEditDialog = false },
            onSave = { w, h, a, f ->
                viewModel.updateClientPhysicalStatsAndFeedback(w, h, a, f) {
                    showStatsEditDialog = false
                }
            }
        )
    }

    // Direct Water Input Dialog
    if (showWaterEditDialog) {
        var directWaterValue by remember { mutableStateOf(user.waterIntakeMl.toString()) }
        Dialog(onDismissRequest = { showWaterEditDialog = false }) {
            CoachFitCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface,
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Set Water Intake (ml)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = directWaterValue,
                        onValueChange = { directWaterValue = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AthleticCyan,
                            focusedLabelColor = AthleticCyan
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showWaterEditDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = directWaterValue.toIntOrNull() ?: user.waterIntakeMl
                                viewModel.setExactWaterIntake(amount)
                                showWaterEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AthleticCyan,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientStatsEditDialog(
    currentWeight: Float,
    currentHeight: Float,
    currentAge: Int,
    currentFeedback: String,
    onDismiss: () -> Unit,
    onSave: (weight: Float, height: Float, age: Int, feedback: String) -> Unit
) {
    var weightInput by remember { mutableStateOf(currentWeight.toString()) }
    var heightInput by remember { mutableStateOf(currentHeight.toInt().toString()) }
    var ageInput by remember { mutableStateOf(currentAge.toString()) }
    var feedbackInput by remember { mutableStateOf(currentFeedback) }

    Dialog(onDismissRequest = onDismiss) {
        CoachFitCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface,
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Update My Stats & Feedback",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Your coach will see your latest measurements & notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dialog_weight_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dialog_height_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f).testTag("dialog_age_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = feedbackInput,
                    onValueChange = { feedbackInput = it },
                    label = { Text("Feedback for Coach") },
                    placeholder = { Text("How are you feeling? Soreness, digestion, energy...", color = TextSecondary) },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_feedback_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val w = weightInput.toFloatOrNull() ?: currentWeight
                            val h = heightInput.toFloatOrNull() ?: currentHeight
                            val a = ageInput.toIntOrNull() ?: currentAge
                            onSave(w, h, a, feedbackInput)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dialog_save_stats_button")
                    ) {
                        Text("Save & Send to Coach", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
