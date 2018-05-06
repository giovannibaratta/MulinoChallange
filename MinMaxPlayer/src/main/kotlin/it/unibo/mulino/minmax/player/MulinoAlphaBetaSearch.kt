package it.unibo.mulino.minmax.player


import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.domain.State.Checker

class MulinoAlphaBetaSearch(coefficients: Array<Double>,
                                  utilMin: Double,
                                  utilMax: Double,
                                  time: Int) : IterativeDeepeningAlphaBetaSearch<State, Action, Checker>(MulinoGame, utilMin, utilMax, time) {

    private val closedMorrisCoeff = coefficients[0]
    private val morrisesNumberCoeff = coefficients[1]
    private val blockedOppPiecesCoeff = coefficients[2]
    private val piecesNumberCoeff = coefficients[3]
    private val num2PiecesCoeff = coefficients[4]
    private val num3PiecesCoeff = coefficients[5]
    private val openedMorrisCoeff = coefficients[6]
    private val doubleMorrisCoeff = coefficients[7]
    private val winningConfCoeff = coefficients[8]

    override fun eval(state: State?, player: Checker?): Double {
        var amount = super.eval(state, player)
        when(amount){
            10000.00, -10000.00 ->{
                println("TERMINAL STATE PHASE ${state!!.currentPhase}: $state ")
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
        when(state!!.currentPhase){
            '1'->{
                amountPlayer = morrisesNumberCoeff * state.getNumMorrises(player!!) +
                        blockedOppPiecesCoeff * state.getBlockedPieces(player) +
                        piecesNumberCoeff * state.getNumPieces(player) +
                        num2PiecesCoeff * state.getNum2Conf(player) +
                        num3PiecesCoeff * state.getNum3Conf(player)
                if (state.hasClosedMorris(player))
                    amountPlayer += closedMorrisCoeff
                amountOpposite = -(morrisesNumberCoeff * state.getNumMorrises(opposite) +
                        blockedOppPiecesCoeff * state.getBlockedPieces(opposite) +
                        piecesNumberCoeff * state.getNumPieces(opposite) +
                        num2PiecesCoeff * state.getNum2Conf(opposite) +
                        num3PiecesCoeff * state.getNum3Conf(opposite))
                if (state.hasClosedMorris(opposite))
                    amountOpposite -= closedMorrisCoeff
            }
            '2'->{
                amountPlayer = morrisesNumberCoeff * state.getNumMorrises(player!!) +
                        blockedOppPiecesCoeff * state.getBlockedPieces(player) +
                        piecesNumberCoeff * state.getNumPieces(player)
                if (state.hasClosedMorris(player))
                    amountPlayer += closedMorrisCoeff
                if (state.hasOpenedMorris(player))
                    amountPlayer += openedMorrisCoeff
                if (state.hasDoubleMorris(player))
                    amountPlayer += doubleMorrisCoeff
                if (MulinoGame.isWinningConfiguration(state, player))
                    amountPlayer += winningConfCoeff

                amountOpposite = -(morrisesNumberCoeff * state.getNumMorrises(opposite) +
                        blockedOppPiecesCoeff * state.getBlockedPieces(opposite) +
                        piecesNumberCoeff * state.getNumPieces(opposite))
                if (state.hasClosedMorris(opposite))
                    amountOpposite -= closedMorrisCoeff
                if (state.hasOpenedMorris(opposite))
                    amountOpposite -= openedMorrisCoeff
                if (state.hasDoubleMorris(opposite))
                    amountOpposite -= doubleMorrisCoeff
                if (MulinoGame.isWinningConfiguration(state, opposite))
                    amountOpposite -= winningConfCoeff
            }
            '3'->{
                amountPlayer = num2PiecesCoeff * state.getNum2Conf(player!!) +
                        num3PiecesCoeff * state.getNum3Conf(player)
                if (state.hasClosedMorris(player))
                    amountPlayer += closedMorrisCoeff
                if (MulinoGame.isWinningConfiguration(state, player))
                    amountPlayer += winningConfCoeff

                amountOpposite = -(num2PiecesCoeff * state.getNum2Conf(opposite) +
                        num3PiecesCoeff * state.getNum3Conf(opposite))
                if (state.hasClosedMorris(opposite))
                    amountOpposite -= closedMorrisCoeff
                if (MulinoGame.isWinningConfiguration(state, opposite))
                    amountOpposite -= winningConfCoeff
            }
        }
        amount += amountPlayer + amountOpposite
        println("Evaluation state $state -> $amount")
        return amount
    }

}

fun main(args: Array<String>) {

    val initialState = State(Checker.WHITE)

    initialState.addPiece(Pair('f',4), Checker.WHITE)
    initialState.addPiece(Pair('a',4), Checker.WHITE)
    initialState.addPiece(Pair('a',1), Checker.WHITE)
    initialState.addPiece(Pair('g',7), Checker.BLACK)
    initialState.addPiece(Pair('g',4), Checker.BLACK)
    initialState.addPiece(Pair('d',2), Checker.BLACK)

    val search = MulinoAlphaBetaSearch(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0, 7.0, 42.0, 1047.0), -10000.00, 10000.00, 1)
    val action = search.makeDecision(initialState)
    //println("Azione scelta: $action")

}