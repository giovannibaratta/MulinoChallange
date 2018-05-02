package it.unibo.mulino.minmax.player

import aima.core.search.adversarial.Game
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State.Checker

abstract class MulinoGame<A> : Game<State, A, Checker> {

    override fun getInitialState(): State {
        return State(Checker.WHITE)
    }

    override fun getPlayer(state: State): Checker {
        return state.checker
    }

    override fun getPlayers(): Array<Checker> {
        return arrayOf(Checker.WHITE, Checker.BLACK)
    }

    override fun getUtility(state: State, player: Checker): Double {
        return when (player) {
            state.checker -> (-10000).toDouble()
            else -> (10000).toDouble()
        }
    }

    abstract fun isWinningConfiguration(state: State, checker: Checker): Boolean

}

object MulinoGamePhase1 : MulinoGame<Phase1Action>() {

    override fun getActions(state: State?): MutableList<Phase1Action> {
        val actions = mutableListOf<Phase1Action>()
        for (possiblePosition in state!!.getEmptyPositions()) {
            if (state.checkMorris(possiblePosition, state.checker)) {
                for (adversarialPosition in state.getPositions(state.opposite())) {
                    val action = Phase1Action()
                    action.putPosition = "" + possiblePosition.first + possiblePosition.second
                    action.removeOpponentChecker = "" + adversarialPosition.first + adversarialPosition.second
                    actions.add(action)
                }
            } else {
                val action = Phase1Action()
                action.putPosition = "" + possiblePosition.first + possiblePosition.second
                actions.add(action)
            }
        }
        return actions
    }

    override fun getResult(state: State?, action: Phase1Action?): State {
        val colNewPosition = action!!.putPosition[0]
        val rowNewPosition = action.putPosition[1].toString().toInt()
        var newState = State(state!!.opposite())
        for (whitePosition in state.getPositions(Checker.WHITE)) {
            newState.addPiece(whitePosition, Checker.WHITE)
        }
        for (blackPosition in state.getPositions(Checker.BLACK)) {
            newState.addPiece(blackPosition, Checker.BLACK)
        }
        newState.addPiece(Pair(colNewPosition, rowNewPosition), state.checker)
        if (action.removeOpponentChecker != null) {
            val colOpponentToRemove = action.removeOpponentChecker[0]
            val rowOpponentToRemove = action.removeOpponentChecker[1].toString().toInt()
            newState.removePiece(Pair(colOpponentToRemove, rowOpponentToRemove))
        }

        return newState
    }

    override fun isTerminal(state: State?): Boolean {
        return state!!.isWinner(Checker.WHITE, 1) || state.isWinner(Checker.BLACK, 1)
    }

    override fun isWinningConfiguration(state: State, checker: Checker): Boolean {
        var check = false
        when (checker) {
            state.checker -> return state.isWinner(checker, 1)
            else -> {
                for (action in getActions(state))
                    if (getResult(state, action).isWinner(checker, 1)) {
                        check = true
                        break
                    }
            }
        }
        return check
    }
}

object MulinoGamePhase2 : MulinoGame<Phase2Action>() {

    override fun getActions(state:
                            State): MutableList<Phase2Action> {
        val actions = mutableListOf<Phase2Action>()
        for (actualPosition in state.getPositions(state.checker)) {
            for (adiacentPosition in state.getAdiacentPositions(actualPosition)) {
                if (state.getPiece(adiacentPosition) == Checker.EMPTY) {
                    if (state.checkMorris(actualPosition, adiacentPosition, state.checker)) {
                        for (adversarialPosition in state.getPositions(state.opposite())) {
                            val action = Phase2Action()
                            action.from = "" + actualPosition.first + actualPosition.second
                            action.to = "" + adiacentPosition.first + adiacentPosition.second
                            action.removeOpponentChecker = "" + adversarialPosition.first + adversarialPosition.second
                            actions.add(action)
                        }
                    } else {
                        val action = Phase2Action()
                        action.from = "" + actualPosition.first + actualPosition.second
                        action.to = "" + adiacentPosition.first + adiacentPosition.second
                        actions.add(action)
                    }
                }
            }
        }
        return actions
    }

    override fun getResult(state: State, action: Phase2Action?): State {
        val colOldPosition = action!!.from[0]
        val rowOldPosition = action.from[1].toString().toInt()
        val colNewPosition = action.to[0]
        val rowNewPosition = action.to[1].toString().toInt()
        var newState = State(state.opposite())
        for (whitePosition in state.getPositions(Checker.WHITE)) {
            newState.addPiece(whitePosition, Checker.WHITE)
        }
        for (blackPosition in state.getPositions(Checker.BLACK)) {
            newState.addPiece(blackPosition, Checker.BLACK)
        }
        newState.removePiece(Pair(colOldPosition, rowOldPosition))
        newState.addPiece(Pair(colNewPosition, rowNewPosition), state.checker)
        if (action.removeOpponentChecker != null) {
            val colOpponentToRemove = action.removeOpponentChecker[0]
            val rowOpponentToRemove = action.removeOpponentChecker[1].toString().toInt()
            newState.removePiece(Pair(colOpponentToRemove, rowOpponentToRemove))
        }
        return newState
    }

    override fun isTerminal(state: State?): Boolean {
        return state!!.isWinner(Checker.WHITE, 2) || state.isWinner(Checker.BLACK, 2)
    }

