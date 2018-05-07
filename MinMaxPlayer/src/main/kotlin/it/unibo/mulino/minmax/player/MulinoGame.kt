package it.unibo.mulino.minmax.player

import aima.core.search.adversarial.Game
import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State.Checker

object MulinoGame : Game<State, String, Checker> {

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

    override fun getActions(state: State?): MutableList<String> {
        val actions = mutableListOf<String>()
        //println("Current state (phase ${state!!.currentPhase}: $state")
        var intChecker = -1
        when(state!!.checker){
            Checker.WHITE -> intChecker = 0
            Checker.BLACK -> intChecker = 1
        }
        when(state.currentPhase){
            '1' ->{
                var numMorrises = 0
                for (possiblePosition in state.getEmptyPositions()) {
                    if (state.checkMorris(possiblePosition, state.checker)) {
                        for (adversarialPosition in state.getPositions(state.opposite())) {
                            if(!state.checkMorris(adversarialPosition, state.opposite())) {
                                actions.add("1$possiblePosition$adversarialPosition")
                            }else numMorrises++
                        }
                        if(numMorrises == state.getNumPieces(state.opposite())){
                            for (adversarialPosition in state.getPositions(state.opposite())) {
                                actions.add("1$possiblePosition$adversarialPosition")
                            }
                        }
                    } else {
                        actions.add("1$possiblePosition")
                    }
                }
            }
            '2' ->{
                var numMorrises = 0
                for (actualPosition in state.getPositions(state.checker)) {
                    for (adiacentPosition in state.getAdiacentPositions(actualPosition)) {
                        if (state.getPiece(adiacentPosition) == Checker.EMPTY) {
                            if (state.checkMorris(actualPosition, adiacentPosition, state.checker)) {
                                for (adversarialPosition in state.getPositions(state.opposite())) {
                                    if(!state.checkMorris(adversarialPosition, state.opposite())) {
                                        actions.add("2$actualPosition$adiacentPosition$adversarialPosition")
                                    }else numMorrises++
                                }
                                if(numMorrises == state.getNumPieces(state.opposite())){
                                    for (adversarialPosition in state.getPositions(state.opposite())) {
                                        actions.add("2$actualPosition$adiacentPosition$adversarialPosition")
                                    }
                                }
                            } else {
                                actions.add("2$actualPosition$adiacentPosition")
                            }
                        }
                    }
                }
            }
            '3'->{
                var numMorrises = 0
                for (actualPosition in state.getPositions(state.checker)) {
                    when(state.getNumPieces(state.checker)){
                        3 ->{
                            for (possiblePosition in state.getEmptyPositions()) {
                                if (state.checkMorris(actualPosition, possiblePosition, state.checker)) {
                                    for (adversarialPosition in state.getPositions(state.opposite())) {
                                        if(!state.checkMorris(adversarialPosition, state.opposite())) {
                                            actions.add("3$actualPosition$possiblePosition$adversarialPosition")
                                        }else numMorrises++
                                    }
                                    if(numMorrises == state.getNumPieces(state.opposite())){
                                        for (adversarialPosition in state.getPositions(state.opposite())) {
                                            actions.add("3$actualPosition$possiblePosition$adversarialPosition")
                                        }
                                    }
                                } else {
                                    actions.add("3$actualPosition$possiblePosition")
                                }
                            }
                        }
                        else ->{
                            for (actualPosition in state.getPositions(state.checker)) {
                                for (adiacentPosition in state.getAdiacentPositions(actualPosition)) {
                                    if (state.getPiece(adiacentPosition) == Checker.EMPTY) {
                                        if (state.checkMorris(actualPosition, adiacentPosition, state.checker)) {
                                            for (adversarialPosition in state.getPositions(state.opposite())) {
                                                if(!state.checkMorris(adversarialPosition, state.opposite())) {
                                                    actions.add("3$actualPosition$adiacentPosition$adversarialPosition")
                                                }else numMorrises++
                                            }
                                            if(numMorrises == state.getNumPieces(state.opposite())){
                                                for (adversarialPosition in state.getPositions(state.opposite())) {
                                                    actions.add("3$actualPosition$adiacentPosition$adversarialPosition")
                                                }
                                            }
                                        } else {
                                            actions.add("3$actualPosition$adiacentPosition")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /*
        println("Possible actions: ")
        for(action in actions)
            println("$action")
        */
        return actions
    }

    override fun getResult(state: State?, action: String): State {

        var newState = state!!.copy(state.opposite())
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
        newState.closedMorris = false
        /*
        newState.checkers[next]=state.checkers[next]
        newState.checkers[current]=state.checkers[current]
        for (whitePosition in state.getPositions(Checker.WHITE)) {
            newState.addPiece(whitePosition, Checker.WHITE)
        }
        for (blackPosition in state.getPositions(Checker.BLACK)) {
            newState.addPiece(blackPosition, Checker.BLACK)
        }
        */
        when(action.get(0)){
            '1' ->{
                newState.addPiece(action.substring(1, 3), state.checker)
                newState.checkers[current]--

                if (action.length>3) {
                    newState.removePiece(action.substring(3, 5))
                    newState.closedMorris = true
                }
                if(state.checkers[next]==0)
                    newState.currentPhase='2'
                else
                    newState.currentPhase='1'
            }

            '2' ->{
                newState.removePiece(action.substring(1, 3))
                newState.addPiece(action.substring(3, 5), state.checker)

                if (action.length>5) {
                    newState.removePiece(action.substring(5, 7))
                    newState.closedMorris = true
                }
                if(state.getNumPieces(state.opposite())==3)
                    newState.currentPhase='3'
                else
                    newState.currentPhase='2'
            }
            '3' ->{
                newState.removePiece(action.substring(1, 3))
                newState.addPiece(action.substring(3, 5), state.checker)
                if (action.length>5) {
                    newState.removePiece(action.substring(5, 7))
                    newState.closedMorris = true
                }
            }
        }
        //println("Action ${state.checker}: $action -> State : $newState")
        return newState
    }

    override fun isTerminal(state: State?): Boolean {
        if(state!!.isWinner(Checker.WHITE) || state.isWinner(Checker.BLACK)){
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
    //println("Turno: ${state.checker}")

    state.addPiece(Pair('a', 1), Checker.WHITE)
    state.addPiece(Pair('a', 4), Checker.WHITE)
    state.addPiece(Pair('b', 4), Checker.WHITE)
    state.addPiece(Pair('d', 1), Checker.WHITE)
    state.addPiece(Pair('d', 2), Checker.WHITE)
    state.addPiece(Pair('e', 3), Checker.WHITE)
    state.addPiece(Pair('d', 7), Checker.WHITE)
    //println("Numero di pedine bianche: ${state.getNumPieces(Checker.WHITE)}")
    //println("Pedine bianche(adiacenti): ")
    for (position in state.getPositions(Checker.WHITE)) {
        print("${position.first}${position.second} (")
        for (adiacentPosition in state.getAdiacentPositions(position))
            print("${adiacentPosition.first}${adiacentPosition.second}, ")
        print(")\n")
    }

    state.addPiece(Pair('g', 4), Checker.BLACK)
    state.addPiece(Pair('b', 2), Checker.BLACK)
    //println("Numero di pedine nere: ${state.getNumPieces(Checker.BLACK)}")
    //println("Pedine nere(adiacenti): ")
    for (position in state.getPositions(Checker.BLACK)) {
        print("${position.first}${position.second} (")
        for (adiacentPosition in state.getAdiacentPositions(position))
            print("${adiacentPosition.first}${adiacentPosition.second}, ")
        print(")\n")
    }

    //println("Starting phase 1..")
    for (action in MulinoGamePhase1.getActions(state))
        //println("Possible action: ${action}")

    //println("Starting phase 2..")
    for (action in MulinoGamePhase2.getActions(state))
        //println("Possible action: ${action}")

    //println("Starting phase 3..")
    for (action in MulinoGamePhaseFinal.getActions(state))
        //println("Possible action: ${action}")

    val action2 = Phase2Action()
    //println("Action phase 2: d7a7b2")
    action2.from = "d7"
    action2.to = "a7"
    action2.removeOpponentChecker = "b2"
    //println("Pedine bianche(adiacenti): ")
    for (position in MulinoGamePhase2.getResult(state, action2).getPositions(Checker.WHITE)) {
        print("${position.first}${position.second} (")
        for (adiacentPosition in state.getAdiacentPositions(position))
            print("${adiacentPosition.first}${adiacentPosition.second}, ")
        print(")\n")
    }
    */
}