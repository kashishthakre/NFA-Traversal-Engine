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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PathStep
import com.example.model.TraversalPath

@Composable
fun PathExplorerView(
    paths: List<TraversalPath>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val acceptedCount = paths.count { it.isAccepted }
    val rejectedCount = paths.size - acceptedCount

    val filteredPaths = when (selectedFilter) {
        "ACCEPTED" -> paths.filter { it.isAccepted }
        "REJECTED" -> paths.filter { !it.isAccepted }
        else -> paths
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("path_explorer_card"),
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
                        Icons.Default.AltRoute,
                        contentDescription = "Paths",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Possible Traversal Paths",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "DFS / Backtracking path exploration trace",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All Paths (${paths.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFEFF6FF),
                        selectedLabelColor = Color(0xFF1D4ED8)
                    )
                )
                FilterChip(
                    selected = selectedFilter == "ACCEPTED",
                    onClick = { selectedFilter = "ACCEPTED" },
                    label = { Text("Accepted ($acceptedCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFECFDF5),
                        selectedLabelColor = Color(0xFF047857)
                    )
                )
                FilterChip(
                    selected = selectedFilter == "REJECTED",
                    onClick = { selectedFilter = "REJECTED" },
                    label = { Text("Rejected ($rejectedCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFEF2F2),
                        selectedLabelColor = Color(0xFFB91C1C)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredPaths.isEmpty()) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No paths matching the selected filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredPaths.forEachIndexed { index, path ->
                        PathCard(index = index + 1, path = path)
                    }
                }
            }
        }
    }
}

@Composable
private fun PathCard(index: Int, path: TraversalPath) {
    val isAccepted = path.isAccepted
    val badgeBg = if (isAccepted) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
    val badgeBorder = if (isAccepted) Color(0xFFA7F3D0) else Color(0xFFFECACA)
    val badgeTint = if (isAccepted) Color(0xFF059669) else Color(0xFFEF4444)

    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Path Number & Acceptance Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Path #$index",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF334155)
                )

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(badgeBorder))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isAccepted) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = badgeTint,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isAccepted) "ACCEPTED" else "REJECTED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step Breadcrumb Chain
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (path.steps.isEmpty()) {
                    StatePill(name = path.finalState, isFinal = isAccepted)
                } else {
                    path.steps.forEachIndexed { i, step ->
                        StatePill(name = step.fromState, isFinal = false)
                        TransitionArrow(symbol = step.symbol, isEpsilon = step.isEpsilon)
                        if (i == path.steps.lastIndex) {
                            StatePill(name = step.toState, isFinal = isAccepted)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = path.summary,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun StatePill(name: String, isFinal: Boolean) {
    Surface(
        color = if (isFinal) Color(0xFFECFDF5) else Color.White,
        shape = RoundedCornerShape(6.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(if (isFinal) Color(0xFF10B981) else Color(0xFF94A3B8)))
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isFinal) Color(0xFF047857) else Color(0xFF1E293B)
            )
        )
    }
}

@Composable
private fun TransitionArrow(symbol: String, isEpsilon: Boolean) {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("──", color = Color(0xFFCBD5E1), fontSize = 11.sp)
        Surface(
            color = if (isEpsilon) Color(0xFFF3E8FF) else Color(0xFFEFF6FF),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Text(
                text = symbol,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isEpsilon) Color(0xFF7C3AED) else Color(0xFF2563EB)
                )
            )
        }
        Text("►", color = Color(0xFF94A3B8), fontSize = 11.sp)
    }
}
