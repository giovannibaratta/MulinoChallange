package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State
import it.unibo.mulino.player.AIPlayer
import java.util.*
import javax.swing.Timer

class MinMaxPlayer(val time: Int = 85) : AIPlayer {

    init {
        require(time > 0)
    }

    override fun playPhase1(state: State, player : State.Checker): Phase1Action {
        return play(state, player) as Phase1Action
    }

    override fun playPhase2(state: State, player : State.Checker): Phase2Action {
        val res = play(state, player)
        if (res is Phase2Action)
            return res
        else if (res is PhaseFinalAction)
            return res.toPhase2Action()
        throw IllegalStateException("Ho ricevuto da play un'azione di tipo non valido")
    }

    private fun PhaseFinalAction.toPhase2Action(): Phase2Action {
        val action = Phase2Action()
        action.from = this.from
        action.to = this.to
        action.removeOpponentChecker = this.removeOpponentChecker
        return action
    }

    override fun playPhaseFinal(state: State, player : State.Checker): PhaseFinalAction {
        return play(state, player) as PhaseFinalAction
    }

    private fun play(state: State, player : State.Checker): Action {

        val game = MulinoGame
        //val startTime = System.nanoTime()
        val diagonalsString = Array(8, { charArrayOf('e','e','e')})
        for(position in state.board.keys){
            val (vertex, level) = game.toInternalPositions[position]!!
            when(state.board[position]){
                State.Checker.WHITE ->{
                    diagonalsString[vertex][level] = 'w'
                    //game.addPiece(clientState, position, State.Checker.WHITE)
                }
                State.Checker.BLACK -> {
                    diagonalsString[vertex][level] = 'b'
                    //game.addPiece(clientState, position, State.Checker.BLACK)
                }
            }
        }
        val diagonals :Array<CharArray> = Array(8, {index->game.diagonals["${diagonalsString[index][0]}${diagonalsString[index][1]}${diagonalsString[index][2]}"]!!})
        var clientState = State(checker = player, board = diagonals, checkers = intArrayOf(state.whiteCheckers, state.blackCheckers), checkersOnBoard = intArrayOf(state.whiteCheckersOnBoard, state.blackCheckersOnBoard))
        clientState.checkers[0]=state.whiteCheckers
        clientState.checkers[1]=state.blackCheckers
        clientState.currentPhase = when{
            state.currentPhase==State.Phase.SECOND -> 2
            state.currentPhase==State.Phase.FINAL -> 3
            else -> 1
        }
        //val totalTime = System.nanoTime()-startTime
        //println("Tempo inizializzazione: $totalTime")
        val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 14.0, 43.0, 10.0, 8.0, 7.0, 42.0, 1086.0, 10.0, 1.0, 16.0, 1190.0), -800.00, 800.00, time)
        val actionString = search.makeDecision(clientState)
        when(actionString.get(0)){
            '1'->{
                val action = Phase1Action()
                action.putPosition=actionString.substring(1,3)
                if(actionString.length>3)
                    action.removeOpponentChecker=actionString.substring(3,5)
                return action
            }
            '2'->{
                val action = Phase2Action()
                action.from=actionString.substring(1,3)
                action.to=actionString.substring(3,5)
                if(actionString.length>5)
                    action.removeOpponentChecker=actionString.substring(5,7)
                return action
            }
            '3'->{
                val action = PhaseFinalAction()
                action.from=actionString.substring(1,3)
                action.to=actionString.substring(3,5)
                if(actionString.length>5)
                    action.removeOpponentChecker=actionString.substring(5,7)
                return action
            }
        }
        return Phase1Action()
    }

    override fun matchStart() {

    }

    override fun matchEnd() {

    }
}