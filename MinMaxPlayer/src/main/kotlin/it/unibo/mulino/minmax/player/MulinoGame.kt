package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.domain.State.Checker
import it.unibo.utils.Game

object MulinoGame : Game<State, String, Int> {

    const val WHITE_PLAYER = 0
    const val BLACK_PLAYER = 1
    const val NO_PLAYER = -1

    val checkersToChar = hashMapOf(
            Pair(NO_PLAYER, 'e'),
            Pair(WHITE_PLAYER, 'w'),
            Pair(BLACK_PLAYER, 'b')
    )
    //val checkersToInt = hashMapOf(Pair<Checker, Int>(Checker.WHITE, 0),
    //        Pair<Checker, Int>(Checker.BLACK, 1))

    val toInternalPositions = hashMapOf(
            Pair("a1", 0),
            Pair("a4", 3),
            Pair("a7", 6),
            Pair("b2", 1),
            Pair("b4", 4),
            Pair("b6", 7),
            Pair("c3", 2),
            Pair("c4", 5),
            Pair("c5", 8),
            Pair("d5", 11),
            Pair("d6", 10),
            Pair("d7", 9),
            Pair("d1", 21),
            Pair("d2", 22),
            Pair("d3", 23),
            Pair("e3", 20),
            Pair("e4", 17),
            Pair("e5", 14),
            Pair("f2", 19),
            Pair("f4", 16),
            Pair("f6", 13),
            Pair("g1", 18),
            Pair("g4", 15),
            Pair("g7", 12)
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

    /*
    private val adiacentPositions = hashMapOf(
            Pair("a1", arrayOf("d1", "a4")),
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
    )*/

    private val adiacentPositions = arrayOf(
            intArrayOf(21, 3),
            intArrayOf(22, 4),
            intArrayOf(23, 8),
            intArrayOf(0, 6, 4),
            intArrayOf(1, 7, 8, 3),
            intArrayOf(2, 8, 4),
            intArrayOf(3, 9),
            intArrayOf(4, 10),
            intArrayOf(8, 11),
            intArrayOf(6, 12, 10),
            intArrayOf(7, 13, 11, 9),
            intArrayOf(8, 14, 10),
            intArrayOf(9, 15),
            intArrayOf(10, 16),
            intArrayOf(17, 11),
            intArrayOf(18, 12, 16),
            intArrayOf(19, 13, 17, 15),
            intArrayOf(20, 14, 16),
            intArrayOf(21, 15),
            intArrayOf(22, 16),
            intArrayOf(23, 17),
            intArrayOf(0, 18, 22),
            intArrayOf(1, 19, 21, 23),
            intArrayOf(2, 20, 22)
    )

    private val nextVertex = hashMapOf(
            Pair(0, 1),
            Pair(1,2),
            Pair(2,3),
            Pair(3,4),
            Pair(4,5),
            Pair(5,6),
            Pair(6,7),
            Pair(7, 0)
    )

    private val precVertex = hashMapOf(
            Pair(0, 7),
            Pair(1,0),
            Pair(2,1),
            Pair(3,2),
            Pair(4,3),
            Pair(5,4),
            Pair(6,5),
            Pair(7, 6)
    )

    private val nextLevel = hashMapOf(
            Pair(0, 1),
            Pair(1,2),
            Pair(2, 0)
    )

    private val adiacentLevels = hashMapOf(
            Pair(0, arrayOf(1)),
            Pair(1,arrayOf(0,2)),
            Pair(2, arrayOf(1))
    )


    fun opposite(playerType: Int) = Math.abs(playerType - 1)

    override fun getInitialState(): State = State(WHITE_PLAYER)

    override fun getPlayer(state: State): Int = state.playerType


    override fun getPlayers(): Array<Int> = arrayOf(WHITE_PLAYER, BLACK_PLAYER)

    override fun getUtility(state: State, player: Int): Double {
        var utility = 0.00
        when (player) {
            state.playerType -> when (state.currentPhase) {
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


    /*
    fun getActionsNew(state : State?) : MutableList<String>{

    }*/


    override fun getActions(state: State?): MutableList<String> {
        if (state == null)
            throw IllegalArgumentException("State is  null")

        val actions = mutableListOf<String>()

        val player = state.playerType
        val opposite = Math.abs(state.playerType - 1)
        when(state.currentPhase){
            1 ->{
                var numMorrises = 0
                for (possiblePosition in getEmptyPositions(state)) {
                    if (checkMorris(state, possiblePosition, player)) {
                        for (adversarialPosition in getPositions(state, opposite)) {
                            if(!checkMorris(state, adversarialPosition, opposite)) {
                                actions.add(ActionMapper.azioniFase1ConRemove[possiblePosition][adversarialPosition])
                            }else numMorrises++
                        }
                        if (numMorrises == state.checkersOnBoard[opposite]) {
                            // avversario ha tutte le pedine in un morris
                            for (adversarialPosition in getPositions(state, opposite)) {
                                actions.add(ActionMapper.azioniFase1ConRemove[possiblePosition][adversarialPosition])
                            }
                        }
                    } else {
                        actions.add(ActionMapper.azioniFase1SenzaRemove[possiblePosition])
                    }
                }
            }
            2 ->{
                var numMorrises = 0
                for (actualPosition in getPositions(state, player)) {
                    for (adiacentPosition in adiacentPositions[actualPosition]) {
                        if (getPiece(state.board, adiacentPosition) == Checker.EMPTY) {
                            if (checkMorris(state, actualPosition, adiacentPosition, player)) {
                                for (adversarialPosition in getPositions(state, opposite)) {
                                    if(!checkMorris(state, adversarialPosition, opposite)) {
                                        actions.add(ActionMapper.azioniFase2ConRemove[actualPosition][adiacentPosition][adversarialPosition])
                                    }else numMorrises++
                                }
                                if (numMorrises == state.checkersOnBoard[opposite]) {
                                    for (adversarialPosition in getPositions(state, opposite)) {
                                        actions.add(ActionMapper.azioniFase2ConRemove[actualPosition][adiacentPosition][adversarialPosition])
                                    }
                                }
                            } else {
                                actions.add(ActionMapper.azioniFase2SenzaRemove[actualPosition][adiacentPosition])
                            }
                        }
                    }
                }
            }
            3->{
                var numMorrises = 0
                when (state.checkersOnBoard[player]) {
                    3 -> {
                        for (actualPosition in getPositions(state, player)) {
                            for (possiblePosition in getEmptyPositions(state)) {
                                if (checkMorris(state, actualPosition, possiblePosition, player)) {
                                    for (adversarialPosition in getPositions(state, opposite)) {
                                        if (!checkMorris(state, adversarialPosition, opposite)) {
                                            actions.add(ActionMapper.azioniFase3ConRemove[actualPosition][possiblePosition][adversarialPosition])
                                        } else numMorrises++
                                    }
                                    if (numMorrises == state.checkersOnBoard[opposite]) {
                                        for (adversarialPosition in getPositions(state, opposite)) {
                                            actions.add(ActionMapper.azioniFase3ConRemove[actualPosition][possiblePosition][adversarialPosition])
                                        }
                                    }
                                } else {
                                    actions.add(ActionMapper.azioniFase3SenzaRemove[actualPosition][possiblePosition])
                                }
                            }
                        }
                    }
                    else -> {
                        for (actualPosition in getPositions(state, player)) {
                            for (adiacentPosition in adiacentPositions[actualPosition]) {
                                if (getPiece(state.board, adiacentPosition) == Checker.EMPTY) {
                                    if (checkMorris(state, actualPosition, adiacentPosition, player)) {
                                        for (adversarialPosition in getPositions(state, opposite)) {
                                            if (!checkMorris(state, adversarialPosition, opposite)) {
                                                actions.add(ActionMapper.azioniFase3ConRemove[actualPosition][adiacentPosition][adversarialPosition])
                                            } else numMorrises++
                                        }
                                        if (numMorrises == state.checkersOnBoard[opposite]) {
                                            for (adversarialPosition in getPositions(state, opposite)) {
                                                actions.add(ActionMapper.azioniFase3ConRemove[actualPosition][adiacentPosition][adversarialPosition])
                                            }
                                        }
                                    } else {
                                        actions.add(ActionMapper.azioniFase3SenzaRemove[actualPosition][adiacentPosition])
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return actions
    }

    // TODO("Cambiare azione in intero")
    override fun getResult(state: State?, action: String): State {
        if (state == null) throw IllegalArgumentException("State is null")
        if (action == "") {
            throw IllegalArgumentException("mossa null. ${state}")
        }

        val player = state.playerType
        val opposite = Math.abs(player - 1)
        val newCheckers = intArrayOf(state.checkers[0], state.checkers[1])
        val newBoardCheckers = intArrayOf(state.checkersOnBoard[0], state.checkersOnBoard[1])
        var newBoard = state.board
        var newClosedMorris = false
        var newPhase = state.currentPhase

        when(action.get(0)){
            '1' ->{
                newBoard = addPiece(newBoard, toInternalPositions[action.substring(1, 3)]!!, player)
                newCheckers[player]-- //newState.checkers[player]--
                newBoardCheckers[player]++ // newState.checkersOnBoard[player]++

                if (action.length>3) {
                    newBoard = removePiece(newBoard, toInternalPositions[action.substring(3, 5)]!!)
                    newBoardCheckers[opposite]--
                    newClosedMorris = true
                }
                if (newCheckers[opposite] == 0)
                    newPhase = 2
                else
                    newPhase = 1
            }

            '2' ->{
                newBoard = removePiece(newBoard, toInternalPositions[action.substring(1, 3)]!!)
                newBoard = addPiece(newBoard, toInternalPositions[action.substring(3, 5)]!!, player)

                if (action.length>5) {
                    newBoard = removePiece(newBoard, toInternalPositions[action.substring(5, 7)]!!)
                    newBoardCheckers[opposite]--
                    newClosedMorris = true
                }
                if (newBoardCheckers[opposite] == 3)
                    newPhase = 3
                else
                    newPhase = 2
            }
            '3' ->{
                newBoard = removePiece(newBoard, toInternalPositions[action.substring(1, 3)]!!)
                newBoard = addPiece(newBoard, toInternalPositions[action.substring(3, 5)]!!, player)
                if (action.length>5) {
                    newBoard = removePiece(newBoard, toInternalPositions[action.substring(5, 7)]!!)
                    newBoardCheckers[opposite]--
                    newClosedMorris = true
                }
                newPhase = 3
            }
        }
        //val totalTime = System.nanoTime()-startTime
        //println("Action ${state.playerType}: $action -> State : ${printState(newState)}")
        return State(opposite, newBoard, newCheckers, newBoardCheckers, newPhase, newClosedMorris)
    }

    override fun isTerminal(state: State?): Boolean {
        var check = false
        return isWinner(state!!, WHITE_PLAYER) || isWinner(state, BLACK_PLAYER)
    }

    private fun getPiece(board: IntArray, position: Int): Checker {
        when {
            State.isSet(board, position, 0) && !State.isSet(board, position, 1) -> return Checker.WHITE
            !State.isSet(board, position, 0) && State.isSet(board, position, 1) -> return Checker.BLACK
            State.isNotSet(board, position) -> return Checker.EMPTY
            else -> throw IllegalStateException("Stato board non valido")
        }
    }

    fun addPiece(board: IntArray, position: Int, playerType: Int): IntArray =
            when (State.isNotSet(board, position)) {
                false -> throw IllegalStateException("In $position non c'è già pezzo")
                true -> when (playerType) {
                    0 -> intArrayOf(board[0] + State.position[position], board[1])
                    1 -> intArrayOf(board[0], board[1] + State.position[position])
                    else -> throw IllegalArgumentException("Player non valido")
                }
            }

    private fun removePiece(board: IntArray, position: Int): IntArray = when {
        State.isSet(board, position, 0) -> intArrayOf(board[0] - State.position[position], board[1])
        State.isSet(board, position, 1) -> intArrayOf(board[0], board[1] - State.position[position])
        else -> throw IllegalStateException("In $position non c'è nessun pezzo")
    }

    private fun getPositions(state: State, playerType: Int): MutableList<Int> {
        val positions = mutableListOf<Int>()
        for (position in 0 until 24)
            if (State.isSet(state.board, position, playerType))
                positions.add(position)
        return positions
    }

    private fun getEmptyPositions(state: State): MutableList<Int> {
        val emptyPositions = mutableListOf<Int>()
        for (position in 0 until 24)
            if (State.isNotSet(state.board, position))
                emptyPositions.add(position)
        return emptyPositions
    }

    val delinearizeVertex = intArrayOf(0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7)
    val deliearizeLevel = intArrayOf(0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2)

    private fun checkMorris(state: State, position: Int, playerType: Int): Boolean {
        val vertex = delinearizeVertex[position]
        val level = deliearizeLevel[position]

        val check = when (vertex) {
            1, 3, 5, 7 -> ((State.isSet(state.board, precVertex[vertex]!!, level, playerType)) &&
                    (State.isSet(state.board, nextVertex[vertex]!!, level, playerType))) ||
                    ((State.isSet(state.board, vertex, nextLevel[level]!!, playerType)) &&
                            (State.isSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!, playerType)))
            else -> ((State.isSet(state.board, nextVertex[vertex]!!, level, playerType)) &&
                    (State.isSet(state.board, nextVertex[nextVertex[vertex]!!]!!, level, playerType))) ||
                    ((State.isSet(state.board, precVertex[vertex]!!, level, playerType)) &&
                            (State.isSet(state.board, precVertex[precVertex[vertex]!!]!!, level, playerType)))
        }
        return check
    }

    private fun checkMorris(state: State, oldPosition: Int, newPosition: Int, playerType: Int): Boolean {
        val oldVertex = delinearizeVertex[oldPosition]
        val oldLevel = deliearizeLevel[oldPosition]
        val newVertex = delinearizeVertex[newPosition]
        val newLevel = deliearizeLevel[newPosition]
        var check = false
        if (adiacentPositions[newPosition].contains(oldPosition)) {
            when (newVertex) {
                1, 3, 5, 7 -> when (oldVertex) {
                    newVertex -> check = ((State.isSet(state.board, precVertex[newVertex]!!, newLevel, playerType)) &&
                            (State.isSet(state.board, nextVertex[newVertex]!!, newLevel, playerType)))
                    else -> check = ((State.isSet(state.board, newVertex, nextLevel[newLevel]!!, playerType)) &&
                            (State.isSet(state.board, newVertex, nextLevel[nextLevel[newLevel]!!]!!, playerType)))
                }
                0, 2, 4, 6 -> when (oldVertex) {
                    nextVertex[newVertex] -> check = ((State.isSet(state.board, precVertex[newVertex]!!, newLevel, playerType)) &&
                            (State.isSet(state.board, precVertex[precVertex[newVertex]!!]!!, newLevel, playerType)))
                    else -> check = ((State.isSet(state.board, nextVertex[newVertex]!!, newLevel, playerType)) &&
                            (State.isSet(state.board, nextVertex[nextVertex[newVertex]!!]!!, newLevel, playerType)))
                }
            }
        } else {
            when (newVertex) {
                1, 3, 5, 7 -> check = ((State.isSet(state.board, precVertex[newVertex]!!, newLevel, playerType)) &&
                        (State.isSet(state.board, nextVertex[newVertex]!!, newLevel, playerType))) ||
                        ((State.isSet(state.board, newVertex, nextLevel[newLevel]!!, playerType)) &&
                                (State.isSet(state.board, newVertex, nextLevel[nextLevel[newLevel]!!]!!, playerType)) &&
                                (oldPosition != newVertex * 3 + nextLevel[newLevel]!!) &&
                                (oldPosition != newVertex * 3 + nextLevel[nextLevel[newLevel]!!]!!))
                0, 2, 4, 6 -> check = ((oldPosition != nextVertex[nextVertex[newVertex]!!]!! * 3 + newLevel) &&
                        (State.isSet(state.board, nextVertex[newVertex]!!, newLevel, playerType)) &&
                        (State.isSet(state.board, nextVertex[nextVertex[newVertex]!!]!!, newLevel, playerType))) ||
                        ((oldPosition != precVertex[precVertex[newVertex]!!]!! * 3 + newLevel) &&
                                (State.isSet(state.board, precVertex[newVertex]!!, newLevel, playerType)) &&
                                (State.isSet(state.board, precVertex[precVertex[newVertex]!!]!!, newLevel, playerType)))
            }
        }
        return check
    }

    private fun checkNoMoves(state: State, playerType: Int): Boolean =
            getBlockedPieces(state, playerType) == state.checkersOnBoard[playerType]


    private fun checkNoMoves(state: State, position: Int, playerType: Int): Boolean {
        var check = false
        val vertex = delinearizeVertex[position]
        val level = deliearizeLevel[position]
        check = (State.isNotSet(state.board, nextVertex[vertex]!!, level) &&
                State.isNotSet(state.board, precVertex[vertex]!!, level))
        if(check){
            when (vertex) {
                1, 3, 5, 7 -> for (adiacentLevel in adiacentLevels[level]!!) {
                    check = check && (State.isNotSet(state.board, vertex, adiacentLevel))
                }
            }
        }
        return check
    }

    fun getBlockedPieces(state: State, playerType: Int): Int {
        var count = 0
        for (adversarialPosition in getPositions(state, playerType))
            if (checkNoMoves(state, adversarialPosition, playerType))
                count++
        return count
    }

    fun getNumMorrises(state: State, playerType: Int): Int {
        var count = 0
        for (position in getPositions(state, playerType)) {
            val vertex = delinearizeVertex[position]
            val level = deliearizeLevel[position]
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (State.isSet(state.board, nextVertex[vertex]!!, level, playerType) &&
                            State.isSet(state.board, precVertex[vertex]!!, level, playerType)) {
                        count++
                    }

                    State.isSet(state.board, vertex, nextLevel[level]!!, playerType)
                    if (level == 1 && (State.isSet(state.board, vertex, nextLevel[level]!!, playerType)) &&
                            (State.isSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!, playerType))) {
                        count++
                    }
                }
            }
        }
        return count
    }


    fun hasOpenedMorris(state: State, playerType: Int): Boolean {
        for (position in getPositions(state, playerType)) {
            for (adiacentPosition in adiacentPositions[position]) {
                val vertex = delinearizeVertex[adiacentPosition]
                val level = deliearizeLevel[adiacentPosition]
                if (State.isNotSet(state.board, vertex, level) && checkMorris(state, position, adiacentPosition, playerType))
                    return true
            }
        }
        return false
    }

    fun hasDoubleMorris(state: State, playerType: Int): Boolean {
        for (position in getPositions(state, playerType)) {
            val vertex = delinearizeVertex[position]
            val level = deliearizeLevel[position]
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (State.isSet(state.board, nextVertex[vertex]!!, level, playerType) &&
                            State.isSet(state.board, nextVertex[nextVertex[vertex]!!]!!, level, playerType) &&
                            State.isSet(state.board, precVertex[vertex]!!, level, playerType) &&
                            State.isSet(state.board, precVertex[precVertex[vertex]!!]!!, level, playerType))
                        return true

                }
                else -> {
                    if (State.isSet(state.board, nextVertex[vertex]!!, level, playerType) &&
                            State.isSet(state.board, precVertex[vertex]!!, level, playerType) &&
                            State.isSet(state.board, vertex, nextLevel[level]!!, playerType) &&
                            State.isSet(state.board, vertex, nextLevel[level]!!, playerType))
                        return true
                }
            }
        }
        return false
    }

    fun getNum2Conf(state: State, playerType: Int): Int {
        var count = 0
        val charChecker = checkersToChar[playerType]
        for (position in getPositions(state, playerType)) {
            val vertex = delinearizeVertex[position]
            val level = deliearizeLevel[position]
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (State.isSet(state.board, nextVertex[vertex]!!, level, playerType) &&
                            State.isNotSet(state.board, precVertex[vertex]!!, level))
                        count++
                    else if (State.isSet(state.board, precVertex[vertex]!!, level, playerType) &&
                            State.isNotSet(state.board, nextVertex[vertex]!!, level))
                        count++
                    when(level){
                        0->{
                            if (State.isSet(state.board, vertex, 1, playerType) &&
                                    State.isNotSet(state.board, vertex, 2))
                                count++
                        }
                        1->{
                            if (State.isSet(state.board, vertex, 0, playerType) &&
                                    State.isNotSet(state.board, vertex, 2))
                                count++
                            else if (State.isSet(state.board, vertex, 2, playerType) &&
                                    State.isNotSet(state.board, vertex, 0))
                                count++
                        }
                        2->{
                            if (State.isSet(state.board, vertex, 1, playerType) &&
                                    State.isNotSet(state.board, vertex, 0))
                                count++
                        }
                    }
                }
            }
        }
        return count
    }

    fun getNum3Conf(state: State, playerType: Int): Int {
        var count = 0
        for (position in getPositions(state, playerType)) {
            val vertex = delinearizeVertex[position]
            val level = deliearizeLevel[position]
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (State.isSet(state.board, nextVertex[vertex]!!, level, playerType) &&
                            State.isSet(state.board, precVertex[vertex]!!, level, playerType) &&
                            State.isNotSet(state.board, nextVertex[nextVertex[vertex]!!]!!, level) &&
                            State.isNotSet(state.board, precVertex[precVertex[vertex]!!]!!, level))
                        count++

                }
                else -> {
                    when (level) {
                        1 -> {
                            if ((State.isSet(state.board, nextVertex[vertex]!!, nextLevel[level]!!, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel[level]!!, playerType)) &&
                                    (State.isNotSet(state.board, precVertex[vertex]!!, nextLevel[level]!!)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!)))
                                count++
                            if ((State.isSet(state.board, nextVertex[vertex]!!, nextLevel[nextLevel[level]!!]!!, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!, playerType)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel[level]!!)))
                                count++
                            if ((State.isSet(state.board, precVertex[vertex]!!, nextLevel[level]!!, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel[level]!!, playerType)) &&
                                    (State.isNotSet(state.board, nextVertex[vertex]!!, nextLevel[level]!!)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!)))
                                count++
                            if ((State.isSet(state.board, precVertex[vertex]!!, nextLevel[nextLevel[level]!!]!!, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!, playerType)) &&
                                    (State.isNotSet(state.board, nextVertex[vertex]!!, nextLevel[nextLevel[level]!!]!!)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel[level]!!)))
                                count++
                            if ((State.isSet(state.board, nextVertex[vertex]!!, level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel[level]!!, playerType)) &&
                                    (State.isNotSet(state.board, precVertex[vertex]!!, level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!)))
                                count++
                            if ((State.isSet(state.board, nextVertex[vertex]!!, level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!, playerType)) &&
                                    (State.isNotSet(state.board, precVertex[vertex]!!, level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel[level]!!)))
                                count++
                            if ((State.isSet(state.board, precVertex[vertex]!!, level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel[level]!!, playerType)) &&
                                    (State.isNotSet(state.board, nextVertex[vertex]!!, level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!)))
                                count++
                            if ((State.isSet(state.board, precVertex[vertex]!!, level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel[nextLevel[level]!!]!!, playerType)) &&
                                    (State.isNotSet(state.board, nextVertex[vertex]!!, level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel[level]!!)))
                                count++
                        }
                    }
                }
            }
        }
        return count
    }

    fun density(state: State, position: Int, checker: Checker): Double {
        var density = 0.0
        for (adiacentPosition in adiacentPositions[position])
            if (getPiece(state.board, adiacentPosition) == checker)
                density++
        return density
    }

    fun isWinner(state: State, playerType: Int): Boolean {
        //var opposite =
        val intOpposite = Math.abs(playerType - 1)
        when(state.currentPhase){
            1->{
                return ((state.checkers[intOpposite]==0) && (state.checkersOnBoard[intOpposite] < 3))
            }
            2->{
                return (state.checkersOnBoard[intOpposite] < 3) || (checkNoMoves(state, intOpposite))
            }
            3->{
                return (state.checkersOnBoard[intOpposite] < 3)
            }
        }
        return false
    }

    fun printState(state : State): String {
        var out = "Phase : ${state.currentPhase}; Checker : ${state.playerType}; ClosedMorris : ${state.closedMorris}; WhiteCheckersOnBoard : ${state.checkersOnBoard[0]}; BlackCheckersOnBoard : ${state.checkersOnBoard[1]}; Positions : "

        for (position in getPositions(state, 0)) {
            out+="($position W) - "
        }
        for (position in getPositions(state, 1)) {
            out+="($position B) - "
        }
        return out
    }

}
/*
fun main(args: Array<String>) {

    var board = intArrayOf(0,0)
    board = MulinoGame.addPiece(board,"c5",0);
    board = MulinoGame.addPiece(board,"g4",0);
    board = MulinoGame.addPiece(board,"c4",0);
    board = MulinoGame.addPiece(board,"b4",0);
    board = MulinoGame.addPiece(board,"a4",0);
    board = MulinoGame.addPiece(board,"e3",0);
    board = MulinoGame.addPiece(board,"d3",0);
    board = MulinoGame.addPiece(board,"c3",0);
    board = MulinoGame.addPiece(board,"d1",0);
    board = MulinoGame.addPiece(board,"b2",1);
    board = MulinoGame.addPiece(board,"f2",1);
    board = MulinoGame.addPiece(board,"g1",1);
    val state = State(1,board, intArrayOf(0,0), intArrayOf(9,3),3,false);

    val actions = MulinoGame.getActions(state);
*/
    /*
    val state = State(Checker.WHITE)
    //println("Turno: ${state.playerType}")

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

}*/