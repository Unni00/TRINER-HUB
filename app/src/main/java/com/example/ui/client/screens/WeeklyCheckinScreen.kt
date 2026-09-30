package com.example.ui.client.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.CoachFitViewModel
import com.example.ui.components.CoachFitCard
import com.example.ui.components.RatingSelector
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyCheckinScreen(
    viewModel: CoachFitViewModel,
    modifier: Modifier = Modifier
) {
    val checkins by viewModel.clientCheckins.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val user = currentUser

    var weightInput by remember(user) { mutableStateOf((user?.currentWeightKg ?: 78.0f).toString()) }
    var heightInput by remember(user) { mutableStateOf((user?.heightCm?.toInt() ?: 175).toString()) }
    var ageInput by remember(user) { mutableStateOf((user?.age ?: 26).toString()) }
    var sleepRating by remember { mutableIntStateOf(4) }
    var energyRating by remember { mutableIntStateOf(4) }
    var notesInput by remember(user) { mutableStateOf(user?.feedback ?: "") }

    var frontPhotoUri by remember { mutableStateOf<String?>("preset:front_male") }
    var sidePhotoUri by remember { mutableStateOf<String?>("preset:side_male") }
    var backPhotoUri by remember { mutableStateOf<String?>("preset:back_male") }

    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Android zero-permission Photo Picker launchers for 3 photos
    var photoTarget by remember { mutableStateOf("front") }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            when (photoTarget) {
                "front" -> frontPhotoUri = it.toString()
                "side" -> sidePhotoUri = it.toString()
                "back" -> backPhotoUri = it.toString()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Weekly Check-in Hub",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Track photos, bodyweight & recovery for your coach",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${checkins.size} Check-ins",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // New Check-in Form Card
            item {
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
                            Text(
                                text = "Submit Weekly Report",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date()),
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "My Physical Stats",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Physical Stats: Weight, Height, Age
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
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("checkin_weight_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { heightInput = it },
                                label = { Text("Height (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("checkin_height_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = ageInput,
                                onValueChange = { ageInput = it },
                                label = { Text("Age (yrs)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("checkin_age_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonGreen
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sleep Quality (1-5)
                        RatingSelector(
                            rating = sleepRating,
                            onRatingSelected = { sleepRating = it },
                            label = "Sleep Quality (1 = Poor, 5 = Deep Rest)",
                            modifier = Modifier.testTag("sleep_rating_selector")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Energy Level (1-5)
                        RatingSelector(
                            rating = energyRating,
                            onRatingSelected = { energyRating = it },
                            label = "Energy Level (1 = Exhausted, 5 = Peak)",
                            modifier = Modifier.testTag("energy_rating_selector")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Photo Picker Section (Front, Side, Back)
                        Text(
                            text = "Physique Check-in Photos",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap any angle to pick photo from gallery or use preset",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Front Photo
                            PhotoUploadBox(
                                title = "Front Photo",
                                photoUri = frontPhotoUri,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_front_photo"),
                                onClick = {
                                    photoTarget = "front"
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )

                            // Side Photo
                            PhotoUploadBox(
                                title = "Side Photo",
                                photoUri = sidePhotoUri,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_side_photo"),
                                onClick = {
                                    photoTarget = "side"
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )

                            // Back Photo
                            PhotoUploadBox(
                                title = "Back Photo",
                                photoUri = backPhotoUri,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_back_photo"),
                                onClick = {
                                    photoTarget = "back"
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Notes for Coach
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Notes for Coach") },
                            placeholder = { Text("Hunger levels, digestion, fatigue, soreness, etc.", color = TextSecondary) },
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkin_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                val weight = weightInput.toFloatOrNull() ?: (user?.currentWeightKg ?: 0f)
                                val height = heightInput.toFloatOrNull() ?: (user?.heightCm ?: 175f)
                                val age = ageInput.toIntOrNull() ?: (user?.age ?: 26)

                                if (weight > 0f) {
                                    viewModel.updateClientPhysicalStatsAndFeedback(
                                        weight = weight,
                                        height = height,
                                        age = age,
                                        feedback = notesInput
                                    ) {
                                        viewModel.submitWeeklyCheckin(
                                            weight = weight,
                                            sleepQuality = sleepRating,
                                            energyLevel = energyRating,
                                            frontPhotoUri = frontPhotoUri,
                                            sidePhotoUri = sidePhotoUri,
                                            backPhotoUri = backPhotoUri,
                                            notes = notesInput.ifBlank { null }
                                        ) {
                                            statusMessage = "Weekly Stats & Feedback sent to Coach!"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("submit_weekly_checkin_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit Stats & Feedback", fontWeight = FontWeight.Bold)
                        }

                        if (statusMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = statusMessage ?: "",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Past Check-ins History Title
            item {
                Text(
                    text = "Check-in History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (checkins.isEmpty()) {
                item {
                    CoachFitCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = DarkSurfaceVariant
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No past check-ins yet. Submit your first weekly check-in above!",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(checkins, key = { it.id }) { checkin ->
                    CoachFitCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = DarkSurface
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = NeonGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(checkin.date)),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${checkin.currentWeight} kg",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteCheckin(checkin.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete checkin",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Sleep: ${checkin.sleepQuality}/5 ★",
                                    fontSize = 12.sp,
                                    color = AthleticCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Energy: ${checkin.energyLevel}/5 ★",
                                    fontSize = 12.sp,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (!checkin.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\"${checkin.notes}\"",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            // Photo previews
                            Spacer(modifier = Modifier.height(10.dp))
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
}

@Composable
fun PhotoUploadBox(
    title: String,
    photoUri: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val hasPhoto = photoUri != null
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .border(
                1.dp,
                if (hasPhoto) NeonGreen.copy(alpha = 0.5f) else Color(0xFF333333),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (hasPhoto && !photoUri.startsWith("preset:")) {
            AsyncImage(
                model = photoUri,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(6.dp)
            ) {
                Icon(
                    imageVector = if (hasPhoto) Icons.Default.Image else Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = if (hasPhoto) NeonGreen else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (hasPhoto) TextPrimary else TextSecondary
                )
                Text(
                    text = if (hasPhoto) "Attached ✓" else "Tap to add",
                    fontSize = 9.sp,
                    color = if (hasPhoto) NeonGreen else TextSecondary
                )
            }
        }
    }
}

@Composable
fun MiniPhotoBadge(
    title: String,
    photoUri: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Text(
                text = if (photoUri != null) "Photo Added" else "No Photo",
                fontSize = 9.sp,
                color = if (photoUri != null) NeonGreen else Color(0xFF666666)
            )
        }
    }
}
