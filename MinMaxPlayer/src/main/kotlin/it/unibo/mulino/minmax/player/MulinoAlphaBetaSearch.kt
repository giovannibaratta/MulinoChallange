package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.mulino.minmax.player.MulinoGame.nextLevel
import it.unibo.mulino.minmax.player.MulinoGame.nextVertex
import it.unibo.mulino.minmax.player.MulinoGame.previousVertex
import it.unibo.utils.FibonacciHeap
import it.unibo.ai.didattica.mulino.domain.State as ChesaniState
import it.unibo.mulino.qlearning.player.model.State as QLearningState

class MulinoAlphaBetaSearch(coefficients: Array<Double>,
                            utilMin: Double,
                            utilMax: Double,
                            timeLimit: Int,
                            private val sortAction: Boolean = false) /*: IterariveDeepingAlphaBetaSearch<State, String, Int>(MulinoGame, utilMin, utilMax, time)*/ {

    private val closedMorrisCoeff = doubleArrayOf(coefficients[0],coefficients[6], coefficients[15])
    private val morrisesNumberCoeff = doubleArrayOf(coefficients[1],coefficients[7])
    private val blockedOppPiecesCoeff = doubleArrayOf(coefficients[2],coefficients[8])
    private val piecesNumberCoeff = doubleArrayOf(coefficients[3],coefficients[9])
    private val num2PiecesCoeff = doubleArrayOf(coefficients[4],coefficients[13])
    private val num3PiecesCoeff =doubleArrayOf(coefficients[5],coefficients[14])
    private val openedMorrisCoeff = coefficients[10]
    private val doubleMorrisCoeff = coefficients[11]
    private val winningConfCoeff = doubleArrayOf(coefficients[12],coefficients[16])

    private var ordered = 0

    private val iterativeSearch = IterativeSearch(MulinoGame,
            utilMin,
            utilMax,
            this::eval,
            orderActions = this::orderdActions,
            maxTime = timeLimit)

    fun makeDecision(state: State): Action = iterativeSearch.makeDecision(state)


    private val thinker = QLearningPlayerAlternative({ 0.0 })

    private fun orderdActions(s: State, al: ArrayList<Int>, p: Int, d: Int): ArrayList<Int> {
        if (!sortAction || d > 2)
            return al
        return when (s.currentPhase) {
            1 -> thinker.playPhase1(s, al)
            2 -> thinker.playPhase2(s, al)
            3 -> thinker.playPhase3(s, al)
            else -> throw IllegalStateException("Fase non valida")
        }
    }


    // le prime 4 posizioni devono essere necessariamente occupate mentre le ultime 2 devono o essere libere
    // o al max 1 occupata
    val paralleli = arrayOf(
            intArrayOf(6, 12, 7, 13, 9, 10),
            intArrayOf(7, 13, 8, 14, 10, 11),
            intArrayOf(2, 20, 1, 19, 23, 22),
            intArrayOf(1, 19, 0, 18, 22, 21),
            intArrayOf(6, 0, 7, 1, 3, 4),
            intArrayOf(7, 8, 1, 2, 4, 5),
            intArrayOf(14, 13, 20, 19, 17, 16),
            intArrayOf(13, 12, 19, 18, 16, 15)
    )

    fun numeroStruttureParallele(board: IntArray, playerType: Int): Int {
        var count = 0
        for (parallelo in paralleli) {
            if (State.isSet(board, parallelo[0], playerType) &&
                    State.isSet(board, parallelo[1], playerType) &&
                    State.isSet(board, parallelo[2], playerType) &&
                    State.isSet(board, parallelo[3], playerType) &&
                    ((State.isNotSet(board, parallelo[4]) && State.isNotSet(board, parallelo[5])) || (State.isNotSet(board, parallelo[4]) && State.isSet(board, parallelo[5], playerType)) || (State.isNotSet(board, parallelo[5]) && State.isSet(board, parallelo[4], playerType))))
                count++
        }
        return count
    }

    //region MixedEval
    fun eval(state: State, player: Int): Double {
        var value = 0.0 /*super.eval(state, player)*/

        //if (state == null) throw IllegalArgumentException("state null")
        //if (player == null) throw IllegalArgumentException("player null")

        val stateOpposite = Math.abs(state.playerType - 1)
        val parOpposite = Math.abs(player - 1)
        val parPlayer = player
        val game = MulinoGame

        val playerPosition = MulinoGame.getPositions(state, parPlayer)
        val enemyPosition = MulinoGame.getPositions(state, parOpposite)

        val parPlayerPhase = when {
            state.checkers[parPlayer] > 0 -> 1
            state.checkers[parPlayer] == 0 && state.checkersOnBoard[parPlayer] > 3 -> 2
            state.checkers[parPlayer] == 0 && state.checkersOnBoard[parPlayer] <= 3 -> 3
            else -> throw IllegalStateException("Stato non valido")
        }

        val parOppositePhase = when {
            state.checkers[parOpposite] > 0 -> 1
            state.checkers[parOpposite] == 0 && state.checkersOnBoard[parOpposite] > 3 -> 2
            state.checkers[parOpposite] == 0 && state.checkersOnBoard[parOpposite] <= 3 -> 3
            else -> throw IllegalStateException("Stato non valido")
        }

        val densita = 20.0

        when (parPlayerPhase) {
            1 -> {
                var numMorrisesPlayer: Int = 0
                var blockedPieces: Int = 0
                var num2Pieces = 0
                var num3Pieces = 0
                /*
                //region densita
                var enemyCount= 0
                var playerCount = 0
                for(vertexIndex in 0 until 8){
                    if(State.isSet(state.board,vertexIndex * 3 + 2,parPlayer))
                        playerCount++
                    else if (State.isSet(state.board,vertexIndex * 3 + 2,parOpposite))
                        enemyCount++
                }

                val differenceOnLevel2 = player-enemyCount
                if(playerCount > 3 && differenceOnLevel2 > 2) {
                    value += densita
                }else if(enemyCount > 2 && differenceOnLevel2 <= -2) {
                    value -= densita
                }
                for(vertexIndex in 0 until 8){
                    if(State.isSet(state.board,vertexIndex * 3 + 1,parPlayer))
                        playerCount++
                    else if (State.isSet(state.board,vertexIndex * 3 + 1,parOpposite))
                        enemyCount++
                }

                val differenceOnLevel1 = player-enemyCount
                if(playerCount > 3 && differenceOnLevel1 > 2) {
                    value += densita
                }else if(enemyCount > 2 && differenceOnLevel1 <= -2) {
                    value -= densita
                }

                for(vertexIndex in 0 until 8){
                    if(State.isSet(state.board,vertexIndex * 3 + 0,parPlayer))
                        playerCount++
                    else if (State.isSet(state.board,vertexIndex * 3 + 0,parOpposite))
                        enemyCount++
                }

                val differenceOnLevel0 = player-enemyCount
                if(playerCount > 3 && differenceOnLevel0 > 2) {
                    value += densita
                }else if(enemyCount > 2 && differenceOnLevel0 <= -2) {
                    value -= densita
                }
                //endregion
                */
                //region Calcolo valori per il value
                for (position in playerPosition) {
                    val vertex = MulinoGame.delinearizeVertex[position]
                    val level = MulinoGame.deliearizeLevel[position]
                    //region Calcolo morris
                    when (vertex) {
                        1, 3, 5, 7 -> {
                            if (State.isSet(state.board, MulinoGame.nextVertex(vertex), level, parPlayer) &&
                                    State.isSet(state.board, MulinoGame.previousVertex(vertex), level, parPlayer)) {
                                numMorrisesPlayer++
                            }
                            if (level == 1 && (State.isSet(state.board, vertex, MulinoGame.nextLevel(level), parPlayer)) &&
                                    (State.isSet(state.board, vertex, MulinoGame.nextLevel(MulinoGame.nextLevel(level)), parPlayer))) {
                                numMorrisesPlayer++
                            }
                        }
                    }
                    //endregion
                    //region BlockerPieces
                    if (MulinoGame.checkNoMoves(state, position, parPlayer))
                        blockedPieces++
                    //endregion
                    //region Num2Pieces
                    when (vertex) {
                        1, 3, 5, 7 -> {
                            if (State.isSet(state.board, MulinoGame.nextVertex(vertex), level, parPlayer) &&
                                    State.isNotSet(state.board, MulinoGame.previousVertex(vertex), level))
                                num2Pieces++
                            else if (State.isSet(state.board, MulinoGame.previousVertex(vertex), level, parPlayer) &&
                                    State.isNotSet(state.board, MulinoGame.nextVertex(vertex), level))
                                num2Pieces++
                            when (level) {
                                0 -> {
                                    if (State.isSet(state.board, vertex, 1, parPlayer) &&
                                            State.isNotSet(state.board, vertex, 2))
                                        num2Pieces++
                                }
                                1 -> {
                                    if (State.isSet(state.board, vertex, 0, parPlayer) &&
                                            State.isNotSet(state.board, vertex, 2))
                                        num2Pieces++
                                    else if (State.isSet(state.board, vertex, 2, parPlayer) &&
                                            State.isNotSet(state.board, vertex, 0))
                                        num2Pieces++
                                }
                                2 -> {
                                    if (State.isSet(state.board, vertex, 1, parPlayer) &&
                                            State.isNotSet(state.board, vertex, 0))
                                        num2Pieces++
                                }
                            }
                        }
                    }
                    //endregion
                    //region Num3Pieces
                    when (vertex) {
                        0, 2, 4, 6 -> {
                            if (State.isSet(state.board, nextVertex(vertex), level, parPlayer) &&
                                    State.isSet(state.board, previousVertex(vertex), level, parPlayer) &&
                                    State.isNotSet(state.board, nextVertex(nextVertex(vertex)), level) &&
                                    State.isNotSet(state.board, previousVertex(previousVertex(vertex)), level))
                                num3Pieces++

                        }
                        else -> {
                            when (level) {
                                1 -> {
                                    if ((State.isSet(state.board, nextVertex(vertex), nextLevel(level), parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parPlayer)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), nextLevel(level))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), nextLevel(level), parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parPlayer)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), nextLevel(level))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), level, parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parPlayer)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), level, parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), level, parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parPlayer)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), level, parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                }
                            }
                        }
                    }
                    //endregion
                }
                //endregion

                value += morrisesNumberCoeff[0] * numMorrisesPlayer -
                        blockedOppPiecesCoeff[0] * blockedPieces +
                        piecesNumberCoeff[0] * state.checkersOnBoard[parPlayer] +
                        piecesNumberCoeff[0] * state.checkers[parPlayer] +
                        num2PiecesCoeff[0] * num2Pieces +
                        num3PiecesCoeff[0] * num3Pieces /*+
                        15.0 * numeroStruttureParallele(state.board, parPlayer)*/
                if (state.closedMorris) {
                    when (stateOpposite) {
                        parPlayer -> value += closedMorrisCoeff[0]
                        parOpposite -> value -= closedMorrisCoeff[0]
                    }
                }
            }

            2 -> {
                value += morrisesNumberCoeff[1] * game.getNumMorrises(state, playerPosition, parPlayer) -
                        blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, playerPosition, parPlayer) +
                        piecesNumberCoeff[1] * state.checkersOnBoard[parPlayer] /*+
                        15 * numeroStruttureParallele(state.board, parPlayer)//+*/
                // aggiunta adesso
                //num2PiecesCoeff[0] * game.getNum2Conf(state, playerPosition, parPlayer)
