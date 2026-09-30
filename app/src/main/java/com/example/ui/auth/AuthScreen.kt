package com.example.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CoachFitViewModel
import com.example.ui.components.CoachFitCard
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    viewModel: CoachFitViewModel,
    modifier: Modifier = Modifier
) {
    // Portal mode: false = Client Portal, true = Trainer Portal
    var isTrainerPortal by remember { mutableStateOf(false) }
    var currentAuthMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Input fields
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var fullNameInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Compact Header: Low-profile, sleek logo with no excessive height
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTrainerPortal) Icons.Default.Shield else Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "COACHFIT",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isTrainerPortal) "TRAINER" else "CLIENT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                    Text(
                        text = if (isTrainerPortal) "Coach Administration & Rosters" else "Workouts, Nutrition & Progress Tracking",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Auth Card
            CoachFitCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                backgroundColor = DarkSurface,
                cornerRadius = 16.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {

                    // Tab Selector: Sign In vs Sign Up
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentAuthMode == AuthMode.SIGN_IN) NeonGreen else Color.Transparent)
                                .clickable {
                                    currentAuthMode = AuthMode.SIGN_IN
                                    errorMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign In",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentAuthMode == AuthMode.SIGN_IN) Color.Black else TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentAuthMode == AuthMode.SIGN_UP) NeonGreen else Color.Transparent)
                                .clickable {
                                    currentAuthMode = AuthMode.SIGN_UP
                                    errorMessage = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sign Up",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentAuthMode == AuthMode.SIGN_UP) Color.Black else TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedContent(
                        targetState = currentAuthMode,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "auth_form_anim"
                    ) { mode ->
                        Column {
                            if (mode == AuthMode.SIGN_UP) {
                                // Full Name field for Sign Up
                                OutlinedTextField(
                                    value = fullNameInput,
                                    onValueChange = { fullNameInput = it; errorMessage = null },
                                    label = { Text("Full Name", color = TextSecondary) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary)
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TextPrimary,
                                        unfocusedBorderColor = CharcoalBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Email Field
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it; errorMessage = null },
                                label = { Text(if (isTrainerPortal) "Coach Email" else "Email", color = TextSecondary) },
                                placeholder = {
                                    Text(
                                        if (isTrainerPortal) "coach@coachfit.com" else "alex@example.com",
                                        color = TextSecondary.copy(alpha = 0.5f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(if (isTrainerPortal) "trainer_email_input" else "client_login_email_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TextPrimary,
                                    unfocusedBorderColor = CharcoalBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Password Field
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it; errorMessage = null },
                                label = { Text("Password", color = TextSecondary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = TextSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(if (isTrainerPortal) "trainer_password_input" else "client_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TextPrimary,
                                    unfocusedBorderColor = CharcoalBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Button: Sign In or Sign Up
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    val trimmedEmail = emailInput.trim()
                                    val trimmedPass = passwordInput.trim()

                                    if (mode == AuthMode.SIGN_UP && fullNameInput.isBlank()) {
                                        errorMessage = "Please enter your full name."
                                        return@Button
                                    }
                                    if (trimmedEmail.isBlank() || trimmedPass.isBlank()) {
                                        errorMessage = "Please enter both email and password."
                                        return@Button
                                    }

                                    isLoading = true
                                    errorMessage = null

                                    if (mode == AuthMode.SIGN_IN) {
                                        viewModel.login(
                                            email = trimmedEmail,
                                            password = trimmedPass,
                                            onError = {
                                                isLoading = false
                                                errorMessage = it
                                            }
                                        )
                                    } else {
                                        viewModel.signUp(
                                            name = fullNameInput.trim(),
                                            email = trimmedEmail,
                                            password = trimmedPass,
                                            isTrainer = isTrainerPortal,
                                            onError = {
                                                isLoading = false
                                                errorMessage = it
                                            }
                                        )
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag(if (isTrainerPortal) "trainer_login_button" else "client_login_submit_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonGreen,
                                    contentColor = Color.Black
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (mode == AuthMode.SIGN_IN) "Signing In..." else "Creating Account...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    Text(
                                        text = if (mode == AuthMode.SIGN_IN) {
                                            if (isTrainerPortal) "Sign In as Trainer" else "Sign In"
                                        } else {
                                            if (isTrainerPortal) "Register as Trainer" else "Create Client Account"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Demo Credentials helper pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .clickable {
                                if (isTrainerPortal) {
                                    emailInput = "coach@coachfit.com"
                                    passwordInput = "password123"
                                } else {
                                    emailInput = "alex@example.com"
                                    passwordInput = "password123"
                                }
                                errorMessage = null
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isTrainerPortal) {
                                "Tap to fill demo coach: coach@coachfit.com"
                            } else {
                                "Tap to fill demo client: alex@example.com"
                            },
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Portal Switcher: When in Trainer mode, client options are NOT shown in the form;
                    // only a bottom navigational switch to toggle between portals
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                isTrainerPortal = !isTrainerPortal
                                emailInput = ""
                                passwordInput = ""
                                fullNameInput = ""
                                errorMessage = null
                            }
                        ) {
                            Text(
                                text = if (isTrainerPortal) {
                                    "← Switch to Client Login"
                                } else {
                                    "Coach or Trainer? Sign in to Trainer Portal →"
                                },
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
