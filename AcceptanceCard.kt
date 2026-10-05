package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NfaDefinition
import com.example.model.SimulationResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AcceptanceCard(
    result: SimulationResult?,
    definition: NfaDefinition,
    modifier: Modifier = Modifier
) {
    if (result == null) return

    val isAccepted = result.isAccepted
    val primaryColor = if (isAccepted) Color(0xFF10B981) else Color(0xFFEF4444)
    val bgColor = if (isAccepted) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
    val borderColor = if (isAccepted) Color(0xFFA7F3D0) else Color(0xFFFECACA)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("acceptance_result_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Main Decision Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = primaryColor,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isAccepted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = if (isAccepted) "Accepted" else "Rejected",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = if (isAccepted) "✓ STRING ACCEPTED" else "✕ STRING REJECTED",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isAccepted) Color(0xFF065F46) else Color(0xFF991B1B)
                    )
                    Text(
                        text = if (result.inputString.isEmpty()) {
                            "Input string ε (empty) is ${if (isAccepted) "accepted" else "rejected"} by the NFA."
                        } else {
                            "Input \"${result.inputString}\" is ${if (isAccepted) "accepted" else "rejected"} by the NFA."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (isAccepted) Color(0xFF047857) else Color(0xFFB91C1C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Explanation & Reason
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Final Active States:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E293B)
                        )
                        val statesStr = if (result.finalActiveStates.isEmpty()) "∅ (No Active States)" else "{${result.finalActiveStates.sorted().joinToString(", ")}}"
                        Text(
                            text = statesStr,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryColor
                        )
                    }

                    if (isAccepted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Accepting States Reached:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "{${result.finalStatesReached.sorted().joinToString(", ")}}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reason: ${result.reason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulation Statistics Card
            Text(
                text = "Simulation Statistics",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val successfulPathsCount = result.paths.count { it.isAccepted }
            val statsList = listOf(
                "Input Length" to "${result.inputString.length}",
                "Total States" to "${definition.states.size}",
                "Transitions" to "${definition.transitions.sumOf { it.toStates.size }}",
                "Sim Steps" to "${result.steps.size}",
                "Final Active" to "${result.finalActiveStates.size}",
                "Paths Explored" to "${result.paths.size} ($successfulPathsCount valid)",
                "Execution Time" to "${result.executionTimeMs} ms",
                "Status" to if (isAccepted) "ACCEPTED" else "REJECTED"
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statsList.forEach { (label, value) ->
                    StatItem(label = label, value = value, isStatus = label == "Status", isAccepted = isAccepted)
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    isStatus: Boolean = false,
    isAccepted: Boolean = false
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = if (isStatus) FontFamily.Default else FontFamily.Monospace
                ),
                color = if (isStatus) (if (isAccepted) Color(0xFF059669) else Color(0xFFDC2626)) else Color(0xFF0F172A),
                maxLines = 1
            )
        }
    }
}
