package it.unibo.mulino.qlearning.player

import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.mulino.ai.ApproximateQLearning
import it.unibo.mulino.player.AIPlayer
import it.unibo.mulino.qlearning.player.model.Action
import it.unibo.mulino.qlearning.player.model.ActionResult
import it.unibo.mulino.qlearning.player.model.Position
import it.unibo.mulino.qlearning.player.model.State
import it.unibo.mulino.qlearning.player.model.State.Type
import it.unibo.utils.Cache
import it.unibo.utils.Matrix
import it.unibo.utils.filterCellIndexed
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.*
import kotlin.collections.List
import kotlin.collections.any
import kotlin.collections.count
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.joinToString
import kotlin.collections.mutableListOf
import it.unibo.ai.didattica.mulino.domain.State as ExternalState

class QLearningPlayer(private val save: Boolean = true,
                      private val explorationRate: () -> Double,
                      private val alpha: () -> Double) : AIPlayer {

    private val previousState = Stack<State>()
    private var numeroSimulazioni = 0

    //<editor-fold desc="DEBUG">
    private var reward1Time = 0L
    private var reward2Time = 0L
    private var reward1Count = 0L
    private var reward2Count = 0L
    private var actionState1Time = 0L
    private var actionState2Time = 0L
    private var actionState3Time = 0L
    private var actionState1Count = 0L
    private var actionState2Count = 0L
    private var actionState3Count = 0L
    //</editor-fold desc="DEBUG">

    //<editor-fold desc="Features">
    /*********************
     *      FEATURES     *
     *********************/

    // TODO("Numero di caselle adiacenti libere disponibili")

    private val bias = { _: State, _: Action, _: State -> 1.0 }
    internal val chiudoUnMill = { oldState: State, action: Action, _: State ->
        when (oldState.simulateWithCache(action).mill) {
            true -> 0.4
            false -> 0.0
        }
    }
    internal val winningState = { oldState: State, action: Action, _: State ->
        when (oldState.simulateWithCache(action).winState) {
            true -> 2.0
            false -> 0.0
        }
    }

    internal val numeroPedineSpostabili = { _: State, _: Action, newState: State ->
        var count = 0
        newState.grid.forEachIndexed { xIndex, yIndex, value ->
            if (value == Type.WHITE) {
                count += newState.adjacent(Position(xIndex, yIndex), true).count { it.second == Type.EMPTY }
            }
        }
        count.toDouble() / 10
    }
    internal val numeroPedineSpostabiliAvversario = { _: State, _: Action, newState: State ->
        var count = 0
        newState.grid.forEachIndexed { xIndex, yIndex, value ->
            if (value == Type.BLACK) {
                count += newState.adjacent(Position(xIndex, yIndex), true).count { it.second == Type.EMPTY }
            }
        }
        -count.toDouble() / 10
    }

    internal val deniedEnemyMillPositive = 0.8
    internal val deniedEnemyMillNegative = 0.0

    internal val deniedEnemyMill = { oldState: State, action: Action, _: State ->
        val enemyType = when (oldState.isWhiteTurn) {
            true -> Type.BLACK
            false -> Type.WHITE
        }
        if (oldState.closeAMill(action.to.get(), enemyType).first)
            deniedEnemyMillPositive
        else
            deniedEnemyMillNegative
    }
    // solo fase 1 e 2
    internal val rimuovoPedinaChePuoChiudereUnMill = { oldState: State, action: Action, newState: State ->
        var featureValue = 0.0
        if (action.remove.isPresent) {
            // posso rimuove pedina. Controllo che negli spazi adiacenti alla pedina da rimuovere si poteva formare un mill
            val enemyType = when (oldState.isWhiteTurn) {
                true -> Type.BLACK
                false -> Type.WHITE
            }

            if (newState.adjacent(action.remove.get(), true).any {
                        it.second == Type.EMPTY && newState.closeAMill(it.first, enemyType).first
                    })
                featureValue += 0.5
        }
        featureValue
    }
    internal val mettoLaPedinaInUnPostoChePuoGenerareUnMill = { oldState: State, action: Action, newState: State ->
        val myType = when (oldState.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }

        when (newState.adjacent(action.to.get()).any { it.second == myType }) {
            true -> 0.3 // ho altre mie pedine vicino
            false -> 0.0
        }
    }
    internal val enemyOpenMorris = { oldState: State, _: Action, newState: State ->
        val enemyType = when (oldState.isWhiteTurn) {
            true -> Type.BLACK
            false -> Type.WHITE
        }
        var count = 0.0
        newState.grid.forEachIndexed { xIndex, yIndex, type ->
            if (type == Type.EMPTY && newState.closeAMill(Position(xIndex, yIndex), enemyType).first)
                count++
        }
        -count / 10
    }

    //internal val myOpenMorrisValue =
    internal val myOpenMorrisMultiplier = 0.1
    internal val myOpenMorris = { oldState: State, _: Action, newState: State ->
        val myType = when (oldState.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }
        var count = 0.0
        val multiplier = when (newState.gamePhase() == State.GamePhase.PHASE2) {
            true -> 0.5
            false -> 1.0
        }
        newState.grid.forEachIndexed { xIndex, yIndex, type ->
            if (type == Type.EMPTY && newState.closeAMill(Position(xIndex, yIndex), myType).first)
                count++
        }
        count * myOpenMorrisMultiplier * multiplier
    }

    internal val enemyCanWinPositive = -2.0
    internal val enemyCanWinNegative = 0.0

    internal val enemyCanWin = { _: State, _: Action, newState: State ->

        val invertedState = newState.invert()

        val actions = when (invertedState.gamePhase()) {
            State.GamePhase.PHASE1 -> actionFromStatePhase1(invertedState)
            State.GamePhase.PHASE2 -> actionFromStatePhase2(invertedState)
            State.GamePhase.PHASE3 -> actionFromStatePhase3(invertedState)
        }

        if (actions.any { invertedState.simulateWithCache(it).winState }) enemyCanWinPositive
        else enemyCanWinNegative
    }

    internal val struttureAdLMultiplier = 0.1

    internal val struttureAdL = { oldState: State, _: Action, newState: State ->
        val myType = when (oldState.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }

        newState.numberOfLBlocks(myType) * struttureAdLMultiplier
    }

    internal val parallelStructures = { oldState: State, _: Action, newState: State ->
        val myType = when (oldState.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }

        newState.parallelStructures(myType) / 10.0
    }

    internal val pezziDa2 = { oldState: State, action: Action, newState: State ->
        val myType = when (oldState.isWhiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
        }
        if (newState.adjacent(action.to.get(), false).any { it == myType })
            0.3
        else
            0.0
    }

    private val featuresPhase12: Array<(State, Action, State) -> Double> =
            arrayOf(
                    bias, //0
                    chiudoUnMill, //1
                    deniedEnemyMill, //2
                    rimuovoPedinaChePuoChiudereUnMill, //3
                    mettoLaPedinaInUnPostoChePuoGenerareUnMill, //4
                    winningState, //5
                    numeroPedineSpostabili, //6
                    myOpenMorris, //7
                    struttureAdL, //8
                    parallelStructures, // 9
                    pezziDa2,
                    //numeroPedineSpostabiliAvversario, //10
                    //enemyOpenMorris, // 11
                    enemyCanWin //12
            )

    private val featuresPhase3: Array<(State, Action, State) -> Double> =
            arrayOf(
                    bias,
                    enemyCanWin,
                    chiudoUnMill,
                    deniedEnemyMill,
                    mettoLaPedinaInUnPostoChePuoGenerareUnMill,
                    winningState,

                    pezziDa2,
                    //numeroPedineSpostabili,
                    //numeroPedineSpostabiliAvversario,
                    //enemyOpenMorris,
                    myOpenMorris
            )
    //</editor-fold

    //<editor-fold desc="Pesi">
    /*********************
     *        PESI       *
     *********************/
    private val phase1Weights = Array(featuresPhase12.size, { 0.0 })
    private val phase2Weights = Array(featuresPhase12.size, { 0.0 })
    private val phaseFinalWeights = Array(featuresPhase3.size, { 0.0 })
    //</editor-fold desc="Features">
    //TODO("Reward blocco struttura ad L")
    //<editor-fold desc="Reward">
    /*********************
     *        REWARD     *
     *********************/
    private val phase1Reward = { oldState: State, action: Action, newState: State ->

        var reward = 0.0

        val (playerType, enemyType) = when (oldState.isWhiteTurn) {
            true -> Pair(Type.WHITE, Type.BLACK)
            false -> Pair(Type.BLACK, Type.WHITE)
        }

        if (newState.adjacent(action.to.get(), false).any { it == playerType })
            reward += 0.5

        var differenceEnemyMorris = 0.0
        var differenceMossePossibiliAvversario = 0.0
        //var newEnemyOpenMorris = 0.0
        for (row in 0 until oldState.grid.rows) {
            for (column in 0 until oldState.grid.columns) {
                val oldCellType = oldState.grid[row, column]
                val newCellType = newState.grid[row, column]
                val position = Position(row, column)

                if (oldCellType == Type.EMPTY && oldState.closeAMill(position, enemyType).first)
                    differenceEnemyMorris++
                if (newCellType == Type.EMPTY && newState.closeAMill(position, enemyType).first)
                    differenceEnemyMorris--

                if (oldCellType == enemyType)
                    differenceMossePossibiliAvversario += oldState.adjacent(position, true).count { it.second == enemyType }
                if (newCellType == enemyType)
                    differenceMossePossibiliAvversario -= newState.adjacent(position, true).count { it.second == enemyType }
            }
        }

        // non ho chiuso un morris aperto
        if (differenceEnemyMorris <= 0)
            reward -= 10.0

        if (differenceMossePossibiliAvversario <= 0)
            reward -= 1.25

        /*
        newState.grid.forEachIndexed { xIndex, yIndex, value ->
            if (value == Type.BLACK) {
                count += newState.adjacent(Position(xIndex, yIndex), true).count { it.second == Type.EMPTY }
            }
        }*/

        /*
        oldState.grid.forEachIndexed { xIndex, yIndex, type ->
            if (type == Type.EMPTY && newState.closeAMill(Position(xIndex, yIndex), enemyType).first)
                oldEnemyOpenMorris++
        }
        newState.grid.forEachIndexed { xIndex, yIndex, type ->
            if (type == Type.EMPTY && newState.closeAMill(Position(xIndex, yIndex), enemyType).first)
                newEnemyOpenMorris++
        }*/

        if (previousState.contains(newState)) {
            if (newState.gamePhase() == State.GamePhase.PHASE3)
                reward += 25.0 // punto al pareggio
            else
                reward -= 25.0 // evito il pareggio
        }



        when (action.remove.isPresent) {
            true -> { // chiuso un mill
                reward += 5.0
                if (newState.closeAMill(action.remove.get(), playerType).first)
                    reward += 5.0 // bonus nel caso libero una pedina delle mie
            }
        //else -> reward += -0.2
        }

        if (oldState.numberOfLBlocks(playerType) < newState.numberOfLBlocks(playerType))
            reward += 0.6
        //else if (oldState.numberOfLBlocks(playerType) > newState.numberOfLBlocks(playerType))
        //    reward -= 0.15

        if (oldState.parallelStructures(playerType) < newState.parallelStructures(playerType))
            reward += 5.0
        //else if (oldState.parallelStructures(playerType) > newState.parallelStructures(playerType))
        //    reward -= 0.15

        // reward per mill bloccato
        if (oldState.closeAMill(action.to.get(), enemyType).first)
            reward += 7.0

        // reward per strutture vicine
        if (newState.adjacent(action.to.get(), false).filter { it == playerType }.any())
            reward += 0.15
        //else  reward -= 0.1

        /*
        // reward partita persa per soffocamento
        if (!newState.playerCanMove(playerType)) {
            reward -= 25.0
        }*/


        // OLD REWARD
        // reward partita vinta per soffocamento
        if (!newState.playerCanMove(enemyType)) {
            reward += 25.0
        }

        // reward partita vinta per kill
        if (newState.blackBoardCount() <= 2 && newState.blackHandCount == 0)
            reward += 25.0
/*
        val invertedState = newState.invert()

        val actions = when (invertedState.gamePhase()) {
            State.GamePhase.PHASE1 -> actionFromStatePhase1(invertedState)
            State.GamePhase.PHASE2 -> actionFromStatePhase2(invertedState)
            State.GamePhase.PHASE3 -> actionFromStatePhase3(invertedState)
        }

        // partita persa con una mossa dell'avversario
        if (actions.any { invertedState.simulateWithCache(it).winState }) reward -= 25.0
        */

        reward += numeroSimulazioni(value = -30.0, number = 2, state = newState.invert())

        reward
    }

    private fun numeroSimulazioni(value: Double, number: Int, state: State): Double {
        if (number == 0) return 0.0

        val actions = when (state.gamePhase()) {
            State.GamePhase.PHASE1 -> actionFromStatePhase1(state)
            State.GamePhase.PHASE2 -> actionFromStatePhase2(state)
            State.GamePhase.PHASE3 -> actionFromStatePhase3(state)
        }

        for (action in actions) {
            val result = state.simulateAction(action)
            if (result.winState) return value
            val simulazione = numeroSimulazioni(-value / 2, number - 1, state.invert())
            if (simulazione != 0.0)
                return simulazione
        }

        return 0.0
        /*
        return when(newState.winState) {
            true -> value
            false -> {
                val invertedState = newState.newState.invert()

                val actions = when (invertedState.gamePhase()) {
                    State.GamePhase.PHASE1 -> actionFromStatePhase1(invertedState)
                    State.GamePhase.PHASE2 -> actionFromStatePhase2(invertedState)
                    State.GamePhase.PHASE3 -> actionFromStatePhase3(invertedState)
                }

                for (action in actions){
                    val result = invertedState.simulateAction(action)
                    if(result.winState) return value
                    else numeroSimulazioni(-value/2,number-1,result.newState,)
                }
                //actions.any { invertedState.simulateAction(action).winState }

                //actions.map { invertedState.simulateAction(action).winState/*numeroSimulazioni(-value/2, number-1, invertedState, it)*/ }.max() ?: 0.0
            }
        }*/
    }


    private val phase2Reward = { oldState: State, action: Action, newState: State ->

        var reward = phase1Reward(oldState, action, newState)

        val invertedState = newState.invert()

        val actions = when (invertedState.gamePhase()) {
            State.GamePhase.PHASE1 -> actionFromStatePhase1(invertedState)
            State.GamePhase.PHASE2 -> actionFromStatePhase2(invertedState)
            State.GamePhase.PHASE3 -> actionFromStatePhase3(invertedState)
        }

        // passo dalla fase 2 alla fase 3, sto per perdere
        if (actions.any {
                    invertedState.simulateWithCache(it)
                            .newState
                            .gamePhase() == State.GamePhase.PHASE3
                })
            reward -= 5.0
        reward
    }

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
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), enemyType).first }

        val emptyCell = it.grid.filterCellIndexed { it == Type.EMPTY }


        for (emptyCellIndex in emptyCell.indices) {
            val toPos = Position(emptyCell[emptyCellIndex].first.first, emptyCell[emptyCellIndex].first.second)
            //var removePos = Optional.empty<Position>()
            if (state.closeAMill(toPos, myType).first) {
                // 1.a
                if (possibleRemove.isEmpty()) {
                    assert(state.isWhiteTurn)
                    if (state.blackBoardCount() == 0)
                        actionList.add(Action.buildPhase1(toPos, Optional.empty()))
                    else {
                        state.grid.forEachIndexed { rIndex, cIndex, value ->
                            if (value == enemyType)
                                actionList.add(Action.buildPhase1(toPos, Optional.of(Position(rIndex, cIndex))))
                        }
                    }
                } else {
                    for (possibleRemoveIndex in possibleRemove.indices) {
                        actionList.add(Action.buildPhase1(toPos, Optional.of(Position(possibleRemove[possibleRemoveIndex].first.first, possibleRemove[possibleRemoveIndex].first.second))))
                    }
                    /*
                        possibleRemove.forEach {
                            actionList.add(Action.buildPhase1(toPos, Optional.of(Position(it.first.first, it.first.second))))
                        }*/
                }
            } else {
                // 1.b
                actionList.add(Action.buildPhase1(toPos, Optional.empty()))
            }
        }

        /*
        emptyCell.forEach {
            val toPos = Position(it.first.first, it.first.second)
            //var removePos = Optional.empty<Position>()
            if (state.closeAMill(toPos, myType).first) {
                // 1.a
                if (possibleRemove.isEmpty()) {
                    assert(state.isWhiteTurn)
                    if (state.blackBoardCount() == 0)
                        actionList.add(Action.buildPhase1(toPos, Optional.empty()))
                    else {
                        state.grid.forEachIndexed { rIndex, cIndex, value ->
                            if (value == enemyType)
                                actionList.add(Action.buildPhase1(toPos, Optional.of(Position(rIndex, cIndex))))
                        }
                    }
                } else
                    possibleRemove.forEach {
                        actionList.add(Action.buildPhase1(toPos, Optional.of(Position(it.first.first, it.first.second))))
                    }
            } else {
                // 1.b
                actionList.add(Action.buildPhase1(toPos, Optional.empty()))
            }
        }*/

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
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), it.second).first }


        /*
        val myFilteredCell = it.grid.filterCellIndexed { it == myType }
        for(myFilteredCellIndex in myFilteredCell.indices){
            state.grid[myFilteredCell[myFilteredCellIndex].first.first, myFilteredCell[myFilteredCellIndex].first.second] = Type.EMPTY
            val fromCell = Position(myFilteredCell[myFilteredCellIndex].first.first, myFilteredCell[myFilteredCellIndex].first.second)
            val adj = state.adjacent(fromCell, true).filter { it.second == Type.EMPTY }

            for(adjIndedx in adj.indices){
                val toCell = adj[adjIndedx].first
                if (!state.closeAMill(toCell, myType).first) {
                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                } else {
                    if (possibleRemove.isEmpty()) {
                        assert(state.isWhiteTurn)
                        if (state.blackBoardCount() == 0)
                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                        else {
                            state.grid.forEachIndexed { rIndex, cIndex, value ->
                                if (value == enemyType)
                                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(Position(rIndex, cIndex))))
                            }
                        }
                        //actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                    } else {
                        for(possibleRemoveIndex in possibleRemove.indices){
                            val removeCell = Position(possibleRemove[possibleRemoveIndex].first.first, possibleRemove[possibleRemoveIndex].first.second)
                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(removeCell)))
                        }
                        /*
                        possibleRemove.forEach {
                            val removeCell = Position(it.first.first, it.first.second)
                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(removeCell)))
                        }*/
                    }
                }
            }

            state.grid[myFilteredCell[myFilteredCellIndex].first.first, myFilteredCell[myFilteredCellIndex].first.second] = myType
        }
        */


        it.grid.filterCellIndexed { it == myType }
                .forEach {
                    state.grid[it.first.first, it.first.second] = Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)

                    state.adjacent(fromCell, true)
                            .filter { it.second == Type.EMPTY }
                            .forEach {
                                val toCell = it.first
                                if (!state.closeAMill(toCell, myType).first) {
                                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                } else {
                                    if (possibleRemove.isEmpty()) {
                                        assert(state.isWhiteTurn)
                                        if (state.blackBoardCount() == 0)
                                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                        else {
                                            state.grid.forEachIndexed { rIndex, cIndex, value ->
                                                if (value == enemyType)
                                                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(Position(rIndex, cIndex))))
                                            }
                                        }
                                        //actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                    } else {
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
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), it.second).first }

        val emptyCell = it.grid.filterCellIndexed { it == Type.EMPTY }

        it.grid.filterCellIndexed { it == myType }
                .forEach {
                    state.grid[it.first.first, it.first.second] = Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)
                    emptyCell.forEach {
                        val toCell = Position(it.first.first, it.first.second)
                        if (!state.closeAMill(toCell, myType).first) {
                            actionList.add(Action.buildPhase3(fromCell, toCell, Optional.empty()))
                        } else {
                            // chiuso mill
                            if (possibleRemove.isEmpty()) {
                                assert(state.isWhiteTurn)
                                if (state.blackBoardCount() == 0)
                                    actionList.add(Action.buildPhase3(fromCell, toCell, Optional.empty()))
                                else {
                                    state.grid.forEachIndexed { rIndex, cIndex, value ->
                                        if (value == enemyType)
                                            actionList.add(Action.buildPhase3(fromCell, toCell, Optional.of(Position(rIndex, cIndex))))
                                    }
                                }
                                //actionList.add(Action.buildPhase3(fromCell, toCell, Optional.empty()))
                            } else {
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
            val newState = state.simulateWithCache(action).newState
            Pair(rewardFunction(state, action, newState), newState)
        }
    }

    private val discount = 0.99

    private val learnerPhase1 = ApproximateQLearning<State, Action>(/*{ 1.0/(numeroSimulazioni+1)*/alpha,
            { discount },
            explorationRate = explorationRate,
            featureExtractors = featuresPhase12,
            weights = phase1Weights,
            actionsFromState = { s ->
                val startTime = System.nanoTime()
                val actions = actionFromStatePhase1(s)
                val endTime = System.nanoTime()
                actionState1Count++
                actionState1Time = endTime - startTime
                actions
            },
            applyAction = applyAction({ s1, a, s2 ->
                val startTime = System.nanoTime()
                val rew = phase1Reward(s1, a, s2)
                val endTime = System.nanoTime()
                reward1Count++
                reward1Time = endTime - startTime
                rew
            }
            ))

    private val learnerPhase2 = ApproximateQLearning<State, Action>(/*{ 1.0/(numeroSimulazioni+1)*/ alpha,
            { discount },
            explorationRate = explorationRate,
            featureExtractors = featuresPhase12,
            weights = phase2Weights,
            actionsFromState = { s ->
                val startTime = System.nanoTime()
                val actions = actionFromStatePhase2(s)
                val endTime = System.nanoTime()
                actionState2Count++
                actionState2Time = endTime - startTime
                actions
            },
            applyAction = applyAction({ s1, a, s2 ->
                val startTime = System.nanoTime()
                val rew = phase2Reward(s1, a, s2)
                val endTime = System.nanoTime()
                reward2Count++
                reward2Time = endTime - startTime
                rew
            }))

    private val learnerPhase3 = ApproximateQLearning<State, Action>(/*{ 1.0/(numeroSimulazioni+1) }*/ alpha,
            { discount },
            explorationRate = explorationRate,
            weights = phaseFinalWeights,
            featureExtractors = featuresPhase3,
            actionsFromState = { s ->
                val startTime = System.nanoTime()
                val actions = actionFromStatePhase3(s)
                val endTime = System.nanoTime()
                actionState3Count++
                actionState3Time = endTime - startTime
                actions
            },
            applyAction = applyAction({ s1, a, s2 ->
                val startTime = System.nanoTime()
                val rew = phase1Reward(s1, a, s2)
                val endTime = System.nanoTime()
                reward1Count++
                reward1Time = endTime - startTime
                rew
            }))

    override fun playPhase1(state: ExternalState, playerType: ExternalState.Checker): Phase1Action {
        val internalState = normalize(state, playerType).remapToInternal()
        val action = learnerPhase1.think(internalState).rempapToExternalPhase1()
        //val weight = StringBuilder()
        //weight.append("[")
        //learnerPhase1.weights.forEach { weight.append("$it ,") }
        //weight.append("]")
        println("Phase 1 : ${learnerPhase1.weights.joinToString(", ", "[", "]")}")
        previousState.add(internalState)
        numeroSimulazioni++
        return action
    }


    override fun playPhase2(state: ExternalState, playerType: ExternalState.Checker): Phase2Action {
        val internalState = normalize(state, playerType).remapToInternal()
        val action = learnerPhase2.think(internalState).rempapToExternalPhase2()
        previousState.add(internalState)
        println("Phase 2 : ${learnerPhase2.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++
        return action
    }

    override fun playPhaseFinal(state: ExternalState, playerType: ExternalState.Checker): PhaseFinalAction {
        val internalState = normalize(state, playerType).remapToInternal()
        val action = learnerPhase3.think(internalState).rempapToExternalPhaseFinal()
        previousState.add(internalState)
        println("Phase 3 : ${learnerPhase3.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++
        return action
    }

    internal fun playPhase1(state: State) {
        learnerPhase1.think(state)

        //println("[alpha : ${alpha}] Phase 1 : ${learnerPhase1.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++

    }

    internal fun playPhase2(state: State) {
        learnerPhase2.think(state)

        //println("[alpha : ${alpha}] Phase 2 : ${learnerPhase2.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++
    }

    internal fun playPhase3(state: State) {
        learnerPhase3.think(state)
        //println("[alpha : ${alpha}] Phase 3 : ${learnerPhase3.weights.joinToString(", ", "[", "]")}")
        numeroSimulazioni++
    }

    internal fun printPar() {
        val sb = StringBuilder()
        sb.append("Stat :\n")
        sb.append("Alpha : ${alpha()}\n")
        sb.append("Cache hit : ${(cache.hits.toDouble() / cache.total) * 100}% , dimensioni ${cache.size}\n")
        if (reward1Count > 0)
            sb.append("Reward 1 : Total Call - ${reward1Count}, Avg Time - ${(reward1Time / (reward1Count * 1000.0))}us\n")
        if (reward2Count > 0)
            sb.append("Reward 2 : Total Call - ${reward2Count}, Avg Time - ${(reward2Time / (reward2Count * 1000.0))}us\n")
        if (actionState1Count > 0)
            sb.append("Action 1 : Total Call - ${actionState1Count}, Avg Time - ${(actionState1Time / (actionState1Count * 1000.0))}us\n")
        if (actionState2Count > 0)
            sb.append("Action 2 : Total Call - ${actionState2Count}, Avg Time - ${(actionState2Time / (actionState2Count * 1000.0))}us\n")
        if (actionState3Count > 0)
            sb.append("Action 3 : Total Call - ${actionState3Count}, Avg Time - ${(actionState3Time / (actionState3Count * 1000.0))}us\n")
        sb.append("Weights 1 : ${learnerPhase1.weights.joinToString(", ", "[", "]")}\n")
        sb.append("Weights 2 : ${learnerPhase2.weights.joinToString(", ", "[", "]")}\n")
        sb.append("Weights 3 : ${learnerPhase3.weights.joinToString(", ", "[", "]")}\n")
        println(sb.toString())
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

    private fun ExternalState.remapToInternal() =
            State(this, true)

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

    private fun State.simulateWithCache(action: Action): ActionResult {
        /*val key = Pair(this, action)
        var cached = cache.get(key)
        if (cached == null) {
            cached = this.simulateAction(action)
            cache.put(key, cached)
        }
        return cached*/
        return simulateAction(action)
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

        if (lines.size != (1 + 1 + phase1Weights.size + phase2Weights.size + phaseFinalWeights.size))
            throw IllegalStateException("File non valido")

        try {
            val versionRead = lines[0].toLong()
            numeroSimulazioni = lines[1].toInt()
            if (versionRead != version)
                throw IllegalStateException("File non valido")
            for (i in phase1Weights.indices)
                phase1Weights[i] = lines[2 + i].toDouble()
            for (i in phase2Weights.indices)
                phase2Weights[i] = lines[2 + i + phase1Weights.size].toDouble()
            for (i in phaseFinalWeights.indices)
                phaseFinalWeights[i] = lines[2 + i + phase1Weights.size + phase2Weights.size].toDouble()

        } catch (e: Exception) {
            throw IllegalStateException("File non valido")
        }

        println("Cariati \n")
        println("ph 1 " + phase1Weights.joinToString(", ", "[", "]"))
        println("ph 2 " + phase2Weights.joinToString(", ", "[", "]"))
        println("ph 3 " + phaseFinalWeights.joinToString(", ", "[", "]"))
    }

    private fun State.invert(): State {
        val newMatrix = Matrix.buildMatrix<Type>(this.grid.rows, this.grid.columns, { x, y ->
            when (this.grid[x, y]) {
                Type.EMPTY -> Type.EMPTY
                Type.BLACK -> Type.WHITE
                Type.WHITE -> Type.BLACK
                Type.INVALID -> Type.INVALID
            }
        })
        return State(newMatrix, true, this.blackHandCount, this.whiteHandCount)
    }

    fun save() {
        val saveFile = File("weights")
        if (saveFile.exists()) {
            saveFile.delete()
        }
        saveFile.createNewFile()
        val os = BufferedOutputStream(FileOutputStream(saveFile)).bufferedWriter()
        os.write(version.toString() + System.lineSeparator())
        os.write("" + numeroSimulazioni + System.lineSeparator())
        phase1Weights.forEach { os.write(it.toString() + System.lineSeparator()) }
        phase2Weights.forEach { os.write(it.toString() + System.lineSeparator()) }
        phaseFinalWeights.forEach { os.write(it.toString() + System.lineSeparator()) }
        os.close()
    }

    companion object {
        const val version: Long = 3L
        private val cache = Cache<Pair<State, Action>, ActionResult>()
    }

    override fun matchStart() {
        load(true)
    }

    override fun matchEnd() {
        if (save)
            save()
        printPar()
    }
}