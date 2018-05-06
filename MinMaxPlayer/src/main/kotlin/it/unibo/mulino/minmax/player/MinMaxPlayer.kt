package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State
import it.unibo.mulino.player.AIPlayer

class MinMaxPlayer : AIPlayer {
    override fun playPhase1(state: State, player : State.Checker): Phase1Action {
        return play(state, player, '1') as Phase1Action
    }

    override fun playPhase2(state: State, player : State.Checker): Phase2Action {
        return play(state, player, '2') as Phase2Action
    }

    override fun playPhaseFinal(state: State, player : State.Checker): PhaseFinalAction {
        return play(state, player, '3') as PhaseFinalAction
    }

    private fun play(state: State, player : State.Checker, phase : Char): Action {
        var clientState = State(player)
        clientState.currentPhase = phase
        clientState.checkers[0]=state.whiteCheckers
        clientState.checkers[1]=state.blackCheckers
        for(position in state.board.keys){
            when(state.board[position]){
                State.Checker.WHITE ->{
                    val column = position[0]
                    val raw = position[1].toString().toInt()
                    clientState.addPiece(Pair(column, raw), State.Checker.WHITE)
                }
                State.Checker.BLACK -> {
                    val column = position[0]
                    val raw = position[1].toString().toInt()
                    clientState.addPiece(Pair(column, raw), State.Checker.BLACK)
                }
            }
        }

        val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 7.0, 42.0, 1047.0), -10000.00, 10000.00, 5)
        return search.makeDecision(clientState)
    }

    override fun matchStart() {

    }

    override fun matchEnd() {

    }
}