/*
                //region densita
                var enemyCount= 0
                var playerCount = 0
                for(vertexIndex in 0 until 8){
                    if(State.isSet(state.board,vertexIndex * 3 + 2,parPlayer))
                        playerCount++
                    else if (State.isSet(state.board,vertexIndex * 3 + 2,parOpposite))
                        enemyCount++
                }

                val differenceOnLevel2 = player-enemyCount
                if(playerCount > 3 && differenceOnLevel2 > 2) {
                    value += densita
                }else if(enemyCount > 2 && differenceOnLevel2 <= -2) {
                    value -= densita
                }
                for(vertexIndex in 0 until 8){
                    if(State.isSet(state.board,vertexIndex * 3 + 1,parPlayer))
                        playerCount++
                    else if (State.isSet(state.board,vertexIndex * 3 + 1,parOpposite))
                        enemyCount++
                }

                val differenceOnLevel1 = player-enemyCount
                if(playerCount > 3 && differenceOnLevel1 > 2) {
                    value += densita
                }else if(enemyCount > 2 && differenceOnLevel1 <= -2) {
                    value -= densita
                }

                for(vertexIndex in 0 until 8){
                    if(State.isSet(state.board,vertexIndex * 3 + 0,parPlayer))
                        playerCount++
                    else if (State.isSet(state.board,vertexIndex * 3 + 0,parOpposite))
                        enemyCount++
                }

                val differenceOnLevel0 = player-enemyCount
                if(playerCount > 3 && differenceOnLevel0 > 2) {
                    value += densita
                }else if(enemyCount > 2 && differenceOnLevel0 <= -2) {
                    value -= densita
                }
                //endregion
*/
                if (state.closedMorris) {
                    when (stateOpposite) {
                        parPlayer -> value += closedMorrisCoeff[1]
                        parOpposite -> value -= closedMorrisCoeff[1]
                    }
                }

                var hasOpenMorris = false
                var enemyImpossible = true
                var bonus = false

                outer@ for (position in playerPosition) {
                    for (adiacentPosition in MulinoGame.adiacentPositions[position]) {
                        val vertex = MulinoGame.delinearizeVertex[adiacentPosition]
                        val level = MulinoGame.deliearizeLevel[adiacentPosition]
                        if (State.isNotSet(state.board, vertex, level) && MulinoGame.checkMorris(state, position, adiacentPosition, parPlayer)) {
                            hasOpenMorris = true
                            if (level == 2 || level == 0)
                                bonus = true
                            if (parPlayer == state.playerType) {
                                impossible@ for (adiacentToMorris in MulinoGame.adiacentPositions[adiacentPosition]) {
                                    // vero solo se entrambi in fase 2, se uno in fase 2 e l'altro in fase 3 non è vero ma meh..
                                    if (State.isSet(state.board, adiacentToMorris, parOpposite)) {
                                        enemyImpossible = false
                                        break@impossible
                                    }
                                }
                            }
                            break@outer
                        }
                    }
                }

                if (hasOpenMorris) {
                    if (enemyImpossible && parPlayer == state.playerType) { // mio turno)
                        if (bonus)
                            value += 15.0
                        value += openedMorrisCoeff * 1.5
                    } else {
                        if (bonus)
                            value += 15.0
                        value += openedMorrisCoeff
                    }
                }
                if (game.hasDoubleMorris(state, playerPosition, parPlayer))
                    value += doubleMorrisCoeff
            }
        /*
        2 -> {
            var numMorrisesPlayer: Int = 0
            var blockedPieces: Int = 0
            var hasOpenedMorris: Boolean = false
            var hasDoubleMorris: Boolean = false

            //region Calcolo valori per il value
            for (position in playerPosition) {
                val vertex = MulinoGame.delinearizeVertex[position]
                val level = MulinoGame.deliearizeLevel[position]
                //region Calcolo morris
                when (vertex) {
                    1, 3, 5, 7 -> {
                        if (State.isSet(state.board, MulinoGame.nextVertex(vertex), level, parPlayer) &&
                                State.isSet(state.board, MulinoGame.previousVertex(vertex), level, parPlayer)) {
                            numMorrisesPlayer++
                        }
                        if (level == 1 && (State.isSet(state.board, vertex, MulinoGame.nextLevel(level), parPlayer)) &&
                                (State.isSet(state.board, vertex, MulinoGame.nextLevel(MulinoGame.nextLevel(level)), parPlayer))) {
                            numMorrisesPlayer++
                        }
                    }
                }
                //endregion
                //region BlockerPieces
                if (MulinoGame.checkNoMoves(state, position, parPlayer))
                    blockedPieces++
                //endregion

                //region hasDoubleMorris
                when (vertex) {
                    0, 2, 4, 6 -> {
                        if (State.isSet(state.board, nextVertex(vertex), level, parPlayer) &&
                                State.isSet(state.board, nextVertex(nextVertex(vertex)), level, parPlayer) &&
                                State.isSet(state.board, previousVertex(vertex), level, parPlayer) &&
                                State.isSet(state.board, previousVertex(previousVertex(vertex)), level, parPlayer))
                            hasDoubleMorris = true

                    }
                    else -> {
                        if (State.isSet(state.board, nextVertex(vertex), level, parPlayer) &&
                                State.isSet(state.board, previousVertex(vertex), level, parPlayer) &&
                                State.isSet(state.board, vertex, nextLevel(level), parPlayer) &&
                                State.isSet(state.board, vertex, nextLevel(level), parPlayer))
                            hasDoubleMorris = true
                    }
                }
                //endregion
            }
            //endregion

            value += morrisesNumberCoeff[1] * numMorrisesPlayer -
                    blockedOppPiecesCoeff[1] * blockedPieces +
                    piecesNumberCoeff[1] * state.checkersOnBoard[parPlayer]

            if (state.closedMorris) {
                when (stateOpposite) {
                    parPlayer -> value += closedMorrisCoeff[1]
                    parOpposite -> value -= closedMorrisCoeff[1]
                }
            }
            if (game.hasOpenedMorris(state, playerPosition, parPlayer))
                value += openedMorrisCoeff
            if (hasDoubleMorris)
                value += doubleMorrisCoeff
        }
*/
            3 -> {
                var num2Pieces = 0
                var num3Pieces = 0

                //region Calcolo valori per il value
                for (position in playerPosition) {
                    val vertex = MulinoGame.delinearizeVertex[position]
                    val level = MulinoGame.deliearizeLevel[position]
                    //region Num2Pieces
                    when (vertex) {
                        1, 3, 5, 7 -> {
                            if (State.isSet(state.board, MulinoGame.nextVertex(vertex), level, parPlayer) &&
                                    State.isNotSet(state.board, MulinoGame.previousVertex(vertex), level))
                                num2Pieces++
                            else if (State.isSet(state.board, MulinoGame.previousVertex(vertex), level, parPlayer) &&
                                    State.isNotSet(state.board, MulinoGame.nextVertex(vertex), level))
                                num2Pieces++
                            when (level) {
                                0 -> {
                                    if (State.isSet(state.board, vertex, 1, parPlayer) &&
                                            State.isNotSet(state.board, vertex, 2))
                                        num2Pieces++
                                }
                                1 -> {
                                    if (State.isSet(state.board, vertex, 0, parPlayer) &&
                                            State.isNotSet(state.board, vertex, 2))
                                        num2Pieces++
                                    else if (State.isSet(state.board, vertex, 2, parPlayer) &&
                                            State.isNotSet(state.board, vertex, 0))
                                        num2Pieces++
                                }
                                2 -> {
                                    if (State.isSet(state.board, vertex, 1, parPlayer) &&
                                            State.isNotSet(state.board, vertex, 0))
                                        num2Pieces++
                                }
                            }
                        }
                    }
                    //endregion
                    //region Num3Pieces
                    when (vertex) {
                        0, 2, 4, 6 -> {
                            if (State.isSet(state.board, nextVertex(vertex), level, parPlayer) &&
                                    State.isSet(state.board, previousVertex(vertex), level, parPlayer) &&
                                    State.isNotSet(state.board, nextVertex(nextVertex(vertex)), level) &&
                                    State.isNotSet(state.board, previousVertex(previousVertex(vertex)), level))
                                num3Pieces++

                        }
                        else -> {
                            when (level) {
                                1 -> {
                                    if ((State.isSet(state.board, nextVertex(vertex), nextLevel(level), parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parPlayer)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), nextLevel(level))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), nextLevel(level), parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parPlayer)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), nextLevel(level))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), level, parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parPlayer)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), level, parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), level, parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parPlayer)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), level, parPlayer)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parPlayer)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                }
                            }
                        }
                    }
                    //endregion
                }
                //endregion

                value += num2PiecesCoeff[1] * num2Pieces +
                        num3PiecesCoeff[1] * num3Pieces

                if (state.closedMorris && stateOpposite == parPlayer)
                    value += closedMorrisCoeff[2]
            }
        }

        when (parOppositePhase) {
            1 -> {
                var numMorrisesPlayer: Int = 0
                var blockedPieces: Int = 0
                var num2Pieces = 0
                var num3Pieces = 0
                //region Calcolo valori per il value
                for (position in enemyPosition) {
                    val vertex = MulinoGame.delinearizeVertex[position]
                    val level = MulinoGame.deliearizeLevel[position]
                    //region Calcolo morris
                    when (vertex) {
                        1, 3, 5, 7 -> {
                            if (State.isSet(state.board, MulinoGame.nextVertex(vertex), level, parOpposite) &&
                                    State.isSet(state.board, MulinoGame.previousVertex(vertex), level, parOpposite)) {
                                numMorrisesPlayer++
                            }
                            if (level == 1 && (State.isSet(state.board, vertex, MulinoGame.nextLevel(level), parOpposite)) &&
                                    (State.isSet(state.board, vertex, MulinoGame.nextLevel(MulinoGame.nextLevel(level)), parOpposite))) {
                                numMorrisesPlayer++
                            }
                        }
                    }
                    //endregion
                    //region BlockerPieces
                    if (MulinoGame.checkNoMoves(state, position, parOpposite))
                        blockedPieces++
                    //endregion
                    //region Num2Pieces
                    when (vertex) {
                        1, 3, 5, 7 -> {
                            if (State.isSet(state.board, MulinoGame.nextVertex(vertex), level, parOpposite) &&
                                    State.isNotSet(state.board, MulinoGame.previousVertex(vertex), level))
                                num2Pieces++
                            else if (State.isSet(state.board, MulinoGame.previousVertex(vertex), level, parOpposite) &&
                                    State.isNotSet(state.board, MulinoGame.nextVertex(vertex), level))
                                num2Pieces++
                            when (level) {
                                0 -> {
                                    if (State.isSet(state.board, vertex, 1, parOpposite) &&
                                            State.isNotSet(state.board, vertex, 2))
                                        num2Pieces++
                                }
                                1 -> {
                                    if (State.isSet(state.board, vertex, 0, parOpposite) &&
                                            State.isNotSet(state.board, vertex, 2))
                                        num2Pieces++
                                    else if (State.isSet(state.board, vertex, 2, parOpposite) &&
                                            State.isNotSet(state.board, vertex, 0))
                                        num2Pieces++
                                }
                                2 -> {
                                    if (State.isSet(state.board, vertex, 1, parOpposite) &&
                                            State.isNotSet(state.board, vertex, 0))
                                        num2Pieces++
                                }
                            }
                        }
                    }
                    //endregion
                    //region Num3Pieces
                    when (vertex) {
                        0, 2, 4, 6 -> {
                            if (State.isSet(state.board, nextVertex(vertex), level, parOpposite) &&
                                    State.isSet(state.board, previousVertex(vertex), level, parOpposite) &&
                                    State.isNotSet(state.board, nextVertex(nextVertex(vertex)), level) &&
                                    State.isNotSet(state.board, previousVertex(previousVertex(vertex)), level))
                                num3Pieces++

                        }
                        else -> {
                            when (level) {
                                1 -> {
                                    if ((State.isSet(state.board, nextVertex(vertex), nextLevel(level), parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parOpposite)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), nextLevel(level))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), nextLevel(level), parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parOpposite)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), nextLevel(level))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), level, parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parOpposite)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), level, parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), level, parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parOpposite)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), level, parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                }
                            }
                        }
                    }
                    //endregion
                }
                //endregion

                value += -morrisesNumberCoeff[0] * numMorrisesPlayer +
                        blockedOppPiecesCoeff[0] * blockedPieces -
                        piecesNumberCoeff[0] * state.checkersOnBoard[parOpposite] -
                        piecesNumberCoeff[0] * state.checkers[parOpposite] -
                        num2PiecesCoeff[0] * num2Pieces -
                        num3PiecesCoeff[0] * num3Pieces/*-
                        15.0 * numeroStruttureParallele(state.board, parOpposite)*//* -
                        5.0 * numeroStruttureParallele(state.board, parPlayer)*/

            }

            2 -> {
                value += -morrisesNumberCoeff[1] * game.getNumMorrises(state, enemyPosition, parOpposite) +
                        blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, enemyPosition, parOpposite) -
                        piecesNumberCoeff[1] * state.checkersOnBoard[parOpposite] /*+
                        15.0 * numeroStruttureParallele(state.board, parOpposite)*///-
                // aggiunta adesso
                //num2PiecesCoeff[0] * game.getNum2Conf(state, enemyPosition, parOpposite)

                var hasOpenMorris = false
                var enemyImpossible = true
                var bonus = false

                outer@ for (position in enemyPosition) {
                    for (adiacentPosition in MulinoGame.adiacentPositions[position]) {
                        val vertex = MulinoGame.delinearizeVertex[adiacentPosition]
                        val level = MulinoGame.deliearizeLevel[adiacentPosition]
                        if (State.isNotSet(state.board, vertex, level) && MulinoGame.checkMorris(state, position, adiacentPosition, parOpposite)) {
                            hasOpenMorris = true
                            if (level == 2 || level == 0)
                                bonus = true
                            if (parPlayer == state.playerType) {
                                impossible@ for (adiacentToMorris in MulinoGame.adiacentPositions[adiacentPosition]) {
                                    // vero solo se entrambi in fase 2, se uno in fase 2 e l'altro in fase 3 non è vero ma meh..
                                    if (State.isSet(state.board, adiacentToMorris, parPlayer)) {
                                        enemyImpossible = false
                                        break@impossible
                                    }
                                }
                            }
                            break@outer
                        }
                    }
                }

                if (hasOpenMorris) {
                    if (enemyImpossible && parPlayer == state.playerType) { // mio turno)
                        if (bonus)
                            value -= 15.0
                        value -= openedMorrisCoeff * 1.5
                    } else {
                        if (bonus)
                            value -= 15.0
                        value -= openedMorrisCoeff
                    }
                }
                if (game.hasDoubleMorris(state, enemyPosition, parOpposite))
                    value -= doubleMorrisCoeff
            }

            3 -> {
                var num2Pieces = 0
                var num3Pieces = 0

                //region Calcolo valori per il value
                for (position in enemyPosition) {
                    val vertex = MulinoGame.delinearizeVertex[position]
                    val level = MulinoGame.deliearizeLevel[position]
                    //region Num2Pieces
                    when (vertex) {
                        1, 3, 5, 7 -> {
                            if (State.isSet(state.board, MulinoGame.nextVertex(vertex), level, parOpposite) &&
                                    State.isNotSet(state.board, MulinoGame.previousVertex(vertex), level))
                                num2Pieces++
                            else if (State.isSet(state.board, MulinoGame.previousVertex(vertex), level, parOpposite) &&
                                    State.isNotSet(state.board, MulinoGame.nextVertex(vertex), level))
                                num2Pieces++
                            when (level) {
                                0 -> {
                                    if (State.isSet(state.board, vertex, 1, parOpposite) &&
                                            State.isNotSet(state.board, vertex, 2))
                                        num2Pieces++
                                }
                                1 -> {
                                    if (State.isSet(state.board, vertex, 0, parOpposite) &&
                                            State.isNotSet(state.board, vertex, 2))
                                        num2Pieces++
                                    else if (State.isSet(state.board, vertex, 2, parOpposite) &&
                                            State.isNotSet(state.board, vertex, 0))
                                        num2Pieces++
                                }
                                2 -> {
                                    if (State.isSet(state.board, vertex, 1, parOpposite) &&
                                            State.isNotSet(state.board, vertex, 0))
                                        num2Pieces++
                                }
                            }
                        }
                    }
                    //endregion
                    //region Num3Pieces
                    when (vertex) {
                        0, 2, 4, 6 -> {
                            if (State.isSet(state.board, nextVertex(vertex), level, parOpposite) &&
                                    State.isSet(state.board, previousVertex(vertex), level, parOpposite) &&
                                    State.isNotSet(state.board, nextVertex(nextVertex(vertex)), level) &&
                                    State.isNotSet(state.board, previousVertex(previousVertex(vertex)), level))
                                num3Pieces++

                        }
                        else -> {
                            when (level) {
                                1 -> {
                                    if ((State.isSet(state.board, nextVertex(vertex), nextLevel(level), parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parOpposite)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), nextLevel(level))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), nextLevel(level), parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parOpposite)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), nextLevel(level))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)))) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), level, parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parOpposite)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, nextVertex(vertex), level, parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), level, parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(level), parOpposite)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                        num3Pieces++
                                    if ((State.isSet(state.board, previousVertex(vertex), level, parOpposite)) &&
                                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), parOpposite)) &&
                                            (State.isNotSet(state.board, nextVertex(vertex), level)) &&
                                            (State.isNotSet(state.board, vertex, nextLevel(level))))
                                        num3Pieces++
                                }
                            }
                        }
                    }
                    //endregion
                }

                value += -num2PiecesCoeff[1] * num2Pieces -
                        num3PiecesCoeff[1] * num3Pieces
                if (state.closedMorris && stateOpposite == parOpposite) {
                    value -= closedMorrisCoeff[2]
                }
            }
        }

        return value
    }
    //endregion

    //TODO("DA TOGLIERE")
    fun evalTest(state: State, player: Int): Double = eval(state, player)

    private val toExternalPositions = hashMapOf(
            Pair(Pair(0, 0), "a1"),
            Pair(Pair(1, 0), "a4"),
            Pair(Pair(2, 0), "a7"),
            Pair(Pair(0, 1), "b2"),
            Pair(Pair(1, 1), "b4"),
            Pair(Pair(2, 1), "b6"),
            Pair(Pair(0, 2), "c3"),
            Pair(Pair(1, 2), "c4"),
            Pair(Pair(2, 2), "c5"),
            Pair(Pair(3, 2), "d5"),
            Pair(Pair(3, 1), "d6"),
            Pair(Pair(3, 0), "d7"),
            Pair(Pair(7, 0), "d1"),
            Pair(Pair(7, 1), "d2"),
            Pair(Pair(7, 2), "d3"),
            Pair(Pair(6, 2), "e3"),
            Pair(Pair(5, 2), "e4"),
            Pair(Pair(4, 2), "e5"),
            Pair(Pair(6, 1), "f2"),
            Pair(Pair(5, 1), "f4"),
            Pair(Pair(4, 1), "f6"),
            Pair(Pair(6, 0), "g1"),
            Pair(Pair(5, 0), "g4"),
            Pair(Pair(4, 0), "g7")
    )


    private fun getPhase(state: State) = when {
        state.currentPhase == 1 -> 1
        state.currentPhase == 2 -> 2
        state.currentPhase == 3 && state.checkersOnBoard[state.playerType] > 3 -> 2
        state.currentPhase == 3 && state.checkersOnBoard[state.playerType] <= 3 -> 2
        else -> throw IllegalStateException("Fase non riconosciuta")
    }

}

