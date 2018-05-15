package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.domain.State.Checker
import it.unibo.mulino.qlearning.player.model.Position
import it.unibo.utils.FibonacciHeap
import it.unibo.utils.IterariveDeepingAlphaBetaSearch
import it.unibo.ai.didattica.mulino.domain.State as ChesaniState
import it.unibo.mulino.qlearning.player.model.State as QLearningState

class MulinoAlphaBetaSearch(coefficients: Array<Double>,
                            utilMin: Double,
                            utilMax: Double,
                            time: Int,
                            private val sortAction: Boolean = false) : IterariveDeepingAlphaBetaSearch<State, String, Checker>(MulinoGame, utilMin, utilMax, time) {

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
    override fun makeDecision(state: State?): String {
        if (state == null) throw IllegalArgumentException("Lo stato è null")

        if (state.currentPhase == 1 || state.currentPhase == 2) {
            this.limit = false
        }
        ordered = 0
        val decision = super.makeDecision(state)
        println("ordered $ordered")
        return decision
    }

    override fun eval(state: State?, player: Checker?): Double {
        if (state == null) throw IllegalArgumentException("State is null")
        if (player == null) throw IllegalArgumentException("Player is null")

        var amount = super.eval(state, player)
        val game = game as MulinoGame
        val opposite = game.opposite[player]!!
        val intPlayer = game.checkersToInt[player]!!
        val intOpposite =  game.checkersToInt[opposite]!!
        when (state.currentPhase) {
            1 -> {
                amount += morrisesNumberCoeff[0] * (game.getNumMorrises(state, player) - game.getNumMorrises(state, opposite)) +
                        blockedOppPiecesCoeff[0] * (game.getBlockedPieces(state, opposite) - game.getBlockedPieces(state, player)) +
                        piecesNumberCoeff[0] * (state.checkersOnBoard[intPlayer] - state.checkersOnBoard[intOpposite] - (state.checkers[intOpposite] - state.checkers[intPlayer])) +
                        num2PiecesCoeff[0] * (game.getNum2Conf(state, player) - game.getNum2Conf(state, opposite)) +
                        num3PiecesCoeff[0] * (game.getNum3Conf(state, player) - game.getNum3Conf(state, opposite))
                if (state.closedMorris){
                    when (game.opposite[state.checker]) {
                        player -> amount += closedMorrisCoeff[0]
                        opposite ->amount -= closedMorrisCoeff[0]
                    }
                }
            }
            2 -> {
                amount += morrisesNumberCoeff[1] * (game.getNumMorrises(state, player) - game.getNumMorrises(state, opposite)) +
                        blockedOppPiecesCoeff[1] * (game.getBlockedPieces(state, opposite) - game.getBlockedPieces(state, player)) +
                        piecesNumberCoeff[1] * (state.checkersOnBoard[intPlayer] - state.checkersOnBoard[intOpposite])
                if (state.closedMorris){
                    when (game.opposite[state.checker]) {
                        player -> amount += closedMorrisCoeff[1]
                        opposite ->amount -= closedMorrisCoeff[1]
                    }
                }
                if (game.hasOpenedMorris(state, player))
                    amount += openedMorrisCoeff
                if (game.hasDoubleMorris(state, player))
                    amount += doubleMorrisCoeff

                if (game.hasOpenedMorris(state, opposite))
                    amount -= openedMorrisCoeff
                if (game.hasDoubleMorris(state, opposite))
                    amount -= doubleMorrisCoeff
            }
            3 -> {
                var amountPlayer = 0.0
                var amountOpposite = 0.00
                if (state.checkersOnBoard[intPlayer] == 3) {
                    amountPlayer = num2PiecesCoeff[1] * game.getNum2Conf(state, player) +
                            num3PiecesCoeff[1] * game.getNum3Conf(state, player)
                    if (state.closedMorris && game.opposite[state.checker] == player) {
                        amountPlayer += closedMorrisCoeff[2]
                    }
                } else {
                    amountPlayer = morrisesNumberCoeff[1] * game.getNumMorrises(state, player) +
                            blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, opposite)  +
                            piecesNumberCoeff[1] * state.checkersOnBoard[intPlayer]
                    if (state.closedMorris && game.opposite[state.checker]==player) {
                        amountPlayer += closedMorrisCoeff[1]
                    }
                    if (game.hasOpenedMorris(state, player))
                        amountPlayer += openedMorrisCoeff
                    if (game.hasDoubleMorris(state, player))
                        amountPlayer += doubleMorrisCoeff
                }

                if (state.checkersOnBoard[intOpposite] == 3) {
                    amountOpposite = num2PiecesCoeff[1] * game.getNum2Conf(state, opposite) +
                            num3PiecesCoeff[1] * game.getNum3Conf(state, opposite)
                    if (state.closedMorris && game.opposite[state.checker] == opposite) {
                        amountOpposite += closedMorrisCoeff[2]
                    }
                } else {
                    amountOpposite = morrisesNumberCoeff[1] * game.getNumMorrises(state, opposite) +
                            blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, player)  +
                            piecesNumberCoeff[1] * state.checkersOnBoard[intPlayer]
                    if (state.closedMorris && game.opposite[state.checker]==opposite) {
                        amountOpposite += closedMorrisCoeff[1]
                    }
                    if (game.hasOpenedMorris(state, opposite))
                        amountOpposite += openedMorrisCoeff
                    if (game.hasDoubleMorris(state, opposite))
                        amountOpposite += doubleMorrisCoeff
                }
                amount += amountPlayer - amountOpposite
            }
        }
        //println("Evaluation state for player $player : ${game.printState(state)} -> $amount")
        return amount
    }

    private val sorter = QLearningPlayerAlternative({ 0.0 })

    override fun orderActions(state: State?, actions: MutableList<String>?, player: Checker?, depth: Int): MutableList<String> {
        if (state == null) throw IllegalArgumentException("State is null")
        if (actions == null) throw IllegalArgumentException("Actions is null")
        if (player == null) throw IllegalArgumentException("Player is null")
        if (depth > 3 || !sortAction)
            return actions

        return when (getPhase(state)) {
            1 -> sorter.playPhase1(state, actions).map { it.first }.toMutableList()// .sort(state,actions).map { it.first }.toMutableList()
            2 -> sorter.playPhase2(state, actions).map { it.first }.toMutableList()
            3 -> sorter.playPhase3(state, actions).map { it.first }.toMutableList()
            else -> throw IllegalStateException("Fase non valida")
        }
        //if(!sortAction) return actions
        //println("Sorting")
        /*return when (getPhase(state)) {
            1 -> sorter.playPhase1(state.remapToQLearningState(player)).map {
                //println("Action ${it.first} -> ${it.second}")
                when (it.first.remove.isPresent) {
                    true -> "1${it.first.to.get().toExternal()}${it.first.remove.get().toExternal()}"
                    false -> "1${it.first.to.get().toExternal()}"
                }
            }.toMutableList()// .sort(state,actions).map { it.first }.toMutableList()
            2 -> sorter.playPhase2(state.remapToQLearningState(player)).map {
                //println("Action ${it.first} -> ${it.second}")
                when (it.first.remove.isPresent) {
                    true -> "2${it.first.from.get().toExternal()}${it.first.to.get().toExternal()}${it.first.remove.get().toExternal()}"
                    false -> "2${it.first.from.get().toExternal()}${it.first.to.get().toExternal()}"
                }
            }.toMutableList()
            3 -> sorter.playPhase3(state.remapToQLearningState(player)).map {
                //println("Action ${it.first} -> ${it.second}")
                when (it.first.remove.isPresent) {
                    true -> "3${it.first.from.get().toExternal()}${it.first.to.get().toExternal()}${it.first.remove.get().toExternal()}"
                    false -> "3${it.first.from.get().toExternal()}${it.first.to.get().toExternal()}"
                }
            }.toMutableList()
            else -> throw IllegalStateException("Fase non valida")
        }*/
    }

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

        state.currentPhase == 3 && when (state.checker) {
            Checker.WHITE -> state.checkersOnBoard[0]
            Checker.BLACK -> state.checkersOnBoard[1]
            else -> throw IllegalStateException("Checker non valido")
        } > 3 -> 2

        state.currentPhase == 3 && when (state.checker) {
            Checker.WHITE -> state.checkersOnBoard[0]
            Checker.BLACK -> state.checkersOnBoard[1]
            else -> throw IllegalStateException("Checker non valido")
        } <= 3 -> 2

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

