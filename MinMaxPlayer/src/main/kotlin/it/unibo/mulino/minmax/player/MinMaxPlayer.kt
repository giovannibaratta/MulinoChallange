package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.mulino.player.AIPlayer
import it.unibo.utils.IterariveDeepingAlphaBetaSearch.*
import it.unibo.utils.Metrics
import it.unibo.ai.didattica.mulino.domain.State as ChesaniState

class MinMaxPlayer(val timeLimit: Int = 55) : AIPlayer {

    init {
        require(timeLimit > 0)
    }

    override fun playPhase1(state: ChesaniState, playerType: ChesaniState.Checker) = play(state, playerType) as Phase1Action

    override fun playPhase2(state: ChesaniState, playerType: ChesaniState.Checker): Phase2Action {

        val res = play(state, playerType)
        return when (res) {
            is Phase2Action -> res
            is PhaseFinalAction -> res.toPhase2Action()
            else -> throw IllegalStateException("Ho ricevuto da play un'azione di tipo non valido")
        }
    }

    override fun playPhaseFinal(state: ChesaniState, playerType: ChesaniState.Checker) =
            play(state, playerType) as PhaseFinalAction


    private fun play(state: ChesaniState, player: ChesaniState.Checker): Action {
        val game = MulinoGame
        val board = intArrayOf(0, 0)
        //val startTime = System.nanoTime()
        //val diagonalsString = Array(8, { charArrayOf('e','e','e')})
        // mapping dello stato esterno
        for(position in state.board.keys){
            val (vertex, level) = game.toInternalPositions[position]!!
            when(state.board[position]){
                ChesaniState.Checker.WHITE -> {
                    board[0] += State.position[vertex * 3 + level]
                    //diagonalsString[vertex][level] = 'w'
                    //game.addPiece(clientState, position, State.Checker.WHITE)
                }
                ChesaniState.Checker.BLACK -> {
                    //diagonalsString[vertex][level] = 'b'
                    board[1] += State.position[vertex * 3 + level]
                    //game.addPiece(clientState, position, State.Checker.BLACK)
                }
            }
        }
        val whiteChecker = state.whiteCheckers
        val blackChecker = state.blackCheckers
        val phase = when {
            state.currentPhase == ChesaniState.Phase.SECOND -> 2
            state.currentPhase == ChesaniState.Phase.FINAL -> 3
            else -> 1
        }
        //val diagonals :Array<CharArray> = Array(8, {index->game.diagonals["${diagonalsString[index][0]}${diagonalsString[index][1]}${diagonalsString[index][2]}"]!!})
        val clientState = State(checker = player, board = board, currentPhase = phase, checkers = intArrayOf(whiteChecker, blackChecker), checkersOnBoard = intArrayOf(state.whiteCheckersOnBoard, state.blackCheckersOnBoard))

        //val totalTime = System.nanoTime()-startTime
        //println("Tempo inizializzazione: $totalTime")
        val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 14.0, 43.0, 10.0, 8.0, 7.0, 42.0, 1086.0, 10.0, 1.0, 16.0, 1190.0), -700.00, 700.00, timeLimit, sortAction = false)
        val actionString = search.makeDecision(clientState)
        when (actionString[0]) {
            '1'->{
                val action = Phase1Action()
                action.putPosition=actionString.substring(1,3)
                if(actionString.length>3)
                    action.removeOpponentChecker=actionString.substring(3,5)

                search.metrics.print()
                println("Azione $action")
                return action
            }
            '2'->{
                val action = Phase2Action()
                action.from=actionString.substring(1,3)
                action.to=actionString.substring(3,5)
                if(actionString.length>5)
                    action.removeOpponentChecker=actionString.substring(5,7)

                search.metrics.print()

                println("Azione $action")
                return action
            }
            '3'->{
                val action = PhaseFinalAction()
                action.from=actionString.substring(1,3)
                action.to=actionString.substring(3,5)
                if(actionString.length>5)
                    action.removeOpponentChecker=actionString.substring(5,7)

                search.metrics.print()

                println("Azione $action")
                return action
            }
        }
        return Phase1Action()
    }

    private fun Metrics.print() {
        println("NODI : ${this.getInt(METRICS_NODES_EXPANDED)}")
        println("MAX DEPTH : ${this.getInt(METRICS_MAX_DEPTH)}")
        println("PRUNE :${this.get(METRICS_PRUNE)}")
    }

    private fun PhaseFinalAction.toPhase2Action(): Phase2Action {
        val action = Phase2Action()
        action.from = this.from
        action.to = this.to
        action.removeOpponentChecker = this.removeOpponentChecker
        return action
    }

    override fun matchStart() {}
    override fun matchEnd() {}
}