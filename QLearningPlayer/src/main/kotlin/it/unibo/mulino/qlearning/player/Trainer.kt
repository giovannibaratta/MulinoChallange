package it.unibo.mulino.qlearning.player

import it.unibo.ai.ApproximateQLearning
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.mulino.player.AIPlayer
import it.unibo.mulino.qlearning.player.model.Action
import it.unibo.mulino.qlearning.player.model.Position
import it.unibo.mulino.qlearning.player.model.State
import it.unibo.mulino.qlearning.player.model.State.Type
import it.unibo.utils.filterCellIndexed
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*
import kotlin.collections.List
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.mutableListOf
import it.unibo.ai.didattica.mulino.domain.State as ExternalState

class Trainer : AIPlayer {

    /*********************
     *      FEATURES     *
     *********************/

    /*********************
     *      FEATURES     *
     *********************/
    private val phase1Features: Array<(State, Action) -> Double> =
            arrayOf(
                    { state, action -> state.whiteCount.toDouble() },
                    { state, action -> state.blackCount.toDouble() },
                    { state, action ->
                        when (state.simulateAction(action).mill) {
                            true -> 1.0
                            false -> 0.0
                        }
                    },
                    { state, action ->
                        when (state.enemyCanMove()) {
                            false -> 1.0
                            true -> 0.0
                        }
                    },
                    { state, action ->
                        when (state.iCanMove()) {
                            true -> 1.0
                            false -> 0.0
                        }
                    }
            )

    private val phase2Features: Array<(State, Action) -> Double> =
            arrayOf(
                    { state, action -> state.whiteCount.toDouble() },
                    { state, action -> state.blackCount.toDouble() },
                    { state, action ->
                        when (state.simulateAction(action).mill) {
                            true -> 1.0
                            false -> 0.0
                        }
                    },
                    { state, action ->
                        when (state.enemyCanMove()) {
                            false -> 1.0
                            true -> 0.0
                        }
                    },
                    { state, action ->
                        when (state.iCanMove()) {
                            true -> 1.0
                            false -> 0.0
                        }
                    }
            )

    private val phaseFinalFeatures: Array<(State, Action) -> Double> =
            arrayOf(
                    { state, action -> state.whiteCount.toDouble() },
                    { state, action -> state.blackCount.toDouble() },
                    { state, action ->
                        when (state.simulateAction(action).mill) {
                            true -> 1.0
                            false -> 0.0
                        }
                    },
                    { state, action ->
                        when (state.enemyCanMove()) {
                            false -> 1.0
                            true -> 0.0
                        }
                    },
                    { state, action ->
                        when (state.iCanMove()) {
                            true -> 1.0
                            false -> 0.0
                        }
                    }
            )

    /*********************
     *        PESI       *
     *********************/
    private val phase1Weights = Array(phase1Features.size, { 0.0 })
    private val phase2Weights = Array(phase2Features.size, { 0.0 })
    private val phaseFinalWeights = Array(phaseFinalFeatures.size, { 0.0 })

    /*********************
     *        REWARD     *
     *********************/
    // TODO("Aggiungere winningState reward")
    private val phase1Reward: (State, Action, State) -> Double = { oldState, action, newState ->
        when (action.remove.isPresent) {
            true -> 1.0
            else -> -0.2
        }
    }

    private val phase2Reward: (State, Action, State) -> Double = { oldState, action, newState ->
        when (action.remove.isPresent) {
            true -> 1.0
            else -> -0.2
        }
    }

    private val phaseFinalReward: (State, Action, State) -> Double = { oldState, action, newState ->
        when (action.remove.isPresent) {
            true -> 1.0
            else -> -0.2
        }
    }

