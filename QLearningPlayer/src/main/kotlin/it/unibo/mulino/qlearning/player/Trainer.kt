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
import it.unibo.filterCellIndexed
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*
import kotlin.collections.List
import kotlin.collections.any
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.joinToString
import kotlin.collections.mutableListOf
import it.unibo.ai.didattica.mulino.domain.State as ExternalState

class Trainer(private val save: Boolean = true) : AIPlayer {

    private val previousState = Stack<State>()

    //<editor-fold desc="Features">
    /*********************
     *      FEATURES     *
     *********************/

    private val miePedineFeature = { state: State, _: Action -> state.whiteBoardCount.toDouble() }
    private val enemyPedineFeature = { state: State, _: Action -> state.blackBoardCount.toDouble() }
    private val chiudoUnMill = { state: State, action: Action ->
        when (state.simulateAction(action).mill) {
            true -> 1.0
            false -> 0.0
        }
    }
    private val avversarioNonSiPuoMuovere = { state: State, _: Action ->
        when (!state.enemyCanMove()) {
            false -> 1.0
            true -> 0.0
        }
    }

    private val ioNonMiPossoMuovere = { state: State, _: Action ->
        when (state.iCanMove()) {
            true -> 1.0
            false -> 0.0
        }
    }

    private val deniedEnemyMill = { state: State, action: Action ->
        val enemyType = when (state.isWhiteTurn) {
            true -> Type.BLACK
            false -> Type.WHITE
        }
        if (state.closeAMill(action.to.get(), enemyType))
            1.0
        else
            0.0
    }

    private val rimuovoPedinaChePuoChiudereUnMill = { state: State, action: Action ->
        var featureValue = 0.0
        val newState = state.simulateAction(action).newState
        if (action.remove.isPresent) {
            // posso rimuove pedina. Controllo che negli spazi adiacenti alla pedina da rimuovere si poteva formare un mill
            // TODO("Nella versione in stato 3 dell'avversario dovrei controllare tutti i possibili mill")
            val enemyType = when (state.isWhiteTurn) {
                true -> Type.BLACK
                false -> Type.WHITE
            }

            val adjs = newState.adjacent(action.remove.get(), true)
                    .filter {
                        it.second == Type.EMPTY
                                && newState.closeAMill(it.first, enemyType)
                    }
            if (adjs.any())
                featureValue += 1.0
        }
        featureValue
    }

    private val mettoLaPedinaInUnPostoChePuoGenerareUnMill = { state: State, action: Action ->
        val myType = when (state.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }

        if (state.adjacent(action.to.get(), false).filter { it.second == myType }.any())
            1.0
        else
            0.0
    }

    private val phase1Features: Array<(State, Action) -> Double> =
            arrayOf(
                    // [0] numero di pedine mie
                    miePedineFeature,
                    // [1] numero di pedine avversarie
                    enemyPedineFeature,
                    // [2] faccio un mill
                    chiudoUnMill,
                    // [3] l'avversario non si può muovere
                    avversarioNonSiPuoMuovere,
                    // [4] io non mi posso muovere
                    ioNonMiPossoMuovere,
                    // [5] impedisco all'avversario di chiudere un mill nel suo turno
                    deniedEnemyMill,
                    // [6] rimozione tattica
                    rimuovoPedinaChePuoChiudereUnMill,
                    // [7] smartPlacing
                    mettoLaPedinaInUnPostoChePuoGenerareUnMill
            )

    private val phase2Features: Array<(State, Action) -> Double> =
            arrayOf(
                    { state, _ -> state.whiteBoardCount.toDouble() },
                    { state, _ -> state.blackBoardCount.toDouble() },
                    { state, action ->
                        when (state.simulateAction(action).mill) {
                            true -> 1.0
                            false -> 0.0
                        }
                    },
                    { state, _ ->
                        when (state.enemyCanMove()) {
                            false -> 1.0
                            true -> 0.0
                        }
                    },
                    { state, _ ->
                        when (!state.iCanMove()) {
                            true -> 1.0
                            false -> 0.0
                        }
                    }
            )

    // TODO("Rimuovo una pedina avversaria che potrebbe chiudere un mill")

    private val phaseFinalFeatures: Array<(State, Action) -> Double> =
            arrayOf(
                    { state, _ -> state.whiteBoardCount.toDouble() },
                    { state, _ -> state.blackBoardCount.toDouble() },
                    { state, action ->
                        when (state.simulateAction(action).mill) {
                            true -> 1.0
                            false -> 0.0
                        }
                    },
                    { state, _ ->
                        when (!state.enemyCanMove()) {
                            false -> 1.0
                            true -> 0.0
                        }
                    },
                    { state, _ ->
                        when (state.iCanMove()) {
                            true -> 1.0
                            false -> 0.0
                        }
                    }
            )

    //</editor-fold

    //<editor-fold desc="Pesi">
    /*********************
     *        PESI       *
     *********************/
    private val phase1Weights = Array(phase1Features.size, { 0.0 })
    private val phase2Weights = Array(phase2Features.size, { 0.0 })
    private val phaseFinalWeights = Array(phaseFinalFeatures.size, { 0.0 })
    //</editor-fold desc="Features">

