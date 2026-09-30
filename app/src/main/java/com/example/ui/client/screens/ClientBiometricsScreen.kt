package com.example.ui.client.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientBiometricsScreen(
    viewModel: CoachFitViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val user = currentUser

    var ageInput by remember(user) { mutableStateOf((user?.age ?: 26).toString()) }
    var sexInput by remember(user) { mutableStateOf(user?.sex ?: "Male") }
    var heightInput by remember(user) { mutableStateOf((user?.heightCm?.toInt() ?: 175).toString()) }
    var weightInput by remember(user) { mutableStateOf((user?.currentWeightKg ?: 78.0f).toString()) }
    var healthInfoInput by remember(user) { mutableStateOf(user?.healthInfo ?: "No known medical issues") }
    var feedbackInput by remember(user) { mutableStateOf(user?.feedback ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var syncResultStatus by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    // Live Math conversions:
    val heightCm = heightInput.toFloatOrNull() ?: 175f
    val weightKg = weightInput.toFloatOrNull() ?: 78f
    val age = ageInput.toIntOrNull() ?: 26

    // Height in feet & inches
    val totalInches = heightCm / 2.54f
    val feet = (totalInches / 12).toInt()
    val inches = (totalInches % 12).toInt()

    // Weight in lbs
    val weightLbs = weightKg * 2.20462f
    val weightLbsFormatted = String.format(Locale.getDefault(), "%.1f", weightLbs)

    // BMI calculation
    val heightInMeters = heightCm / 100f
    val bmi = if (heightInMeters > 0) weightKg / (heightInMeters * heightInMeters) else 0f
    val bmiFormatted = String.format(Locale.getDefault(), "%.1f", bmi)

    val bmiCategory = when {
        bmi < 18.5f -> Pair("Underweight", AthleticCyan)
        bmi < 25.0f -> Pair("Normal Weight (Healthy)", NeonGreen)
        bmi < 30.0f -> Pair("Overweight / Muscular", AthleticOrange)
        else -> Pair("High Body Mass", Color(0xFFFF5252))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Personal Biometrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_from_biometrics_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
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
                .imePadding()
                .padding(16.dp)
        ) {
            // Header Info Card
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Firebase Profile Sync",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Target: users/${user?.id ?: "current"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonGreen
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "CLOUD SYNC",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Calculated Health Gauge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Current Calculated BMI", fontSize = 11.sp, color = TextSecondary)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = bmiFormatted,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = bmiCategory.second
                                )
                                Text(
                                    text = " kg/m²",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bmiCategory.second.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = bmiCategory.first,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = bmiCategory.second
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Biometric Measurements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Enter your current age, height, and weight for accurate calorie & coaching plans",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. AGE INPUT CARD
            BiometricInputCard(
                title = "Age (Years)",
                subtitle = "Used for Basal Metabolic Rate (BMR)",
                value = ageInput,
                onValueChange = { ageInput = it },
                unit = "years",
                secondaryNote = "Category: ${if (age < 30) "Young Adult" else if (age < 50) "Adult" else "Master"}",
                icon = Icons.Default.Person,
                accentColor = NeonGreen,
                onDecrement = {
                    val current = ageInput.toIntOrNull() ?: 25
                    if (current > 10) ageInput = (current - 1).toString()
                },
                onIncrement = {
                    val current = ageInput.toIntOrNull() ?: 25
                    if (current < 120) ageInput = (current + 1).toString()
                },
                testTag = "input_biometrics_age"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. HEIGHT INPUT CARD
            BiometricInputCard(
                title = "Height (Centimeters)",
                subtitle = "Imperial conversion: $feet ft $inches in",
                value = heightInput,
                onValueChange = { heightInput = it },
                unit = "cm",
                secondaryNote = "$feet' $inches\"",
                icon = Icons.Default.Straighten,
                accentColor = AthleticCyan,
                onDecrement = {
                    val current = heightInput.toFloatOrNull() ?: 170f
                    if (current > 50f) heightInput = (current - 1f).toInt().toString()
                },
                onIncrement = {
                    val current = heightInput.toFloatOrNull() ?: 170f
                    if (current < 250f) heightInput = (current + 1f).toInt().toString()
                },
                testTag = "input_biometrics_height"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. WEIGHT INPUT CARD
            BiometricInputCard(
                title = "Current Weight (Kilograms)",
                subtitle = "Imperial conversion: $weightLbsFormatted lbs",
                value = weightInput,
                onValueChange = { weightInput = it },
                unit = "kg",
                secondaryNote = "$weightLbsFormatted lbs",
                icon = Icons.Default.MonitorWeight,
                accentColor = AthleticOrange,
                onDecrement = {
                    val current = weightInput.toFloatOrNull() ?: 70f
                    if (current > 20f) weightInput = String.format(Locale.getDefault(), "%.1f", current - 0.5f)
                },
                onIncrement = {
                    val current = weightInput.toFloatOrNull() ?: 70f
                    if (current < 300f) weightInput = String.format(Locale.getDefault(), "%.1f", current + 0.5f)
                },
                testTag = "input_biometrics_weight"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Biological Sex Selector Card
            CoachFitCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface,
                cornerRadius = 14.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Biological Sex",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Used for accurate metabolic and hormonal basal rate calibrations",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Male", "Female", "Other").forEach { sexOption ->
                            val isSelected = sexInput.equals(sexOption, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) NeonGreen else DarkSurfaceVariant)
                                    .clickable { sexInput = sexOption },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sexOption,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Health Related Info Card
            CoachFitCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface,
                cornerRadius = 14.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Health Related Info",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Injuries, medical conditions, medications, or dietary restrictions",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = healthInfoInput,
                        onValueChange = { healthInfoInput = it },
                        placeholder = { Text("e.g. Cleared for heavy training. Mild seasonal asthma...", color = TextSecondary) },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_biometrics_health_info"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. NOTES / FEEDBACK INPUT
            CoachFitCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurface,
                cornerRadius = 14.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Feedback & Physical Notes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Notes synced to your Firebase user profile for your coach",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedbackInput,
                        onValueChange = { feedbackInput = it },
                        placeholder = { Text("How do you feel? Energy, recovery, training notes...", color = TextSecondary) },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_biometrics_feedback"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sync Result Status Banner
            syncResultStatus?.let { (success, message) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (success) NeonGreen.copy(alpha = 0.15f) else Color(0xFFFF5252).copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (success) NeonGreen else Color(0xFFFF5252),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (success) Icons.Default.CloudDone else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (success) NeonGreen else Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = message,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (success) NeonGreen else Color(0xFFFF5252)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Save and Sync to Firebase Action Button
            Button(
                onClick = {
                    val finalAge = ageInput.toIntOrNull() ?: (user?.age ?: 26)
                    val finalHeight = heightInput.toFloatOrNull() ?: (user?.heightCm ?: 175f)
                    val finalWeight = weightInput.toFloatOrNull() ?: (user?.currentWeightKg ?: 78f)

                    isSaving = true
                    viewModel.saveClientBiometricsToFirebase(
                        age = finalAge,
                        height = finalHeight,
                        weight = finalWeight,
                        sex = sexInput,
                        healthInfo = healthInfoInput,
                        feedback = feedbackInput
                    ) { success, message ->
                        isSaving = false
                        syncResultStatus = Pair(success, message)
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_biometrics_firebase_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = Color.Black
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving to Firebase...", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                } else {
                    Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Sync to Firebase", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Return to Dashboard", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun BiometricInputCard(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    secondaryNote: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    testTag: String
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = secondaryNote, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                }
            }

            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Decrement Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .clickable(onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary)
                }

                // Input Field
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(testTag),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Increment Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .clickable(onClick = onIncrement),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary)
                }
            }
        }
    }
}
