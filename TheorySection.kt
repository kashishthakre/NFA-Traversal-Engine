package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TheorySection(modifier: Modifier = Modifier) {
    var expandedHowItWorks by remember { mutableStateOf(true) }
    var expandedComplexity by remember { mutableStateOf(true) }
    var expandedPseudocode by remember { mutableStateOf(false) }
    var expandedVivaQnA by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("theory_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. How NFA Traversal Works
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedHowItWorks = !expandedHowItWorks },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                        Text(
                            "How NFA Traversal Works",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                    }
                    Icon(
                        imageVector = if (expandedHowItWorks) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }

                AnimatedVisibility(visible = expandedHowItWorks) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "A Nondeterministic Finite Automaton (NFA) is formally defined as a 5-tuple M = (Q, Σ, δ, q₀, F):",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FormulaRow("Q", "Finite set of states")
                        FormulaRow("Σ", "Finite set of input symbols (alphabet)")
                        FormulaRow("δ", "Transition function: Q × (Σ ∪ {ε}) → P(Q)")
                        FormulaRow("q₀", "Initial / start state (q₀ ∈ Q)")
                        FormulaRow("F", "Set of accepting / final states (F ⊆ Q)")

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Step-by-Step Traversal Protocol:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        NumberedItem(1, "Start State & ε-Closure: Initialize active set with ε-closure({q₀}).")
                        NumberedItem(2, "Symbol Consumption: Read the next symbol 'c' from the input stream.")
                        NumberedItem(3, "Parallel Transition: For every state q currently active, compute all targets δ(q, c).")
                        NumberedItem(4, "Combine & Epsilon-Closure: Union all targets, compute their ε-closure, and eliminate duplicate states.")
                        NumberedItem(5, "Repetition: Repeat steps 2-4 until the entire input string is consumed.")
                        NumberedItem(6, "Acceptance Check: String is ACCEPTED if and only if ActiveStates ∩ F ≠ ∅.")
                    }
                }
            }
        }

        // 2. Complexity Analysis Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedComplexity = !expandedComplexity },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        Text(
                            "Time & Space Complexity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                    }
                    Icon(
                        imageVector = if (expandedComplexity) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }

                AnimatedVisibility(visible = expandedComplexity) {
                    Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ComplexityBox(
                            title = "Parallel NFA Time Complexity: O(|w| · (|Q| + |E|))",
                            content = "For an input string w of length |w|, in each step the engine inspects at most |Q| active states and traverses their outgoing transitions. ε-closure computation via BFS runs in O(|Q| + |E_ε|) per step."
                        )
                        ComplexityBox(
                            title = "Space Complexity: O(|Q|)",
                            content = "At any moment during parallel simulation, the engine stores only the subset of active states, which is at most |Q| state identifiers."
                        )
                        ComplexityBox(
                            title = "Comparison with Backtracking (DFS): O(|Q|^|w|)",
                            content = "Naive backtracking without memoization may re-explore overlapping state branches exponentially. The parallel subset simulation is significantly more time-efficient."
                        )
                    }
                }
            }
        }

        // 3. Algorithm Pseudocode
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedPseudocode = !expandedPseudocode },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                        Text(
                            "Parallel Traversal Pseudocode",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                    }
                    Icon(
                        imageVector = if (expandedPseudocode) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }

                AnimatedVisibility(visible = expandedPseudocode) {
                    val pseudocode = """
algorithm ParallelNfaTraversal(M = (Q, Σ, δ, q₀, F), w = c₁c₂...cₙ):
    // Step 0: Initial active states with ε-closure
    activeStates ← ε_closure({q₀})
    
    // Process input symbols one by one
    for each symbol c in w do:
        nextStates ← ∅
        for each state q in activeStates do:
            // δ(q, c) returns set of destination states
            nextStates ← nextStates ∪ δ(q, c)
            
        // Expand via ε-transitions
        activeStates ← ε_closure(nextStates)
        
        // Early termination if all branches died
        if activeStates = ∅ then:
            return REJECTED
            
    // Acceptance condition
    if (activeStates ∩ F) ≠ ∅ then:
        return ACCEPTED
    else:
        return REJECTED
                    """.trimIndent()

                    val scroll = rememberScrollState()
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = pseudocode,
                            modifier = Modifier
                                .horizontalScroll(scroll)
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // 4. College Viva / Exam Q&A Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedVivaQnA = !expandedVivaQnA },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                        Text(
                            "Viva & Automata Theory Q&A",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F172A)
                        )
                    }
                    Icon(
                        imageVector = if (expandedVivaQnA) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }

                AnimatedVisibility(visible = expandedVivaQnA) {
                    Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        VivaItem(
                            q = "Q1: What makes an automaton non-deterministic?",
                            a = "In an NFA, for a given state and input symbol, there can be zero, one, or multiple destination states (δ: Q × (Σ ∪ {ε}) → P(Q)), unlike a DFA where exactly one transition exists for each symbol."
                        )
                        VivaItem(
                            q = "Q2: Are NFAs more expressive / powerful than DFAs?",
                            a = "No! By the Subset Construction Theorem (Rabin & Scott), every NFA can be converted into an equivalent DFA recognizing the exact same regular language. However, an NFA can be exponentially smaller (N states vs up to 2^N states for DFA)."
                        )
                        VivaItem(
                            q = "Q3: What is the formal definition of ε-closure?",
                            a = "ε-closure(S) is the set of all states reachable from any state in S by making zero or more ε-transitions without consuming any input symbol."
                        )
                        VivaItem(
                            q = "Q4: Why simulate in parallel rather than backtracking?",
                            a = "Parallel simulation tracks the active state set simultaneously, running in polynomial time O(|w|·|Q|²), preventing exponential branch explosion."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormulaRow(symbol: String, description: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = Color(0xFFEFF6FF),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.width(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1D4ED8)
                )
            }
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF475569)
        )
    }
}

@Composable
private fun NumberedItem(number: Int, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "$number",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF334155),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ComplexityBox(title: String, content: String) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF475569)
            )
        }
    }
}

@Composable
private fun VivaItem(q: String, a: String) {
    Surface(
        color = Color(0xFFFFFBEB),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFDE68A))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = q,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF92400E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = a,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF78350F)
            )
        }
    }
}
