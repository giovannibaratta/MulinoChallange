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
import java.util.*
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
                .filter { !state.isAClosedMill(Position(it.first.first, it.first.second), it.second) }

        val emptyCell = it.grid.filterCellIndexed { it == Type.EMPTY }

        emptyCell.forEach {
            val toPos = Position(it.first.first, it.first.second)
            var removePos = Optional.empty<Position>()
            if (state.closeAMill(toPos)) {
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
                    state.grid[it.first.first][it.first.second] = State.Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)

                    state.adjacent(fromCell, true)
                            .filter { it.second == Type.EMPTY }
                            .forEach {
                                val toCell = it.first
                                if (state.closeAMill(toCell, myType)) {
                                    actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                                } else {
                                    possibleRemove.forEach {
                                        val removeCell = Position(it.first.first, it.first.second)
                                        actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(removeCell)))
                                    }
                                }
                            }
                    state.grid[it.first.first][it.first.second] = myType
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
                    state.grid[it.first.first][it.first.second] = State.Type.EMPTY
                    val fromCell = Position(it.first.first, it.first.second)
                    emptyCell.forEach {
                        val toCell = Position(it.first.first, it.first.second)
                        if (state.closeAMill(toCell, myType)) {
                            actionList.add(Action.buildPhase2(fromCell, toCell, Optional.empty()))
                        } else {
                            possibleRemove.forEach {
                                val removeCell = Position(it.first.first, it.first.second)
                                actionList.add(Action.buildPhase2(fromCell, toCell, Optional.of(removeCell)))
                            }
                        }
                    }
                    state.grid[it.first.first][it.first.second] = myType
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

    override fun playPhase1(state: ExternalState): Phase1Action {
        val learner = ApproximateQLearning<State, Action>(0.01, 0.01, { 0 },
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playPhase2(state: ExternalState): Phase2Action {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playPhaseFinal(state: ExternalState): PhaseFinalAction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /*
    private fun tunrType(isWhiteTurn : Boolean) : Pair<Type,Type>
        = when(state.isWhiteTurn){
        true -> (Type.WHITE,Type.BLACK)
            false -> (Type.BLACK,Type.WHITE)
    }   */

    private fun ExternalState.remap() = it.unibo.mulino.qlearning.player.model.State(this)
}