package com.example.engine

import com.example.model.EPSILON
import com.example.model.NfaDefinition
import com.example.model.NfaPreset
import com.example.model.NfaTransition
import com.example.model.PathStep
import com.example.model.SimulationMode
import com.example.model.SimulationResult
import com.example.model.SimulationStep
import com.example.model.TraversalPath
import com.example.model.isEpsilon
import kotlin.system.measureNanoTime

object NfaSimulator {

    /**
     * Calculates the ε-closure of a given set of states.
     * ε-closure(S) is the set of all states reachable from any state in S
     * by making zero or more ε-transitions.
     */
    fun computeEpsilonClosure(
        initialStates: Set<String>,
        transitions: List<NfaTransition>
    ): Pair<Set<String>, Set<String>> {
        val closure = initialStates.toMutableSet()
        val queue = ArrayDeque(initialStates)
        val addedViaEpsilon = mutableSetOf<String>()

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            val epsTransitions = transitions.filter { it.fromState == curr && it.normalizedSymbol == EPSILON }
            for (t in epsTransitions) {
                for (target in t.toStates) {
                    val trimmed = target.trim()
                    if (trimmed.isNotEmpty() && closure.add(trimmed)) {
                        queue.add(trimmed)
                        addedViaEpsilon.add(trimmed)
                    }
                }
            }
        }
        return Pair(closure, addedViaEpsilon)
    }

    /**
     * Executes the Parallel NFA Traversal Simulation.
     */
    fun simulate(
        definition: NfaDefinition,
        inputString: String,
        mode: SimulationMode = SimulationMode.PARALLEL
    ): SimulationResult {
        var result: SimulationResult
        val timeNano = measureNanoTime {
            val steps = mutableListOf<SimulationStep>()
            val allVisitedStates = mutableSetOf<String>()
            var maxParallel = 0

            // Step 0: Start state & its ε-closure
            val (initialClosure, epsAddedStart) = computeEpsilonClosure(
                setOf(definition.startState),
                definition.transitions
            )
            allVisitedStates.addAll(initialClosure)
            maxParallel = maxOf(maxParallel, initialClosure.size)

            val startExplanation = if (epsAddedStart.isNotEmpty()) {
                "Initial state {${definition.startState}} with ε-closure expanding to {${initialClosure.sorted().joinToString(", ")}}"
            } else {
                "Automaton initialized at start state {${definition.startState}}"
            }

            steps.add(
                SimulationStep(
                    stepIndex = 0,
                    inputSymbol = null,
                    activeStates = initialClosure,
                    transitionResults = listOf("Start at ${definition.startState}" + if (epsAddedStart.isNotEmpty()) " + ε-closure {${epsAddedStart.sorted().joinToString(", ")}}" else ""),
                    activeTransitions = emptySet(),
                    explanation = startExplanation,
                    epsilonExpandedStates = epsAddedStart
                )
            )

            var currentActive = initialClosure

            // Process each input symbol
            for (i in inputString.indices) {
                val symbol = inputString[i].toString()
                val nextDirectStates = mutableSetOf<String>()
                val activeEdges = mutableSetOf<Pair<String, String>>()
                val transitionExplanations = mutableListOf<String>()

                for (state in currentActive) {
                    val matchingTransitions = definition.transitions.filter {
                        it.fromState == state && it.normalizedSymbol == symbol
                    }

                    if (matchingTransitions.isNotEmpty()) {
                        val targets = matchingTransitions.flatMap { it.toStates }.map { it.trim() }.filter { it.isNotEmpty() }
                        if (targets.isNotEmpty()) {
                            nextDirectStates.addAll(targets)
                            targets.forEach { target ->
                                activeEdges.add(Pair(state, target))
                            }
                            transitionExplanations.add("$state ──($symbol)──► {${targets.distinct().sorted().joinToString(", ")}}")
                        } else {
                            transitionExplanations.add("$state has no transition on '$symbol' (path terminated)")
                        }
                    } else {
                        transitionExplanations.add("$state has no transition on '$symbol' (path terminated)")
                    }
                }

                // Compute ε-closure of the newly reached direct states
                val (nextClosure, epsAdded) = computeEpsilonClosure(nextDirectStates, definition.transitions)
                allVisitedStates.addAll(nextClosure)
                maxParallel = maxOf(maxParallel, nextClosure.size)

                val stepExplanation = when {
                    nextClosure.isEmpty() -> "No valid transitions on symbol '$symbol'. All active paths terminated (Dead State)."
                    epsAdded.isNotEmpty() -> "Read symbol '$symbol'. Reached {${nextDirectStates.sorted().joinToString(", ")}}, then ε-closure added {${epsAdded.sorted().joinToString(", ")}}."
                    else -> "Read symbol '$symbol'. Advanced to active states: {${nextClosure.sorted().joinToString(", ")}}."
                }

                steps.add(
                    SimulationStep(
                        stepIndex = i + 1,
                        inputSymbol = symbol,
                        activeStates = nextClosure,
                        transitionResults = if (transitionExplanations.isEmpty()) listOf("No transitions available") else transitionExplanations,
                        activeTransitions = activeEdges,
                        explanation = stepExplanation,
                        epsilonExpandedStates = epsAdded
                    )
                )

                currentActive = nextClosure
            }

            // Acceptance Evaluation
            val reachedFinals = currentActive.filter { definition.finalStates.contains(it) }.toSet()
            val isAccepted = reachedFinals.isNotEmpty()

            val reason = when {
                isAccepted -> "At least one final state (${reachedFinals.sorted().joinToString(", ")}) is active after reading the entire input."
                currentActive.isEmpty() -> "Automaton blocked; no active states remained after processing."
                else -> "None of the final active states {${currentActive.sorted().joinToString(", ")}} belong to the set of accepting/final states {${definition.finalStates.sorted().joinToString(", ")}}."
            }

            // Generate Traversal Paths (Backtracking exploration)
            val generatedPaths = generatePaths(definition, inputString)

            result = SimulationResult(
                inputString = inputString,
                isAccepted = isAccepted,
                finalActiveStates = currentActive,
                finalStatesReached = reachedFinals,
                reason = reason,
                steps = steps,
                paths = generatedPaths,
                executionTimeMs = 0L,
                mode = mode,
                totalStatesVisited = allVisitedStates.size,
                maxParallelStates = maxParallel
            )
        }

        return result.copy(executionTimeMs = maxOf(1L, timeNano / 1_000_000L))
    }

    /**
     * Generates concrete execution paths via DFS / Backtracking to show
     * how individual branches explored the NFA.
     */
    private fun generatePaths(
        definition: NfaDefinition,
        inputString: String,
        maxPaths: Int = 40
    ): List<TraversalPath> {
        val paths = mutableListOf<TraversalPath>()

        fun dfs(
            currState: String,
            inputIdx: Int,
            currentSteps: List<PathStep>,
            epsCycleGuard: Set<String>
        ) {
            if (paths.size >= maxPaths) return

            // If we consumed the whole input string
            if (inputIdx == inputString.length) {
                val isFinal = definition.finalStates.contains(currState)
                val summary = buildString {
                    append(currState)
                    if (isFinal) append(" [FINAL ✓]") else append(" [NOT FINAL ✕]")
                }
                paths.add(
                    TraversalPath(
                        steps = currentSteps,
                        isAccepted = isFinal,
                        finalState = currState,
                        summary = summary
                    )
                )

                // Can we also take ε-transitions at the end?
                val epsTransitions = definition.transitions.filter {
                    it.fromState == currState && it.normalizedSymbol == EPSILON
                }
                for (t in epsTransitions) {
                    for (to in t.toStates) {
                        val trimmed = to.trim()
                        if (trimmed.isNotEmpty() && !epsCycleGuard.contains(trimmed)) {
                            val newStep = PathStep(currState, EPSILON, trimmed, isEpsilon = true)
                            dfs(trimmed, inputIdx, currentSteps + newStep, epsCycleGuard + trimmed)
                        }
                    }
                }
                return
            }

            // 1. Try taking ε-transition before reading the next symbol
            val epsTransitions = definition.transitions.filter {
                it.fromState == currState && it.normalizedSymbol == EPSILON
            }
            for (t in epsTransitions) {
                for (to in t.toStates) {
                    val trimmed = to.trim()
                    if (trimmed.isNotEmpty() && !epsCycleGuard.contains(trimmed)) {
                        val newStep = PathStep(currState, EPSILON, trimmed, isEpsilon = true)
                        dfs(trimmed, inputIdx, currentSteps + newStep, epsCycleGuard + trimmed)
                    }
                }
            }

            // 2. Read the next input symbol
            val symbol = inputString[inputIdx].toString()
            val symTransitions = definition.transitions.filter {
                it.fromState == currState && it.normalizedSymbol == symbol
            }

            if (symTransitions.isEmpty() && epsTransitions.isEmpty()) {
                // Dead end
                paths.add(
                    TraversalPath(
                        steps = currentSteps,
                        isAccepted = false,
                        finalState = currState,
                        summary = "$currState [DEAD END on '$symbol' ✕]"
                    )
                )
                return
            }

            for (t in symTransitions) {
                for (to in t.toStates) {
                    val trimmed = to.trim()
                    if (trimmed.isNotEmpty()) {
                        val newStep = PathStep(currState, symbol, trimmed, isEpsilon = false)
                        dfs(trimmed, inputIdx + 1, currentSteps + newStep, emptySet())
                    }
                }
            }
        }

        dfs(definition.startState, 0, emptyList(), setOf(definition.startState))
        return paths.distinctBy { path ->
            path.steps.joinToString("->") { "${it.fromState}(${it.symbol})${it.toState}" } + path.isAccepted
        }
    }

    /**
     * Standard built-in pre-configured Example NFAs.
     */
    val PRESETS = listOf(
        NfaPreset(
            name = "Ends with '01'",
            subtitle = "Binary strings ending with suffix 01",
            description = "Nondeterministically guesses when the string is near the end. At state q0, on input 0, it can either stay at q0 or branch to q1 anticipating a terminal '1'.",
            defaultInput = "0101",
            definition = NfaDefinition(
                states = listOf("q0", "q1", "q2"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q2"),
                transitions = listOf(
                    NfaTransition(fromState = "q0", symbol = "0", toStates = listOf("q0", "q1")),
                    NfaTransition(fromState = "q0", symbol = "1", toStates = listOf("q0")),
                    NfaTransition(fromState = "q1", symbol = "1", toStates = listOf("q2"))
                )
            )
        ),
        NfaPreset(
            name = "Contains '101'",
            subtitle = "Sub-pattern match anywhere in the stream",
            description = "Maintains a base path in q0. Once '1' is seen, it simultaneously initiates a pattern matcher across q1 -> q2 -> q3.",
            defaultInput = "11010",
            definition = NfaDefinition(
                states = listOf("q0", "q1", "q2", "q3"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q3"),
                transitions = listOf(
                    NfaTransition(fromState = "q0", symbol = "0", toStates = listOf("q0")),
                    NfaTransition(fromState = "q0", symbol = "1", toStates = listOf("q0", "q1")),
                    NfaTransition(fromState = "q1", symbol = "0", toStates = listOf("q2")),
                    NfaTransition(fromState = "q2", symbol = "1", toStates = listOf("q3")),
                    NfaTransition(fromState = "q3", symbol = "0", toStates = listOf("q3")),
                    NfaTransition(fromState = "q3", symbol = "1", toStates = listOf("q3"))
                )
            )
        ),
        NfaPreset(
            name = "Parallel Path Fork (a/b)",
            subtitle = "Demonstrates clean multi-branch parallel exploration",
            description = "Branches on symbol 'a' into two parallel sub-automata: Upper path matching 'ab', Lower path matching 'aa'.",
            defaultInput = "ab",
            definition = NfaDefinition(
                states = listOf("q0", "q1", "q2", "q3", "q4"),
                alphabet = listOf("a", "b"),
                startState = "q0",
                finalStates = setOf("q2", "q4"),
                transitions = listOf(
                    NfaTransition(fromState = "q0", symbol = "a", toStates = listOf("q1", "q3")),
                    NfaTransition(fromState = "q1", symbol = "b", toStates = listOf("q2")),
                    NfaTransition(fromState = "q3", symbol = "a", toStates = listOf("q4")),
                    NfaTransition(fromState = "q4", symbol = "b", toStates = listOf("q4"))
                )
            )
        ),
        NfaPreset(
            name = "ε-Transition Automaton",
            subtitle = "Spontaneous transitions without reading input",
            description = "Illustrates ε-closure. State q0 jumps spontaneously to q1 via an ε-edge, allowing immediate transition into the second component.",
            defaultInput = "100",
            definition = NfaDefinition(
                states = listOf("q0", "q1", "q2", "q3"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q3"),
                transitions = listOf(
                    NfaTransition(fromState = "q0", symbol = "0", toStates = listOf("q0")),
                    NfaTransition(fromState = "q0", symbol = "1", toStates = listOf("q0")),
                    NfaTransition(fromState = "q0", symbol = EPSILON, toStates = listOf("q1")),
                    NfaTransition(fromState = "q1", symbol = "0", toStates = listOf("q2")),
                    NfaTransition(fromState = "q2", symbol = "0", toStates = listOf("q3"))
                )
            )
        ),
        NfaPreset(
            name = "3rd Symbol from End is '1'",
            subtitle = "Classic non-deterministic state efficiency",
            description = "While a DFA requires 8 states (2^3), an NFA recognizes this language in just 4 states by guessing 2 steps ahead.",
            defaultInput = "0110",
            definition = NfaDefinition(
                states = listOf("q0", "q1", "q2", "q3"),
                alphabet = listOf("0", "1"),
                startState = "q0",
                finalStates = setOf("q3"),
                transitions = listOf(
                    NfaTransition(fromState = "q0", symbol = "0", toStates = listOf("q0")),
                    NfaTransition(fromState = "q0", symbol = "1", toStates = listOf("q0", "q1")),
                    NfaTransition(fromState = "q1", symbol = "0", toStates = listOf("q2")),
                    NfaTransition(fromState = "q1", symbol = "1", toStates = listOf("q2")),
                    NfaTransition(fromState = "q2", symbol = "0", toStates = listOf("q3")),
                    NfaTransition(fromState = "q2", symbol = "1", toStates = listOf("q3"))
                )
            )
        )
    )
}
