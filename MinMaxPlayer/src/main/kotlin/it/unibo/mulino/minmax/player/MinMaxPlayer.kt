package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.mulino.player.AIPlayer
import it.unibo.ai.didattica.mulino.actions.Action as ChesaniAction
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


    private fun play(state: ChesaniState, player: ChesaniState.Checker): ChesaniAction {
        val game = MulinoGame
        // board iniziale vuota da riempire con le pedine ricevute dal server
        val board = intArrayOf(0, 0)

        for(position in state.board.keys){
            val intPosition = game.toInternalPositions[position]!!

            when(state.board[position]){
                ChesaniState.Checker.WHITE -> board[0] += State.position[intPosition]
                ChesaniState.Checker.BLACK -> board[1] += State.position[intPosition]
            // else può essere una casella vuota e non devo fare niente
            }
        }

        val (playerChekersHand, playerCheckerBoard) = when (player) {
            it.unibo.ai.didattica.mulino.domain.State.Checker.WHITE -> Pair(state.whiteCheckers, state.whiteCheckersOnBoard)
            it.unibo.ai.didattica.mulino.domain.State.Checker.BLACK -> Pair(state.blackCheckers, state.blackCheckersOnBoard)
            else -> throw IllegalStateException("Checker non valido")
        }

        val phase = when {
            playerChekersHand > 0 -> 1
            playerChekersHand == 0 && playerCheckerBoard > 3 -> 2
            playerChekersHand == 0 && playerCheckerBoard <= 3 -> 3
            else -> throw IllegalStateException("Fase non riconosciuta")
        }

        val clientState = State(
                playerType = player.toInt(),
                board = board,
                checkers = intArrayOf(state.whiteCheckers, state.blackCheckers),
                checkersOnBoard = intArrayOf(state.whiteCheckersOnBoard, state.blackCheckersOnBoard)
        )

        //val totalTime = System.nanoTime()-startTime
        //println("Tempo inizializzazione: $totalTime")
        val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 14.0, 43.0, 10.0, 8.0, 7.0, 42.0, 1086.0, 10.0, 1.0, 16.0, 1190.0), -700.00, 700.00, timeLimit, sortAction = true)
        val actionHashCode = search.makeDecision(clientState)
        val action = ActionMapper.actionMap[actionHashCode]!!

        // TODO("Assumo azione corretta, da verificare")

        when (state.currentPhase) {
            ChesaniState.Phase.FIRST -> {
                val ph1Action = Phase1Action()
                if (action.remove != -1) {
                    val removeVertex = MulinoGame.delinearizeVertex[action.remove]
                    val removeLevel = MulinoGame.deliearizeLevel[action.remove]
                    ph1Action.removeOpponentChecker = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]!!
                }
                val toVertex = MulinoGame.delinearizeVertex[action.to]
                val toLevel = MulinoGame.deliearizeLevel[action.to]
                ph1Action.putPosition = MulinoGame.toExternalPositions[Pair(toVertex, toLevel)]!!
                return ph1Action
            }
            ChesaniState.Phase.SECOND -> {
                val ph2Action = Phase2Action()
                if (action.remove != -1) {
                    val removeVertex = MulinoGame.delinearizeVertex[action.remove]
                    val removeLevel = MulinoGame.deliearizeLevel[action.remove]
                    ph2Action.removeOpponentChecker = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]!!
                }
                val toVertex = MulinoGame.delinearizeVertex[action.to]
                val toLevel = MulinoGame.deliearizeLevel[action.to]
                val fromVertex = MulinoGame.delinearizeVertex[action.from]
                val fromLevel = MulinoGame.deliearizeLevel[action.from]
                ph2Action.to = MulinoGame.toExternalPositions[Pair(toVertex, toLevel)]!!
                ph2Action.from = MulinoGame.toExternalPositions[Pair(fromVertex, fromLevel)]!!
                return ph2Action
            }
            ChesaniState.Phase.FINAL -> {
                val ph3Action = PhaseFinalAction()
                if (action.remove != -1) {
                    val removeVertex = MulinoGame.delinearizeVertex[action.remove]
                    val removeLevel = MulinoGame.deliearizeLevel[action.remove]
                    ph3Action.removeOpponentChecker = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]!!
                }
                val toVertex = MulinoGame.delinearizeVertex[action.to]
                val toLevel = MulinoGame.deliearizeLevel[action.to]
                val fromVertex = MulinoGame.delinearizeVertex[action.from]
                val fromLevel = MulinoGame.deliearizeLevel[action.from]
                ph3Action.to = MulinoGame.toExternalPositions[Pair(toVertex, toLevel)]!!
                ph3Action.from = MulinoGame.toExternalPositions[Pair(fromVertex, fromLevel)]!!
                return ph3Action
            }
            null -> throw IllegalStateException("phase non valida")
        }
        /*
        when (actionString[0]) {
            '1'->{
                val action = Phase1Action()
                action.putPosition=actionString.substring(1,3)
                if(actionString.length>3)
                    action.removeOpponentChecker=actionString.substring(3,5)

                //search.metrics.print()
                println("Azione $action")
                return action
            }
            '2'->{
                val action = Phase2Action()
                action.from=actionString.substring(1,3)
                action.to=actionString.substring(3,5)
                if(actionString.length>5)
                    action.removeOpponentChecker=actionString.substring(5,7)

                //search.metrics.print()

                println("Azione $action")
                return action
            }
            '3'->{
                val action = PhaseFinalAction()
                action.from=actionString.substring(1,3)
                action.to=actionString.substring(3,5)
                if(actionString.length>5)
                    action.removeOpponentChecker=actionString.substring(5,7)

                //search.metrics.print()

                println("Azione $action")
                return action
            }
        }*/
    }

    fun it.unibo.ai.didattica.mulino.domain.State.Checker.toInt() = when (this) {
        it.unibo.ai.didattica.mulino.domain.State.Checker.WHITE -> 0
        it.unibo.ai.didattica.mulino.domain.State.Checker.BLACK -> 1
        else -> throw IllegalArgumentException("Checker $this non valido")
    }

    /*
    private fun Metrics.print() {
        println("NODI : ${this.getInt(METRICS_NODES_EXPANDED)}")
        println("MAX DEPTH : ${this.getInt(METRICS_MAX_DEPTH)}")
        println("PRUNE :${this.get(METRICS_PRUNE)}")
    }*/

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

