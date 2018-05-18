package it.unibo.mulino.minmax.player

import it.unibo.mulino.qlearning.player.model.Position
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
            orderActions = { s, al, p, d -> al },
            maxTime = timeLimit)

    fun makeDecision(state: State): String = iterativeSearch.makeDecision(state)

    /*override fun makeDecision(state: State?): String {
        if (state == null) throw IllegalArgumentException("Lo stato è null")

        if (state.currentPhase == 1 || state.currentPhase == 2) {
            this.limit = false
        }
        ordered = 0
        val decision = super.makeDecision(state)
        println("ordered $ordered")
        return decision
    }*/


    /*override*/ fun eval(state: State?, player: Int?): Double {
        var value = 0.0 /*super.eval(state, player)*/

        if (state == null) throw IllegalArgumentException("state null")
        if (player == null) throw IllegalArgumentException("player null")

        val statePlayer = state.playerType
        val stateOpposite = Math.abs(state.playerType - 1)
        val parOpposite = Math.abs(player - 1)
        val parPlayer = player
        val game = MulinoGame

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

        when (parPlayerPhase) {
            1 -> {
                value += morrisesNumberCoeff[0] * game.getNumMorrises(state, parPlayer) -
                        blockedOppPiecesCoeff[0] * game.getBlockedPieces(state, parPlayer) +
                        piecesNumberCoeff[0] * state.checkersOnBoard[statePlayer] +
                        piecesNumberCoeff[0] * state.checkers[statePlayer] +
                        num2PiecesCoeff[0] * game.getNum2Conf(state, parPlayer) +
                        num3PiecesCoeff[0] * game.getNum3Conf(state, parPlayer)
                if (state.closedMorris) {
                    when (stateOpposite) {
                        parPlayer -> value += closedMorrisCoeff[0]
                        parOpposite -> value -= closedMorrisCoeff[0]
                    }
                }
            }

            2 -> {
                value += morrisesNumberCoeff[1] * game.getNumMorrises(state, parPlayer) -
                        blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, parPlayer) +
                        piecesNumberCoeff[1] * state.checkersOnBoard[statePlayer]

                if (state.closedMorris) {
                    when (game.opposite(state.playerType)) {
                        parPlayer -> value += closedMorrisCoeff[1]
                        parOpposite -> value -= closedMorrisCoeff[1]
                    }
                }
                if (game.hasOpenedMorris(state, parPlayer))
                    value += openedMorrisCoeff
                if (game.hasDoubleMorris(state, parPlayer))
                    value += doubleMorrisCoeff
            }

            3 -> {
                value += num2PiecesCoeff[1] * game.getNum2Conf(state, parPlayer) +
                        num3PiecesCoeff[1] * game.getNum3Conf(state, parPlayer)

                if (state.closedMorris && game.opposite(state.playerType) == parPlayer)
                    value += closedMorrisCoeff[2]
            }
        }

        when (parOppositePhase) {
            1 -> {
                value += -morrisesNumberCoeff[0] * game.getNumMorrises(state, parOpposite) +
                        blockedOppPiecesCoeff[0] * game.getBlockedPieces(state, parOpposite) -
                        piecesNumberCoeff[0] * state.checkersOnBoard[stateOpposite] -
                        piecesNumberCoeff[0] * state.checkers[stateOpposite] -
                        num2PiecesCoeff[0] * game.getNum2Conf(state, parOpposite) -
                        num3PiecesCoeff[0] * -game.getNum3Conf(state, parOpposite)
            }

            2 -> {
                value += -morrisesNumberCoeff[1] * game.getNumMorrises(state, parOpposite) +
                        blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, parOpposite) -
                        piecesNumberCoeff[1] * state.checkersOnBoard[stateOpposite]

                if (game.hasOpenedMorris(state, parOpposite))
                    value -= openedMorrisCoeff
                if (game.hasDoubleMorris(state, parOpposite))
                    value -= doubleMorrisCoeff
            }

            3 -> {
                value += -num2PiecesCoeff[1] * game.getNum2Conf(state, parOpposite) -
                        num3PiecesCoeff[1] * game.getNum3Conf(state, parOpposite)
                if (state.closedMorris && game.opposite(state.playerType) == parOpposite) {
                    value -= closedMorrisCoeff[2]
                }
            }
        }

        return value
    }

    //TODO("DA TOGLIERE")
    fun evalTest(state: State, player: Int): Double = eval(state, player)

    /*
    fun oldeval(state: State?, player: Int?): Double {
        //if (state == null) throw IllegalArgumentException("State is null")
        //if (player == null) throw IllegalArgumentException("Player is null")

        var amount = super.eval(state, player)

        if (state == null) throw IllegalArgumentException("State is null")
        if (player == null) throw IllegalArgumentException("Player is null")


        val game = game as MulinoGame
        val statePlayer = state.playerType
        val stateOpposite = Math.abs(state.playerType - 1)
        val parOpposite = Math.abs(player - 1)
        val parPlayer = player
        //val intOpposite =  game.checkersToInt[opposite]!!
        when (state.currentPhase) {
            1 -> {
                amount += morrisesNumberCoeff[0] * (game.getNumMorrises(state, parPlayer) - game.getNumMorrises(state, parOpposite)) +
                        blockedOppPiecesCoeff[0] * (game.getBlockedPieces(state, parOpposite) - game.getBlockedPieces(state, parPlayer)) +
                        piecesNumberCoeff[0] * (state.checkersOnBoard[statePlayer] - state.checkersOnBoard[stateOpposite] - (state.checkers[stateOpposite] - state.checkers[statePlayer])) +
                        num2PiecesCoeff[0] * (game.getNum2Conf(state, parPlayer) - game.getNum2Conf(state, parOpposite)) +
                        num3PiecesCoeff[0] * (game.getNum3Conf(state, parPlayer) - game.getNum3Conf(state, parOpposite))
                if (state.closedMorris){
                    when (stateOpposite) {
                        parPlayer -> amount += closedMorrisCoeff[0]
                        parOpposite -> amount -= closedMorrisCoeff[0]
                    }
                }
            }
            2 -> {
                amount += morrisesNumberCoeff[1] * (game.getNumMorrises(state, parPlayer) - game.getNumMorrises(state, parOpposite)) +
                        blockedOppPiecesCoeff[1] * (game.getBlockedPieces(state, parOpposite) - game.getBlockedPieces(state, parPlayer)) +
                        piecesNumberCoeff[1] * (state.checkersOnBoard[statePlayer] - state.checkersOnBoard[stateOpposite])
                if (state.closedMorris){
                    when (game.opposite(state.playerType)) {
                        parPlayer -> amount += closedMorrisCoeff[1]
                        parOpposite -> amount -= closedMorrisCoeff[1]
                    }
                }
                if (game.hasOpenedMorris(state, parPlayer))
                    amount += openedMorrisCoeff
                if (game.hasDoubleMorris(state, parPlayer))
                    amount += doubleMorrisCoeff
                if (game.hasOpenedMorris(state, parOpposite))
                    amount -= openedMorrisCoeff
                if (game.hasDoubleMorris(state, parOpposite))
                    amount -= doubleMorrisCoeff
            }
            3 -> {
                var amountPlayer = 0.0
                var amountOpposite = 0.00
                if (state.checkersOnBoard[statePlayer] == 3) {
                    amountPlayer = num2PiecesCoeff[1] * game.getNum2Conf(state, parPlayer) +
                            num3PiecesCoeff[1] * game.getNum3Conf(state, parPlayer)
                    if (state.closedMorris && game.opposite(state.playerType) == parPlayer) {
                        amountPlayer += closedMorrisCoeff[2]
                    }
                } else {
                    amountPlayer = morrisesNumberCoeff[1] * game.getNumMorrises(state, parPlayer) +
                            blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, parOpposite) +
                            piecesNumberCoeff[1] * state.checkersOnBoard[statePlayer]
                    if (state.closedMorris && game.opposite(state.playerType) == parPlayer) {
                        amountPlayer += closedMorrisCoeff[1]
                    }
                    if (game.hasOpenedMorris(state, parPlayer))
                        amountPlayer += openedMorrisCoeff
                    if (game.hasDoubleMorris(state, parPlayer))
                        amountPlayer += doubleMorrisCoeff
                }

                if (state.checkersOnBoard[stateOpposite] == 3) {
                    amountOpposite = num2PiecesCoeff[1] * game.getNum2Conf(state, parOpposite) +
                            num3PiecesCoeff[1] * game.getNum3Conf(state, parOpposite)
                    if (state.closedMorris && game.opposite(state.playerType) == parOpposite) {
                        amountOpposite += closedMorrisCoeff[2]
                    }
                } else {
                    amountOpposite = morrisesNumberCoeff[1] * game.getNumMorrises(state, parOpposite) +
                            blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, parPlayer) +
                            piecesNumberCoeff[1] * state.checkersOnBoard[statePlayer]
                    if (state.closedMorris && game.opposite(state.playerType) == parOpposite) {
                        amountOpposite += closedMorrisCoeff[1]
                    }
                    if (game.hasOpenedMorris(state, parOpposite))
                        amountOpposite += openedMorrisCoeff
                    if (game.hasDoubleMorris(state, parOpposite))
                        amountOpposite += doubleMorrisCoeff
                }
                amount += amountPlayer - amountOpposite
            }
        }
        //println("Evaluation state for player $player : ${game.printState(state)} -> $amount")
        return amount
    }*/

    private val sorter = QLearningPlayerAlternative({ 0.0 })


