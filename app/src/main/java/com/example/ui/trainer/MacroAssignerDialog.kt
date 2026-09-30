package com.example.ui.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.UserEntity
import com.example.ui.components.CoachFitCard
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MacroAssignerDialog(
    client: UserEntity,
    onDismiss: () -> Unit,
    onSaveMacros: (calories: Int, protein: Int, carbs: Int, fats: Int) -> Unit
) {
    var caloriesInput by remember { mutableStateOf(client.targetCalories.toString()) }
    var proteinInput by remember { mutableStateOf(client.targetProtein.toString()) }
    var carbsInput by remember { mutableStateOf(client.targetCarbs.toString()) }
    var fatsInput by remember { mutableStateOf(client.targetFats.toString()) }

    // Calculated calories from entered macros: P*4 + C*4 + F*9
    val calculatedFromMacros = remember(proteinInput, carbsInput, fatsInput) {
        val p = proteinInput.toIntOrNull() ?: 0
        val c = carbsInput.toIntOrNull() ?: 0
        val f = fatsInput.toIntOrNull() ?: 0
        (p * 4) + (c * 4) + (f * 9)
    }

    Dialog(onDismissRequest = onDismiss) {
        CoachFitCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface,
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Assign Target Macros",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "For ${client.fullName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Target Calories
                OutlinedTextField(
                    value = caloriesInput,
                    onValueChange = { caloriesInput = it },
                    label = { Text("Target Daily Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_calories_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Protein
                OutlinedTextField(
                    value = proteinInput,
                    onValueChange = { proteinInput = it },
                    label = { Text("Target Protein (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_protein_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Carbs
                OutlinedTextField(
                    value = carbsInput,
                    onValueChange = { carbsInput = it },
                    label = { Text("Target Carbs (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_carbs_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AthleticOrange
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Target Fats
                OutlinedTextField(
                    value = fatsInput,
                    onValueChange = { fatsInput = it },
                    label = { Text("Target Fats (grams)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_fats_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AthleticCyan
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Nutrition Math Helper box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Macro Energy Sum:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "$calculatedFromMacros kcal",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (calculatedFromMacros.toString() == caloriesInput) NeonGreen else AthleticOrange
                            )
                        }
                        Text(
                            text = "(P×4 + C×4 + F×9)",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cals = caloriesInput.toIntOrNull() ?: client.targetCalories
                            val p = proteinInput.toIntOrNull() ?: client.targetProtein
                            val c = carbsInput.toIntOrNull() ?: client.targetCarbs
                            val f = fatsInput.toIntOrNull() ?: client.targetFats
                            onSaveMacros(cals, p, c, f)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_macros_button")
                    ) {
                        Text("Save Targets", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
