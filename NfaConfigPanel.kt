package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EPSILON
import com.example.model.NfaDefinition

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NfaConfigPanel(
    definition: NfaDefinition,
    onAddState: (String) -> Unit,
    onRemoveState: (String) -> Unit,
    onAddSymbol: (String) -> Unit,
    onRemoveSymbol: (String) -> Unit,
    onSetStartState: (String) -> Unit,
    onToggleFinalState: (String) -> Unit,
    onAddTransition: (String, String, List<String>) -> Unit,
    onRemoveTransition: (String) -> Unit,
    onClearNfa: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newStateInput by remember { mutableStateOf("") }
    var newSymbolInput by remember { mutableStateOf("") }

    // New transition draft inputs
    var fromStateDraft by remember(definition.states) { mutableStateOf(definition.states.firstOrNull() ?: "") }
    var symbolDraft by remember(definition.alphabet) { mutableStateOf(definition.alphabet.firstOrNull() ?: "0") }
    var toStatesDraft by remember { mutableStateOf("") }

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("nfa_config_panel"),
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
                        Icons.Default.Settings,
                        contentDescription = "Config",
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "NFA Formal Configuration",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F172A)
                    )
                }

                OutlinedButton(
                    onClick = { showClearConfirmDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear NFA", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. States Section Q
            SectionHeader(title = "1. States (Q)", subtitle = "Finite set of states in the automaton")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                definition.states.forEach { state ->
                    val isStart = definition.startState == state
                    val isFinal = definition.finalStates.contains(state)
                    Surface(
                        color = when {
                            isStart && isFinal -> Color(0xFFFEF3C7)
                            isFinal -> Color(0xFFECFDF5)
                            isStart -> Color(0xFFEFF6FF)
                            else -> Color(0xFFF1F5F9)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1)))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = state,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF1E293B)
                            )
                            if (definition.states.size > 1) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove $state",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onRemoveState(state) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Add State Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newStateInput,
                    onValueChange = { newStateInput = it },
                    placeholder = { Text("e.g. q${definition.states.size}", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("add_state_input")
                )
                Button(
                    onClick = {
                        val name = newStateInput.trim().ifEmpty { "q${definition.states.size}" }
                        onAddState(name)
                        newStateInput = ""
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("add_state_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add State", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Alphabet Section Σ
            SectionHeader(title = "2. Alphabet (Σ)", subtitle = "Set of recognized input symbols")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                definition.alphabet.forEach { sym ->
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1)))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = sym,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF0F172A)
                            )
                            if (definition.alphabet.size > 1) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove $sym",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onRemoveSymbol(sym) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Add Symbol Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newSymbolInput,
                    onValueChange = { newSymbolInput = it },
                    placeholder = { Text("e.g. 0, 1, a, b", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("add_symbol_input")
                )
                Button(
                    onClick = {
                        val sym = newSymbolInput.trim()
                        if (sym.isNotEmpty()) {
                            onAddSymbol(sym)
                            newSymbolInput = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("add_symbol_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Symbol", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Start State & Final States
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start State Dropdown
                Column(modifier = Modifier.weight(1f)) {
                    SectionHeader(title = "Start State (q₀)", subtitle = "Single initial state")
                    var expandedStartDropdown by remember { mutableStateOf(false) }

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1))),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clickable { expandedStartDropdown = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = definition.startState.ifEmpty { "Select" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color(0xFF1E40AF)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
                        }

                        DropdownMenu(
                            expanded = expandedStartDropdown,
                            onDismissRequest = { expandedStartDropdown = false }
                        ) {
                            definition.states.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s, fontFamily = FontFamily.Monospace) },
                                    onClick = {
                                        onSetStartState(s)
                                        expandedStartDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Final States (F) Multi-Select
                Column(modifier = Modifier.weight(1.3f)) {
                    SectionHeader(title = "Final States (F)", subtitle = "Accepting state set")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        definition.states.forEach { s ->
                            val isSelected = definition.finalStates.contains(s)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggleFinalState(s) },
                                label = {
                                    Text(
                                        s,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFECFDF5),
                                    selectedLabelColor = Color(0xFF065F46)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Dynamic Transition Table (δ)
            SectionHeader(
                title = "4. Transition Table (δ: Q × (Σ ∪ {ε}) → P(Q))",
                subtitle = "Supports non-determinism (multiple destination states) and spontaneous ε-transitions"
            )

            // Current Transitions Table View
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .horizontalScroll(scrollState)
            ) {
                // Table Header
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("From State", modifier = Modifier.width(90.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF334155))
                    Text("Input Symbol", modifier = Modifier.width(95.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF334155))
                    Text("To State(s)", modifier = Modifier.width(150.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF334155))
                    Text("Action", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF334155))
                }

                if (definition.transitions.isEmpty()) {
                    Box(modifier = Modifier.padding(14.dp)) {
                        Text("No transitions configured yet. Add below.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                } else {
                    definition.transitions.forEachIndexed { i, t ->
                        val isEps = t.normalizedSymbol == EPSILON
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (i % 2 == 1) Color(0xFFF8FAFC) else Color.White)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t.fromState, modifier = Modifier.width(90.dp), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Row(modifier = Modifier.width(95.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (isEps) Color(0xFFF3E8FF) else Color(0xFFEFF6FF),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        t.normalizedSymbol,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isEps) Color(0xFF7C3AED) else Color(0xFF1D4ED8)
                                    )
                                }
                            }
                            Text(
                                t.toStates.joinToString(", "),
                                modifier = Modifier.width(150.dp),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A)
                            )
                            IconButton(
                                onClick = { onRemoveTransition(t.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add Transition Row Form
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        "Add Transition Row",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // From State Selector
                        var fromDropdown by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1))),
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { fromDropdown = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "From: ${fromStateDraft.ifEmpty { "Select" }}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            DropdownMenu(expanded = fromDropdown, onDismissRequest = { fromDropdown = false }) {
                                definition.states.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s, fontFamily = FontFamily.Monospace) },
                                        onClick = {
                                            fromStateDraft = s
                                            fromDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Symbol Selector (including ε)
                        var symbolDropdown by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1))),
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clickable { symbolDropdown = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "Sym: $symbolDraft",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            DropdownMenu(expanded = symbolDropdown, onDismissRequest = { symbolDropdown = false }) {
                                (definition.alphabet + EPSILON).forEach { sym ->
                                    DropdownMenuItem(
                                        text = { Text(sym, fontFamily = FontFamily.Monospace) },
                                        onClick = {
                                            symbolDraft = sym
                                            symbolDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Quick ε button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (symbolDraft == EPSILON) Color(0xFF7C3AED) else Color(0xFFF3E8FF),
                            modifier = Modifier
                                .height(42.dp)
                                .clickable { symbolDraft = EPSILON }
                                .padding(horizontal = 8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "ε",
                                    fontWeight = FontWeight.Bold,
                                    color = if (symbolDraft == EPSILON) Color.White else Color(0xFF7C3AED),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = toStatesDraft,
                            onValueChange = { toStatesDraft = it },
                            placeholder = { Text("To State(s), e.g. q0, q1", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("to_states_input")
                        )

                        Button(
                            onClick = {
                                val targets = toStatesDraft.split(",", " ").map { it.trim() }.filter { it.isNotEmpty() }
                                if (fromStateDraft.isNotEmpty() && targets.isNotEmpty()) {
                                    onAddTransition(fromStateDraft, symbolDraft, targets)
                                    toStatesDraft = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("add_transition_button")
                        ) {
                            Text("Add", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Confirm Clear NFA Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear Entire NFA?") },
            text = { Text("This will remove all states, alphabet symbols, and transitions. You can always reload a preset example afterwards.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearNfa()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF1E293B)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF64748B)
        )
    }
}
