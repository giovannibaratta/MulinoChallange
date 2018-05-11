package it.unibo.mulino.qlearning.player

import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.mulino.ai.ApproximateQLearning
import it.unibo.mulino.player.AIPlayer
import it.unibo.mulino.qlearning.player.model.Action
import it.unibo.mulino.qlearning.player.model.Position
import it.unibo.mulino.qlearning.player.model.State
import it.unibo.mulino.qlearning.player.model.State.Type
import it.unibo.utils.filterCellIndexed
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.mutableListOf
import it.unibo.ai.didattica.mulino.domain.State as ExternalState

class QLearningPlayer : AIPlayer {

    internal val actionFromStatePhase1: (State) -> List<Action> = {
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

        emptyCell.forEach {
            val toPos = Position(it.first.first, it.first.second)
            var removePos = Optional.empty<Position>()
            if (state.closeAMill(toPos).first) {
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
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), it.second).first }

        it.grid.filterCellIndexed { it == myType }
                .forEach {
                    state.grid[it.first.first, it.first.second] = State.Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)

                    state.adjacent(fromCell, true)
                            .filter { it.second == Type.EMPTY }
                            .forEach {
                                val toCell = it.first
                                if (state.closeAMill(toCell, myType).first) {
                                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                } else {
                                    possibleRemove.forEach {
                                        val removeCell = Position(it.first.first, it.first.second)
                                        actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(removeCell)))
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
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), it.second).first }

        val emptyCell = it.grid.filterCellIndexed { it == Type.EMPTY }

        it.grid.filterCellIndexed { it == myType }
                .forEach {
                    state.grid[it.first.first, it.first.second] = State.Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)
                    emptyCell.forEach {
                        val toCell = Position(it.first.first, it.first.second)
                        if (state.closeAMill(toCell, myType).first) {
                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                        } else {
                            possibleRemove.forEach {
                                val removeCell = Position(it.first.first, it.first.second)
                                actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(removeCell)))
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


    private val applyAction: (State, Action) -> Pair<Double, State> = { state, action ->
        val reward = when (action.remove.isPresent) {
            true -> 1.0
            else -> -0.2
        }
        Pair(reward, state.simulateAction(action).newState)
    }

    private val phase1Features: Array<(State, Action, State) -> Double> =
            arrayOf(
                    { state, _, newState -> state.whiteBoardCount().toDouble() },
                    { state, _, newState -> state.blackBoardCount().toDouble() },
                    { state, action, newState ->
                        when (state.simulateAction(action).mill) {
                            true -> 1.0
                            false -> 0.0
                        }
                    },
                    { state, _, newState ->
                        when (state.enemyCanMove()) {
                            false -> 1.0
                            true -> 0.0
                        }
                    },
                    { state, _, newState ->
                        when (state.iCanMove()) {
                            true -> 1.0
                            false -> 0.0
                        }
                    }
            )

    private val learnerPhase1 = ApproximateQLearning<State, Action>({ 0.01 },
            { 0.01 },
            featureExtractors = phase1Features,
            actionsFromState = actionFromStatePhase1,
            applyAction = applyAction)

    private val learnerPhase2 = ApproximateQLearning<State, Action>({ 0.01 },
            { 0.01 },
            featureExtractors = phase1Features,
            actionsFromState = actionFromStatePhase2,
            applyAction = applyAction)

    private val learnerPhase3 = ApproximateQLearning<State, Action>({ 0.01 },
            { 0.01 },
            featureExtractors = phase1Features,
            actionsFromState = actionFromStatePhase3,
            applyAction = applyAction)

    override fun playPhase1(state: ExternalState, playerType: ExternalState.Checker): Phase1Action =
        /*
        ApproximateQLearning<T, E>(private val alpha: Double,
                                 private val discount : Double,
                                 private val featureExtractors : Array<(T,E) -> Double>,
                                 val weights : Array<Double> = Array(featureExtractors.size,{0.0}),
                                 private val actionsFromState : (T) -> List<E>,
                                 private val applyAction : (T, E) -> Pair<Double,T>){
         */

            learnerPhase1.think(normalize(state, playerType).remapToInternal()).rempapToExternalPhase1()
    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.


    override fun playPhase2(state: ExternalState, playerType: ExternalState.Checker): Phase2Action = learnerPhase2.think(normalize(state, playerType).remapToInternal()).rempapToExternalPhase2()

    override fun playPhaseFinal(state: ExternalState, playerType: ExternalState.Checker): PhaseFinalAction = learnerPhase3.think(normalize(state, playerType).remapToInternal()).rempapToExternalPhaseFinal()

    /*
    private fun tunrType(isWhiteTurn : Boolean) : Pair<Type,Type>
        = when(state.isWhiteTurn){
        true -> (Type.WHITE,Type.BLACK)
            false -> (Type.BLACK,Type.WHITE)
    }   */


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
                    state
                }
                else -> throw IllegalArgumentException("Tipo di giocatore non valido")
            }

    // TODO("DA CAMBIARE .METODO SBAGLIATO NON UTILIZZARE")
    private fun ExternalState.remapToInternal() = it.unibo.mulino.qlearning.player.model.State(this, true)

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
        val yPos: String = this.y.toString()
        return xPos + yPos
    }

    override fun matchStart() {

    }

    override fun matchEnd() {

    }
}