package com.example.model

import java.util.UUID

/**
 * Standard symbol representation for epsilon transition.
 */
const val EPSILON = "ε"

fun isEpsilon(symbol: String): Boolean {
    val trimmed = symbol.trim().lowercase()
    return trimmed == "ε" || trimmed == "e" || trimmed == "eps" || trimmed == "epsilon" || trimmed == "λ"
}

data class NfaState(
    val name: String,
    val isStart: Boolean = false,
    val isFinal: Boolean = false
)

data class NfaTransition(
    val id: String = UUID.randomUUID().toString(),
    val fromState: String,
    val symbol: String,
    val toStates: List<String>
) {
    val normalizedSymbol: String
        get() = if (isEpsilon(symbol)) EPSILON else symbol.trim()
}

data class NfaDefinition(
    val states: List<String>,
    val alphabet: List<String>,
    val startState: String,
    val finalStates: Set<String>,
    val transitions: List<NfaTransition>
)

data class SimulationStep(
    val stepIndex: Int,
    val inputSymbol: String?,
    val activeStates: Set<String>,
    val transitionResults: List<String>,
    val activeTransitions: Set<Pair<String, String>>, // fromState to toState
    val explanation: String,
    val epsilonExpandedStates: Set<String> = emptySet()
)

data class PathStep(
    val fromState: String,
    val symbol: String,
    val toState: String,
    val isEpsilon: Boolean = false
)

data class TraversalPath(
    val id: String = UUID.randomUUID().toString(),
    val steps: List<PathStep>,
    val isAccepted: Boolean,
    val finalState: String,
    val summary: String
)

enum class SimulationMode(val displayName: String, val description: String) {
    PARALLEL(
        "Parallel NFA",
        "Tracks all possible active states simultaneously at each input step (Breadth-First Exploration)."
    ),
    BACKTRACKING(
        "Backtracking",
        "Explores individual paths depth-first, backtracking when dead ends or unaccepted states are met."
    )
}

data class SimulationResult(
    val inputString: String,
    val isAccepted: Boolean,
    val finalActiveStates: Set<String>,
    val finalStatesReached: Set<String>,
    val reason: String,
    val steps: List<SimulationStep>,
    val paths: List<TraversalPath>,
    val executionTimeMs: Long,
    val mode: SimulationMode,
    val totalStatesVisited: Int,
    val maxParallelStates: Int
)

data class NfaPreset(
    val name: String,
    val subtitle: String,
    val description: String,
    val defaultInput: String,
    val definition: NfaDefinition
)
