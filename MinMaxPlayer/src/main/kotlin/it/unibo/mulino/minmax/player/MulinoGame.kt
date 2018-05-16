package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.domain.State.Checker
import it.unibo.utils.Game
import java.util.*

object MulinoGame : Game<State, String, Checker> {

    val checkersToChar = hashMapOf(Pair<Checker, Char>(Checker.EMPTY, 'e'),
            Pair<Checker, Char>(Checker.WHITE, 'w'),
            Pair<Checker, Char>(Checker.BLACK, 'b'))
    val checkersToInt = hashMapOf(Pair<Checker, Int>(Checker.WHITE, 0),
            Pair<Checker, Int>(Checker.BLACK, 1))
    val charsToCheckers = hashMapOf(Pair<Char, Checker>('e', Checker.EMPTY),
            Pair<Char, Checker>('w', Checker.WHITE),
            Pair<Char, Checker>('b', Checker.BLACK))
    val toInternalPositions = hashMapOf(Pair(
            "a1", Pair(0, 0)),
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
    val toExternalPositions = hashMapOf(
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

    private val adiacentPositions = hashMapOf(Pair("a1", arrayOf("d1", "a4")),
            Pair("a4", arrayOf("a1", "a7", "b4")),
            Pair("a7", arrayOf("a4", "d7")),
            Pair("b2", arrayOf("d2", "b4")),
            Pair("b4", arrayOf("b2", "b6", "c4", "a4")),
            Pair("b6", arrayOf("b4", "d6")),
            Pair("c3", arrayOf("d3", "c4")),
            Pair("c4", arrayOf("c3", "c5", "b4")),
            Pair("c5", arrayOf("c4", "d5")),
            Pair("d5", arrayOf("c5", "e5", "d6")),
            Pair("d6", arrayOf("b6", "f6", "d5", "d7")),
            Pair("d7", arrayOf("a7", "g7", "d6")),
            Pair("d1", arrayOf("a1", "g1", "d2")),
            Pair("d2", arrayOf("b2", "f2", "d1", "d3")),
            Pair("d3", arrayOf("c3", "e3", "d2")),
            Pair("e3", arrayOf("d3", "e4")),
            Pair("e4", arrayOf("e3", "e5", "f4")),
            Pair("e5", arrayOf("e4", "d5")),
            Pair("f2", arrayOf("d2", "f4")),
            Pair("f4", arrayOf("f2", "f6", "e4", "g4")),
            Pair("f6", arrayOf("d6", "f4")),
            Pair("g1", arrayOf("d1", "g4")),
            Pair("g4", arrayOf("g1", "g7", "f4")),
            Pair("g7", arrayOf("d7", "g4"))
    )

    private val nextVertex = hashMapOf(Pair(0,1),
            Pair(1,2),
            Pair(2,3),
            Pair(3,4),
            Pair(4,5),
            Pair(5,6),
            Pair(6,7),
            Pair(7,0))

    private val precVertex = hashMapOf(Pair(0,7),
            Pair(1,0),
            Pair(2,1),
            Pair(3,2),
            Pair(4,3),
            Pair(5,4),
            Pair(6,5),
            Pair(7,6))

    private val nextLevel = hashMapOf(Pair(0,1),
            Pair(1,2),
            Pair(2,0))

    private val adiacentLevels = hashMapOf(Pair(0,arrayOf(1)),
            Pair(1,arrayOf(0,2)),
            Pair(2,arrayOf(1)))

    /*
    val diagonals : HashMap<String, IntArray> = hashMapOf(Pair("000", intArrayOf(0,0,0)),
            Pair("111", intArrayOf(1,1,1)),
            Pair("222", intArrayOf(2,2,2)),
            Pair("001", intArrayOf(0,0,1)),
            Pair("002", intArrayOf(0,0,2)),
            Pair("110", intArrayOf(1,1,0)),
            Pair("112", intArrayOf(1,1,2)),
            Pair("220", intArrayOf(2,2,0)),
            Pair("221", intArrayOf(2,2,1)),
            Pair("010", intArrayOf(0,1,0)),
            Pair("011", intArrayOf(0,1,1)),
            Pair("012", intArrayOf(0,1,2)),
            Pair("020", intArrayOf(0,2,0)),
            Pair("022", intArrayOf(0,2,2)),
            Pair("021", intArrayOf(0,2,1)),
            Pair("100", intArrayOf(1,0,0)),
            Pair("101", intArrayOf(1,0,1)),
            Pair("102", intArrayOf(1,0,2)),
            Pair("120", intArrayOf(1,2,0)),
            Pair("121", intArrayOf(1,2,1)),
            Pair("122", intArrayOf(1,2,2)),
            Pair("200", intArrayOf(2,0,0)),
            Pair("201", intArrayOf(2,0,1)),
            Pair("202", intArrayOf(2,0,2)),
            Pair("210", intArrayOf(2,1,0)),
            Pair("212", intArrayOf(2,1,2)),
            Pair("211", intArrayOf(2,1,1)))
    */

    val diagonals: HashMap<String, CharArray> = hashMapOf(
            Pair("eee", charArrayOf('e', 'e', 'e')),
            Pair("www", charArrayOf('w','w','w')),
            Pair("bbb", charArrayOf('b','b','b')),
            Pair("eew", charArrayOf('e','e','w')),
            Pair("eeb", charArrayOf('e','e','b')),
            Pair("wwe", charArrayOf('w','w','e')),
            Pair("wwb", charArrayOf('w','w','b')),
            Pair("bbe", charArrayOf('b','b','e')),
            Pair("bbw", charArrayOf('b','b','w')),
            Pair("ewe", charArrayOf('e','w','e')),
            Pair("eww", charArrayOf('e','w','w')),
            Pair("ewb", charArrayOf('e','w','b')),
            Pair("ebe", charArrayOf('e','b','e')),
            Pair("ebb", charArrayOf('e','b','b')),
            Pair("ebw", charArrayOf('e','b','w')),
            Pair("wee", charArrayOf('w','e','e')),
            Pair("wew", charArrayOf('w','e','w')),
            Pair("web", charArrayOf('w','e','b')),
            Pair("wbe", charArrayOf('w','b','e')),
            Pair("wbw", charArrayOf('w','b','w')),
            Pair("wbb", charArrayOf('w','b','b')),
            Pair("bee", charArrayOf('b','e','e')),
            Pair("bew", charArrayOf('b','e','w')),
            Pair("beb", charArrayOf('b','e','b')),
            Pair("bwe", charArrayOf('b','w','e')),
            Pair("bwb", charArrayOf('b','w','b')),
            Pair("bww", charArrayOf('b', 'w', 'w'))
    )

    val opposite = hashMapOf(
            Pair(Checker.BLACK, Checker.WHITE),
            Pair(Checker.WHITE, Checker.BLACK)
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
        var utility = 0.00
        when (player) {
            state.checker -> when(state.currentPhase) {
                2-> utility = (-1086).toDouble()
                3-> utility = (-1190).toDouble()
            }
            else -> when(state.currentPhase){
                2-> utility = (1086).toDouble()
                3-> utility = (1190).toDouble()
            }
        }
        return utility
    }

    override fun getActions(state: State?): MutableList<String> {
        val actions = mutableListOf<String>()
        //println("Current state : ${printState(state!!)}")
        val player = state!!.checker
        val opposite = opposite[state.checker]!!
        val intChecker = checkersToInt[player]!!
        val intOpposite = checkersToInt[opposite]!!
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
                        if(numMorrises == state.checkersOnBoard[intOpposite]){
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
                    for (adiacentPosition in adiacentPositions[actualPosition]!!) {
                        if (getPiece(state, adiacentPosition) == Checker.EMPTY) {
                            if (checkMorris(state, actualPosition, adiacentPosition, player)) {
                                for (adversarialPosition in getPositions(state, opposite)) {
                                    if(!checkMorris(state, adversarialPosition, opposite)) {
                                        actions.add("2$actualPosition$adiacentPosition$adversarialPosition")
                                    }else numMorrises++
                                }
                                if(numMorrises == state.checkersOnBoard[intOpposite]){
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
                when(state.checkersOnBoard[intChecker]){
                    3 ->{
                        for (actualPosition in getPositions(state, player)) {
                            for (possiblePosition in getEmptyPositions(state)) {
                                if (checkMorris(state, actualPosition, possiblePosition, player)) {
                                    for (adversarialPosition in getPositions(state, opposite)) {
                                        if (!checkMorris(state, adversarialPosition, opposite)) {
                                            actions.add("3$actualPosition$possiblePosition$adversarialPosition")
                                        } else numMorrises++
                                    }
                                    if (numMorrises == state.checkersOnBoard[intOpposite]) {
                                        for (adversarialPosition in getPositions(state, opposite)) {
                                            actions.add("3$actualPosition$possiblePosition$adversarialPosition")
                                        }
                                    }
                                } else {
                                    actions.add("3$actualPosition$possiblePosition")
                                }
                            }
                        }
                    }
                    else ->{
                        for (actualPosition in getPositions(state, player)) {
                            for (adiacentPosition in adiacentPositions[actualPosition]!!) {
                                if (getPiece(state, adiacentPosition) == Checker.EMPTY) {
                                    if (checkMorris(state, actualPosition, adiacentPosition, player)) {
                                        for (adversarialPosition in getPositions(state, opposite)) {
                                            if(!checkMorris(state, adversarialPosition, opposite)) {
                                                actions.add("3$actualPosition$adiacentPosition$adversarialPosition")
                                            }else numMorrises++
                                        }
                                        if(numMorrises == state.checkersOnBoard[intOpposite]){
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
        /*
        println("Possible actions: ")
        for(action in actions)
            println("$action")
        */
        return actions
    }

    override fun getResult(state: State?, action: String): State {
        if (state == null) throw IllegalArgumentException("State is null")

        val player = state.checker
        val opposite = opposite[state.checker]!!
        //val startTime = System.nanoTime()
        val diagonals : Array<CharArray> = Array(8, {index-> diagonals["${state.board[index][0]}${state.board[index][1]}${state.board[index][2]}"]!!})
        val newState = State(checker = opposite, board = diagonals, checkers = intArrayOf(state.checkers[0], state.checkers[1]), checkersOnBoard = intArrayOf(state.checkersOnBoard[0], state.checkersOnBoard[1]))
        val current = checkersToInt[player]!!
        val next = checkersToInt[opposite]!!
        for(vertex in 0..7)
            for(level in 0..2)
                newState.board[vertex][level]=state.board[vertex][level]
        when(action.get(0)){
            '1' ->{
                addPiece(newState, action.substring(1, 3), player)
                newState.checkers[current]--
                newState.checkersOnBoard[current]++

                if (action.length>3) {
                    removePiece(newState, action.substring(3, 5))
                    newState.checkersOnBoard[next]--
                    newState.closedMorris = true
                }
                if(newState.checkers[next]==0)
                    newState.currentPhase=2
                else
                    newState.currentPhase=1
            }

            '2' ->{
                removePiece(newState, action.substring(1, 3))
                addPiece(newState, action.substring(3, 5), player)

                if (action.length>5) {
                    removePiece(newState, action.substring(5, 7))
                    newState.checkersOnBoard[next]--
                    newState.closedMorris = true
                }
                if(newState.checkersOnBoard[next]==3)
                    newState.currentPhase = 3
                else
                    newState.currentPhase=2
            }
            '3' ->{
                removePiece(newState, action.substring(1, 3))
                addPiece(newState, action.substring(3, 5), player)
                if (action.length>5) {
                    removePiece(newState, action.substring(5, 7))
                    newState.checkersOnBoard[next]--
                    newState.closedMorris = true
                }
                newState.currentPhase=3
            }
        }
        //val totalTime = System.nanoTime()-startTime
        //println("Action ${state.checker}: $action -> State : ${printState(newState)}")
        return newState
    }

    override fun isTerminal(state: State?): Boolean {
        var check = false
        return isWinner(state!!, Checker.WHITE) || isWinner(state, Checker.BLACK)
    }

    /*
    fun isWinningConfiguration(state: State, checker: Checker): Boolean {
        var check = false
        var opposite = opposite[checker]
        val intChecker = checkersToInt[checker]!!
        val intOpposite = checkersToInt[opposite]!!
        if(checker!=state.checker || state.checkersOnBoard[intOpposite]>3) return false
        when(state.checkersOnBoard[intChecker]){
            3 ->{
                if(hasOpenedMorris(state, checker))
                    return true
            }
            else ->{
                for(position in getPositions(state, checker))
                    for(adiacentPosition in adiacentPositions[position]!!)
                        if(getPiece(state, adiacentPosition)==Checker.EMPTY && checkMorris(state, position, adiacentPosition, checker))
                            return true
            }
        }
        return false
    }
    */

    private fun getPiece(state : State, position: String): Checker {
        val (vertex, level) = toInternalPositions[position]!!
        return charsToCheckers[state.board[vertex][level]]!!
    }

    fun addPiece(state : State, position: String, checker: Checker) {
        val (vertex, level) = toInternalPositions[position]!!
        //state.board[vertex][level] = checkersToChar[checker]!!
        val intChecker = checkersToChar[checker]
        state.board[vertex] = when(level){
            0->diagonals["$intChecker${state.board[vertex][1]}${state.board[vertex][2]}"]!!
            1->diagonals["${state.board[vertex][0]}$intChecker${state.board[vertex][2]}"]!!
            2->diagonals["${state.board[vertex][0]}${state.board[vertex][1]}$intChecker"]!!
            else -> charArrayOf('e','e','e')
        }
    }

    private fun removePiece(state : State, position: String) {
        val (vertex, level) = toInternalPositions[position]!!
        //state.board[vertex][level] = 'e'
        state.board[vertex] = when(level){
            0->diagonals["e${state.board[vertex][1]}${state.board[vertex][2]}"]!!
            1->diagonals["${state.board[vertex][0]}e${state.board[vertex][2]}"]!!
            2->diagonals["${state.board[vertex][0]}${state.board[vertex][1]}e"]!!
            else ->charArrayOf('e','e','e')
        }
    }

    private fun getPositions(state : State, checker: Checker): List<String> {
        val positions = LinkedList<String>()
        for (position in toInternalPositions.keys) {
            val (vertex, level) = toInternalPositions[position]!!
            if (state.board[vertex][level] == checkersToChar[checker]!!)
                positions.add(position)
        }
        return positions
    }

    private fun getEmptyPositions(state : State): List<String> {
        val positions = LinkedList<String>()
        for (position in toInternalPositions.keys) {
            val (vertex, level) = toInternalPositions[position]!!
            if (state.board[vertex][level] == 'e')
                positions.add(position)
        }
        return positions
    }

    private fun checkMorris(state : State,position: String, checker: Checker): Boolean {
        val (vertex, level) = toInternalPositions[position]!!
        val check = when (vertex) {
            1, 3, 5, 7 -> ((state.board[precVertex[vertex]!!][level] == checkersToChar[checker]) &&
                    (state.board[nextVertex[vertex]!!][level] == checkersToChar[checker])) ||
                    ((state.board[vertex][nextLevel[level]!!] == checkersToChar[checker]) &&
                            ((state.board[vertex][nextLevel[nextLevel[level]!!]!!]) == checkersToChar[checker]))
            else -> ((state.board[nextVertex[vertex]!!][level] == checkersToChar[checker]) &&
                    (state.board[nextVertex[nextVertex[vertex]!!]!!][level] == checkersToChar[checker])) ||
                    ((state.board[precVertex[vertex]!!][level] == checkersToChar[checker]) &&
                            (state.board[precVertex[precVertex[vertex]!!]!!][level] == checkersToChar[checker]))
        }
        return check
    }

    private fun checkMorris(state : State, oldPosition: String, newPosition: String, checker: Checker): Boolean {
        val (oldVertex, oldLevel) = toInternalPositions[oldPosition]!!
        val (newVertex, newLevel) = toInternalPositions[newPosition]!!
        var check = false
        if (adiacentPositions[newPosition]!!.contains(oldPosition)) {
            when (newVertex) {
                1, 3, 5, 7 -> when (oldVertex) {
                    newVertex -> check = ((state.board[precVertex[newVertex]!!][newLevel] == checkersToChar[checker]) &&
                            (state.board[nextVertex[newVertex]!!][newLevel] == checkersToChar[checker]))
                    else -> check = ((state.board[newVertex][nextLevel[newLevel]!!] == checkersToChar[checker]) &&
                            (state.board[newVertex][nextLevel[nextLevel[newLevel]!!]!!] == checkersToChar[checker]))
                }
                0, 2, 4, 6 -> when (oldVertex) {
                    nextVertex[newVertex] -> check = ((state.board[precVertex[newVertex]!!][newLevel] == checkersToChar[checker]) &&
                            (state.board[precVertex[precVertex[newVertex]!!]!!][newLevel] == checkersToChar[checker]))
                    else -> check = ((state.board[nextVertex[newVertex]!!][newLevel] == checkersToChar[checker]) &&
                            (state.board[nextVertex[nextVertex[newVertex]!!]!!][newLevel] == checkersToChar[checker]))
                }
            }
        } else {
            when (newVertex) {
                1, 3, 5, 7 -> check = ((state.board[precVertex[newVertex]!!][newLevel] == checkersToChar[checker]) &&
                        (state.board[nextVertex[newVertex]!!][newLevel] == checkersToChar[checker])) ||
                        ((state.board[newVertex][nextLevel[newLevel]!!] == checkersToChar[checker]) &&
                                (state.board[newVertex][nextLevel[nextLevel[newLevel]!!]!!] == checkersToChar[checker]) &&
                                (toInternalPositions[oldPosition] != Pair(newVertex, nextLevel[newLevel]!!) &&
                                        (toInternalPositions[oldPosition] != Pair(newVertex, nextLevel[nextLevel[newLevel]!!]!!))))
                0, 2, 4, 6 -> check = ((toInternalPositions[oldPosition] != Pair(nextVertex[nextVertex[newVertex]!!]!!, newLevel)) &&
                        (state.board[nextVertex[newVertex]!!][newLevel] == checkersToChar[checker]) &&
                        (state.board[nextVertex[nextVertex[newVertex]!!]!!][newLevel] == checkersToChar[checker])) ||
                        ((toInternalPositions[oldPosition] != Pair(precVertex[precVertex[newVertex]!!]!!, newLevel)) &&
                                (state.board[precVertex[newVertex]!!][newLevel] == checkersToChar[checker]) &&
                                (state.board[precVertex[precVertex[newVertex]!!]!!][newLevel] == checkersToChar[checker]))
            }
        }
        return check
    }

    private fun checkNoMoves(state: State, checker: Checker): Boolean {
        val intChecker = checkersToInt[checker]!!
        return getBlockedPieces(state, checker)==state.checkersOnBoard[intChecker]

    }

    private fun checkNoMoves(state: State, position: String, checker: Checker): Boolean {
        var check = false
        val (vertex, level) = toInternalPositions[position]!!
        check = (state.board[nextVertex[vertex]!!][level] != 'e' &&
                state.board[precVertex[vertex]!!][level] != 'e')
        if(check){
            when (vertex) {
                1, 3, 5, 7 -> for (adiacentLevel in adiacentLevels[level]!!) {
                    check = check && (state.board[vertex][adiacentLevel] != 'e')
                }
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
                    if (state.board[nextVertex[vertex]!!][level] == checkersToChar[checker] &&
                            state.board[precVertex[vertex]!!][level] == checkersToChar[checker]) {
                        count++
                    }
                    if(level==1 && (state.board[vertex][nextLevel[level]!!] == checkersToChar[checker]) &&
                            (state.board[vertex][nextLevel[nextLevel[level]!!]!!] == checkersToChar[checker])){
                        count++
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
            for (adiacentPosition in adiacentPositions[position]!!) {
                val (vertex, level) = toInternalPositions[adiacentPosition]!!
                if (state.board[vertex][level] == 'e' && checkMorris(state, position, adiacentPosition, checker))
                    return true
            }
        }
        return false
    }

    /*
    fun hasDoubleMorris(state : State, checker: Checker): Boolean {
        var count = 0
        for (position in getPositions(state, checker)) {
            for (adiacentPosition in adiacentPositions[position]!!) {
                val (vertex, level) = toInternalPositions[adiacentPosition]!!
                if (state.board[vertex][level] == 'e' && checkMorris(state, position, adiacentPosition, checker))
                    count++
                if(count==2)
                    return true
            }
        }
        return false
    }
    */
    fun hasDoubleMorris(state : State, checker: Checker): Boolean {
        for (position in getPositions(state, checker)) {
            val (vertex, level) = toInternalPositions[position]!!
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (state.board[nextVertex[vertex]!!][level] == checkersToChar[checker] &&
                            state.board[nextVertex[nextVertex[vertex]!!]!!][level] == checkersToChar[checker] &&
                            state.board[precVertex[vertex]!!][level] == checkersToChar[checker] &&
                            state.board[precVertex[precVertex[vertex]!!]!!][level] == checkersToChar[checker])
                        return true

                }
                else -> {
                    if (state.board[nextVertex[vertex]!!][level] == checkersToChar[checker] &&
                            state.board[precVertex[vertex]!!][level] == checkersToChar[checker] &&
                            state.board[vertex][nextLevel[level]!!] == checkersToChar[checker] &&
                            state.board[vertex][nextLevel[level]!!] == checkersToChar[checker])
                        return true
                }
            }
        }
        return false
    }

    fun getNum2Conf(state: State, checker: Checker): Int {
        var count = 0
        val charChecker = checkersToChar[checker]
        for (position in getPositions(state, checker)) {
            val (vertex, level) = toInternalPositions[position]!!
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (state.board[nextVertex[vertex]!!][level] == charChecker &&
                            state.board[precVertex[vertex]!!][level] == 'e')
                        count++
                    else if (state.board[precVertex[vertex]!!][level] == charChecker &&
                            state.board[nextVertex[vertex]!!][level] == 'e')
                        count++
                    when(level){
                        0->{
                            if(state.board[vertex][1] == charChecker &&
                                    state.board[vertex][2] == 'e')
                                count++
                        }
                        1->{
                            if(state.board[vertex][0] == charChecker &&
                                    state.board[vertex][2] == 'e')
                                count++
                            else if (state.board[vertex][2] == charChecker &&
                                    state.board[vertex][0] == 'e')
                                count++
                        }
                        2->{
                            if(state.board[vertex][1] == charChecker &&
                                    state.board[vertex][0] == 'e')
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
            val (vertex, level) = toInternalPositions[position]!!
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (state.board[nextVertex[vertex]!!][level] == checkersToChar[checker] &&
                            state.board[precVertex[vertex]!!][level] == checkersToChar[checker] &&
                            state.board[nextVertex[nextVertex[vertex]!!]!!][level] == 'e' &&
                            state.board[precVertex[precVertex[vertex]!!]!!][level] == 'e')
                        count++

                }
                else -> {
                    when (level) {
                        1 -> {
                            if ((state.board[nextVertex[vertex]!!][nextLevel[level]!!] == checkersToChar[checker]) &&
                                    (state.board[vertex][nextLevel[level]!!] == checkersToChar[checker]) &&
                                    (state.board[precVertex[vertex]!!][nextLevel[level]!!] == 'e') &&
                                    (state.board[vertex][nextLevel[nextLevel[level]!!]!!] == 'e'))
                                count++
                            if ((state.board[nextVertex[vertex]!!][nextLevel[nextLevel[level]!!]!!] == checkersToChar[checker]) &&
                                    (state.board[vertex][nextLevel[nextLevel[level]!!]!!] == checkersToChar[checker]) &&
                                    (state.board[precVertex[vertex]!!][nextLevel[nextLevel[level]!!]!!] == 'e') &&
                                    (state.board[vertex][nextLevel[level]!!] == 'e'))
                                count++
                            if ((state.board[precVertex[vertex]!!][nextLevel[level]!!] == checkersToChar[checker]) &&
                                    (state.board[vertex][nextLevel[level]!!] == checkersToChar[checker]) &&
                                    (state.board[nextVertex[vertex]!!][nextLevel[level]!!] =='e') &&
                                    (state.board[vertex][nextLevel[nextLevel[level]!!]!!] == 'e'))
                                count++
                            if ((state.board[precVertex[vertex]!!][nextLevel[nextLevel[level]!!]!!] == checkersToChar[checker]) &&
                                    (state.board[vertex][nextLevel[nextLevel[level]!!]!!] == checkersToChar[checker]) &&
                                    (state.board[nextVertex[vertex]!!][nextLevel[nextLevel[level]!!]!!] =='e') &&
                                    (state.board[vertex][nextLevel[level]!!] == 'e'))
                                count++
                            if ((state.board[nextVertex[vertex]!!][level] == checkersToChar[checker]) &&
                                    (state.board[vertex][nextLevel[level]!!] == checkersToChar[checker]) &&
                                    (state.board[precVertex[vertex]!!][level] == 'e') &&
                                    (state.board[vertex][nextLevel[nextLevel[level]!!]!!] == 'e'))
                                count++
                            if ((state.board[nextVertex[vertex]!!][level] == checkersToChar[checker]) &&
                                    (state.board[vertex][nextLevel[nextLevel[level]!!]!!] == checkersToChar[checker]) &&
                                    (state.board[precVertex[vertex]!!][level] == 'e') &&
                                    (state.board[vertex][nextLevel[level]!!] == 'e'))
                                count++
                            if ((state.board[precVertex[vertex]!!][level] == checkersToChar[checker]) &&
                                    (state.board[vertex][nextLevel[level]!!] == checkersToChar[checker]) &&
                                    (state.board[nextVertex[vertex]!!][level] == 'e') &&
                                    (state.board[vertex][nextLevel[nextLevel[level]!!]!!] == 'e'))
                                count++
                            if ((state.board[precVertex[vertex]!!][level] == checkersToChar[checker]) &&
                                    (state.board[vertex][nextLevel[nextLevel[level]!!]!!] == checkersToChar[checker]) &&
                                    (state.board[nextVertex[vertex]!!][level] == 'e') &&
                                    (state.board[vertex][nextLevel[level]!!] == 'e'))
                                count++
                        }
                    }
                }
            }
        }
        return count
    }

    fun density(state: State, position : String, checker : Checker) : Double{
        var density = 0.0
        for(adiacentPosition in adiacentPositions[position]!!)
            if(getPiece(state, adiacentPosition)==checker)
                density++
        return density
    }

    fun isWinner(state: State, checker: Checker): Boolean {
        var opposite = opposite[checker]!!
        var intOpposite = checkersToInt[opposite]!!
        when(state.currentPhase){
            1->{
                return ((state.checkers[intOpposite]==0) && (state.checkersOnBoard[intOpposite] < 3))
            }
            2->{
                return (state.checkersOnBoard[intOpposite] < 3) || (checkNoMoves(state, opposite))
            }
            3->{
                return (state.checkersOnBoard[intOpposite] < 3)
            }
        }
        return false
    }

    fun printState(state : State): String {
        var out ="Phase : ${state.currentPhase}; Checker : ${state.checker}; ClosedMorris : ${state.closedMorris}; WhiteCheckersOnBoard : ${state.checkersOnBoard[0]}; BlackCheckersOnBoard : ${state.checkersOnBoard[1]}; Positions : "

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