fun main(args: Array<String>) {

    val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 14.0, 43.0, 10.0, 8.0, 7.0, 42.0, 1086.0, 10.0, 1.0, 16.0, 1190.0), -700.00, 700.00, Int.MAX_VALUE / 2000, sortAction = false)
    search.makeDecision(State(0, intArrayOf(0, 0), intArrayOf(9, 9), intArrayOf(0, 0), false))
    /* stato buggato no isterminal */
//var board = intArrayOf(6206,12067969)
    //  println(MulinoGame.isTerminal(State(0,board, intArrayOf(0,0), intArrayOf(7,8))))

/*
    // genera -inf
    var board = intArrayOf(0, 0)

    MulinoGame.addPiece(board, 6, 0)

    MulinoGame.addPiece(board, 9, 0)

    MulinoGame.addPiece(board, 7, 0)

    MulinoGame.addPiece(board, 11, 0)

    MulinoGame.addPiece(board, 3, 0)

    MulinoGame.addPiece(board, 1, 0)

    MulinoGame.addPiece(board, 22, 0)


    // neri
    MulinoGame.addPiece(board, 12, 1)

    MulinoGame.addPiece(board, 10, 1)

    MulinoGame.addPiece(board, 5, 1)

    MulinoGame.addPiece(board, 15, 1)

    MulinoGame.addPiece(board, 20, 1)

    MulinoGame.addPiece(board, 19, 1)

    MulinoGame.addPiece(board, 0, 1)

    MulinoGame.addPiece(board, 18, 1)
    val state = State(0, board, intArrayOf(1, 1), intArrayOf(7, 8), false)
    val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 14.0, 43.0, 10.0, 8.0, 7.0, 42.0, 1086.0, 10.0, 1.0, 16.0, 1190.0), -700.00, 700.00, Int.MAX_VALUE / 2000, sortAction = false)
    println(search.makeDecision(state))
*/

//    val state = State(0, board, intArrayOf(1, 1), intArrayOf(7, 8), false)
//    */
//    val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 14.0, 43.0, 10.0, 8.0, 7.0, 42.0, 1086.0, 10.0, 1.0, 16.0, 1190.0), -700.00, 700.00, Int.MAX_VALUE/2000, sortAction = false)
//    //println(search.makeDecision(state))
//    val board = intArrayOf(14894,11322385)
//    MulinoGame.removePiece(board,MulinoGame.toInternalPositions["d5"]!!)
//    MulinoGame.addPiece(board,MulinoGame.toInternalPositions["c5"]!!,0)
//    val state = State(0,board, intArrayOf(0,0), intArrayOf(8,9),false)
//
//    //var alpha = Double.NEGATIVE_INFINITY
//    //var beta = -77.0
//    //var value : Double = Double.NEGATIVE_INFINITY
///*
//    for (action in MulinoGame.getActions(state)) {
//        value = Math.max(value, minValue(game.getResult(state, action), //
//                player, alpha, beta, depth + 1))
//        if (value >= beta) {
//            metrics.incrementInt(METRICS_PRUNE)
//            return value
//        }
//        alpha = Math.max(alpha, value)
//    }
//
//    search.currDepthLimit = 9
//    val value = search.minValue(state, 0,Double.NEGATIVE_INFINITY,-77.0,6)
//    */
//    println(MulinoGame.getActions(state))
}