    //<editor-fold desc="Reward">
    /*********************
     *        REWARD     *
     *********************/
    // TODO("Aggiungere winningState reward")
    private val phase1Reward: (State, Action, State) -> Double = { oldState, action, newState ->

        //println(oldState.toString())
        //println(newState.toString())

        var reward = 0.0
        //val simulation = oldState.simulateAction(action)

        //if(previousState.peek().whiteBoardCount > oldState)

        if (newState.whiteBoardCount > newState.blackBoardCount) {
            reward += 5.0
        }

        when (action.remove.isPresent) {
            true -> reward += 10.0
            else -> reward += -1.0
        }

        val (playerType, enemyType) = when (oldState.isWhiteTurn) {
            true -> Pair(Type.WHITE, Type.BLACK)
            false -> Pair(Type.BLACK, Type.WHITE)
        }

        if (!newState.playerCanMove(playerType)) {
            reward -= 200.0
        }

        if (!newState.playerCanMove(enemyType)) {
            reward += 200.0
        }

        if (newState.blackBoardCount <= 2 && newState.blackHandCount == 0)
            reward += 200.0
        else if (newState.whiteBoardCount <= 2 && newState.whiteHandCount == 0)
            reward -= 200.0

        reward
    }

    /*
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
    }*/
    //</editor-fold desc="Reward">

    //<editor-fold desc="Generatore azioni">
    internal val actionFromStatePhase1: (State) -> List<Action> = {
        //println("Azione tipo 1")
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
            //var removePos = Optional.empty<Position>()
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
        //println("Azione tipo 2")
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
        //println("Azione tipo 3")
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
                            actionList.add(Action.buildPhase3(fromCell, toCell, Optional.empty()))
                        } else {
                            // chiuso mill
                            if (possibleRemove.isEmpty())
                                actionList.add(Action.buildPhase3(fromCell, toCell, Optional.empty()))
                            else {
                                possibleRemove.forEach {
                                    val removeCell = Position(it.first.first, it.first.second)
                                    actionList.add(Action.buildPhase3(fromCell, toCell, Optional.of(removeCell)))
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

    //</editor-fold desc="Features">

    private val applyAction: ((State, Action, State) -> Double) -> (State, Action) -> Pair<Double, State> = {
        val rewardFunction = it
        { state, action ->
            val newState = state.simulateAction(action).newState
            Pair(rewardFunction(state, action, newState), newState)
        }
    }

    private val discount = 0.2
    private val alpha = 0.5
    private val explorationRate = 0.0

    private val learnerPhase1 = ApproximateQLearning<State, Action>({ alpha },
            { discount },
            explorationRate = { explorationRate },
            featureExtractors = phase1Features,
            weights = phase1Weights,
            actionsFromState = actionFromStatePhase1,
            applyAction = applyAction(phase1Reward))

    private val learnerPhase2 = ApproximateQLearning<State, Action>({ alpha },
            { discount },
            explorationRate = { explorationRate },
            featureExtractors = phase1Features,
            weights = phase1Weights,
            actionsFromState = actionFromStatePhase2,
            applyAction = applyAction(phase1Reward))

    private val learnerPhase3 = ApproximateQLearning<State, Action>({ alpha },
            { discount },
            explorationRate = { explorationRate },
            weights = phase1Weights,
            featureExtractors = phase1Features,
            actionsFromState = actionFromStatePhase3,
            applyAction = applyAction(phase1Reward))

    override fun playPhase1(state: ExternalState, playerType: ExternalState.Checker): Phase1Action {
        val internalState = normalize(state, playerType).remapToInternal(playerType)
        val action = learnerPhase1.think(internalState).rempapToExternalPhase1()
        //val weight = StringBuilder()
        //weight.append("[")
        //learnerPhase1.weights.forEach { weight.append("$it ,") }
        //weight.append("]")
        println("Phase 1 : ${learnerPhase1.weights.joinToString(", ", "[", "]")}")
        //previousState.add(internalState)
        return action
    }


    override fun playPhase2(state: ExternalState, playerType: ExternalState.Checker): Phase2Action {
        val internalState = normalize(state, playerType).remapToInternal(playerType)
        val action = learnerPhase2.think(internalState).rempapToExternalPhase2()
        //previousState.add(internalState)
        println("Phase 2 : ${learnerPhase2.weights.joinToString(", ", "[", "]")}")
        return action
    }

    override fun playPhaseFinal(state: ExternalState, playerType: ExternalState.Checker): PhaseFinalAction {
        val internalState = normalize(state, playerType).remapToInternal(playerType)
        val action = learnerPhase3.think(internalState).rempapToExternalPhaseFinal()
        //previousState.add(internalState)
        println("Phase 3 : ${learnerPhase3.weights.joinToString(", ", "[", "]")}")
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
                    val temp = state.whiteCheckers
                    state.whiteCheckers = state.blackCheckers
                    state.blackCheckers = temp
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
                phase2Weights[i] = lines[1 + i + phase1Weights.size].toDouble()
            for (i in phaseFinalWeights.indices)
                phaseFinalWeights[i] = lines[1 + i + phase1Weights.size + phase2Weights.size].toDouble()

        } catch (e: Exception) {
            throw IllegalStateException("File non valido")
        }

        println("Cariati \n")
        println("ph 1 " + phase1Weights.joinToString(", ", "[", "]"))
        println("ph 2 " + phase2Weights.joinToString(", ", "[", "]"))
        println("ph 3 " + phaseFinalWeights.joinToString(", ", "[", "]"))
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
        const val version: Long = 2L
    }


    override fun matchStart() {
        load(true)
    }

    override fun matchEnd() {
        if (save)
            save()
    }
}