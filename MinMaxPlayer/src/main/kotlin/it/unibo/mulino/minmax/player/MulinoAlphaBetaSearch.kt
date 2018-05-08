package it.unibo.mulino.minmax.player


import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.domain.State.Checker

class MulinoAlphaBetaSearch(coefficients: Array<Double>,
                                  utilMin: Double,
                                  utilMax: Double,
                                  time: Int) : IterativeDeepeningAlphaBetaSearch<State, String, Checker>(MulinoGame, utilMin, utilMax, time) {

    private val closedMorrisCoeff = doubleArrayOf(coefficients[0],coefficients[6], coefficients[15])
    private val morrisesNumberCoeff = doubleArrayOf(coefficients[1],coefficients[7])
    private val blockedOppPiecesCoeff = doubleArrayOf(coefficients[2],coefficients[8])
    private val piecesNumberCoeff = doubleArrayOf(coefficients[3],coefficients[9])
    private val num2PiecesCoeff = doubleArrayOf(coefficients[4],coefficients[13])
    private val num3PiecesCoeff =doubleArrayOf(coefficients[5],coefficients[14])
    private val openedMorrisCoeff = coefficients[10]
    private val doubleMorrisCoeff = coefficients[11]
    private val winningConfCoeff = doubleArrayOf(coefficients[12],coefficients[16])

    override fun eval(state: State?, player: Checker?): Double {
        var amount = super.eval(state, player)
        when(amount){
            10000.00, -10000.00 ->{
                //println("TERMINAL STATE PHASE ${state!!.currentPhase}: $state ")
                return amount
            }
        }
        var opposite = Checker.EMPTY
        if (player == Checker.WHITE) {
            opposite = Checker.BLACK
        } else
            opposite = Checker.WHITE
        var amountPlayer = 0.0
        var amountOpposite = 0.0
        val game = game as MulinoGame
        when(state!!.currentPhase){
            '1'->{
                amountPlayer = morrisesNumberCoeff[0] * game.getNumMorrises(state, player!!) +
                        blockedOppPiecesCoeff[0] * game.getBlockedPieces(state, player) +
                        piecesNumberCoeff[0] * game.getNumPieces(state, player) +
                        num2PiecesCoeff[0] * game.getNum2Conf(state, player) +
                        num3PiecesCoeff[0] * game.getNum3Conf(state, player)
                if (state.closedMorris && game.opposite(state)==player)
                    amountPlayer += closedMorrisCoeff[0]
                amountOpposite = -(morrisesNumberCoeff[0] * game.getNumMorrises(state, opposite)) -
                        (blockedOppPiecesCoeff[0] * game.getBlockedPieces(state, opposite)) -
                        (piecesNumberCoeff[0] * game.getNumPieces(state, opposite)) -
                        (num2PiecesCoeff[0] * game.getNum2Conf(state, opposite)) -
                        (num3PiecesCoeff[0] * game.getNum3Conf(state, opposite))
                if (state.closedMorris && game.opposite(state)==opposite)
                    amountOpposite -= closedMorrisCoeff[0]
            }
            '2'->{
                amountPlayer = morrisesNumberCoeff[1] * game.getNumMorrises(state, player!!) +
                        blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, player) +
                        piecesNumberCoeff[1] * game.getNumPieces(state, player)
                if (state.closedMorris && game.opposite(state)==player)
                    amountPlayer += closedMorrisCoeff[1]
                if (game.hasOpenedMorris(state, player))
                    amountPlayer += openedMorrisCoeff
                if (game.hasDoubleMorris(state, player))
                    amountPlayer += doubleMorrisCoeff
                if (game.isWinningConfiguration(state, player!!))
                    amountPlayer += winningConfCoeff[0]

                amountOpposite = -(morrisesNumberCoeff[1] * game.getNumMorrises(state, opposite)) -
                        (blockedOppPiecesCoeff[1] * game.getBlockedPieces(state, opposite)) -
                        (piecesNumberCoeff[1] * game.getNumPieces(state, opposite))
                if (state.closedMorris && game.opposite(state)==opposite)
                    amountOpposite -= closedMorrisCoeff[1]
                if (game.hasOpenedMorris(state, opposite))
                    amountOpposite -= openedMorrisCoeff
                if (game.hasDoubleMorris(state, opposite))
                    amountOpposite -= doubleMorrisCoeff
                if (game.isWinningConfiguration(state, opposite))
                    amountOpposite -= winningConfCoeff[0]
            }
            '3'->{
                amountPlayer = num2PiecesCoeff[1] * game.getNum2Conf(state, player!!) +
                        num3PiecesCoeff[1] * game.getNum3Conf(state, player)
                if (state.closedMorris && game.opposite(state)==player)
                    amountPlayer += closedMorrisCoeff[2]
                if (game.isWinningConfiguration(state, player))
                    amountPlayer += winningConfCoeff[1]

                amountOpposite = -(num2PiecesCoeff[1] * game.getNum2Conf(state, opposite)) -
                        (num3PiecesCoeff[1] * game.getNum3Conf(state, opposite))
                if (state.closedMorris && game.opposite(state)==opposite)
                    amountOpposite -= closedMorrisCoeff[2]
                if (game.isWinningConfiguration(state, opposite))
                    amountOpposite -= winningConfCoeff[1]
            }
        }
        amount += amountPlayer + amountOpposite
        //println("Evaluation state $state -> $amount")
        return amount
    }

    /*override fun orderActions(state: State?, actions: MutableList<String>?, player: Checker?, depth: Int): MutableList<String> {
        actions!!.sortBy{ eval(game.getResult(state, it), player) }
        return actions
    }*/

    override fun incrementDepthLimit() {
        super.incrementDepthLimit()
        println("Profondità attuale: $currDepthLimit")
    }

}

fun main(args: Array<String>) {

    val initialState = State(Checker.WHITE)

    /*
    initialState.addPiece(Pair('f',4), Checker.WHITE)
    initialState.addPiece(Pair('a',4), Checker.WHITE)
    initialState.addPiece(Pair('a',1), Checker.WHITE)
    initialState.addPiece(Pair('g',7), Checker.BLACK)
    initialState.addPiece(Pair('g',4), Checker.BLACK)
    initialState.addPiece(Pair('d',2), Checker.BLACK)
    */


    val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 7.0, 42.0, 1047.0), -10000.00, 10000.00, 1)
    val action = search.makeDecision(initialState)
    //println("Azione scelta: $action")

}