//    override fun orderActions(state: State?, actions: MutableList<String>?, player: Int?, depth: Int): MutableList<String> {
//        if (state == null) throw IllegalArgumentException("State is null")
//        if (actions == null) throw IllegalArgumentException("Actions is null")
//        if (player == null) throw IllegalArgumentException("Player is null")
//        if (depth > 2 || !sortAction)
//            return actions
//
//        return when (getPhase(state)) {
//            1 -> sorter.playPhase1(state, actions)//.map { it.first }.toMutableList()// .sort(state,actions).map { it.first }.toMutableList()
//            2 -> sorter.playPhase2(state, actions)//.map { it.first }.toMutableList()
//            3 -> sorter.playPhase3(state, actions)//.map { it.first }.toMutableList()
//            else -> throw IllegalStateException("Fase non valida")
//        }
//        //if(!sortAction) return actions
//        //println("Sorting")
//        /*return when (getPhase(state)) {
//            1 -> sorter.playPhase1(state.remapToQLearningState(player)).map {
//                //println("Action ${it.first} -> ${it.second}")
//                when (it.first.remove.isPresent) {
//                    true -> "1${it.first.to.get().toExternal()}${it.first.remove.get().toExternal()}"
//                    false -> "1${it.first.to.get().toExternal()}"
//                }
//            }.toMutableList()// .sort(state,actions).map { it.first }.toMutableList()
//            2 -> sorter.playPhase2(state.remapToQLearningState(player)).map {
//                //println("Action ${it.first} -> ${it.second}")
//                when (it.first.remove.isPresent) {
//                    true -> "2${it.first.from.get().toExternal()}${it.first.to.get().toExternal()}${it.first.remove.get().toExternal()}"
//                    false -> "2${it.first.from.get().toExternal()}${it.first.to.get().toExternal()}"
//                }
//            }.toMutableList()
//            3 -> sorter.playPhase3(state.remapToQLearningState(player)).map {
//                //println("Action ${it.first} -> ${it.second}")
//                when (it.first.remove.isPresent) {
//                    true -> "3${it.first.from.get().toExternal()}${it.first.to.get().toExternal()}${it.first.remove.get().toExternal()}"
//                    false -> "3${it.first.from.get().toExternal()}${it.first.to.get().toExternal()}"
//                }
//            }.toMutableList()
//            else -> throw IllegalStateException("Fase non valida")
//        }*/
//    }

    /*
        private fun State.remapToQLearningState(player: Checker): QLearningState = QLearningState(this.toChesaniState(), when (player) {
            ChesaniState.Checker.WHITE -> true
            Checker.BLACK -> false
            else -> throw IllegalStateException("Checker non valido")
        })

        private fun State.toChesaniState(): ChesaniState {
            val state = ChesaniState()
            state.blackCheckers = this.checkers[1]
            state.whiteCheckers = this.checkers[0]
            state.currentPhase = when (this.currentPhase) {
                1 -> ChesaniState.Phase.FIRST
                2 -> ChesaniState.Phase.SECOND
                3 -> ChesaniState.Phase.FINAL
                else -> throw IllegalStateException("Fase non valida")
            }
            state.blackCheckersOnBoard = this.checkersOnBoard[1]
            state.whiteCheckersOnBoard = this.checkersOnBoard[0]

            this.board.forEachIndexed { index, charArray ->
                state.board.put(toExternalPositions.get(Pair(index, 0)), charArray[0].toChecker())
                state.board.put(toExternalPositions.get(Pair(index, 1)), charArray[1].toChecker())
                state.board.put(toExternalPositions.get(Pair(index, 2)), charArray[2].toChecker())
            }

            return state
        }
    */
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

    private fun Char.toChecker() = when (this) {
        'e' -> ChesaniState.Checker.EMPTY
        'w' -> ChesaniState.Checker.WHITE
        'b' -> ChesaniState.Checker.BLACK
        else -> throw  IllegalArgumentException("Non esiste un mapping per $this")
    }

    private fun Position.toExternal(): String {
        val xPos: Char = 'a' + this.x
        val yPos: String = (this.y + 1).toString()
        return xPos + yPos
    }

    private fun getPhase(state: State) = when {
        state.currentPhase == 1 -> 1
        state.currentPhase == 2 -> 2
        state.currentPhase == 3 && state.checkersOnBoard[state.playerType] > 3 -> 2
        state.currentPhase == 3 && state.checkersOnBoard[state.playerType] <= 3 -> 2
        else -> throw IllegalStateException("Fase non riconosciuta")
    }

}

fun <T> FibonacciHeap<T>.dequeueAll() : MutableList<T>{
    val mutableList = arrayListOf<T>()
    while (!this.isEmpty) {
        mutableList.add(this.dequeueMin().value)
    }
    return mutableList
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