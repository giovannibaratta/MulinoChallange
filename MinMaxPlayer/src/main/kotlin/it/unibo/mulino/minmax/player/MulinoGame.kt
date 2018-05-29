package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.domain.State.Checker

/**
 * Implementa le funzioni di [Game] più altre funzioni per la valutazione di uno stato
 */
object MulinoGame : Game() {

    const val WHITE_PLAYER = 0
    const val BLACK_PLAYER = 1
    const val NO_PLAYER = -1

    val checkersToChar = hashMapOf(
            Pair(NO_PLAYER, 'e'),
            Pair(WHITE_PLAYER, 'w'),
            Pair(BLACK_PLAYER, 'b')
    )

    // Mapping tra le posizioni utilizzare nello stato di chesani e le posizioni interne
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
    val adiacentPositions = arrayOf(
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

    fun nextVertex(vertex: Int) = (vertex + 1) % 8
    fun nextLevel(level: Int) = (level + 1) % 3
    fun previousVertex(vertex: Int) = when (vertex) {
        0 -> 7
        else -> vertex - 1
    }

    fun previousLevel(level: Int) = when (level) {
        0 -> 2
        else -> level - 1
    }

    private val adiacentLevels = hashMapOf(
            Pair(0, arrayOf(1)),
            Pair(1,arrayOf(0,2)),
            Pair(2, arrayOf(1))
    )

    fun opposite(playerType: Int) = Math.abs(playerType - 1)

    override val initialState: State
        get() = State(playerType = WHITE_PLAYER, board = intArrayOf(0, 0), checkers = intArrayOf(9, 9), checkersOnBoard = intArrayOf(0, 0), closedMorris = false)
    override val players: IntArray
        get() = intArrayOf(WHITE_PLAYER, BLACK_PLAYER)

    override fun getPlayer(state: State): Int = state.playerType

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

    private fun getPhase1Action(state: State): Actions {
        val actions = ArrayList<Int>(16) // possibili azioni per il player in questo stato
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
                        actions.add(ActionMapper.generateHashPh1(emptyPosition, enemyPosition))
                        added++
                    }
                if (added == 0 && enemyPositions.size > 0)
                // tutte le pedine sono bloccate in un mill
                    for (enemyPosition in enemyPositions)
                        actions.add(ActionMapper.generateHashPh1(emptyPosition, enemyPosition))
            } else
            // aggiungo l'azione senza remove
                actions.add(ActionMapper.generateHashPh1(emptyPosition))
        }
        return actions
    }

    private fun getPhase2Action(state: State): Actions {
        val actions = ArrayList<Int>(16) // possibili azioni per il player in questo stato
        val playerIndex = state.playerType // indice del player che deve giocare il turno
        val enemyIndex = Math.abs(state.playerType - 1) // indice del player avversario
        val playerPositions = getPositions(state, playerIndex)
        val enemyPositions = getPositions(state, enemyIndex)

        for (playerPosition in playerPositions)
            for (adiacentPlayerPosition in adiacentPositions[playerPosition]) {
                // se la posizione è libera aggiungo la mossa
                if (getPiece(state.board, adiacentPlayerPosition) == Checker.EMPTY) {
                    // verifico la chiusura del mill
                    // modificato da remoto
                    if (checkMorris(state, playerPosition, adiacentPlayerPosition, playerIndex)) {
                        // chiuso un mill devo rimuovere
                        var added = 0
                        for (enemyPosition in enemyPositions)
                            if (!checkMorris(state, enemyPosition, enemyIndex)) {
                                actions.add(ActionMapper.generateHashPh23(playerPosition, adiacentPlayerPosition, enemyPosition))
                                added++
                            }
                        if (added == 0 && enemyPositions.size > 0)
                        // tutte le pedine sono bloccate in un mill
                            for (enemyPosition in enemyPositions)
                                actions.add(ActionMapper.generateHashPh23(playerPosition, adiacentPlayerPosition, enemyPosition))
                    } else
                    // aggiungo l'azione senza remove
                        actions.add(ActionMapper.generateHashPh23(playerPosition, adiacentPlayerPosition))
                }
            }

        return actions
    }

    private fun getPhase3Action(state: State): Actions {
        val actions = ArrayList<Int>(16) // possibili azioni per il player in questo stato
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
                            actions.add(ActionMapper.generateHashPh23(playerPosition, emptyPosition, enemyPosition))
                            added++
                        }
                    if (added == 0 && enemyPositions.size > 0)
                    // tutte le pedine sono bloccate in un mill
                        for (enemyPosition in enemyPositions)
                            actions.add(ActionMapper.generateHashPh23(playerPosition, emptyPosition, enemyPosition))
                } else
                // aggiungo l'azione senza remove
                    actions.add(ActionMapper.generateHashPh23(playerPosition, emptyPosition))
            }

        return actions
    }

    override fun getActions(state: State): Actions {
        //if (state == null)
        //   throw IllegalArgumentException("State is  null")

        // assumo di aver ricevuto una fase corretta
        return when (state.currentPhase) {
            1 -> getPhase1Action(state)
            2 -> getPhase2Action(state)
            3 -> getPhase3Action(state)
            else -> throw IllegalStateException("Fase non valida")
        }
    }

    // TODO("Manca un controllo di coerenza, assumo che l'azione sia valida")
    override fun getResult(state: State, actionHash: Int): State {
        //if (state == null) throw IllegalArgumentException("State is null")

        // copio lo stato precedente
        val playerIndex = state.playerType
        val enemyIndex = Math.abs(playerIndex - 1)
        val newCheckers = intArrayOf(state.checkers[0], state.checkers[1])
        val newBoardCheckers = intArrayOf(state.checkersOnBoard[0], state.checkersOnBoard[1])
        val newBoard = intArrayOf(state.board[0], state.board[1])
        var newClosedMorris = false

        val action = ActionMapper.actionMap[actionHash]!!
        if (action.from != -1)
            removePiece(board = newBoard, position = action.from)
        else {
            newCheckers[playerIndex]-- // non c'è la from quindi è fase 1
            newBoardCheckers[playerIndex]++
        }
        addPiece(board = newBoard, position = action.to, playerType = playerIndex)
        if (action.remove != -1) {
            removePiece(board = newBoard, position = action.remove)
            newBoardCheckers[enemyIndex]--
            newClosedMorris = true
        }

        return State(playerType = enemyIndex, board = newBoard, checkers = newCheckers, checkersOnBoard = newBoardCheckers, closedMorris = newClosedMorris)
    }

    override fun isTerminal(state: State): Boolean =
            isWinner(state, WHITE_PLAYER) || isWinner(state, BLACK_PLAYER)

    fun getPiece(board: IntArray, position: Int): Checker {
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

    fun removePiece(board: IntArray, position: Int) = when {
        State.isSet(board, position, 0) -> board[0] -= State.position[position]
        State.isSet(board, position, 1) -> board[1] -= State.position[position]
        else -> throw IllegalStateException("In $position non c'è nessun pezzo")
    }

    fun getPositions(state: State, playerType: Int): ArrayList<Int> {
        val positions = ArrayList<Int>(24)
        for (position in 0 until 24)
            if (State.isSet(state.board, position, playerType))
                positions.add(position)
        return positions
    }

    fun getEmptyPositions(state: State): ArrayList<Int> {
        val emptyPositions = ArrayList<Int>(24)
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

    fun checkMorris(state: State, oldPosition: Int, newPosition: Int, playerType: Int): Boolean {
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


    fun checkNoMoves(state: State, playerType: Int): Boolean {
        for (position in getPositions(state, playerType))
            if (!checkNoMoves(state, position, playerType))
                return false
        return true
    }

    /**
     * true se il player non si può muovere
     */
    fun checkNoMoves(state: State, position: Int, playerType: Int): Boolean {
        val vertex = delinearizeVertex[position]
        val level = deliearizeLevel[position]

        if (State.isNotSet(state.board, nextVertex(vertex), level) ||
                State.isNotSet(state.board, previousVertex(vertex), level))
        // si può muovere
            return false

        // TODO("Possibile cambiare in vertex %2 != 0")
        when (vertex) {
            1, 3, 5, 7 -> {
                for (adiacentLevel in adiacentLevels[level]!!)
                // TODO("chekc && è ridondante")
                    if (State.isNotSet(state.board, vertex, adiacentLevel))
                        return false

            }
        }
        return true // tutte occupare
    }

    fun getBlockedPieces(state: State, playerPosition: ArrayList<Int>, playerType: Int): Int {
        var count = 0
        for (position in playerPosition)
            if (checkNoMoves(state, position, playerType))
                count++
        return count
    }

    fun getNumMorrises(state: State, playerPosition: ArrayList<Int>, playerType: Int): Int {
        var count = 0
        for (position in playerPosition) {
            val vertex = delinearizeVertex[position]
            val level = deliearizeLevel[position]
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (State.isSet(state.board, nextVertex(vertex), level, playerType) &&
                            State.isSet(state.board, previousVertex(vertex), level, playerType)) {
                        count++
                    }
                    if (level == 1 && (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                            (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType))) {
                        count++
                    }
                }
            }
        }
        return count
    }


    fun hasOpenedMorris(state: State, positions: ArrayList<Int>, playerType: Int): Boolean {
        for (position in positions) {
            for (adiacentPosition in adiacentPositions[position]) {
                val vertex = delinearizeVertex[adiacentPosition]
                val level = deliearizeLevel[adiacentPosition]
                if (State.isNotSet(state.board, vertex, level) && checkMorris(state, position, adiacentPosition, playerType))
                    return true
            }
        }
        return false
    }

    fun hasDoubleMorris(state: State, positions: ArrayList<Int>, playerType: Int): Boolean {
        for (position in positions) {
            val vertex = delinearizeVertex[position]
            val level = deliearizeLevel[position]
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (State.isSet(state.board, nextVertex(vertex), level, playerType) &&
                            State.isSet(state.board, nextVertex(nextVertex(vertex)), level, playerType) &&
                            State.isSet(state.board, previousVertex(vertex), level, playerType) &&
                            State.isSet(state.board, previousVertex(previousVertex(vertex)), level, playerType))
                        return true

                }
                else -> {
                    if (State.isSet(state.board, nextVertex(vertex), level, playerType) &&
                            State.isSet(state.board, previousVertex(vertex), level, playerType) &&
                            State.isSet(state.board, vertex, nextLevel(level), playerType) &&
                            State.isSet(state.board, vertex, nextLevel(level), playerType))
                        return true
                }
            }
        }
        return false
    }

    fun getNum2Conf(state: State, playerPosition: ArrayList<Int>, playerType: Int): Int {
        var count = 0
        val charChecker = checkersToChar[playerType]
        for (position in playerPosition) {
            val vertex = delinearizeVertex[position]
            val level = deliearizeLevel[position]
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (State.isSet(state.board, nextVertex(vertex), level, playerType) &&
                            State.isNotSet(state.board, previousVertex(vertex), level))
                        count++
                    else if (State.isSet(state.board, previousVertex(vertex), level, playerType) &&
                            State.isNotSet(state.board, nextVertex(vertex), level))
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

    fun getNum3Conf(state: State, playerPosition: ArrayList<Int>, playerType: Int): Int {
        var count = 0
        for (position in playerPosition) {
            val vertex = delinearizeVertex[position]
            val level = deliearizeLevel[position]
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (State.isSet(state.board, nextVertex(vertex), level, playerType) &&
                            State.isSet(state.board, previousVertex(vertex), level, playerType) &&
                            State.isNotSet(state.board, nextVertex(nextVertex(vertex)), level) &&
                            State.isNotSet(state.board, previousVertex(previousVertex(vertex)), level))
                        count++

                }
                else -> {
                    when (level) {
                        1 -> {
                            if ((State.isSet(state.board, nextVertex(vertex), nextLevel(level), playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                                    (State.isNotSet(state.board, previousVertex(vertex), nextLevel(level))) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                count++
                            if ((State.isSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isNotSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)))) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(level))))
                                count++
                            if ((State.isSet(state.board, previousVertex(vertex), nextLevel(level), playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                                    (State.isNotSet(state.board, nextVertex(vertex), nextLevel(level))) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                count++
                            if ((State.isSet(state.board, previousVertex(vertex), nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isNotSet(state.board, nextVertex(vertex), nextLevel(nextLevel(level)))) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(level))))
                                count++
                            if ((State.isSet(state.board, nextVertex(vertex), level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                                    (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                count++
                            if ((State.isSet(state.board, nextVertex(vertex), level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isNotSet(state.board, previousVertex(vertex), level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(level))))
                                count++
                            if ((State.isSet(state.board, previousVertex(vertex), level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(level), playerType)) &&
                                    (State.isNotSet(state.board, nextVertex(vertex), level)) &&
                                    (State.isNotSet(state.board, vertex, nextLevel(nextLevel(level)))))
                                count++
                            if ((State.isSet(state.board, previousVertex(vertex), level, playerType)) &&
                                    (State.isSet(state.board, vertex, nextLevel(nextLevel(level)), playerType)) &&
                                    (State.isNotSet(state.board, nextVertex(vertex), level)) &&
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
        val intOpposite = Math.abs(playerType - 1)
        return when (state.currentPhase) {
            1 -> ((state.checkers[intOpposite] == 0) && (state.checkersOnBoard[intOpposite] < 3))
            2 -> (state.checkersOnBoard[intOpposite] < 3) || (checkNoMoves(state, intOpposite))
            3 -> state.checkersOnBoard[intOpposite] < 3
            else -> throw IllegalStateException("Fase non valida")
        }
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