fun <T> FibonacciHeap<T>.dequeueAll(): ArrayList<T> {
    val mutableList = ArrayList<T>(16)
    while (!this.isEmpty) {
        mutableList.add(this.dequeueMin().value)
    }
    return mutableList
}

fun main(args: Array<String>) {
    var board = intArrayOf(0, 0)
    MulinoGame.addPiece(board, 6, 0)
    MulinoGame.addPiece(board, 13, 0)
    MulinoGame.addPiece(board, 14, 0)
    MulinoGame.addPiece(board, 16, 1)
    MulinoGame.addPiece(board, 15, 1)
    MulinoGame.addPiece(board, 18, 1)

    val state = State(0, board, intArrayOf(6, 6), intArrayOf(3, 3), false)
    val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 14.0, 43.0, 10.0, 8.0, 7.0, 42.0, 1086.0, 10.0, 1.0, 16.0, 1190.0), -700.00, 700.00, Int.MAX_VALUE / 2000, sortAction = false)
    val actions = MulinoGame.getActions(state)
    for (action in actions) {
        val newState = MulinoGame.getResult(state, action)

        val ph1Action = Phase1Action()
        val ac = ActionMapper.actionMap[action]!!
        if (ac.remove != -1) {
            val removeVertex = MulinoGame.delinearizeVertex[ac.remove]
            val removeLevel = MulinoGame.deliearizeLevel[ac.remove]
            ph1Action.removeOpponentChecker = MulinoGame.toExternalPositions[Pair(removeVertex, removeLevel)]!!
        }
        val toVertex = MulinoGame.delinearizeVertex[ac.to]
        val toLevel = MulinoGame.deliearizeLevel[ac.to]
        ph1Action.putPosition = MulinoGame.toExternalPositions[Pair(toVertex, toLevel)]!!
        println("$ph1Action -> ${search.eval(newState, 0)}")

        //println(MulinoGame.getNum2Conf(newState, 0))

        //println(MulinoGame.getNum3Conf(newState, 0))

        //println("\n\n")
    }
}

