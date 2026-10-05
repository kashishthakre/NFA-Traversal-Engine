package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AboutDialog
import com.example.ui.components.AcceptanceCard
import com.example.ui.components.InputStringCard
import com.example.ui.components.NfaConfigPanel
import com.example.ui.components.NfaGraphVisualizer
import com.example.ui.components.NfaHeader
import com.example.ui.components.PathExplorerView
import com.example.ui.components.PresetsDialog
import com.example.ui.components.StepTraversalTable
import com.example.ui.components.TapeReaderView
import com.example.ui.components.TheorySection
import com.example.ui.components.TransitionMatrixCard
import com.example.viewmodel.NfaViewModel
import kotlinx.coroutines.launch

data class NavTab(val title: String, val icon: ImageVector)

@Composable
fun NfaMainScreen(
    viewModel: NfaViewModel = viewModel()
) {
    val nfaDefinition by viewModel.nfaDefinition.collectAsState()
    val inputString by viewModel.inputString.collectAsState()
    val simulationMode by viewModel.simulationMode.collectAsState()
    val simulationResult by viewModel.simulationResult.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val validationError by viewModel.validationError.collectAsState()

    var showPresetsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val navTabs = listOf(
        NavTab("Simulator", Icons.Default.PlayCircle),
        NavTab("Automaton", Icons.Default.Settings),
        NavTab("Paths", Icons.Default.AltRoute),
        NavTab("Theory", Icons.Default.MenuBook)
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .border(1.dp, Color(0xFFE2E8F0))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                navTabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(index) },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2563EB),
                            selectedTextColor = Color(0xFF2563EB),
                            indicatorColor = Color(0xFFEFF6FF),
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B)
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Lab Telemetry
                item {
                    val activeCount = viewModel.currentStep?.activeStates?.size ?: 1
                    NfaHeader(
                        onLoadExampleClick = { showPresetsDialog = true },
                        onResetClick = { viewModel.resetSimulation() },
                        onRunClick = { viewModel.runSimulation() },
                        onAboutClick = { showAboutDialog = true },
                        simulationMode = simulationMode,
                        activeStatesCount = activeCount,
                        currentStepIndex = currentStepIndex
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // TAB 0: MAIN DASHBOARD & SIMULATOR
                        // 1. Input String & Simulation Mode
                        item {
                            InputStringCard(
                                inputString = inputString,
                                definition = nfaDefinition,
                                simulationMode = simulationMode,
                                validationError = validationError,
                                onInputChange = { viewModel.setInputString(it) },
                                onModeChange = { viewModel.setSimulationMode(it) },
                                onRunSimulation = { viewModel.runSimulation() },
                                onStepByStep = {
                                    viewModel.runSimulation()
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(4)
                                    }
                                },
                                onReset = { viewModel.resetSimulation() }
                            )
                        }

                        // 2. Physical Automaton Tape Reader
                        item {
                            TapeReaderView(
                                inputString = inputString,
                                currentStepIndex = currentStepIndex,
                                onCellClick = { stepIdx ->
                                    viewModel.jumpToStep(stepIdx)
                                }
                            )
                        }

                        // 3. Graph Visualizer
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
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
                                            Icons.Default.AccountTree,
                                            contentDescription = null,
                                            tint = Color(0xFF2563EB),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "NFA State Transition Graph",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF0F172A)
                                        )
                                    }

                                    val activeStates = viewModel.currentStep?.activeStates ?: emptySet()
                                    Surface(
                                        color = if (activeStates.isNotEmpty()) Color(0xFFEFF6FF) else Color(0xFFFEF2F2),
                                        shape = RoundedCornerShape(8.dp),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(
                                                if (activeStates.isNotEmpty()) Color(0xFFBFDBFE) else Color(0xFFFECACA)
                                            )
                                        )
                                    ) {
                                        Text(
                                            text = if (activeStates.isEmpty()) "Active: ∅ (Dead)" else "Active: {${activeStates.sorted().joinToString(", ")}}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (activeStates.isNotEmpty()) Color(0xFF1D4ED8) else Color(0xFFDC2626)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                NfaGraphVisualizer(
                                    definition = nfaDefinition,
                                    currentStep = viewModel.currentStep,
                                    selectedNode = selectedNode,
                                    onSelectNode = { viewModel.setSelectedNode(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(350.dp)
                                )
                            }
                        }

                        // 4. Step-by-Step Traversal Table
                        item {
                            val steps = simulationResult?.steps ?: emptyList()
                            StepTraversalTable(
                                steps = steps,
                                currentStepIndex = currentStepIndex,
                                isPlaying = isPlaying,
                                playbackSpeed = playbackSpeed,
                                finalStates = nfaDefinition.finalStates,
                                onSelectStep = { viewModel.jumpToStep(it) },
                                onStepPrev = { viewModel.stepPrev() },
                                onStepNext = { viewModel.stepNext() },
                                onTogglePlay = { viewModel.togglePlay() },
                                onSetSpeed = { viewModel.setPlaybackSpeed(it) },
                                onRestart = { viewModel.resetSimulation() }
                            )
                        }

                        // 5. Formal State Transition Matrix
                        item {
                            val activeStates = viewModel.currentStep?.activeStates ?: emptySet()
                            val currentSymbol = viewModel.currentStep?.inputSymbol
                            TransitionMatrixCard(
                                definition = nfaDefinition,
                                activeStates = activeStates,
                                currentSymbol = currentSymbol
                            )
                        }

                        // 6. Acceptance Result & Statistics Card
                        item {
                            AcceptanceCard(
                                result = simulationResult,
                                definition = nfaDefinition
                            )
                        }

                        // 7. Possible Traversal Paths Preview
                        item {
                            val paths = simulationResult?.paths ?: emptyList()
                            PathExplorerView(paths = paths)
                        }
                    }

                    1 -> {
                        // TAB 1: FORMAL CONFIGURATION
                        item {
                            NfaConfigPanel(
                                definition = nfaDefinition,
                                onAddState = { viewModel.addState(it) },
                                onRemoveState = { viewModel.removeState(it) },
                                onAddSymbol = { viewModel.addSymbol(it) },
                                onRemoveSymbol = { viewModel.removeSymbol(it) },
                                onSetStartState = { viewModel.setStartState(it) },
                                onToggleFinalState = { viewModel.toggleFinalState(it) },
                                onAddTransition = { from, sym, to -> viewModel.addTransition(from, sym, to) },
                                onRemoveTransition = { viewModel.removeTransition(it) },
                                onClearNfa = { viewModel.clearNfa() }
                            )
                        }

                        item {
                            val activeStates = viewModel.currentStep?.activeStates ?: emptySet()
                            val currentSymbol = viewModel.currentStep?.inputSymbol
                            TransitionMatrixCard(
                                definition = nfaDefinition,
                                activeStates = activeStates,
                                currentSymbol = currentSymbol
                            )
                        }
                    }

                    2 -> {
                        // TAB 2: PATH EXPLORER
                        item {
                            val paths = simulationResult?.paths ?: emptyList()
                            PathExplorerView(paths = paths)
                        }
                    }

                    3 -> {
                        // TAB 3: THEORY & VIVA PREPARATION
                        item {
                            TheorySection()
                        }
                    }
                }

                // Bottom spacer for comfortable scrolling
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Presets Dialog
    if (showPresetsDialog) {
        PresetsDialog(
            onDismiss = { showPresetsDialog = false },
            onSelectPreset = { preset ->
                viewModel.loadPreset(preset)
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}
