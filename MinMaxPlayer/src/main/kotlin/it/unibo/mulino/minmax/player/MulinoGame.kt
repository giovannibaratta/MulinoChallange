package it.unibo.mulino.minmax.player

import aima.core.search.adversarial.Game
import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State.Checker

object MulinoGame : Game<State, Action, Checker> {

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

    override fun getActions(state: State?): MutableList<Action> {
        val actions = mutableListOf<Action>()
        //println("Current state : $state")
        var intChecker = -1
        when(state!!.checker){
            Checker.WHITE -> intChecker = 0
            Checker.BLACK -> intChecker = 1
        }
        when(state.currentPhase){
            '1' ->{
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
            }
            '2' ->{
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
            }
            '3'->{
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
            }
        }
        /*println("Possible actions: ")
        for(action in actions)
            println("$action")
        */
        return actions
    }

    override fun getResult(state: State?, action: Action): State {
        var newState = State(state!!.opposite())
        var current = -1
        var next = -1
        when(state.checker){
            Checker.WHITE -> current = 0
            Checker.BLACK -> current = 1
        }
        when(state.opposite()){
            Checker.WHITE -> next = 0
            Checker.BLACK -> next = 1
        }
        newState.checkers[next]=state.checkers[next]
        newState.checkers[current]=state.checkers[current]
        for (whitePosition in state.getPositions(Checker.WHITE)) {
            newState.addPiece(whitePosition, Checker.WHITE)
        }
        for (blackPosition in state.getPositions(Checker.BLACK)) {
            newState.addPiece(blackPosition, Checker.BLACK)
        }
        when(action){
            is Phase1Action ->{
                var phase1Action : Phase1Action= action
                val colNewPosition = phase1Action.putPosition[0]
                val rowNewPosition = phase1Action.putPosition[1].toString().toInt()

                newState.addPiece(Pair(colNewPosition, rowNewPosition), state.checker)
                newState.checkers[current]--

                if (phase1Action.removeOpponentChecker != null) {
                    val colOpponentToRemove = phase1Action.removeOpponentChecker[0]
                    val rowOpponentToRemove = phase1Action.removeOpponentChecker[1].toString().toInt()
                    newState.removePiece(Pair(colOpponentToRemove, rowOpponentToRemove))
                }
                if(state.checkers[next]==0)
                    newState.currentPhase='2'
                else
                    newState.currentPhase='1'
            }

            is Phase2Action ->{
                var phase2Action : Phase2Action= action
                val colOldPosition = phase2Action.from[0]
                val rowOldPosition = phase2Action.from[1].toString().toInt()
                val colNewPosition = phase2Action.to[0]
                val rowNewPosition = phase2Action.to[1].toString().toInt()

                newState.removePiece(Pair(colOldPosition, rowOldPosition))
                newState.addPiece(Pair(colNewPosition, rowNewPosition), state.checker)

                if (action.removeOpponentChecker != null) {
                    val colOpponentToRemove = phase2Action.removeOpponentChecker[0]
                    val rowOpponentToRemove = phase2Action.removeOpponentChecker[1].toString().toInt()
                    newState.removePiece(Pair(colOpponentToRemove, rowOpponentToRemove))
                }
                if(state.getNumPieces(state.opposite())==3)
                    newState.currentPhase='3'
                else
                    newState.currentPhase='2'
            }
            is PhaseFinalAction ->{
                var phaseFinalAction : PhaseFinalAction= action
                val colOldPosition = phaseFinalAction.from[0]
                val rowOldPosition = phaseFinalAction.from[1].toString().toInt()
                val colNewPosition = phaseFinalAction.to[0]
                val rowNewPosition = phaseFinalAction.to[1].toString().toInt()

                newState.removePiece(Pair(colOldPosition, rowOldPosition))
                newState.addPiece(Pair(colNewPosition, rowNewPosition), state.checker)
                if (action.removeOpponentChecker != null) {
                    val colOpponentToRemove = phaseFinalAction.removeOpponentChecker[0]
                    val rowOpponentToRemove = phaseFinalAction.removeOpponentChecker[1].toString().toInt()
                    newState.removePiece(Pair(colOpponentToRemove, rowOpponentToRemove))
                }
                if(state.getNumPieces(state.opposite())>3)
                    newState.currentPhase='2'
                else
                    newState.currentPhase='3'
            }
        }
        //println("Action ${state.checker}: $action -> State : $newState")
        return newState
    }

    override fun isTerminal(state: State?): Boolean {
        if(state!!.isWinner(Checker.WHITE) || state.isWinner(Checker.BLACK)){
            println("TERMINAL STATE : $state")
            return true
        }
        return false
    }

    fun isWinningConfiguration(state: State, checker: Checker): Boolean {
        var check = false
        when (checker) {
            state.checker->{
                for (action in getActions(state))
                    if (getResult(state, action).isWinner(checker)) {
                        println("WINNING CONFIGURATION FOR $checker : Action $action to state $state")
                        check = true
                        break
                    }
            }
        }
        return check
    }

}

fun main(args: Array<String>) {

    /*
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
    */
}