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

    // Mapping tra le posizioni utilizzare nello stao di chesani e le posizioni interne
    val toInternalPositions = hashMapOf(
            Pair("a1", 0),
            Pair("b2", 1),
            Pair("c3", 2),
            Pair("a4", 3),
            Pair("b4", 4),
            Pair("c4", 5),
            Pair("a7", 6),
            Pair("b6", 7),
            Pair("c5", 8),
            Pair("d7", 9),
            Pair("d6", 10),
            Pair("d5", 11),
            Pair("g7", 12),
            Pair("f6", 13),
            Pair("e5", 14),
            Pair("g4", 15),
            Pair("f4", 16),
            Pair("e4", 17),
            Pair("g1", 18),
            Pair("f2", 19),
            Pair("e3", 20),
            Pair("d1", 21),
            Pair("d2", 22),
            Pair("d3", 23)
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

    // Posizione adiacenti ad ogni posizione della board. L'indice dell'array esterno
    // indica la posizione della board, mentre l'array interno sono le relative posizioni
    // adiacenti
    private val adiacentPositions = arrayOf(
            intArrayOf(21, 3), // 0
            intArrayOf(22, 4), // 1
            intArrayOf(23, 5), // 2
            intArrayOf(0, 6, 4), // 3
            intArrayOf(1, 7, 5, 3), // 4
            intArrayOf(2, 8, 4), // 5
            intArrayOf(3, 9), // 6
            intArrayOf(4, 10), // 7
            intArrayOf(5, 11), // 8
            intArrayOf(6, 12, 10), // 9
            intArrayOf(7, 13, 11, 9), // 10
            intArrayOf(8, 14, 10), // 11
            intArrayOf(9, 15), // 12
            intArrayOf(10, 16), // 13
            intArrayOf(17, 11), // 14
            intArrayOf(18, 12, 16), // 15
            intArrayOf(19, 13, 17, 15), // 16
            intArrayOf(20, 14, 16), // 17
            intArrayOf(21, 15), // 18
            intArrayOf(22, 16), // 19
            intArrayOf(23, 17), // 20
            intArrayOf(0, 18, 22), // 21
            intArrayOf(1, 19, 21, 23), // 22
            intArrayOf(2, 20, 22) // 23
    )

    private fun nextVertex(vertex: Int) = (vertex + 1) % 8
    private fun nextLevel(level: Int) = (level + 1) % 3
    private fun previousVertex(vertex: Int) = when (vertex) {
        0 -> 6
        else -> vertex - 1
    }

    private fun previousLevel(level: Int) = when (level) {
        0 -> 2
        else -> level - 1
    }

    //private fun previousVertex(vertex : Int) = (vertex - 1) % 7
    //private fun previousLevel(level : Int) = (level - 1) % 3

    private val adiacentLevels = hashMapOf(
            Pair(0, arrayOf(1)),
            Pair(1,arrayOf(0,2)),
            Pair(2, arrayOf(1))
    )

    fun opposite(playerType: Int) = Math.abs(playerType - 1)

    override fun getInitialState(): State = State(playerType = WHITE_PLAYER, board = intArrayOf(0, 0), checkers = intArrayOf(9, 9), checkersOnBoard = intArrayOf(0, 0), closedMorris = false)
    override fun getPlayer(state: State): Int = state.playerType
    override fun getPlayers(): Array<Int> = arrayOf(WHITE_PLAYER, BLACK_PLAYER)

    override fun getUtility(state: State, player: Int): Double =
            when (player) {
                state.playerType -> when (state.currentPhase) {
                    2 -> -1086.0
                    3 -> -1190.0
                    else -> throw IllegalStateException("Fase non valida")
                }
                else -> when (state.currentPhase) {
                    2 -> 1086.0
                    3 -> 1190.0
                    else -> throw IllegalStateException("Fase non valida")
                }
            }

    private fun getPhase1Action(state: State): MutableList<String> {
        val actions = mutableListOf<String>() // possibili azioni per il player in questo stato
        val playerIndex = state.playerType // indice del player che deve giocare il turno
        val enemyIndex = Math.abs(state.playerType - 1) // indice del player avversario
        val emptyPositions = getEmptyPositions(state)
        val enemyPositions = getPositions(state, enemyIndex)
        for (emptyPosition in emptyPositions) {
            if (checkMorris(state, emptyPosition, playerIndex)) {
                // chiuso un mill devo rimuovere
                var added = 0
                for (enemyPosition in enemyPositions)
                    if (!checkMorris(state, enemyPosition, enemyIndex)) {
                        actions.add(ActionMapper.azioniFase1ConRemove[emptyPosition][enemyPosition])
                        added++
                    }
                if (added == 0 && enemyPositions.size > 0)
                // tutte le pedine sono bloccate in un mill
                    for (enemyPosition in enemyPositions)
                        actions.add(ActionMapper.azioniFase1ConRemove[emptyPosition][enemyPosition])
            } else
            // aggiungo l'azione senza remove
                actions.add(ActionMapper.azioniFase1SenzaRemove[emptyPosition])
        }
        return actions
    }

    private fun getPhase2Action(state: State): MutableList<String> {
        val actions = mutableListOf<String>() // possibili azioni per il player in questo stato
        val playerIndex = state.playerType // indice del player che deve giocare il turno
        val enemyIndex = Math.abs(state.playerType - 1) // indice del player avversario
        val playerPositions = getPositions(state, playerIndex)
        val enemyPositions = getPositions(state, enemyIndex)

        for (playerPosition in playerPositions)
            for (adiacentPlayerPosition in adiacentPositions[playerPosition]) {
                // se la posizione è libera aggiungo la mossa
                if (getPiece(state.board, adiacentPlayerPosition) == Checker.EMPTY) {
                    // verifico la chiusura del mill
                    if (checkMorris(state, adiacentPlayerPosition, playerIndex)) {
                        // chiuso un mill devo rimuovere
                        var added = 0
                        for (enemyPosition in enemyPositions)
                            if (!checkMorris(state, enemyPosition, enemyIndex)) {
                                actions.add(ActionMapper.azioniFase2ConRemove[playerPosition][adiacentPlayerPosition][enemyPosition])
                                added++
                            }
                        if (added == 0 && enemyPositions.size > 0)
                        // tutte le pedine sono bloccate in un mill
                            for (enemyPosition in enemyPositions)
                                actions.add(ActionMapper.azioniFase2ConRemove[playerPosition][adiacentPlayerPosition][enemyPosition])
                    } else
                    // aggiungo l'azione senza remove
                        actions.add(ActionMapper.azioniFase2SenzaRemove[playerPosition][adiacentPlayerPosition])
                }
            }

        return actions
    }

    private fun getPhase3Action(state: State): MutableList<String> {
        val actions = mutableListOf<String>() // possibili azioni per il player in questo stato
        val playerIndex = state.playerType // indice del player che deve giocare il turno
        val enemyIndex = Math.abs(state.playerType - 1) // indice del player avversario
        val emptyPositions = getEmptyPositions(state)
        val enemyPositions = getPositions(state, enemyIndex)
        val playerPositions = getPositions(state, playerIndex)

        for (playerPosition in playerPositions)
            for (emptyPosition in emptyPositions) {
                if (checkMorris(state, playerPosition, emptyPosition, playerIndex)) {
                    // chiuso un mill devo rimuovere
                    var added = 0
                    for (enemyPosition in enemyPositions)
                        if (!checkMorris(state, enemyPosition, enemyIndex)) {
                            actions.add(ActionMapper.azioniFase3ConRemove[playerPosition][emptyPosition][enemyPosition])
                            added++
                        }
                    if (added == 0 && enemyPositions.size > 0)
                    // tutte le pedine sono bloccate in un mill
                        for (enemyPosition in enemyPositions)
                            actions.add(ActionMapper.azioniFase3ConRemove[playerPosition][emptyPosition][enemyPosition])
                } else
                // aggiungo l'azione senza remove
                    actions.add(ActionMapper.azioniFase3SenzaRemove[playerPosition][emptyPosition])
            }

        return actions
    }

    override fun getActions(state: State?): MutableList<String> {
        if (state == null)
            throw IllegalArgumentException("State is  null")

        // assumo di aver ricevuto una fase corretta
        return when (state.currentPhase) {
            1 -> getPhase1Action(state)
            2 -> getPhase2Action(state)
            3 -> getPhase3Action(state)
            else -> throw IllegalStateException("Fase non valida")
        }
    }

    // TODO("Cambiare azione in intero")
    override fun getResult(state: State?, action: String): State {
        if (state == null) throw IllegalArgumentException("State is null")

        // copio lo stato precedente
        val playerIndex = state.playerType
        val enemyIndex = Math.abs(playerIndex - 1)
        val newCheckers = intArrayOf(state.checkers[0], state.checkers[1])
        val newBoardCheckers = intArrayOf(state.checkersOnBoard[0], state.checkersOnBoard[1])
        val newBoard = intArrayOf(state.board[0], state.board[1])
        var newClosedMorris = false

        when(action.get(0)){
            '1' ->{
                addPiece(newBoard, toInternalPositions[action.substring(1, 3)]!!, playerIndex)
                newCheckers[playerIndex]--
                newBoardCheckers[playerIndex]++

                if (action.length>3) {
                    // presente una remove
                    removePiece(newBoard, toInternalPositions[action.substring(3, 5)]!!)
                    newBoardCheckers[enemyIndex]--
                    newClosedMorris = true
                }
            }

            '2' ->{
                removePiece(newBoard, toInternalPositions[action.substring(1, 3)]!!)
                addPiece(newBoard, toInternalPositions[action.substring(3, 5)]!!, playerIndex)

                if (action.length>5) {
                    // presente una remove
                    removePiece(newBoard, toInternalPositions[action.substring(5, 7)]!!)
                    newBoardCheckers[enemyIndex]--
                    newClosedMorris = true
                }
            }
            '3' ->{
                removePiece(newBoard, toInternalPositions[action.substring(1, 3)]!!)
                addPiece(newBoard, toInternalPositions[action.substring(3, 5)]!!, playerIndex)
                if (action.length>5) {
                    // presente una remove
                    removePiece(newBoard, toInternalPositions[action.substring(5, 7)]!!)
                    newBoardCheckers[enemyIndex]--
                    newClosedMorris = true
                }
            }
        }
        //val totalTime = System.nanoTime()-startTime
        //println("Action ${state.playerType}: $action -> State : ${printState(newState)}")
        return State(playerType = enemyIndex, board = newBoard, checkers = newCheckers, checkersOnBoard = newBoardCheckers, closedMorris = newClosedMorris)
    }

    override fun isTerminal(state: State?): Boolean =
            isWinner(state!!, WHITE_PLAYER) || isWinner(state, BLACK_PLAYER)

    private fun getPiece(board: IntArray, position: Int): Checker {
        when {
            State.isSet(board, position, 0) && !State.isSet(board, position, 1) -> return Checker.WHITE
            !State.isSet(board, position, 0) && State.isSet(board, position, 1) -> return Checker.BLACK
            State.isNotSet(board, position) -> return Checker.EMPTY
            else -> throw IllegalStateException("Stato board non valido")
        }
    }

    fun addPiece(board: IntArray, position: Int, playerType: Int) {
        if (!State.isNotSet(board, position))
            throw IllegalStateException("In $position non c'è già pezzo")
        board[playerType] += State.position[position]
    }

    private fun removePiece(board: IntArray, position: Int) = when {
        State.isSet(board, position, 0) -> board[0] -= State.position[position]
        State.isSet(board, position, 1) -> board[1] -= State.position[position]
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

    /**
     * @return true se mettendo la pedina in [position] si genera un morris per il [playerType] indicato
     */
    private fun checkMorris(state: State, position: Int, playerType: Int): Boolean {
        val vertex = delinearizeVertex[position]
        val level = deliearizeLevel[position]

        return when (vertex) {
            1, 3, 5, 7 -> {
                // mill verticale
                (((State.isSet(state.board, previousVertex(vertex), level, playerType)) && (State.isSet(state.board, nextVertex(vertex), level, playerType)))
                        // mill orizzontale
                        || ((State.isSet(state.board, vertex, nextLevel(level), playerType)) && (State.isSet(state.board, vertex, previousLevel(level), playerType))))
            }
            else -> {
                (((State.isSet(state.board, nextVertex(vertex), level, playerType)) && (State.isSet(state.board, nextVertex(nextVertex(vertex)), level, playerType)))
                        || ((State.isSet(state.board, previousVertex(vertex), level, playerType)) && (State.isSet(state.board, previousVertex(previousVertex(vertex)), level, playerType))))
            }
        }
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
                    newVertex -> check = ((State.isSet(state.board, previousVertex(newVertex), newLevel, playerType)) &&
                            (State.isSet(state.board, nextVertex(newVertex), newLevel, playerType)))
                    else -> check = ((State.isSet(state.board, newVertex, nextLevel(newLevel), playerType)) &&
                            (State.isSet(state.board, newVertex, previousLevel(newLevel), playerType)))
                }
                0, 2, 4, 6 -> when (oldVertex) {
                    nextVertex(newVertex) -> check = ((State.isSet(state.board, previousVertex(newVertex), newLevel, playerType)) &&
                            (State.isSet(state.board, previousVertex(previousVertex(newVertex)), newLevel, playerType)))
                    else -> check = ((State.isSet(state.board, nextVertex(newVertex), newLevel, playerType)) &&
                            (State.isSet(state.board, nextVertex(nextVertex(newVertex)), newLevel, playerType)))
                }
            }
        } else {
            when (newVertex) {
                1, 3, 5, 7 -> check = ((State.isSet(state.board, previousVertex(newVertex), newLevel, playerType)) &&
                        (State.isSet(state.board, nextVertex(newVertex), newLevel, playerType))) ||
                        ((State.isSet(state.board, newVertex, nextLevel(newLevel), playerType)) &&
                                (State.isSet(state.board, newVertex, previousLevel(newLevel), playerType)) &&
                                (oldPosition != newVertex * 3 + nextLevel(newLevel)) &&
                                (oldPosition != newVertex * 3 + nextLevel(nextLevel(newLevel))))
                0, 2, 4, 6 -> check = ((oldPosition != nextVertex(nextVertex(newVertex)) * 3 + newLevel) &&
                        (State.isSet(state.board, nextVertex(newVertex), newLevel, playerType)) &&
                        (State.isSet(state.board, nextVertex(nextVertex(newVertex)), newLevel, playerType))) ||
                        ((oldPosition != previousVertex(previousVertex(newVertex)) * 3 + newLevel) &&
                                (State.isSet(state.board, previousVertex(newVertex), newLevel, playerType)) &&
                                (State.isSet(state.board, previousVertex(previousVertex(newVertex)), newLevel, playerType)))
            }
        }
        return check
    }

    // TODO("Controllare da qui in poi")











    private fun checkNoMoves(state: State, playerType: Int): Boolean =
            getBlockedPieces(state, playerType) == state.checkersOnBoard[playerType]


    private fun checkNoMoves(state: State, position: Int, playerType: Int): Boolean {
        var check = false
        val vertex = delinearizeVertex[position]
        val level = deliearizeLevel[position]
        check = (State.isNotSet(state.board, nextVertex(level), level) &&
                State.isNotSet(state.board, previousVertex(vertex), level))
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
                    if (State.isSet(state.board, nextVertex(level), level, playerType) &&
                            State.isSet(state.board, previousVertex(vertex), level, playerType)) {
                        count++
                    }

                    State.isSet(state.board, vertex, nextLevel(level), playerType)
                    if (level == 1 && (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType))) {
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
                    if (State.isSet(state.board, nextVertex(level), level, playerType) &&
                            State.isSet(state.board, nextVertex(nextVertex(level)), level, playerType) &&
                            State.isSet(state.board, previousVertex(vertex), level, playerType) &&
                            State.isSet(state.board, previousVertex(previousVertex(vertex)), level, playerType))
                        return true

                }
                else -> {
                    if (State.isSet(state.board, nextVertex(level), level, playerType) &&
                            State.isSet(state.board, previousVertex(vertex), level, playerType) &&
                            State.isSet(state.board, vertex, nextLevel(level), playerType) &&
                            State.isSet(state.board, vertex, nextLevel(level), playerType))
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
                    if (State.isSet(state.board, nextVertex(level), level, playerType) &&
                            State.isNotSet(state.board, previousVertex(vertex), level))
                        count++
                    else if (State.isSet(state.board, previousVertex(vertex), level, playerType) &&
                            State.isNotSet(state.board, nextVertex(level), level))
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
                    if (State.isSet(state.board, nextVertex(level), level, playerType) &&
                            State.isSet(state.board, previousVertex(vertex), level, playerType) &&
                            State.isNotSet(state.board, nextVertex(nextVertex(level)), level) &&
                            State.isNotSet(state.board, previousVertex(previousVertex(vertex)), level))
                        count++

                }
                else -> {
                    when (level) {
                        1 -> {
                            if ((State.isSet(state.board, nextVertex(level), nextLevel(level), playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                                    (State.isNotSet(state.board, previousVertex(vertex), nextLevel(level))) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                count++
                            if ((State.isSet(state.board, nextVertex(level), nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(level))))
                                count++
                            if ((State.isSet(state.board, previousVertex(vertex), nextLevel(level), playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                                    (State.isNotSet(state.board, nextVertex(level), nextLevel(level))) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                count++
                            if ((State.isSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isNotSet(state.board, nextVertex(level), nextLevel(nextLevel(level)))) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(level))))
                                count++
                            if ((State.isSet(state.board, nextVertex(level), level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                                    (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                count++
                            if ((State.isSet(state.board, nextVertex(level), level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(level))))
                                count++
                            if ((State.isSet(state.board, previousVertex(vertex), level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                                    (State.isNotSet(state.board, nextVertex(level), level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                count++
                            if ((State.isSet(state.board, previousVertex(vertex), level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isNotSet(state.board, nextVertex(level), level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(level))))
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


fun main(args: Array<String>) {


    var board = intArrayOf(0, 0)
    MulinoGame.addPiece(board, 0, 0)
    MulinoGame.addPiece(board, 15, 0)
    MulinoGame.addPiece(board, 3, 0)
    MulinoGame.addPiece(board, 5, 0)
    MulinoGame.addPiece(board, 20, 0)
    MulinoGame.addPiece(board, 11, 0)
    MulinoGame.addPiece(board, 10, 0)
    MulinoGame.addPiece(board, 21, 1)
    MulinoGame.addPiece(board, 1, 1)
    MulinoGame.addPiece(board, 19, 1)
    MulinoGame.addPiece(board, 16, 1)
    MulinoGame.addPiece(board, 4, 1)

    MulinoGame.addPiece(board, 7, 1)

    MulinoGame.addPiece(board, 6, 1)

    MulinoGame.addPiece(board, 9, 1)

    MulinoGame.addPiece(board, 12, 1)
    val state = State(0, board, intArrayOf(0, 0), intArrayOf(6, 9), false)

    val actions = MulinoGame.getActions(state)
}