/*
fun main(args: Array<String>) {

    var newState = State(playerType = Checker.WHITE, checkers = intArrayOf(0, 0), checkersOnBoard = intArrayOf(9, 5), currentPhase = 2)
    MulinoGame.addPiece(newState, "a1", Checker.WHITE)
    MulinoGame.addPiece(newState, "a7", Checker.WHITE)
    MulinoGame.addPiece(newState, "b4",Checker.WHITE)
    MulinoGame.addPiece(newState, "c3", Checker.WHITE)
    MulinoGame.addPiece(newState, "d2", Checker.WHITE)
    MulinoGame.addPiece(newState, "d5",Checker.WHITE)
    MulinoGame.addPiece(newState, "e4", Checker.WHITE)
    MulinoGame.addPiece(newState, "f4",Checker.WHITE)
    MulinoGame.addPiece(newState, "g4", Checker.WHITE)
    MulinoGame.addPiece(newState, "c5", Checker.BLACK)
    MulinoGame.addPiece(newState, "e5",Checker.BLACK)
    MulinoGame.addPiece(newState, "e3", Checker.BLACK)
    MulinoGame.addPiece(newState, "d3", Checker.BLACK)
    MulinoGame.addPiece(newState, "f2", Checker.BLACK)

    /*
    initialState.addPiece(Pair('f',4), Checker.WHITE)
    initialState.addPiece(Pair('a',4), Checker.WHITE)
    initialState.addPiece(Pair('a',1), Checker.WHITE)
    initialState.addPiece(Pair('g',7), Checker.BLACK)
    initialState.addPiece(Pair('g',4), Checker.BLACK)
    initialState.addPiece(Pair('d',2), Checker.BLACK)
    */

    val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 14.0, 43.0, 10.0, 8.0, 7.0, 42.0, 1086.0, 10.0, 1.0, 16.0, 1190.0), -1000.00, 1000.00, 1)
    val action = search.makeDecision(newState)
    println("Azione scelta: $action")

}
        */