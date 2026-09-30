package com.example.ui.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.CoachFitCard
import com.example.ui.theme.AthleticCyan
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TrainerAddFoodDialog(
    onDismiss: () -> Unit,
    onSaveFood: (name: String, calories: Float, protein: Float, carbs: Float, fat: Float, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Poultry") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Poultry", "Seafood", "Grains", "Vegetables", "Dairy", "Fruits", "Supplements", "General")

    val calculatedCals = remember(protein, carbs, fat) {
        val p = protein.toFloatOrNull() ?: 0f
        val c = carbs.toFloatOrNull() ?: 0f
        val f = fat.toFloatOrNull() ?: 0f
        ((p * 4f) + (c * 4f) + (f * 9f)).toInt()
    }

    Dialog(onDismissRequest = onDismiss) {
        CoachFitCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface,
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = AthleticOrange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Add Food to Diet Database",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Prescribe new nutritional items for clients",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Food Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Food / Diet Item Name") },
                    placeholder = { Text("e.g. Skinless Chicken Breast", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trainer_food_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AthleticOrange),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Selector
                Text("Category", fontSize = 11.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AthleticOrange else DarkSurfaceVariant)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calories per 100g
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it; errorMessage = null },
                    label = { Text("Calories (kcal per 100g)") },
                    placeholder = { if (calculatedCals > 0) Text("Auto: $calculatedCals", color = TextSecondary) else null },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trainer_calories_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AthleticOrange),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Protein, Carbs, Fat Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it; errorMessage = null },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("trainer_protein_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("trainer_carbs_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AthleticOrange),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("trainer_fats_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AthleticCyan),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                if (calculatedCals > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Calculated Energy: ~$calculatedCals kcal / 100g (P×4 + C×4 + F×9)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

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
                            if (name.isBlank()) {
                                errorMessage = "Please enter a food name."
                                return@Button
                            }
                            val p = protein.toFloatOrNull() ?: 0f
                            val c = carbs.toFloatOrNull() ?: 0f
                            val f = fat.toFloatOrNull() ?: 0f
                            val cal = calories.toFloatOrNull() ?: calculatedCals.toFloat()

                            if (cal <= 0f && (p + c + f) <= 0f) {
                                errorMessage = "Please enter valid nutrition values."
                                return@Button
                            }

                            onSaveFood(name.trim(), cal, p, c, f, selectedCategory)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AthleticOrange,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_food_item_button")
                    ) {
                        Text("Save to Diet Catalog", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
