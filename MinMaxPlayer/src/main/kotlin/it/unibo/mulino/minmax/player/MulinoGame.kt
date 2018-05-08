package it.unibo.mulino.minmax.player

import aima.core.search.adversarial.Game
import it.unibo.ai.didattica.mulino.domain.State.Checker
import java.util.*

object MulinoGame : Game<State, String, Checker> {

    private val checkersToShort = hashMapOf(Pair<Checker, Char>(Checker.EMPTY, 'e'),
            Pair<Checker, Char>(Checker.WHITE, 'w'),
            Pair<Checker, Char>(Checker.BLACK, 'b'))
    private val internalPositions = hashMapOf(Pair("a1", Pair(0, 0)),
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
        var intChecker = -1
        when(state!!.checker){
            Checker.WHITE -> intChecker = 0
            Checker.BLACK -> intChecker = 1
        }
        val player = state.checker
        val opposite = opposite(state)
        when(state.currentPhase){
            '1' ->{
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
            '2' ->{
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
            '3'->{
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
        val newBoard : Array<CharArray> = Array(8, { CharArray(3, { 'e' }) })
        val newCheckers = intArrayOf(state.checkers[0],state.checkers[1])
        //System.arraycopy(state.board, 0, newBoard, 0, state.board.size )
        var newState = State(checker = opposite, board = newBoard, checkers = newCheckers, closedMorris = false)
        var current = -1
        var next = -1
        when(state.checker){
            Checker.WHITE -> current = 0
            Checker.BLACK -> current = 1
        }
        when(opposite){
            Checker.WHITE -> next = 0
            Checker.BLACK -> next = 1
        }
        /*
        newState.checkers[current]=state.checkers[current]
        newState.checkers[next]=state.checkers[next]
        */
        for(whitePosition in getPositions(state, Checker.WHITE)){
            addPiece(newState, whitePosition, Checker.WHITE )
        }
        for(blackPosition in getPositions(state, Checker.BLACK)){
            addPiece(newState, blackPosition, Checker.BLACK )
        }
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
                addPiece(newState, action.substring(1, 3), player)
                newState.checkers[current]--

                if (action.length>3) {
                    removePiece(newState, action.substring(3, 5))
                    newState.closedMorris = true
                }
                if(state.checkers[next]==0)
                    newState.currentPhase='2'
                else
                    newState.currentPhase='1'
            }

            '2' ->{
                removePiece(newState, action.substring(1, 3))
                addPiece(newState, action.substring(3, 5), player)

                if (action.length>5) {
                    removePiece(newState, action.substring(5, 7))
                    newState.closedMorris = true
                }
                if(getNumPieces(newState, opposite)==3)
                    newState.currentPhase='3'
                else
                    newState.currentPhase='2'
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

    fun isWinningConfiguration(state: State, checker: Checker): Boolean {
        var check = false
        when (checker) {
            state.checker->{
                for (action in getActions(state))
                    if (isWinner(getResult(state, action), checker)) {
                        check = true
                        break
                    }
            }
        }
        return check
    }

    private fun getPiece(state : State, position: String): Checker {
        val (vertex, level) = internalPositions.getValue(position)
        val charChecker = state.board[vertex][level]
        when (charChecker) {
            'w' -> return Checker.WHITE
            'b' -> return Checker.BLACK
        }
        return Checker.EMPTY
    }

    fun addPiece(state : State, position: String, checker: Checker) {
        val (vertex, level) = internalPositions.getValue(position)
        state.board[vertex][level] = checkersToShort.getValue(checker)
    }

    private fun removePiece(state : State, position: String) {
        val (vertex, level) = internalPositions.getValue(position)
        state.board[vertex][level] = 'e'
    }

    fun getNumPieces(state : State,player: Checker): Int {
        var count = 0
        val intChecker = checkersToShort.getValue(player)
        for (diagonal in state.board) {
            for (position in diagonal) {
                if (position == intChecker) count++
            }
        }
        return count
    }

    private fun getPositions(state : State, checker: Checker): List<String> {
        val positions = LinkedList<String>()
        for (position in internalPositions.keys) {
            val (vertex, level) = internalPositions.getValue(position)
            if (state.board[vertex][level] == checkersToShort.getValue(checker))
                positions.add(position)
        }
        return positions
    }

    private fun getEmptyPositions(state : State): List<String> {
        val positions = LinkedList<String>()
        for (position in internalPositions.keys) {
            val (vertex, level) = internalPositions.getValue(position)
            if (state.board[vertex][level] == 'e')
                positions.add(position)
        }
        return positions
    }

    private fun getAdiacentPositions(position: String): List<String> {
        val positions = LinkedList<String>()
        val (vertex, level) = internalPositions.getValue(position)
        positions.add(retrievePosition(Pair(nextVertex(vertex), level))!!)
        positions.add(retrievePosition(Pair(precVertex(vertex), level))!!)
        when (vertex) {
            1, 3, 5, 7 -> for (adiacentLevel in adiacentLevels(level)) {
                positions.add(retrievePosition(Pair(vertex, adiacentLevel))!!)
            }
        }
        return positions
    }

    private fun retrievePosition(internalPosition: Pair<Int, Int>): String? {

        for (key in internalPositions.keys) {
            if (internalPositions[key] == internalPosition)
                return key
        }
        return null
    }

    private fun checkMorris(state : State,position: String, checker: Checker): Boolean {
        val (vertex, level) = internalPositions.getValue(position)
        var check = false
        when (vertex) {
            1, 3, 5, 7 -> check = ((state.board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                    (state.board[nextVertex(vertex)][level] == checkersToShort.getValue(checker))) ||
                    ((state.board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)) &&
                            (state.board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
            0, 2, 4, 6 -> check = ((state.board[nextVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                    (state.board[nextVertex(nextVertex(vertex))][level] == checkersToShort.getValue(checker))) ||
                    ((state.board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                            (state.board[precVertex(precVertex(vertex))][level] == checkersToShort.getValue(checker)))
        }
        return check
    }

    private fun checkMorris(state : State, oldPosition: String, newPosition: String, checker: Checker): Boolean {
        val (oldVertex, oldLevel) = internalPositions.getValue(oldPosition)
        val (newVertex, newLevel) = internalPositions.getValue(newPosition)
        var check = false
        if (getAdiacentPositions(newPosition).contains(oldPosition)) {
            when (newVertex) {
                1, 3, 5, 7 -> when (oldVertex) {
                    newVertex -> check = ((state.board[precVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                            (state.board[nextVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)))
                    else -> check = ((state.board[newVertex][nextLevel(newLevel)] == checkersToShort.getValue(checker)) &&
                            (state.board[newVertex][nextLevel(nextLevel(newLevel))] == checkersToShort.getValue(checker)))
                }
                0, 2, 4, 6 -> when (oldVertex) {
                    nextVertex(newVertex) -> check = ((state.board[precVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                            (state.board[precVertex(precVertex(newVertex))][newLevel] == checkersToShort.getValue(checker)))
                    else -> check = ((state.board[nextVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                            (state.board[nextVertex(nextVertex(nextVertex(newVertex)))][newLevel] == checkersToShort.getValue(checker)))
                }
            }
        } else {
            when (newVertex) {
                1, 3, 5, 7 -> check = ((state.board[precVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                        (state.board[nextVertex(newVertex)][newLevel] == checkersToShort.getValue(checker))) ||
                        ((state.board[newVertex][nextLevel(newLevel)] == checkersToShort.getValue(checker)) &&
                                (state.board[newVertex][nextLevel(nextLevel(newLevel))] == checkersToShort.getValue(checker)) &&
                                (internalPositions.getValue(oldPosition) != Pair(newVertex, nextLevel(newLevel)) &&
                                        (internalPositions.getValue(oldPosition) != Pair(newVertex, nextLevel(nextLevel(newLevel))))))
                0, 2, 4, 6 -> check = ((state.board[nextVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                        (state.board[nextVertex(nextVertex(nextVertex(newVertex)))][newLevel] == checkersToShort.getValue(checker)) &&
                        (internalPositions.getValue(oldPosition) != Pair(nextVertex(nextVertex(newVertex)), newLevel))) ||
                        ((state.board[precVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                                (state.board[precVertex(precVertex(newVertex))][newLevel] == checkersToShort.getValue(checker)) &&
                                (internalPositions.getValue(oldPosition) != Pair(precVertex(precVertex(newVertex)), newLevel)))
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
        var opposite = Checker.EMPTY
        if (checker == Checker.WHITE) {
            opposite = Checker.BLACK
        } else
            opposite = Checker.WHITE
        val (vertex, level) = internalPositions.getValue(position)
        check = ((state.board[nextVertex(vertex)][level] == 'w' || (state.board[nextVertex(vertex)][level] == 'b')) &&
                ((state.board[precVertex(vertex)][level] == 'w') || (state.board[precVertex(vertex)][level] == 'b')))
        when (vertex) {
            1, 3, 5, 7 -> for (adiacentLevel in adiacentLevels(level)) {
                check = check && ((state.board[vertex][adiacentLevel] == 'w') ||
                        (state.board[vertex][adiacentLevel] == 'b'))
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
            val (vertex, level) = internalPositions.getValue(position)
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            state.board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) {
                        count++
                    }
                    when (level) {
                        1 -> if ((state.board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)) &&
                                (state.board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker))) {
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
            val (vertex, level) = internalPositions.getValue(position)
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
                val (vertex, level) = internalPositions.getValue(adiacentPosition)
                if (state.board[vertex][level] != 'e' && checkMorris(state, position, adiacentPosition, checker))
                    return true
            }
        }
        return false
    }

    fun hasDoubleMorris(state : State, checker: Checker): Boolean {
        for (position in getPositions(state, checker)) {
            val (vertex, level) = internalPositions.getValue(position)
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            state.board[nextVertex(nextVertex(vertex))][level] == checkersToShort.getValue(checker) &&
                            state.board[precVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            state.board[precVertex(precVertex(vertex))][level] == checkersToShort.getValue(checker))
                        return true

                }
                else -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            state.board[precVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            state.board[vertex][nextLevel(level)] == checkersToShort.getValue(checker) &&
                            state.board[vertex][nextLevel(level)] == checkersToShort.getValue(checker))
                        return true
                }
            }
        }
        return false
    }

    fun getNum2Conf(state: State, checker: Checker): Int {
        var count = 0
        for (position in getPositions(state, checker)) {
            val (vertex, level) = internalPositions.getValue(position)
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToShort.getValue(checker))
                        count++
                    if (state.board[precVertex(vertex)][level] == checkersToShort.getValue(checker))
                        count++
                    when (level) {
                        1 -> {
                            if (state.board[vertex][nextLevel(level)] == checkersToShort.getValue(checker))
                                count++
                            if (state.board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker))
                                count++
                        }
                    }
                }
            }
        }
        return count
    }

    fun getNum3Conf(state: State, checker: Checker): Int {
        var count = 0
        for (position in getPositions(state, checker)) {
            val (vertex, level) = internalPositions.getValue(position)
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (state.board[nextVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            state.board[precVertex(vertex)][level] == checkersToShort.getValue(checker))
                        count++

                }
                else -> {
                    when (level) {
                        1 -> {
                            if ((state.board[nextVertex(vertex)][nextLevel(level)] == checkersToShort.getValue(checker)) &&
                                    (state.board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)))
                                count++
                            if ((state.board[nextVertex(vertex)][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)) &&
                                    (state.board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
                                count++
                            if ((state.board[precVertex(vertex)][nextLevel(level)] == checkersToShort.getValue(checker)) &&
                                    (state.board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)))
                                count++
                            if ((state.board[precVertex(vertex)][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)) &&
                                    (state.board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
                                count++
                            if ((state.board[nextVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                                    (state.board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)))
                                count++
                            if ((state.board[nextVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                                    (state.board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
                                count++
                            if ((state.board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                                    (state.board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)))
                                count++
                            if ((state.board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                                    (state.board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
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
        var intOpposite = -1
        when(opposite){
            Checker.WHITE -> intOpposite = 0
            Checker.BLACK -> intOpposite = 1
        }
        when(state.currentPhase){
            '1'->{
                return ((state.checkers[intOpposite]==0) && (getNumPieces(state, opposite) < 3))
            }
            '2'->{
                return (getNumPieces(state, opposite) < 3) || (checkNoMoves(state, opposite))
            }
            '3'->{
                return (getNumPieces(state, opposite) < 3)
            }
        }
        return false
    }

    fun opposite(state : State): Checker {
        when (state.checker) {
            Checker.WHITE -> return Checker.BLACK
            Checker.BLACK -> return Checker.WHITE
        }
        return Checker.EMPTY
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
        when (level) {
            0 -> return arrayOf(1)
            1 -> return arrayOf(0, 2)
            2 -> return arrayOf(1)
        }
        return arrayOf()
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