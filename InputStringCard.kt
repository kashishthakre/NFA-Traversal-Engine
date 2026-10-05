package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NfaDefinition
import com.example.model.SimulationMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputStringCard(
    inputString: String,
    definition: NfaDefinition,
    simulationMode: SimulationMode,
    validationError: String?,
    onInputChange: (String) -> Unit,
    onModeChange: (SimulationMode) -> Unit,
    onRunSimulation: () -> Unit,
    onStepByStep: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("input_string_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Mode Selector Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Simulation Mode",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E293B)
                )

                Row(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SimulationMode.values().forEach { mode ->
                        val isSelected = simulationMode == mode
                        Surface(
                            color = if (isSelected) Color(0xFF2563EB) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onModeChange(mode) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = simulationMode.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Main Input Field
            Text(
                text = "Enter Input String",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = inputString,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        "e.g. 0101 (or leave empty for ε-test)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFF94A3B8),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                trailingIcon = {
                    if (inputString.isNotEmpty()) {
                        IconButton(onClick = { onInputChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear input", tint = Color(0xFF475569))
                        }
                    }
                },
                isError = validationError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nfa_input_string_field")
            )

            // Inline Validation Error
            AnimatedVisibility(visible = validationError != null) {
                Surface(
                    color = Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFECACA))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = validationError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Symbol / Test Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Quick Input:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF64748B)
                )

                // Chips for each symbol in alphabet
                definition.alphabet.forEach { sym ->
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { onInputChange(inputString + sym) }
                            .padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = "+$sym",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF1D4ED8)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .clickable { onInputChange("") }
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = "ε (Empty)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Run Simulation, Step-by-Step, Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRunSimulation,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("run_simulation_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run Simulation", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onStepByStep,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("step_by_step_button")
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Step-by-Step")
                }

                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .testTag("reset_input_button")
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = Color(0xFF475569))
                }
            }
        }
    }
}