    override fun isWinningConfiguration(state: State, checker: Checker): Boolean {
        var check = false
        when (checker) {
            state.checker -> return state.isWinner(checker, 2)
            else -> {
                for (action in MulinoGamePhase1.getActions(state))
                    if (MulinoGamePhase1.getResult(state, action).isWinner(checker, 2)) {
                        check = true
                        break
                    }
            }
        }
        return check
    }
}

object MulinoGamePhaseFinal : MulinoGame<PhaseFinalAction>() {

    override fun getActions(state: State): MutableList<PhaseFinalAction> {
        val actions = mutableListOf<PhaseFinalAction>()
        for (actualPosition in state.getPositions(state.checker)) {
            for (possiblePosition in state.getEmptyPositions()) {
                if (state.checkMorris(actualPosition, possiblePosition, state.checker)) {
                    for (adversarialPosition in state.getPositions(state.opposite())) {
                        val action = PhaseFinalAction()
                        action.from = "" + actualPosition.first + actualPosition.second
                        action.to = "" + possiblePosition.first + possiblePosition.second
                        action.removeOpponentChecker = "" + adversarialPosition.first + adversarialPosition.second
                        actions.add(action)
                    }
                } else {
                    val action = PhaseFinalAction()
                    action.from = "" + actualPosition.first + actualPosition.second
                    action.to = "" + possiblePosition.first + possiblePosition.second
                    actions.add(action)
                }
            }
        }
        return actions
    }

    override fun getResult(state: State, action: PhaseFinalAction?): State {
        val colOldPosition = action!!.from[0]
        val rowOldPosition = action.from[1].toString().toInt()
        val colNewPosition = action.to[0]
        val rowNewPosition = action.to[1].toString().toInt()
        var newState = State(state.opposite())
        for (whitePosition in state.getPositions(Checker.WHITE)) {
            newState.addPiece(whitePosition, Checker.WHITE)
        }
        for (blackPosition in state.getPositions(Checker.BLACK)) {
            newState.addPiece(blackPosition, Checker.BLACK)
        }
        newState.removePiece(Pair(colOldPosition, rowOldPosition))
        newState.addPiece(Pair(colNewPosition, rowNewPosition), state.checker)
        if (action.removeOpponentChecker != null) {
            val colOpponentToRemove = action.removeOpponentChecker[0]
            val rowOpponentToRemove = action.removeOpponentChecker[1].toString().toInt()
            newState.removePiece(Pair(colOpponentToRemove, rowOpponentToRemove))
        }

        return newState
    }

    override fun isTerminal(state: State?): Boolean {
        return state!!.isWinner(Checker.WHITE, 3) || state.isWinner(Checker.BLACK, 3)
    }

    override fun isWinningConfiguration(state: State, checker: Checker): Boolean {
        var check = false
        when (checker) {
            state.checker -> {
                for (action in MulinoGamePhase1.getActions(state))
                    if (MulinoGamePhase1.getResult(state, action).isWinner(checker, 3)) {
                        check = true
                        break
                    }
            }
        }
        return check
    }
}

fun main(args: Array<String>) {

    val state = State(Checker.WHITE)
    println("Turno: ${state.checker}")

    state.addPiece(Pair('a', 1), Checker.WHITE)
    state.addPiece(Pair('a', 4), Checker.WHITE)
    state.addPiece(Pair('b', 4), Checker.WHITE)
    state.addPiece(Pair('d', 1), Checker.WHITE)
    state.addPiece(Pair('d', 2), Checker.WHITE)
    state.addPiece(Pair('e', 3), Checker.WHITE)
    state.addPiece(Pair('d', 7), Checker.WHITE)
    println("Numero di pedine bianche: ${state.getNumPieces(Checker.WHITE)}")
    println("Pedine bianche(adiacenti): ")
    for (position in state.getPositions(Checker.WHITE)) {
        print("${position.first}${position.second} (")
        for (adiacentPosition in state.getAdiacentPositions(position))
            print("${adiacentPosition.first}${adiacentPosition.second}, ")
        print(")\n")
    }

    state.addPiece(Pair('g', 4), Checker.BLACK)
    state.addPiece(Pair('b', 2), Checker.BLACK)
    println("Numero di pedine nere: ${state.getNumPieces(Checker.BLACK)}")
    println("Pedine nere(adiacenti): ")
    for (position in state.getPositions(Checker.BLACK)) {
        print("${position.first}${position.second} (")
        for (adiacentPosition in state.getAdiacentPositions(position))
            print("${adiacentPosition.first}${adiacentPosition.second}, ")
        print(")\n")
    }

    println("Starting phase 1..")
    for (action in MulinoGamePhase1.getActions(state))
        println("Possible action: ${action}")

    println("Starting phase 2..")
    for (action in MulinoGamePhase2.getActions(state))
        println("Possible action: ${action}")

    println("Starting phase 3..")
    for (action in MulinoGamePhaseFinal.getActions(state))
        println("Possible action: ${action}")

    val action2 = Phase2Action()
    println("Action phase 2: d7a7b2")
    action2.from = "d7"
    action2.to = "a7"
    action2.removeOpponentChecker = "b2"
    println("Pedine bianche(adiacenti): ")
    for (position in MulinoGamePhase2.getResult(state, action2).getPositions(Checker.WHITE)) {
        print("${position.first}${position.second} (")
        for (adiacentPosition in state.getAdiacentPositions(position))
            print("${adiacentPosition.first}${adiacentPosition.second}, ")
        print(")\n")
    }
}