    internal val actionFromStatePhase1: (State) -> List<Action> = {
        val state = it
        val actionList = mutableListOf<Action>()
        val (myType, enemyType) = when (state.isWhiteTurn) {
            true -> Pair(Type.WHITE, Type.BLACK)
            false -> Pair(Type.BLACK, Type.WHITE)
        }

        val possibleRemove = state.grid
                .filterCellIndexed { it == enemyType }
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), enemyType) }

        val emptyCell = it.grid.filterCellIndexed { it == Type.EMPTY }

        emptyCell.forEach {
            val toPos = Position(it.first.first, it.first.second)
            var removePos = Optional.empty<Position>()
            if (state.closeAMill(toPos, myType)) {
                // 1.a
                if (possibleRemove.isEmpty())
                    actionList.add(Action.buildPhase1(toPos, Optional.empty()))
                else
                    possibleRemove.forEach {
                        actionList.add(Action.buildPhase1(toPos, Optional.of(Position(it.first.first, it.first.second))))
                    }
            } else {
                // 1.b
                actionList.add(Action.buildPhase1(toPos, Optional.empty()))
            }
        }

        actionList
        /* per ogni cella vuota
            1.a) se mulino verifico le celle removibili
            1.b) se non è un mulino inserisco solo le celle vuote
        */
    }

    internal val actionFromStatePhase2: (State) -> List<Action> = {
        val state = it
        val actionList = mutableListOf<Action>()
        val (myType, enemyType) = when (state.isWhiteTurn) {
            true -> Pair(Type.WHITE, Type.BLACK)
            false -> Pair(Type.BLACK, Type.WHITE)
        }

        val possibleRemove = state.grid
                .filterCellIndexed { it == enemyType }
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), it.second) }

        it.grid.filterCellIndexed { it == myType }
                .forEach {
                    state.grid[it.first.first, it.first.second] = Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)

                    state.adjacent(fromCell, true)
                            .filter { it.second == Type.EMPTY }
                            .forEach {
                                val toCell = it.first
                                if (!state.closeAMill(toCell, myType)) {
                                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                } else {
                                    if (possibleRemove.isEmpty())
                                        actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                    else {
                                        possibleRemove.forEach {
                                            val removeCell = Position(it.first.first, it.first.second)
                                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(removeCell)))
                                        }
                                    }
                                }
                            }
                    state.grid[it.first.first, it.first.second] = myType
                }
        actionList
        /*
            Preparazione:
            calcolo le celle dell'avversario

            per ogni mia cella
            1) rimuovo la pedina
            2) la metto in una cella vuota adiacente
                2.a) se mulino verifico le celle removibili
                2.b) se non è un mulino inserisco solo le celle vuote
        */
    }

    internal val actionFromStatePhase3: (State) -> List<Action> = {
        val state = it
        val actionList = mutableListOf<Action>()
        val (myType, enemyType) = when (state.isWhiteTurn) {
            true -> Pair(Type.WHITE, Type.BLACK)
            false -> Pair(Type.BLACK, Type.WHITE)
        }

        val possibleRemove = state.grid
                .filterCellIndexed { it == enemyType }
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), it.second) }

        val emptyCell = it.grid.filterCellIndexed { it == Type.EMPTY }

        it.grid.filterCellIndexed { it == myType }
                .forEach {
                    state.grid[it.first.first, it.first.second] = Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)
                    emptyCell.forEach {
                        val toCell = Position(it.first.first, it.first.second)
                        if (!state.closeAMill(toCell, myType)) {
                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                        } else {
                            if (possibleRemove.isEmpty())
                                actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                            else {
                                possibleRemove.forEach {
                                    val removeCell = Position(it.first.first, it.first.second)
                                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(removeCell)))
                                }
                            }
                        }
                    }
                    state.grid[it.first.first, it.first.second] = myType
                }
        actionList
        /*
            Preparazione:
            calcolo le celle vuote
            calcolo le celle dell'avversario

            per ogni mia cella
            1) rimuovo la pedina
            2) la metto in una cella vuota
                2.a) se mulino verifico le celle removibili
                2.b) se non è un mulino inserisco solo le celle vuote
        */
    }

    private val applyAction: ((State, Action, State) -> Double) -> (State, Action) -> Pair<Double, State> = {
        val rewardFunction = it
        { state, action ->
            val newState = state.simulateAction(action).newState
            Pair(rewardFunction(state, action, newState), newState)
        }
    }

    private val learnerPhase1 = ApproximateQLearning<State, Action>(0.9,
            0.01,
            featureExtractors = phase1Features,
            weights = phase1Weights,
            actionsFromState = actionFromStatePhase1,
            applyAction = applyAction(phase1Reward))

    private val learnerPhase2 = ApproximateQLearning<State, Action>(0.9,
            0.01,
            featureExtractors = phase2Features,
            weights = phase2Weights,
            actionsFromState = actionFromStatePhase2,
            applyAction = applyAction(phase2Reward))

    private val learnerPhase3 = ApproximateQLearning<State, Action>(0.9,
            0.01,
            weights = phaseFinalWeights,
            featureExtractors = phaseFinalFeatures,
            actionsFromState = actionFromStatePhase3,
            applyAction = applyAction(phaseFinalReward))

    override fun playPhase1(state: ExternalState, playerType: ExternalState.Checker): Phase1Action {
        val action = learnerPhase1.think(normalize(state, playerType).remapToInternal(playerType)).rempapToExternalPhase1()
        val weight = StringBuilder()
        weight.append("[")
        learnerPhase1.weights.forEach { weight.append("$it ,") }
        weight.append("]")
        println("Phase 1 : $weight")
        return action
    }


    override fun playPhase2(state: ExternalState, playerType: ExternalState.Checker): Phase2Action {
        val action = learnerPhase2.think(normalize(state, playerType).remapToInternal(playerType)).rempapToExternalPhase2()
        val weight = StringBuilder()
        weight.append("[")
        learnerPhase2.weights.forEach { weight.append("$it ,") }
        weight.append("]")
        println("Phase 2 : $weight")
        return action
    }

    override fun playPhaseFinal(state: ExternalState, playerType: ExternalState.Checker): PhaseFinalAction {
        val action = learnerPhase3.think(normalize(state, playerType).remapToInternal(playerType)).rempapToExternalPhaseFinal()
        val weight = StringBuilder()
        weight.append("[")
        learnerPhase3.weights.forEach { weight.append("$it ,") }
        weight.append("]")
        println("Phase 3 : $weight")
        return action
    }

    private fun normalize(state: ExternalState, playerType: ExternalState.Checker): ExternalState =
            when (playerType) {
                ExternalState.Checker.WHITE -> state
                ExternalState.Checker.BLACK -> {
                    val newBoard = HashMap<String, ExternalState.Checker>()
                    state.board.forEach { key, value ->
                        newBoard.put(key, when (value) {
                            ExternalState.Checker.BLACK -> ExternalState.Checker.WHITE
                            ExternalState.Checker.WHITE -> ExternalState.Checker.BLACK
                            ExternalState.Checker.EMPTY -> ExternalState.Checker.EMPTY
                            null -> throw IllegalArgumentException("null checker type")
                        })
                    }
                    state.board = newBoard
                    state
                }
                else -> throw IllegalArgumentException("Tipo di giocatore non valido")
            }

    private fun ExternalState.remapToInternal(checker: ExternalState.Checker) =
            State(this, true/*when (checker) {
                Type.WHITE -> true
                Type.BLACK -> false
                else -> throw IllegalArgumentException("Tipo non valido")
            }*/)

    private fun Action.rempapToExternalPhase1(): Phase1Action {
        if (this.from.isPresent || !this.to.isPresent)
            throw IllegalStateException("La mossa non può essere convertita")
        val action = Phase1Action()
        action.putPosition = this.to.get().toExternal()
        if (this.remove.isPresent)
            action.removeOpponentChecker = this.remove.get().toExternal()
        return action
    }

    private fun Action.rempapToExternalPhase2(): Phase2Action {
        if (!this.from.isPresent || !this.to.isPresent)
            throw IllegalStateException("La mossa non può essere convertita")
        val action = Phase2Action()
        action.from = this.from.get().toExternal()
        action.to = this.to.get().toExternal()
        if (this.remove.isPresent)
            action.removeOpponentChecker = this.remove.get().toExternal()
        return action
    }

    private fun Action.rempapToExternalPhaseFinal(): PhaseFinalAction {
        if (!this.from.isPresent || !this.to.isPresent)
            throw IllegalStateException("La mossa non può essere convertita")
        val action = PhaseFinalAction()
        action.from = this.from.get().toExternal()
        action.to = this.to.get().toExternal()
        if (this.remove.isPresent)
            action.removeOpponentChecker = this.remove.get().toExternal()
        return action
    }

    private fun Position.toExternal(): String {
        val xPos: Char = 'a' + this.x
        val yPos: String = (this.y + 1).toString()
        return xPos + yPos
    }

    fun load(noError: Boolean) {
        val saveFile = File("weights")
        if (!saveFile.exists()) {
            if (noError)
                return
            throw IllegalStateException("File non esiste")
        }

        val isr = FileReader(saveFile)
        val lines = isr.readLines()
        isr.close()

        if (lines.size != (1 + phase1Weights.size + phase2Weights.size + phaseFinalWeights.size))
            throw IllegalStateException("File non valido")

        try {
            val versionRead = lines[0].toLong()
            if (versionRead != version)
                throw IllegalStateException("File non valido")
            for (i in phase1Weights.indices)
                phase1Weights[i] = lines[1 + i].toDouble()
            for (i in phase2Weights.indices)
                phase1Weights[i] = lines[1 + i + phase1Weights.size].toDouble()
            for (i in phaseFinalWeights.indices)
                phase1Weights[i] = lines[1 + i + phase1Weights.size + phase2Weights.size].toDouble()

        } catch (e: Exception) {
            throw IllegalStateException("File non valido")
        }

        println("Cariati \n")
        println("ph 1 " + phase1Weights)

        println("ph 2 " + phase2Weights)

        println("ph 3 " + phaseFinalWeights)
    }

    fun save() {
        val saveFile = File("weights")
        if (saveFile.exists()) {
            saveFile.delete()
        }
        saveFile.createNewFile()
        val os = BufferedOutputStream(FileOutputStream(saveFile)).bufferedWriter()
        os.write(version.toString() + System.lineSeparator())
        phase1Weights.forEach { os.write(it.toString() + System.lineSeparator()) }
        phase2Weights.forEach { os.write(it.toString() + System.lineSeparator()) }
        phaseFinalWeights.forEach { os.write(it.toString() + System.lineSeparator()) }
        os.close()
    }


    companion object {
        const val version: Long = 1L
    }


    override fun matchStart() {
        load(true)
    }

    override fun matchEnd() {
        save()
    }
}