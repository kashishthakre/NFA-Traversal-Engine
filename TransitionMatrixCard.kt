package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.model.EPSILON
import com.example.model.NfaDefinition

@Composable
fun TransitionMatrixCard(
    definition: NfaDefinition,
    activeStates: Set<String>,
    currentSymbol: String?,
    modifier: Modifier = Modifier
) {
    val allSymbols = definition.alphabet + EPSILON
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transition_matrix_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.GridOn,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Formal Transition Matrix (δ)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "→ Start   * Accept",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The Matrix Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .horizontalScroll(scrollState)
            ) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "State (q)",
                        modifier = Modifier.width(90.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF1E293B)
                    )

                    allSymbols.forEach { sym ->
                        val isHighlightedSym = currentSymbol == sym
                        Box(
                            modifier = Modifier
                                .width(95.dp)
                                .background(
                                    if (isHighlightedSym) Color(0xFFDBEAFE) else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "δ(q, $sym)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isHighlightedSym) Color(0xFF1D4ED8) else Color(0xFF334155)
                            )
                        }
                    }
                }

                // Table Data Rows
                definition.states.forEachIndexed { rowIndex, state ->
                    val isStart = definition.startState == state
                    val isFinal = definition.finalStates.contains(state)
                    val isActive = activeStates.contains(state)

                    val rowBg = when {
                        isActive -> Color(0xFFEFF6FF)
                        rowIndex % 2 == 1 -> Color(0xFFF8FAFC)
                        else -> Color.White
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // State identifier with standard automata prefix symbols:
                        // → for start, * for final
                        val prefix = buildString {
                            if (isStart) append("→ ")
                            if (isFinal) append("* ")
                        }

                        Row(
                            modifier = Modifier.width(90.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prefix,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isFinal) Color(0xFF059669) else Color(0xFF2563EB)
                            )
                            Text(
                                text = state,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (isActive) Color(0xFF1D4ED8) else Color(0xFF0F172A)
                            )
                        }

                        // Cells for each symbol
                        allSymbols.forEach { sym ->
                            val isHighlightedCell = isActive && currentSymbol == sym

                            // Find transitions from this state with this symbol
                            val targets = definition.transitions
                                .filter { it.fromState == state && it.normalizedSymbol == sym }
                                .flatMap { it.toStates }
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .distinct()

                            val cellText = if (targets.isEmpty()) "∅" else "{${targets.sorted().joinToString(", ")}}"

                            Box(
                                modifier = Modifier
                                    .width(95.dp)
                                    .padding(horizontal = 2.dp)
                                    .background(
                                        if (isHighlightedCell) Color(0xFFBFDBFE) else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cellText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (targets.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = when {
                                        isHighlightedCell -> Color(0xFF1E3A8A)
                                        targets.isEmpty() -> Color(0xFF94A3B8)
                                        else -> Color(0xFF1E293B)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
