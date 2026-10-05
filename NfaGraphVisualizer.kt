package com.example.ui.components

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NfaDefinition
import com.example.model.SimulationStep
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun NfaGraphVisualizer(
    definition: NfaDefinition,
    currentStep: SimulationStep?,
    selectedNode: String?,
    onSelectNode: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val nodeRadiusPx = with(density) { 32.dp.toPx() }

    // Pulsing animation for active states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Animated signal wave on active transitions
    val signalProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "signalProgress"
    )

    // Zoom, pan & grid
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var showGrid by remember { mutableStateOf(true) }

    // Node positions (draggable by user)
    val nodePositions = remember { mutableStateMapOf<String, Offset>() }
    var draggedNodeKey by remember { mutableStateOf<String?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
            .testTag("nfa_graph_canvas_container")
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // Initialize positions if not present or on state list change
        LaunchedEffect(definition.states, widthPx, heightPx) {
            val states = definition.states
            if (states.isEmpty() || widthPx <= 0f || heightPx <= 0f) return@LaunchedEffect

            val cx = widthPx / 2f
            val cy = heightPx / 2f

            if (states.size <= 4) {
                // Linear / graceful curve layout
                val spacing = (widthPx - 160f) / (states.size.coerceAtLeast(2) - 1)
                states.forEachIndexed { i, state ->
                    val x = 90f + i * spacing
                    val y = cy + if (i % 2 == 1 && states.size > 2) -45f else 35f
                    nodePositions[state] = Offset(x, y)
                }
            } else {
                // Circular layout for 5+ states
                val circleRadius = minOf(widthPx, heightPx) * 0.36f
                states.forEachIndexed { i, state ->
                    val angle = 2 * PI * i / states.size - PI / 2
                    val x = cx + (circleRadius * cos(angle)).toFloat()
                    val y = cy + (circleRadius * sin(angle)).toFloat()
                    nodePositions[state] = Offset(x, y)
                }
            }
        }

        // Canvas for the Graph
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(definition.states) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val transformedStart = (startOffset - panOffset) / zoomScale
                            val touched = definition.states.find { state ->
                                val pos = nodePositions[state] ?: Offset.Zero
                                (transformedStart - pos).getDistance() <= nodeRadiusPx * 1.5f
                            }
                            if (touched != null) {
                                draggedNodeKey = touched
                                onSelectNode(touched)
                            } else {
                                draggedNodeKey = null
                                onSelectNode(null)
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val key = draggedNodeKey
                            if (key != null) {
                                val curPos = nodePositions[key] ?: Offset.Zero
                                nodePositions[key] = curPos + (dragAmount / zoomScale)
                            } else {
                                panOffset += dragAmount
                            }
                        },
                        onDragEnd = {
                            draggedNodeKey = null
                        }
                    )
                }
        ) {
            val activeStateSet = currentStep?.activeStates ?: emptySet()
            val activeTransitions = currentStep?.activeTransitions ?: emptySet()

            // Save transform
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.translate(panOffset.x, panOffset.y)
            drawContext.canvas.nativeCanvas.scale(zoomScale, zoomScale)

            // 1. Draw Technical Dotted Grid Paper Texture
            if (showGrid) {
                val gridSize = 32f
                val startX = -panOffset.x / zoomScale - 60f
                val startY = -panOffset.y / zoomScale - 60f
                val endX = startX + size.width / zoomScale + 120f
                val endY = startY + size.height / zoomScale + 120f

                var gx = (startX / gridSize).toInt() * gridSize
                while (gx < endX) {
                    var gy = (startY / gridSize).toInt() * gridSize
                    while (gy < endY) {
                        drawCircle(
                            color = Color(0xFFCBD5E1).copy(alpha = 0.6f),
                            radius = 1.3f,
                            center = Offset(gx, gy)
                        )
                        gy += gridSize
                    }
                    gx += gridSize
                }
            }

            // Group transitions by (fromState, toState) to aggregate symbols
            val transitionGroups = mutableMapOf<Pair<String, String>, MutableList<String>>()
            for (t in definition.transitions) {
                for (to in t.toStates) {
                    val key = Pair(t.fromState, to.trim())
                    if (definition.states.contains(key.first) && definition.states.contains(key.second)) {
                        transitionGroups.getOrPut(key) { mutableListOf() }.add(t.normalizedSymbol)
                    }
                }
            }

            // 2. Draw Edges
            for ((pair, symbols) in transitionGroups) {
                val (fromState, toState) = pair
                val fromPos = nodePositions[fromState] ?: continue
                val toPos = nodePositions[toState] ?: continue

                val isTraversed = activeTransitions.contains(Pair(fromState, toState))
                val edgeColor = if (isTraversed) Color(0xFF2563EB) else Color(0xFF94A3B8)
                val strokeWidth = if (isTraversed) 4.8f else 2.2f
                val symbolLabel = symbols.distinct().joinToString(", ")

                if (fromState == toState) {
                    // Self-loop transition
                    drawSelfLoop(
                        center = fromPos,
                        radius = nodeRadiusPx,
                        label = symbolLabel,
                        color = edgeColor,
                        isTraversed = isTraversed,
                        strokeWidth = strokeWidth,
                        signalProgress = if (isTraversed) signalProgress else 0f
                    )
                } else {
                    // Directed edge between two distinct states
                    val oppositeExists = transitionGroups.containsKey(Pair(toState, fromState))
                    drawDirectedEdge(
                        from = fromPos,
                        to = toPos,
                        nodeRadius = nodeRadiusPx,
                        label = symbolLabel,
                        color = edgeColor,
                        isTraversed = isTraversed,
                        curved = oppositeExists,
                        strokeWidth = strokeWidth,
                        signalProgress = if (isTraversed) signalProgress else 0f
                    )
                }
            }

            // 3. Draw Start Arrow into the start state
            val startState = definition.startState
            val startPos = nodePositions[startState]
            if (startPos != null) {
                drawStartArrow(
                    target = startPos,
                    nodeRadius = nodeRadiusPx,
                    color = Color(0xFF475569)
                )
            }

            // 4. Draw State Nodes
            for (state in definition.states) {
                val pos = nodePositions[state] ?: continue
                val isActive = activeStateSet.contains(state)
                val isFinal = definition.finalStates.contains(state)
                val isSelected = selectedNode == state

                drawStateNode(
                    center = pos,
                    name = state,
                    radius = nodeRadiusPx,
                    isActive = isActive,
                    isFinal = isFinal,
                    isSelected = isSelected,
                    pulseScale = if (isActive) pulseScale else 1.0f,
                    pulseAlpha = if (isActive) pulseAlpha else 0f
                )
            }

            drawContext.canvas.nativeCanvas.restore()
        }

        // Overlay Technical Controls Toolbar (Grid toggle, Zoom In, Zoom Out, Auto-Center)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Grid Toggle
            IconButton(
                onClick = { showGrid = !showGrid },
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.95f), CircleShape)
                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
            ) {
                Icon(
                    imageVector = if (showGrid) Icons.Default.Grid3x3 else Icons.Default.GridOff,
                    contentDescription = "Toggle Grid",
                    tint = if (showGrid) Color(0xFF2563EB) else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Zoom In
            IconButton(
                onClick = { zoomScale = (zoomScale * 1.2f).coerceAtMost(2.5f) },
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.95f), CircleShape)
                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
            ) {
                Icon(
                    Icons.Default.ZoomIn,
                    contentDescription = "Zoom In",
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Zoom Out
            IconButton(
                onClick = { zoomScale = (zoomScale / 1.2f).coerceAtLeast(0.5f) },
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.95f), CircleShape)
                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
            ) {
                Icon(
                    Icons.Default.ZoomOut,
                    contentDescription = "Zoom Out",
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Reset & Recenter View
            IconButton(
                onClick = {
                    zoomScale = 1.0f
                    panOffset = Offset.Zero
                },
                modifier = Modifier
                    .size(34.dp)
                    .background(Color.White.copy(alpha = 0.95f), CircleShape)
                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
            ) {
                Icon(
                    Icons.Default.CenterFocusStrong,
                    contentDescription = "Recenter",
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Status pill indicator at bottom-left
        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(3.dp),
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1)))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val activeCount = currentStep?.activeStates?.size ?: 0
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (activeCount > 0) Color(0xFF2563EB) else Color(0xFF94A3B8), CircleShape)
                )
                Text(
                    text = "Active: $activeCount states",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "• Drag nodes to arrange",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Node Inspector Card if a node is tapped
        if (selectedNode != null && definition.states.contains(selectedNode)) {
            val isFinal = definition.finalStates.contains(selectedNode)
            val isStart = definition.startState == selectedNode
            val isActive = currentStep?.activeStates?.contains(selectedNode) == true
            val outgoing = definition.transitions.filter { it.fromState == selectedNode }

            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .width(220.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.97f)),
                elevation = CardDefaults.cardElevation(5.dp),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1)))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isActive) (if (isFinal) Color(0xFF10B981) else Color(0xFF2563EB)) else Color(0xFFE2E8F0),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    selectedNode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.White else Color(0xFF1E293B)
                                )
                            }
                        }
                        Text(
                            text = "State $selectedNode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = buildString {
                            if (isStart) append("• Start State (q₀)\n")
                            if (isFinal) append("• Final/Accepting State (F)\n")
                            if (isActive) append("• Currently ACTIVE in Step ${currentStep?.stepIndex ?: 0}\n")
                            if (outgoing.isEmpty()) append("• No outgoing transitions")
                        }.trimEnd(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569)
                    )

                    if (outgoing.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Outgoing Transitions:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF334155)
                        )
                        outgoing.forEach { t ->
                            Text(
                                "──(${t.normalizedSymbol})──► {${t.toStates.joinToString(", ")}}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws a single state node (circle, double circle for final, labels, pulse glow).
 */
private fun DrawScope.drawStateNode(
    center: Offset,
    name: String,
    radius: Float,
    isActive: Boolean,
    isFinal: Boolean,
    isSelected: Boolean,
    pulseScale: Float,
    pulseAlpha: Float
) {
    // Pulse ring around active states
    if (isActive) {
        val glowColor = if (isFinal) Color(0xFF10B981) else Color(0xFF3B82F6)
        drawCircle(
            color = glowColor.copy(alpha = pulseAlpha),
            radius = radius * pulseScale,
            center = center
        )
    }

    // Node fill color
    val fillColor = when {
        isActive && isFinal -> Color(0xFFD1FAE5) // Soft emerald
        isActive -> Color(0xFFDBEAFE) // Soft blue
        isFinal -> Color(0xFFF0FDF4)
        else -> Color.White
    }

    // Node border color
    val strokeColor = when {
        isSelected -> Color(0xFF8B5CF6) // Purple selection
        isActive && isFinal -> Color(0xFF10B981)
        isActive -> Color(0xFF2563EB)
        isFinal -> Color(0xFF059669)
        else -> Color(0xFF334155)
    }

    val borderWidth = if (isActive || isSelected) 4.5f else 2.5f

    // Soft drop shadow
    drawCircle(
        color = Color(0x22000000),
        radius = radius + 2f,
        center = Offset(center.x + 2f, center.y + 3f)
    )

    // Draw main circle background
    drawCircle(
        color = fillColor,
        radius = radius,
        center = center
    )

    // Draw outer border
    drawCircle(
        color = strokeColor,
        radius = radius,
        center = center,
        style = Stroke(width = borderWidth)
    )

    // If final state: draw double circle (inner concentric circle)
    if (isFinal) {
        drawCircle(
            color = strokeColor,
            radius = radius - 7f,
            center = center,
            style = Stroke(width = if (isActive) 3.5f else 2f)
        )
    }

    // State text label
    val paint = Paint().apply {
        isAntiAlias = true
        textSize = radius * 0.72f
        color = strokeColor.toArgb()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val textBounds = Rect()
    paint.getTextBounds(name, 0, name.length, textBounds)
    val textY = center.y + (textBounds.height() / 2f)

    drawContext.canvas.nativeCanvas.drawText(
        name,
        center.x,
        textY,
        paint
    )
}

/**
 * Draws a directed edge from one node to another with arrow and symbol label.
 */
private fun DrawScope.drawDirectedEdge(
    from: Offset,
    to: Offset,
    nodeRadius: Float,
    label: String,
    color: Color,
    isTraversed: Boolean,
    curved: Boolean,
    strokeWidth: Float,
    signalProgress: Float
) {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val distance = sqrt(dx * dx + dy * dy)
    if (distance <= 0.001f) return

    val angle = atan2(dy, dx)

    if (!curved) {
        // Straight line
        val startX = from.x + (nodeRadius * cos(angle))
        val startY = from.y + (nodeRadius * sin(angle))
        val endX = to.x - (nodeRadius * cos(angle))
        val endY = to.y - (nodeRadius * sin(angle))

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
        )

        // Arrow head
        drawArrowHead(
            tip = Offset(endX, endY),
            angle = angle,
            color = color,
            size = if (isTraversed) 22f else 18f
        )

        // Traveling Signal Pulse Bead
        if (isTraversed) {
            val px = startX + (endX - startX) * signalProgress
            val py = startY + (endY - startY) * signalProgress
            drawCircle(
                color = Color(0xFF60A5FA),
                radius = 5.5f,
                center = Offset(px, py)
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = Offset(px, py)
            )
        }

        // Label at midpoint
        val midX = (startX + endX) / 2f
        val midY = (startY + endY) / 2f
        drawEdgeLabel(
            text = label,
            center = Offset(midX, midY - 14f),
            isTraversed = isTraversed
        )
    } else {
        // Curved quadratic arc so bidirectional transitions don't overlap
        val curveOffset = 36f
        val midX = (from.x + to.x) / 2f - (curveOffset * sin(angle))
        val midY = (from.y + to.y) / 2f + (curveOffset * cos(angle))
        val controlPoint = Offset(midX, midY)

        val startX = from.x + (nodeRadius * cos(angle + 0.3f))
        val startY = from.y + (nodeRadius * sin(angle + 0.3f))
        val endX = to.x - (nodeRadius * cos(angle - 0.3f))
        val endY = to.y - (nodeRadius * sin(angle - 0.3f))

        val path = Path().apply {
            moveTo(startX, startY)
            quadraticTo(controlPoint.x, controlPoint.y, endX, endY)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // Arrowhead at end
        val arrowAngle = atan2(endY - controlPoint.y, endX - controlPoint.x)
        drawArrowHead(
            tip = Offset(endX, endY),
            angle = arrowAngle,
            color = color,
            size = if (isTraversed) 22f else 18f
        )

        // Traveling Signal Pulse along Quadratic Bezier
        if (isTraversed) {
            val t = signalProgress
            val invT = 1f - t
            val bx = invT * invT * startX + 2 * invT * t * controlPoint.x + t * t * endX
            val by = invT * invT * startY + 2 * invT * t * controlPoint.y + t * t * endY
            drawCircle(
                color = Color(0xFF60A5FA),
                radius = 5.5f,
                center = Offset(bx, by)
            )
            drawCircle(
                color = Color.White,
                radius = 2.5f,
                center = Offset(bx, by)
            )
        }

        // Label near apex of curve
        drawEdgeLabel(
            text = label,
            center = Offset(controlPoint.x, controlPoint.y),
            isTraversed = isTraversed
        )
    }
}

/**
 * Draws a self-loop (arc back to the same node).
 */
private fun DrawScope.drawSelfLoop(
    center: Offset,
    radius: Float,
    label: String,
    color: Color,
    isTraversed: Boolean,
    strokeWidth: Float,
    signalProgress: Float
) {
    val loopCenterY = center.y - radius - 24f
    val loopRadius = 24f

    // Draw circular arc above the node
    drawCircle(
        color = color,
        radius = loopRadius,
        center = Offset(center.x, loopCenterY),
        style = Stroke(width = strokeWidth)
    )

    // Arrowhead touching the top-right of the node
    val arrowAngle = (PI * 0.75).toFloat()
    drawArrowHead(
        tip = Offset(center.x + (radius * 0.45f), center.y - (radius * 0.88f)),
        angle = arrowAngle,
        color = color,
        size = if (isTraversed) 20f else 16f
    )

    // Traveling Signal Pulse around loop
    if (isTraversed) {
        val loopAngle = signalProgress * 2 * PI
        val px = center.x + (loopRadius * cos(loopAngle)).toFloat()
        val py = loopCenterY + (loopRadius * sin(loopAngle)).toFloat()
        drawCircle(
            color = Color(0xFF60A5FA),
            radius = 5f,
            center = Offset(px, py)
        )
    }

    // Label above self loop
    drawEdgeLabel(
        text = label,
        center = Offset(center.x, loopCenterY - loopRadius - 10f),
        isTraversed = isTraversed
    )
}

/**
 * Draws the "start" pointer arrow indicating the automaton's initial state.
 */
private fun DrawScope.drawStartArrow(
    target: Offset,
    nodeRadius: Float,
    color: Color
) {
    val startX = target.x - nodeRadius - 55f
    val startY = target.y
    val endX = target.x - nodeRadius
    val endY = target.y

    drawLine(
        color = color,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 3f
    )

    drawArrowHead(
        tip = Offset(endX, endY),
        angle = 0f,
        color = color,
        size = 18f
    )

    val paint = Paint().apply {
        isAntiAlias = true
        textSize = 28f
        this.color = color.toArgb()
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    drawContext.canvas.nativeCanvas.drawText("start", startX - 8f, startY + 9f, paint)
}

/**
 * Draws a sharp triangular arrowhead.
 */
private fun DrawScope.drawArrowHead(
    tip: Offset,
    angle: Float,
    color: Color,
    size: Float
) {
    val arrowAngle = PI / 6
    val x1 = tip.x - (size * cos(angle - arrowAngle)).toFloat()
    val y1 = tip.y - (size * sin(angle - arrowAngle)).toFloat()
    val x2 = tip.x - (size * cos(angle + arrowAngle)).toFloat()
    val y2 = tip.y - (size * sin(angle + arrowAngle)).toFloat()

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(x1, y1)
        lineTo(x2, y2)
        close()
    }
    drawPath(path = path, color = color)
}

/**
 * Draws a clean badge pill containing the transition symbol.
 */
private fun DrawScope.drawEdgeLabel(
    text: String,
    center: Offset,
    isTraversed: Boolean
) {
    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = if (isTraversed) android.graphics.Color.rgb(37, 99, 235) else android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    val borderPaint = Paint().apply {
        isAntiAlias = true
        color = if (isTraversed) android.graphics.Color.rgb(29, 78, 216) else android.graphics.Color.rgb(203, 213, 225)
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 28f
        color = if (isTraversed) android.graphics.Color.WHITE else android.graphics.Color.rgb(30, 41, 59)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    val bounds = Rect()
    textPaint.getTextBounds(text, 0, text.length, bounds)
    val padH = 14f
    val padV = 8f
    val left = center.x - bounds.width() / 2f - padH
    val right = center.x + bounds.width() / 2f + padH
    val top = center.y - bounds.height() / 2f - padV
    val bottom = center.y + bounds.height() / 2f + padV

    val rect = android.graphics.RectF(left, top, right, bottom)
    drawContext.canvas.nativeCanvas.drawRoundRect(rect, 8f, 8f, bgPaint)
    drawContext.canvas.nativeCanvas.drawRoundRect(rect, 8f, 8f, borderPaint)
    drawContext.canvas.nativeCanvas.drawText(text, center.x, center.y + bounds.height() / 2f - 2f, textPaint)
}
