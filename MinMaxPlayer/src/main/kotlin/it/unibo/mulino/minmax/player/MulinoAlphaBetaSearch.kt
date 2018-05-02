package it.unibo.mulino.minmax.player


import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State.Checker

class MulinoAlphaBetaSearchPhase1(coefficients: Array<Double>,
                                  utilMin: Double,
                                  utilMax: Double,
                                  time: Int) : IterativeDeepeningAlphaBetaSearch<State, Phase1Action, Checker>(MulinoGamePhase1, utilMin, utilMax, time) {

    private val closedMorrisCoeff = coefficients[0]
    private val morrisesNumberCoeff = coefficients[1]
    private val blockedOppPiecesCoeff = coefficients[2]
    private val piecesNumberCoeff = coefficients[3]
    private val num2PiecesCoeff = coefficients[4]
    private val num3PiecesCoeff = coefficients[5]

    override fun eval(state: State?, player: Checker?): Double {
        super.eval(state, player)
        var opposite = Checker.EMPTY
        if (player == Checker.WHITE) {
            opposite = Checker.BLACK
        } else
            opposite = Checker.WHITE
        var amountPlayer = morrisesNumberCoeff * state!!.getNumMorrises(player!!) +
                blockedOppPiecesCoeff * state.getBlockedPieces(player) +
                piecesNumberCoeff * state.getNumPieces(player) +
                num2PiecesCoeff * state.getNum2Conf(player) +
                num3PiecesCoeff * state.getNum3Conf(player)
        if (state.hasClosedMorris(player))
            amountPlayer += closedMorrisCoeff

        var amountOpposite = -(morrisesNumberCoeff * state.getNumMorrises(opposite) +
                blockedOppPiecesCoeff * state.getBlockedPieces(opposite) +
                piecesNumberCoeff * state.getNumPieces(opposite) +
                num2PiecesCoeff * state.getNum2Conf(opposite) +
                num3PiecesCoeff * state.getNum3Conf(opposite))
        if (state.hasClosedMorris(opposite))
            amountOpposite -= closedMorrisCoeff

        return amountPlayer + amountOpposite
    }

}

class MulinoAlphaBetaSearchPhase2(coefficients: Array<Double>,
                                  utilMin: Double,
                                  utilMax: Double,
                                  time: Int) : IterativeDeepeningAlphaBetaSearch<State, Phase2Action, Checker>(MulinoGamePhase2, utilMin, utilMax, time) {

    private val closedMorrisCoeff = coefficients[0]
    private val morrisesNumberCoeff = coefficients[1]
    private val blockedOppPiecesCoeff = coefficients[2]
    private val piecesNumberCoeff = coefficients[3]
    private val openedMorrisCoeff = coefficients[4]
    private val doubleMorrisCoeff = coefficients[5]
    private val winningConfCoeff = coefficients[6]

    override fun eval(state: State?, player: Checker?): Double {
        super.eval(state, player)
        var opposite = Checker.EMPTY
        if (player == Checker.WHITE) {
            opposite = Checker.BLACK
        } else
            opposite = Checker.WHITE
        var amountPlayer = morrisesNumberCoeff * state!!.getNumMorrises(player!!) +
                blockedOppPiecesCoeff * state.getBlockedPieces(player) +
                piecesNumberCoeff * state.getNumPieces(player)
        if (state.hasClosedMorris(player))
            amountPlayer += closedMorrisCoeff
        if (state.hasOpenedMorris(player))
            amountPlayer += openedMorrisCoeff
        if (state.hasDoubleMorris(player))
            amountPlayer += doubleMorrisCoeff
        if (MulinoGamePhase2.isWinningConfiguration(state, player))
            amountPlayer += winningConfCoeff

        var amountOpposite = -(morrisesNumberCoeff * state.getNumMorrises(opposite) +
                blockedOppPiecesCoeff * state.getBlockedPieces(opposite) +
                piecesNumberCoeff * state.getNumPieces(opposite))
        if (state.hasClosedMorris(opposite))
            amountOpposite -= closedMorrisCoeff
        if (state.hasOpenedMorris(opposite))
            amountOpposite -= openedMorrisCoeff
        if (state.hasDoubleMorris(opposite))
            amountOpposite -= doubleMorrisCoeff
        if (MulinoGamePhase2.isWinningConfiguration(state, opposite))
            amountOpposite -= winningConfCoeff

        return amountPlayer + amountOpposite
    }

}

class MulinoAlphaBetaSearchPhaseFinal(coefficients: Array<Double>,
                                      utilMin: Double,
                                      utilMax: Double,
                                      time: Int) : IterativeDeepeningAlphaBetaSearch<State, PhaseFinalAction, Checker>(MulinoGamePhaseFinal, utilMin, utilMax, time) {

    private val num2PiecesCoeff = coefficients[4]
    private val num3PiecesCoeff = coefficients[5]
    private val closedMorrisCoeff = coefficients[0]
    private val winningConfCoeff = coefficients[6]


    override fun eval(state: State?, player: Checker?): Double {
        super.eval(state, player)
        var opposite = Checker.EMPTY
        if (player == Checker.WHITE) {
            opposite = Checker.BLACK
        } else
            opposite = Checker.WHITE
        var amountPlayer = num2PiecesCoeff * state!!.getNum2Conf(player!!) +
                num3PiecesCoeff * state.getNum3Conf(player)
        if (state.hasClosedMorris(player))
            amountPlayer += closedMorrisCoeff
        if (MulinoGamePhaseFinal.isWinningConfiguration(state, player))
            amountPlayer += winningConfCoeff

        var amountOpposite = -(num2PiecesCoeff * state.getNum2Conf(opposite) +
                num3PiecesCoeff * state.getNum3Conf(opposite))
        if (state.hasClosedMorris(opposite))
            amountOpposite -= closedMorrisCoeff
        if (MulinoGamePhaseFinal.isWinningConfiguration(state, opposite))
            amountOpposite -= winningConfCoeff

        return amountPlayer + amountOpposite
    }

}

fun main(args: Array<String>) {

    val initialState = State(Checker.WHITE)
    initialState.addPiece(Pair('a', 1), Checker.WHITE)
    initialState.addPiece(Pair('a', 4), Checker.WHITE)
    initialState.addPiece(Pair('b', 2), Checker.BLACK)
    initialState.addPiece(Pair('f', 2), Checker.BLACK)
    initialState.addPiece(Pair('f', 4), Checker.WHITE)
    initialState.addPiece(Pair('g', 4), Checker.BLACK)
    val search = MulinoAlphaBetaSearchPhase1(arrayOf(18.0, 26.0, 1.0, 6.0, 12.0, 7.0), -10000.00, 10000.00, 10)
    val action = search.makeDecision(initialState)
    println("Azione scelta: $action")
}