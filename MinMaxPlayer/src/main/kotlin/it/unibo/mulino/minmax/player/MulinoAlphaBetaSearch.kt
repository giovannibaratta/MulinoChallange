package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.domain.State.Checker
import it.unibo.utils.*

class MulinoAlphaBetaSearch(coefficients: Array<Double>,
                                  utilMin: Double,
                                  utilMax: Double,
                                  time: Int) : IterariveDeepingAlphaBetaSearch<State, String, Checker>(MulinoGame, utilMin, utilMax, time) {

    private val closedMorrisCoeff = doubleArrayOf(coefficients[0],coefficients[6], coefficients[15])
    private val morrisesNumberCoeff = doubleArrayOf(coefficients[1],coefficients[7])
    private val blockedOppPiecesCoeff = doubleArrayOf(coefficients[2],coefficients[8])
    private val piecesNumberCoeff = doubleArrayOf(coefficients[3],coefficients[9])
    private val num2PiecesCoeff = doubleArrayOf(coefficients[4],coefficients[13])
    private val num3PiecesCoeff =doubleArrayOf(coefficients[5],coefficients[14])
    private val openedMorrisCoeff = coefficients[10]
    private val doubleMorrisCoeff = coefficients[11]
    private val winningConfCoeff = doubleArrayOf(coefficients[12],coefficients[16])

    override fun makeDecision(state: State?): String {
        return super.makeDecision(state)
    }

    override fun eval(state: State?, player: Checker?): Double {
        var amount = super.eval(state, player)
        val game = game as MulinoGame
        var opposite = game.opposite[player]!!
        val intPlayer = game.checkersToInt[player]!!
        val intOpposite =  game.checkersToInt[opposite]!!
        when(state!!.currentPhase) {
            1 -> {
                amount += morrisesNumberCoeff[0] * (game.getNumMorrises(state, player!!) - game.getNumMorrises(state, opposite)) +
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
                amount += morrisesNumberCoeff[1] * (game.getNumMorrises(state, player!!) - game.getNumMorrises(state, opposite)) +
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
                    amountPlayer = num2PiecesCoeff[1] * game.getNum2Conf(state, player!!) +
                            num3PiecesCoeff[1] * game.getNum3Conf(state, player)
                    if (state.closedMorris && game.opposite[state.checker] == player) {
                        amountPlayer += closedMorrisCoeff[2]
                    }
                } else {
                    amountPlayer = morrisesNumberCoeff[1] * game.getNumMorrises(state, player!!) +
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
                    amountOpposite = num2PiecesCoeff[1] * game.getNum2Conf(state, opposite!!) +
                            num3PiecesCoeff[1] * game.getNum3Conf(state, opposite)
                    if (state.closedMorris && game.opposite[state.checker] == opposite) {
                        amountOpposite += closedMorrisCoeff[2]
                    }
                } else {
                    amountOpposite =  morrisesNumberCoeff[1] * game.getNumMorrises(state, opposite!!) +
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
                amount+=amountPlayer-amountOpposite
            }
        }
        //println("Evaluation state for player $player : ${game.printState(state)} -> $amount")
        return amount
    }

    /*
    override fun orderActions(state: State?, actions: MutableList<String>?, player: Checker?, depth: Int): MutableList<String> {
        val orderedActions = FibonacciHeap<String>()
        val game = game as MulinoGame
        for(action in actions!!){
            /*var value = game.density(state!!, action.substring(1,3), player!!)
            if(action.length>3)
                value = game.density(state!!, action.substring(3,5), player!!) - value
            */
            orderedActions.enqueue(action, action.length.toDouble())
        }
        return orderedActions.dequeueAll()
    }
    */

}

fun <T> FibonacciHeap<T>.dequeueAll() : MutableList<T>{
    val mutableList = arrayListOf<T>()
    while(!this.isEmpty){
        mutableList.add(this.dequeueMin().value)
    }
    return mutableList
}

fun main(args: Array<String>) {

    var newState = State(checker = Checker.WHITE, checkers = intArrayOf(0,0), checkersOnBoard = intArrayOf(9,5), currentPhase = 2)
    MulinoGame.addPiece(newState, "a1",Checker.WHITE)
    MulinoGame.addPiece(newState, "a7",Checker.WHITE)
    MulinoGame.addPiece(newState, "b4",Checker.WHITE)
    MulinoGame.addPiece(newState, "c3",Checker.WHITE)
    MulinoGame.addPiece(newState, "d2",Checker.WHITE)
    MulinoGame.addPiece(newState, "d5",Checker.WHITE)
    MulinoGame.addPiece(newState, "e4",Checker.WHITE)
    MulinoGame.addPiece(newState, "f4",Checker.WHITE)
    MulinoGame.addPiece(newState, "g4",Checker.WHITE)
    MulinoGame.addPiece(newState, "c5",Checker.BLACK)
    MulinoGame.addPiece(newState, "e5",Checker.BLACK)
    MulinoGame.addPiece(newState, "e3",Checker.BLACK)
    MulinoGame.addPiece(newState, "d3",Checker.BLACK)
    MulinoGame.addPiece(newState, "f2",Checker.BLACK)

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