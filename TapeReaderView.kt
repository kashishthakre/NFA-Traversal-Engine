package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EPSILON

@Composable
fun TapeReaderView(
    inputString: String,
    currentStepIndex: Int,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto-scroll tape to keep the reading head centered
    LaunchedEffect(currentStepIndex) {
        val targetScroll = (currentStepIndex * 54 - 100).coerceAtLeast(0)
        scrollState.animateScrollTo(targetScroll)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tape_reader_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep slate terminal background
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF38BDF8), CircleShape)
                    )
                    Text(
                        text = "INPUT TAPE READER (READ HEAD)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }

                // Head Position Badge
                val headPosText = when {
                    inputString.isEmpty() -> "Tape: ε (Empty)"
                    currentStepIndex == 0 -> "Head: Before Index 0"
                    currentStepIndex <= inputString.length -> "Read: Index ${currentStepIndex - 1} ['${inputString[currentStepIndex - 1]}']"
                    else -> "Head: End of Tape ⊣"
                }
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF475569)))
                ) {
                    Text(
                        text = headPosText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tape Track with Cells and Pointer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                // Pointer indicator row
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cell 0 is start before reading
                    TapePointer(
                        isActive = currentStepIndex == 0,
                        label = "START"
                    )

                    for (i in inputString.indices) {
                        TapePointer(
                            isActive = currentStepIndex == i + 1,
                            label = "HEAD"
                        )
                    }

                    // End marker pointer
                    TapePointer(
                        isActive = currentStepIndex > inputString.length,
                        label = "END"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // The physical tape cells row
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start marker cell
                    TapeCell(
                        symbol = "⊢",
                        subtext = "Start",
                        isRead = currentStepIndex > 0,
                        isCurrent = currentStepIndex == 0,
                        onClick = { onCellClick(0) }
                    )

                    if (inputString.isEmpty()) {
                        TapeCell(
                            symbol = "ε",
                            subtext = "Empty",
                            isRead = currentStepIndex > 0,
                            isCurrent = currentStepIndex == 1,
                            onClick = { onCellClick(0) }
                        )
                    } else {
                        inputString.forEachIndexed { idx, ch ->
                            val stepNumber = idx + 1
                            val isRead = currentStepIndex > stepNumber
                            val isCurrent = currentStepIndex == stepNumber
                            TapeCell(
                                symbol = ch.toString(),
                                subtext = "[$idx]",
                                isRead = isRead,
                                isCurrent = isCurrent,
                                onClick = { onCellClick(stepNumber) }
                            )
                        }
                    }

                    // End of tape marker cell
                    TapeCell(
                        symbol = "⊣",
                        subtext = "End",
                        isRead = false,
                        isCurrent = currentStepIndex > inputString.length,
                        onClick = { onCellClick(inputString.length) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TapePointer(isActive: Boolean, label: String) {
    Box(
        modifier = Modifier.width(48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = Color(0xFF38BDF8),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(23.dp))
        }
    }
}

@Composable
private fun TapeCell(
    symbol: String,
    subtext: String,
    isRead: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val cellBg by animateColorAsState(
        targetValue = when {
            isCurrent -> Color(0xFF2563EB) // Bright glowing blue
            isRead -> Color(0xFF1E293B) // Processed/dimmed
            else -> Color(0xFF334155) // Pending
        },
        label = "cellBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isCurrent -> Color(0xFF60A5FA)
            isRead -> Color(0xFF475569)
            else -> Color(0xFF64748B)
        },
        label = "cellBorder"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isCurrent -> Color.White
            isRead -> Color(0xFF94A3B8)
            else -> Color(0xFFF8FAFC)
        },
        label = "cellText"
    )

    Surface(
        color = cellBg,
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor)),
        modifier = Modifier
            .width(48.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag("tape_cell_$symbol")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                ),
                color = textColor
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                ),
                color = if (isCurrent) Color(0xFFBFDBFE) else Color(0xFF64748B)
            )
        }
    }
}
