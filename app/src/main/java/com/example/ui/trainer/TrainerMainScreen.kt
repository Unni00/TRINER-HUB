package com.example.ui.trainer

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entity.UserEntity
import com.example.ui.CoachFitViewModel
import com.example.ui.TrainerTab
import com.example.ui.components.CoachFitCard
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerMainScreen(
    viewModel: CoachFitViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allClients by viewModel.allClients.collectAsStateWithLifecycle()
    val selectedClient by viewModel.selectedClient.collectAsStateWithLifecycle()
    val trainerTab by viewModel.trainerTab.collectAsStateWithLifecycle()

    // Role Enforcement Guard: ensure Trainer portal is only accessible to users with isTrainer == true
    if (currentUser?.isTrainer != true) {
        TrainerAccessDeniedScreen(onSwitchToDemoTrainer = { viewModel.loginAsDemoTrainer() })
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    var inChatWithClient by remember { mutableStateOf(false) }
    var showEditCoachProfileDialog by remember { mutableStateOf(false) }
    var editCoachName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var editCoachProfession by remember { mutableStateOf(currentUser?.profession ?: "") }
    var editCoachPhotoUri by remember { mutableStateOf<String?>(currentUser?.profilePictureUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { editCoachPhotoUri = it.toString() }
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

    // If client is selected for detail view
    if (selectedClient != null) {
        if (inChatWithClient) {
            TrainerChatView(
                client = selectedClient!!,
                viewModel = viewModel,
                onBack = { inChatWithClient = false }
            )
        } else {
            ClientDetailScreen(
                client = selectedClient!!,
                viewModel = viewModel,
                onBackToRoster = { viewModel.setSelectedClient(null) },
                onOpenChat = { inChatWithClient = true }
            )
        }
        return
    }

    val filteredClients = remember(allClients, searchQuery) {
        if (searchQuery.isBlank()) allClients
        else allClients.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    val currentTabIndex = when (trainerTab) {
        TrainerTab.DASHBOARD -> 0
        TrainerTab.ROSTER -> 1
        TrainerTab.INBOX -> 2
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9100).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFFF9100), CircleShape)
                                .clickable {
                                    editCoachName = currentUser?.fullName ?: ""
                                    editCoachProfession = currentUser?.profession ?: ""
                                    editCoachPhotoUri = currentUser?.profilePictureUri ?: ""
                                    showEditCoachProfileDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUri = currentUser?.profilePictureUri
                            if (!photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Trainer Photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = "Edit Profile & Photo",
                                    tint = Color(0xFFFF9100),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "COACH HUB",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Trainer Portal • ${currentUser?.fullName ?: "Coach"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF9100)
                            )
                        }
                    }
                },
                actions = {
                    // Quick Switch to Client Demo
                    OutlinedButton(
                        onClick = { viewModel.loginAsDemoClient() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("switch_to_client_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(NeonGreen.copy(alpha = 0.6f))
                        )
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Client View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("trainer_logout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log out", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            // Trainer Navigation Tabs: Dashboard (Macro & Workout Logs), Roster, Inbox
            TabRow(
                selectedTabIndex = currentTabIndex,
                containerColor = DarkBackground,
                contentColor = NeonGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentTabIndex]),
                        color = NeonGreen
                    )
                }
            ) {
                Tab(
                    selected = trainerTab == TrainerTab.DASHBOARD,
                    onClick = { viewModel.setTrainerTab(TrainerTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = {
                        Text(
                            "Dashboard",
                            fontWeight = if (trainerTab == TrainerTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("trainer_dashboard_tab")
                )

                Tab(
                    selected = trainerTab == TrainerTab.ROSTER,
                    onClick = { viewModel.setTrainerTab(TrainerTab.ROSTER) },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = {
                        Text(
                            "Roster (${allClients.size})",
                            fontWeight = if (trainerTab == TrainerTab.ROSTER) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("trainer_roster_tab")
                )

                Tab(
                    selected = trainerTab == TrainerTab.INBOX,
                    onClick = { viewModel.setTrainerTab(TrainerTab.INBOX) },
                    icon = { Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    text = {
                        Text(
                            "Inbox",
                            fontWeight = if (trainerTab == TrainerTab.INBOX) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("trainer_inbox_tab")
                )
            }

            when (trainerTab) {
                TrainerTab.DASHBOARD -> {
                    TrainerDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToClientDetail = { client -> viewModel.setSelectedClient(client) }
                    )
                }
                TrainerTab.ROSTER -> {
                    // Client Roster View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search client roster by name or email...", color = TextSecondary) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonGreen)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_clients_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Active Clients Assigned to You",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (filteredClients.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No clients found matching '$searchQuery'", color = TextSecondary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredClients, key = { it.id }) { client ->
                                    ClientRosterCard(
                                        client = client,
                                        onClick = {
                                            viewModel.setSelectedClient(client)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                TrainerTab.INBOX -> {
                    // Trainer Inbox View
                    TrainerInboxScreen(
                        viewModel = viewModel,
                        clients = allClients
                    )
                }
            }
        }
    }
}

@Composable
fun ClientRosterCard(
    client: UserEntity,
    onClick: () -> Unit
) {
    val heightInMeters = client.heightCm / 100f
    val bmi = if (heightInMeters > 0) client.currentWeightKg / (heightInMeters * heightInMeters) else 0f
    val bmiFormatted = String.format(java.util.Locale.getDefault(), "%.1f", bmi)

    CoachFitCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("client_card_${client.id}"),
        backgroundColor = DarkSurface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
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
                        text = "${client.currentWeightKg} kg • ${client.heightCm.toInt()} cm • ${client.age} yrs • BMI $bmiFormatted",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    if (client.feedback.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Feedback: \"${client.feedback}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Target: ${client.targetCalories} kcal (P:${client.targetProtein}g C:${client.targetCarbs}g F:${client.targetFats}g)",
                        fontSize = 10.sp,
                        color = Color(0xFFFF9100)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View client",
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
