package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SimulationStep

@Composable
fun StepTraversalTable(
    steps: List<SimulationStep>,
    currentStepIndex: Int,
    isPlaying: Boolean,
    playbackSpeed: Float,
    finalStates: Set<String>,
    onSelectStep: (Int) -> Unit,
    onStepPrev: () -> Unit,
    onStepNext: () -> Unit,
    onTogglePlay: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("step_traversal_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Step Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Step-by-Step Traversal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Step $currentStepIndex of ${steps.lastIndex.coerceAtLeast(0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }

                // Speed Selector Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.5f, 1.0f, 2.0f).forEach { spd ->
                        val isSelected = playbackSpeed == spd
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetSpeed(spd) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${spd}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF475569)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Bar: Prev, Play/Pause, Next, Slider, Restart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRestart,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = "Restart", tint = Color(0xFF475569))
                }

                IconButton(
                    onClick = onStepPrev,
                    enabled = currentStepIndex > 0,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous Step",
                        tint = if (currentStepIndex > 0) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2563EB),
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onTogglePlay() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onStepNext,
                    enabled = currentStepIndex < steps.lastIndex,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next Step",
                        tint = if (currentStepIndex < steps.lastIndex) Color(0xFF2563EB) else Color(0xFFCBD5E1)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (steps.isNotEmpty() && steps.size > 1) {
                    Slider(
                        value = currentStepIndex.toFloat(),
                        onValueChange = { onSelectStep(it.toInt()) },
                        valueRange = 0f..steps.lastIndex.toFloat(),
                        steps = (steps.size - 2).coerceAtLeast(0),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("step_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Explanation Pill
            val activeStep = steps.getOrNull(currentStepIndex)
            if (activeStep != null) {
                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFDBFE))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$currentStepIndex",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeStep.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF1E3A8A)
                            )
                            if (activeStep.epsilonExpandedStates.isNotEmpty()) {
                                Text(
                                    text = "ε-closure reached: {${activeStep.epsilonExpandedStates.sorted().joinToString(", ")}}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF7C3AED)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The Step-by-Step Table
            val horizontalScroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .horizontalScroll(horizontalScroll)
            ) {
                // Table Header
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Step",
                        modifier = Modifier.width(60.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF334155)
                    )
                    Text(
                        "Symbol",
                        modifier = Modifier.width(70.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF334155)
                    )
                    Text(
                        "Active States",
                        modifier = Modifier.width(130.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF334155)
                    )
                    Text(
                        "Transition Details",
                        modifier = Modifier.width(260.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF334155)
                    )
                }

                // Table Rows
                steps.forEachIndexed { idx, step ->
                    val isCurrent = idx == currentStepIndex
                    val hasFinal = step.activeStates.any { finalStates.contains(it) }

                    val rowBg by animateColorAsState(
                        targetValue = when {
                            isCurrent && hasFinal -> Color(0xFFECFDF5)
                            isCurrent -> Color(0xFFEFF6FF)
                            idx % 2 == 1 -> Color(0xFFF8FAFC)
                            else -> Color.White
                        },
                        label = "rowBg"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .clickable { onSelectStep(idx) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Step #
                        Row(
                            modifier = Modifier.width(60.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color(0xFF2563EB), CircleShape)
                                )
                            }
                            Text(
                                "$idx",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color(0xFF2563EB) else Color(0xFF334155)
                                )
                            )
                        }

                        // Symbol
                        Text(
                            text = step.inputSymbol ?: "—",
                            modifier = Modifier.width(70.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = if (step.inputSymbol != null) Color(0xFF0F172A) else Color(0xFF94A3B8)
                            )
                        )

                        // Active States
                        val activeStr = if (step.activeStates.isEmpty()) "∅ (Dead)" else "{${step.activeStates.sorted().joinToString(", ")}}"
                        Text(
                            text = activeStr,
                            modifier = Modifier.width(130.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (step.activeStates.isEmpty()) Color(0xFFEF4444) else Color(0xFF1E40AF)
                            )
                        )

                        // Transitions Details
                        Text(
                            text = step.transitionResults.joinToString("; "),
                            modifier = Modifier.width(260.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF475569)
                            )
                        )
                    }
                }
            }
        }
    }
}