fun main(args: Array<String>) {

    var newState = State(checker = Checker.WHITE, checkers = intArrayOf(0,0), checkersOnBoard = intArrayOf(9,4), currentPhase = 2)
    MulinoGame.addPiece(newState, "a4",Checker.WHITE)
    MulinoGame.addPiece(newState, "b4",Checker.WHITE)
    MulinoGame.addPiece(newState, "c4",Checker.WHITE)
    MulinoGame.addPiece(newState, "c5",Checker.WHITE)
    MulinoGame.addPiece(newState, "d5",Checker.WHITE)
    MulinoGame.addPiece(newState, "d6",Checker.WHITE)
    MulinoGame.addPiece(newState, "d7",Checker.WHITE)
    MulinoGame.addPiece(newState, "f4",Checker.WHITE)
    MulinoGame.addPiece(newState, "e3",Checker.WHITE)
    MulinoGame.addPiece(newState, "a7",Checker.BLACK)
    MulinoGame.addPiece(newState, "b6",Checker.BLACK)
    MulinoGame.addPiece(newState, "e5",Checker.BLACK)
    MulinoGame.addPiece(newState, "f6",Checker.BLACK)

    /*
    initialState.addPiece(Pair('f',4), Checker.WHITE)
    initialState.addPiece(Pair('a',4), Checker.WHITE)
    initialState.addPiece(Pair('a',1), Checker.WHITE)
    initialState.addPiece(Pair('g',7), Checker.BLACK)
    initialState.addPiece(Pair('g',4), Checker.BLACK)
    initialState.addPiece(Pair('d',2), Checker.BLACK)
    */

    val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 14.0, 43.0, 10.0, 8.0, 7.0, 42.0, 1086.0, 10.0, 1.0, 16.0, 1190.0), -10000.00, 10000.00, 1)
    val action = search.makeDecision(newState)
    println("Azione scelta: $action")

}