package it.unibo.mulino.minmax.player

import aima.core.search.adversarial.Game
import it.unibo.ai.didattica.mulino.domain.State.Checker
import java.util.*

object MulinoGame : Game<State, String, Checker> {

    private val checkersToInt = hashMapOf(Pair<Checker, Int>(Checker.EMPTY, 0),
            Pair<Checker, Int>(Checker.WHITE, 1),
            Pair<Checker, Int>(Checker.BLACK, 2))
    private val intToCheckers = hashMapOf(Pair<Int, Checker>(0, Checker.EMPTY),
            Pair<Int, Checker>(1, Checker.WHITE),
            Pair<Int, Checker>(2, Checker.BLACK))
    private val toInternalPositions = hashMapOf(Pair("a1", Pair(0, 0)),
            Pair("a4", Pair(1, 0)),
            Pair("a7", Pair(2, 0)),
            Pair("b2", Pair(0, 1)),
            Pair("b4", Pair(1, 1)),
            Pair("b6", Pair(2, 1)),
            Pair("c3", Pair(0, 2)),
            Pair("c4", Pair(1, 2)),
            Pair("c5", Pair(2, 2)),
            Pair("d5", Pair(3, 2)),
            Pair("d6", Pair(3, 1)),
            Pair("d7", Pair(3, 0)),
            Pair("d1", Pair(7, 0)),
            Pair("d2", Pair(7, 1)),
            Pair("d3", Pair(7, 2)),
            Pair("e3", Pair(6, 2)),
            Pair("e4", Pair(5, 2)),
            Pair("e5", Pair(4, 2)),
            Pair("f2", Pair(6, 1)),
            Pair("f4", Pair(5, 1)),
            Pair("f6", Pair(4, 1)),
            Pair("g1", Pair(6, 0)),
            Pair("g4", Pair(5, 0)),
            Pair("g7", Pair(4, 0))
    )
    private val toExternalPositions = hashMapOf(Pair(Pair(0, 0), "a1"),
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
        //println("Current state : ${printState(state!!)}")
        val player = state!!.checker
        var intChecker = checkersToInt[player]!! -1
        val opposite = opposite(state)
        when(state.currentPhase){
            1 ->{
                var numMorrises = 0
                for (possiblePosition in getEmptyPositions(state)) {
                    if (checkMorris(state, possiblePosition, player)) {
                        for (adversarialPosition in getPositions(state, opposite)) {
                            if(!checkMorris(state, adversarialPosition, opposite)) {
                                actions.add("1$possiblePosition$adversarialPosition")
                            }else numMorrises++
                        }
                        if(numMorrises == getNumPieces(state, opposite)){
                            for (adversarialPosition in getPositions(state, opposite)) {
                                actions.add("1$possiblePosition$adversarialPosition")
                            }
                        }
                    } else {
                        actions.add("1$possiblePosition")
                    }
                }
            }
            2 ->{
                var numMorrises = 0
                for (actualPosition in getPositions(state, player)) {
                    for (adiacentPosition in getAdiacentPositions(actualPosition)) {
                        if (getPiece(state, adiacentPosition) == Checker.EMPTY) {
                            if (checkMorris(state, actualPosition, adiacentPosition, player)) {
                                for (adversarialPosition in getPositions(state, opposite)) {
                                    if(!checkMorris(state, adversarialPosition, opposite)) {
                                        actions.add("2$actualPosition$adiacentPosition$adversarialPosition")
                                    }else numMorrises++
                                }
                                if(numMorrises == getNumPieces(state, opposite)){
                                    for (adversarialPosition in getPositions(state, opposite)) {
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
            3->{
                var numMorrises = 0
                for (actualPosition in getPositions(state, player)) {
                    when(getNumPieces(state, player)){
                        3 ->{
                            for (possiblePosition in getEmptyPositions(state)) {
                                if (checkMorris(state, actualPosition, possiblePosition, player)) {
                                    for (adversarialPosition in getPositions(state, opposite)) {
                                        if(!checkMorris(state, adversarialPosition, opposite)) {
                                            actions.add("3$actualPosition$possiblePosition$adversarialPosition")
                                        }else numMorrises++
                                    }
                                    if(numMorrises == getNumPieces(state, opposite)){
                                        for (adversarialPosition in getPositions(state, opposite)) {
                                            actions.add("3$actualPosition$possiblePosition$adversarialPosition")
                                        }
                                    }
                                } else {
                                    actions.add("3$actualPosition$possiblePosition")
                                }
                            }
                        }
                        else ->{
                            for (actualPosition in getPositions(state, player)) {
                                for (adiacentPosition in getAdiacentPositions(actualPosition)) {
                                    if (getPiece(state, adiacentPosition) == Checker.EMPTY) {
                                        if (checkMorris(state, actualPosition, adiacentPosition, player)) {
                                            for (adversarialPosition in getPositions(state, opposite)) {
                                                if(!checkMorris(state, adversarialPosition, opposite)) {
                                                    actions.add("3$actualPosition$adiacentPosition$adversarialPosition")
                                                }else numMorrises++
                                            }
                                            if(numMorrises == getNumPieces(state, opposite)){
                                                for (adversarialPosition in getPositions(state, opposite)) {
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

        /*println("Possible actions: ")
        for(action in actions)
            println("$action")
        */

        return actions
    }

    override fun getResult(state: State?, action: String): State {
        val player = state!!.checker
        val opposite = opposite(state)
        //System.arraycopy(state.board, 0, newBoard, 0, state.board.size )
        var newState = State(checker = opposite, checkers = intArrayOf(state.checkers[0],state.checkers[1]), currentPhase = state.currentPhase)
        var current = checkersToInt[player]!! - 1
        var next = checkersToInt[opposite]!! - 1
        for(vertex in 0..7)
            for(level in 0..2)
                newState.board[vertex][level]=state.board[vertex][level]
        when(action.get(0)){
            '1' ->{
                addPiece(newState, action.substring(1, 3), player)
                newState.checkers[current]--

                if (action.length>3) {
                    removePiece(newState, action.substring(3, 5))
                    newState.closedMorris = true
                }
                if(state.checkers[next]==0)
                    newState.currentPhase=2
                else
                    newState.currentPhase=1
            }

            '2' ->{
                removePiece(newState, action.substring(1, 3))
                addPiece(newState, action.substring(3, 5), player)

                if (action.length>5) {
                    removePiece(newState, action.substring(5, 7))
                    newState.closedMorris = true
                }
                if(getNumPieces(newState, opposite)==3)
                    newState.currentPhase=3
                else
                    newState.currentPhase=2
            }
            '3' ->{
                removePiece(newState, action.substring(1, 3))
                addPiece(newState, action.substring(3, 5), player)
                if (action.length>5) {
                    removePiece(newState, action.substring(5, 7))
                    newState.closedMorris = true
                }
            }
        }
        //println("Action ${state.checker}: $action -> State : ${printState(newState)}")
        return newState
    }

    override fun isTerminal(state: State?): Boolean {
        if(isWinner(state!!, Checker.WHITE) || isWinner(state, Checker.BLACK)){
            return true
        }
        return false
    }

    //MIGLIORABILE
    fun isWinningConfiguration(state: State, checker: Checker): Boolean {
        var check = false
        var opposite = Checker.WHITE
        if (checker==Checker.WHITE) {
            opposite = Checker.BLACK
        }
        if(checker!=state.checker || getNumPieces(state, opposite)>3) return false
        when(getNumPieces(state, checker)){
            3 ->{
                if(hasOpenedMorris(state, checker))
                    return true
            }
            else ->{
                for(position in getPositions(state, checker))
                    for(adiacentPosition in getAdiacentPositions(position))
                        if(getPiece(state, adiacentPosition)==Checker.EMPTY && checkMorris(state, position, adiacentPosition, checker))
                            return true
            }
        }
        return false
    }

    private fun getPiece(state : State, position: String): Checker {
        val (vertex, level) = toInternalPositions[position]!!
        return intToCheckers[state.board[vertex][level]]!!
    }

    fun addPiece(state : State, position: String, checker: Checker) {
        val (vertex, level) = toInternalPositions[position]!!
        state.board[vertex][level] = checkersToInt[checker]!!
    }

    private fun removePiece(state : State, position: String) {
        val (vertex, level) = toInternalPositions[position]!!
        state.board[vertex][level] = 0
    }

    fun getNumPieces(state : State,player: Checker): Int {
        var count = 0
        val intChecker = checkersToInt[player]
        for (diagonal in state.board) {
            for (position in diagonal) {
                if (position == intChecker) count++
            }
        }
        return count
    }

    private fun getPositions(state : State, checker: Checker): List<String> {
        val positions = LinkedList<String>()
        for (position in toInternalPositions.keys) {
            val (vertex, level) = toInternalPositions[position]!!
            if (state.board[vertex][level] == checkersToInt[checker]!!)
                positions.add(position)
        }
        return positions
    }

    private fun getEmptyPositions(state : State): List<String> {
        val positions = LinkedList<String>()
        for (position in toInternalPositions.keys) {
            val (vertex, level) = toInternalPositions[position]!!
            if (state.board[vertex][level] == 0)
                positions.add(position)
        }
        return positions
    }

    private fun getAdiacentPositions(position: String): List<String> {
        val positions = LinkedList<String>()
        val (vertex, level) = toInternalPositions[position]!!
        positions.add(toExternalPositions[Pair(nextVertex(vertex), level)]!!)
        positions.add(toExternalPositions[Pair(precVertex(vertex), level)]!!)
        when (vertex) {
            1, 3, 5, 7 -> for (adiacentLevel in adiacentLevels(level)) {
                positions.add(toExternalPositions[Pair(vertex, adiacentLevel)]!!)
            }
        }
        return positions
    }

    private fun checkMorris(state : State,position: String, checker: Checker): Boolean {
        val (vertex, level) = toInternalPositions[position]!!
        var check = false
        check = when (vertex) {
            1, 3, 5, 7 -> ((state.board[precVertex(vertex)][level] == checkersToInt[checker]) &&
                    (state.board[nextVertex(vertex)][level] == checkersToInt[checker])) ||
                    ((state.board[vertex][nextLevel(level)] == checkersToInt[checker]) &&
                            (state.board[vertex][nextLevel(nextLevel(level))] == checkersToInt[checker]))
            else -> ((state.board[nextVertex(vertex)][level] == checkersToInt[checker]) &&
                    (state.board[nextVertex(nextVertex(vertex))][level] == checkersToInt[checker])) ||
                    ((state.board[precVertex(vertex)][level] == checkersToInt[checker]) &&
                            (state.board[precVertex(precVertex(vertex))][level] == checkersToInt[checker]))
        }
        return check
    }

    private fun checkMorris(state : State, oldPosition: String, newPosition: String, checker: Checker): Boolean {
        val (oldVertex, oldLevel) = toInternalPositions[oldPosition]!!
        val (newVertex, newLevel) = toInternalPositions[newPosition]!!
        var check = false
        if (getAdiacentPositions(newPosition).contains(oldPosition)) {
            when (newVertex) {
                1, 3, 5, 7 -> when (oldVertex) {
                    newVertex -> check = ((state.board[precVertex(newVertex)][newLevel] == checkersToInt[checker]) &&
                            (state.board[nextVertex(newVertex)][newLevel] == checkersToInt[checker]))
                    else -> check = ((state.board[newVertex][nextLevel(newLevel)] == checkersToInt[checker]) &&
                            (state.board[newVertex][nextLevel(nextLevel(newLevel))] == checkersToInt[checker]))
                }
                0, 2, 4, 6 -> when (oldVertex) {
                    nextVertex(newVertex) -> check = ((state.board[precVertex(newVertex)][newLevel] == checkersToInt[checker]) &&
                            (state.board[precVertex(precVertex(newVertex))][newLevel] == checkersToInt[checker]))
                    else -> check = ((state.board[nextVertex(newVertex)][newLevel] == checkersToInt[checker]) &&
                            (state.board[nextVertex(nextVertex(nextVertex(newVertex)))][newLevel] == checkersToInt[checker]))
                }
            }
        } else {
            when (newVertex) {
                1, 3, 5, 7 -> check = ((state.board[precVertex(newVertex)][newLevel] == checkersToInt[checker]) &&
                        (state.board[nextVertex(newVertex)][newLevel] == checkersToInt[checker])) ||
                        ((state.board[newVertex][nextLevel(newLevel)] == checkersToInt[checker]) &&
                                (state.board[newVertex][nextLevel(nextLevel(newLevel))] == checkersToInt[checker]) &&
                                (toInternalPositions[oldPosition] != Pair(newVertex, nextLevel(newLevel)) &&
                                        (toInternalPositions[oldPosition] != Pair(newVertex, nextLevel(nextLevel(newLevel))))))
                0, 2, 4, 6 -> check = ((toInternalPositions[oldPosition] != Pair(nextVertex(nextVertex(newVertex)), newLevel)) &&
                        (state.board[nextVertex(newVertex)][newLevel] == checkersToInt[checker]) &&
                        (state.board[nextVertex(nextVertex(nextVertex(newVertex)))][newLevel] == checkersToInt[checker])) ||
                        ((toInternalPositions[oldPosition] != Pair(precVertex(precVertex(newVertex)), newLevel)) &&
                                (state.board[precVertex(newVertex)][newLevel] == checkersToInt[checker]) &&
                                (state.board[precVertex(precVertex(newVertex))][newLevel] == checkersToInt[checker]))
            }
        }
        return check
    }

    private fun checkNoMoves(state: State, checker: Checker): Boolean {
        var check = true
        for (position in getPositions(state, checker)) {
            check = check && checkNoMoves(state, position, checker)
        }
        //if(check)
        //println("No moves possible for $checker!")
        return check
    }

    private fun checkNoMoves(state: State, position: String, checker: Checker): Boolean {
        var check = false
        var opposite = when(checker){
            Checker.WHITE -> Checker.BLACK
            else -> Checker.BLACK
        }
        val (vertex, level) = toInternalPositions[position]!!
        check = (state.board[nextVertex(vertex)][level] != 0 &&
                state.board[precVertex(vertex)][level] != 0)
        when (vertex) {
            1, 3, 5, 7 -> for (adiacentLevel in adiacentLevels(level)) {
                check = check && (state.board[vertex][adiacentLevel] !=0)
            }
        }
        return check
    }

    fun getBlockedPieces(state: State, checker: Checker): Int {
        var count = 0
        for (adversarialPosition in getPositions(state, checker))
            if (checkNoMoves(state, adversarialPosition, checker))
                count++
        return count
    }

    fun getNumMorrises(state: State, checker: Checker): Int {
        var count = 0
        for (position in getPositions(state, checker)) {
            val (vertex, level) = toInternalPositions[position]!!
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToInt[checker] &&
                            state.board[precVertex(vertex)][level] == checkersToInt[checker]) {
                        count++
                    }
                    when (level) {
                        1 -> if ((state.board[vertex][nextLevel(level)] == checkersToInt[checker]) &&
                                (state.board[vertex][nextLevel(nextLevel(level))] == checkersToInt[checker])) {
                            count++
                        }
                    }
                }
            }
        }
        return count
    }

    /*
    fun hasClosedMorris(checker: Checker): Boolean {
        for (position in getPositions(checker)) {
            val (vertex, level) = toInternalPositions.getValue(position)
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (board[nextVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) {
                        return true
                    }
                    when (level) {
                        1 -> if ((board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)) &&
                                (board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker))) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
    */

    fun hasOpenedMorris(state: State, checker: Checker): Boolean {
        for (position in getPositions(state, checker)) {
            for (adiacentPosition in getAdiacentPositions(position)) {
                val (vertex, level) = toInternalPositions[adiacentPosition]!!
                if (state.board[vertex][level] == 0 && checkMorris(state, position, adiacentPosition, checker))
                    return true
            }
        }
        return false
    }

    //DA VERIFICARE
    fun hasDoubleMorris(state : State, checker: Checker): Boolean {
        for (position in getPositions(state, checker)) {
            val (vertex, level) = toInternalPositions[position]!!
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToInt[checker] &&
                            state.board[nextVertex(nextVertex(vertex))][level] == checkersToInt[checker] &&
                            state.board[precVertex(vertex)][level] == checkersToInt[checker] &&
                            state.board[precVertex(precVertex(vertex))][level] == checkersToInt[checker])
                        return true

                }
                else -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToInt[checker] &&
                            state.board[precVertex(vertex)][level] == checkersToInt[checker] &&
                            state.board[vertex][nextLevel(level)] == checkersToInt[checker] &&
                            state.board[vertex][nextLevel(level)] == checkersToInt[checker])
                        return true
                }
            }
        }
        return false
    }

    fun getNum2Conf(state: State, checker: Checker): Int {
        var count = 0
        for (position in getPositions(state, checker)) {
            val (vertex, level) = toInternalPositions[position]!!
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToInt[checker])
                        count++
                    if (state.board[precVertex(vertex)][level] == checkersToInt[checker])
                        count++
                    for(adiacentLevel in adiacentLevels(level)){
                        if (state.board[vertex][adiacentLevel] == checkersToInt[checker])
                            count++
                    }
                }
            }
        }
        return count
    }

    fun getNum3Conf(state: State, checker: Checker): Int {
        var count = 0
        for (position in getPositions(state, checker)) {
            val (vertex, level) = toInternalPositions[position]!!
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToInt[checker] &&
                            state.board[precVertex(vertex)][level] == checkersToInt[checker])
                        count++

                }
                else -> {
                    when (level) {
                        1 -> {
                            if ((state.board[nextVertex(vertex)][nextLevel(level)] == checkersToInt[checker]) &&
                                    (state.board[vertex][nextLevel(level)] == checkersToInt[checker]))
                                count++
                            if ((state.board[nextVertex(vertex)][nextLevel(nextLevel(level))] == checkersToInt[checker]) &&
                                    (state.board[vertex][nextLevel(nextLevel(level))] == checkersToInt[checker]))
                                count++
                            if ((state.board[precVertex(vertex)][nextLevel(level)] == checkersToInt[checker]) &&
                                    (state.board[vertex][nextLevel(level)] == checkersToInt[checker]))
                                count++
                            if ((state.board[precVertex(vertex)][nextLevel(nextLevel(level))] == checkersToInt[checker]) &&
                                    (state.board[vertex][nextLevel(nextLevel(level))] == checkersToInt[checker]))
                                count++
                            if ((state.board[nextVertex(vertex)][level] == checkersToInt[checker]) &&
                                    (state.board[vertex][nextLevel(level)] == checkersToInt[checker]))
                                count++
                            if ((state.board[nextVertex(vertex)][level] == checkersToInt[checker]) &&
                                    (state.board[vertex][nextLevel(nextLevel(level))] == checkersToInt[checker]))
                                count++
                            if ((state.board[precVertex(vertex)][level] == checkersToInt[checker]) &&
                                    (state.board[vertex][nextLevel(level)] == checkersToInt[checker]))
                                count++
                            if ((state.board[precVertex(vertex)][level] == checkersToInt[checker]) &&
                                    (state.board[vertex][nextLevel(nextLevel(level))] == checkersToInt[checker]))
                                count++
                        }
                    }
                }
            }
        }
        return count
    }

    fun isWinner(state: State, checker: Checker): Boolean {
        var opposite = Checker.WHITE
        if (checker==Checker.WHITE) {
            opposite = Checker.BLACK
        }
        var intOpposite = checkersToInt[opposite]!! - 1
        when(state.currentPhase){
            1->{
                return ((state.checkers[intOpposite]==0) && (getNumPieces(state, opposite) < 3))
            }
            2->{
                return (getNumPieces(state, opposite) < 3) || (checkNoMoves(state, opposite))
            }
            3->{
                return (getNumPieces(state, opposite) < 3)
            }
        }
        return false
    }

    fun opposite(state : State): Checker {
        return when(state.checker) {
            Checker.WHITE -> Checker.BLACK
            Checker.BLACK -> Checker.WHITE
            Checker.EMPTY -> Checker.EMPTY
        }
    }

    private fun nextVertex(vertex: Int): Int {
        if (vertex == 7)
            return 0
        else return vertex + 1
    }

    private fun precVertex(vertex: Int): Int {
        if (vertex == 0)
            return 7
        else return vertex - 1
    }

    private fun nextLevel(level: Int): Int {
        if (level == 2)
            return 0
        else return level + 1
    }

    private fun adiacentLevels(level: Int): Array<Int> {
        return when (level) {
            0,2 -> arrayOf(1)
            1 -> arrayOf(0, 2)
            else -> arrayOf()
        }
    }

    fun printState(state : State): String {
        var out ="Phase : ${state.currentPhase}; Checker : ${state.checker}; ClosedMorris : ${state.closedMorris}; WhiteCheckers : ${state.checkers[0]}; BlackCheckers : ${state.checkers[1]}; Positions : "

        for(position in getPositions(state, Checker.WHITE)){
            out+="($position W) - "
        }
        for(position in getPositions(state, Checker.BLACK)){
            out+="($position B) - "
        }
        return out
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