package it.unibo.mulino.qlearning.player.model

import it.unibo.utils.Matrix
import it.unibo.utils.filterCellIndexed
import java.util.*
import java.util.regex.Pattern
import kotlin.math.max
import it.unibo.ai.didattica.mulino.domain.State as ExternalState

internal class State(externalGrid: Matrix<Type>? = null,
                     val isWhiteTurn: Boolean,
                     val whiteHandCount: Int,
                     val blackHandCount: Int) {

    var grid: Matrix<Type>
    val boardSize: Int
    val middleLine: Int
    val midllePoint: Position

    init {
        require(whiteHandCount >= 0, { "White Hand count < 0" })
        require(blackHandCount >= 0, { "Black Hand count < 0" })
        if (externalGrid == null) {
            grid = Matrix.buildMatrix(7, 7, { _, _ -> Type.INVALID })
            boardSize = grid.rows
            middleLine = (boardSize - 1) / 2
            midllePoint = Position(middleLine, middleLine)
            // Inserisco le caselle vuote valide
            grid.forEachIndexed { xIndex, yIndex, _ ->
                if ((xIndex == middleLine && yIndex != middleLine) ||
                        (xIndex != middleLine && yIndex == middleLine) ||
                        (xIndex == yIndex && xIndex != middleLine) ||
                        (xIndex + yIndex == boardSize - 1 && xIndex != middleLine))
                    grid[xIndex, yIndex] = Type.EMPTY
            }
        } else {
            grid = externalGrid
            boardSize = grid.rows
            middleLine = (boardSize - 1) / 2
            midllePoint = Position(middleLine, middleLine)
        }
    }

    fun blackBoardCount() = grid.count { it == Type.BLACK }

    fun whiteBoardCount() = grid.count { it == Type.WHITE }
    //val whiteBoardCount() = grid.count { it == Type.WHITE }

    constructor(state: ExternalState, isWhiteTurn: Boolean)
            : this(isWhiteTurn = isWhiteTurn, whiteHandCount = state.whiteCheckers, blackHandCount = state.blackCheckers) {
        // mapping della rappresentazione esterna in rappresentazione interna
        state.board.forEach {
            val xAxis = it.key.split(Pattern.compile("[0-9]+$"))
            val yAxis = it.key.split(Pattern.compile("^[a-zA-Z]+"))
            if (xAxis.size != 2 && yAxis.size != 2)
                throw IllegalArgumentException("Posizione dello stato non valido")

            // TODO("Aggiungere compatibilità con estensione della board")
            val parsedPosition = Position(xAxis[0].toLowerCase().get(0) - 'a', yAxis[1].toInt() - 1)
            if (parsedPosition.x < 0 || parsedPosition.x >= boardSize
                    || parsedPosition.y < 0 || parsedPosition.y >= boardSize || grid[parsedPosition] == Type.INVALID)
                throw IllegalArgumentException("Stai tentando di mettere la pedina dove non dovresti")
            grid[parsedPosition] = when (it.value) {
                ExternalState.Checker.EMPTY -> Type.EMPTY
                ExternalState.Checker.WHITE -> Type.WHITE
                ExternalState.Checker.BLACK -> Type.BLACK
                else -> throw IllegalStateException("Il valore ricevuto non è mappabile")
            }
        }
    }

    fun numberOfLBlocks(type: Type? = null): Int {
        val typeToCheck = when (type == null) {
            false -> type
            true -> when (isWhiteTurn) {
                true -> Type.WHITE
                false -> Type.BLACK
            }
        }

        if (typeToCheck == Type.WHITE && whiteBoardCount() < 3)
            return 0

        if (typeToCheck == Type.BLACK && blackBoardCount() < 3)
            return 0

        var count = 0
        lBlocks.forEach {
            if (grid[it[0]] == typeToCheck && grid[it[1]] == typeToCheck && grid[it[2]] == typeToCheck)
                count++
        }

        return count
    }


    // TODO("Da rivedere e verificare che le pedine siano unicamente sui vertici e non solo in numero")
    fun parallelStructures(type: Type? = null): Int {
        val typeToCheck = when (type == null) {
            false -> type
            true -> when (isWhiteTurn) {
                true -> Type.WHITE
                false -> Type.BLACK
            }
        }

        val enemyType = when (typeToCheck) {
            Type.WHITE -> Type.BLACK
            Type.BLACK -> Type.WHITE
            else -> throw IllegalStateException("Type to check non valido")
        }
        var count = 0

        // lato sx verticale
        if (grid[0, 0] == typeToCheck && grid[0, 6] == typeToCheck
                && grid[1, 1] == typeToCheck && grid[1, 5] == typeToCheck
                && (grid[0, 3] != enemyType) && grid[1, 3] != enemyType)
            count++

        if (grid[2, 2] == typeToCheck && grid[2, 4] == typeToCheck
                && grid[1, 1] == typeToCheck && grid[1, 5] == typeToCheck
                && (grid[2, 3] != enemyType) && grid[1, 3] != enemyType)
            count++

        // lato dx verticale
        if (grid[6, 0] == typeToCheck && grid[6, 6] == typeToCheck
                && grid[5, 1] == typeToCheck && grid[5, 5] == typeToCheck
                && (grid[6, 3] != enemyType) && grid[5, 3] != enemyType)
            count++

        if (grid[4, 2] == typeToCheck && grid[4, 4] == typeToCheck
                && grid[5, 1] == typeToCheck && grid[5, 5] == typeToCheck
                && (grid[5, 3] != enemyType) && grid[4, 3] != enemyType)
            count++

        // sopra orizzontale
        if (grid[0, 6] == typeToCheck && grid[6, 6] == typeToCheck
                && grid[1, 5] == typeToCheck && grid[5, 5] == typeToCheck
                && (grid[3, 6] != enemyType) && grid[3, 5] != enemyType)
            count++

        if (grid[2, 4] == typeToCheck && grid[4, 4] == typeToCheck
                && grid[1, 5] == typeToCheck && grid[5, 5] == typeToCheck
                && (grid[3, 4] != enemyType) && grid[3, 5] != enemyType)
            count++

        // lato dx verticale
        if (grid[0, 0] == typeToCheck && grid[6, 0] == typeToCheck
                && grid[1, 1] == typeToCheck && grid[5, 1] == typeToCheck
                && (grid[3, 0] != enemyType) && grid[3, 1] != enemyType)
            count++

        if (grid[2, 2] == typeToCheck && grid[4, 2] == typeToCheck
                && grid[1, 1] == typeToCheck && grid[5, 1] == typeToCheck
                && (grid[3, 2] != enemyType) && grid[3, 1] != enemyType)
            count++

        return count
    }

    fun formLBlock(xIndex: Int, yIndex: Int): Boolean {
        val type = grid[xIndex, yIndex]
        val position = Position(xIndex, yIndex)
        val left = leftAdjacent(position)
        val right = rightAdjacent(position)
        val top = topAdjacent(position)
        val bottom = bottomAdjacent(position)

        return (left.isPresent && left.get() == type && top.isPresent && top.get() == type)
                || (left.isPresent && left.get() == type && bottom.isPresent && bottom.get() == type)
                || (right.isPresent && right.get() == type && top.isPresent && top.get() == type)
                || (right.isPresent && right.get() == type && bottom.isPresent && bottom.get() == type)
    }

    private fun millTest(position: Position, typeToMill: Type? = null): Pair<Boolean, Optional<Pair<Position, Position>>> {
        require(position.x >= 0 && position.x < boardSize, { "Coordinata X non valida" })
        require(position.y >= 0 && position.y < boardSize, { "Coordinata Y non valida" })
        require(grid.get(position) != Type.INVALID, { "Casella non valida" })

        val type = when (typeToMill != null) {
            true -> typeToMill
            false -> when (isWhiteTurn) {
                true -> Type.WHITE
                false -> Type.BLACK
            }
        }
        var mill = false
        var millPos1: Position? = null
        var millPos2: Position? = null

        // top mill
        topAdjacent(position).ifPresent {
            if (it.second == type) {
                millPos1 = it.first
                topAdjacent(it.first).ifPresent {
                    millPos2 = it.first
                    mill = it.second == type
                    return@ifPresent
                }
                bottomAdjacent(position).ifPresent {
                    mill = it.second == type
                    millPos2 = it.first
                }
            }
        }
        if (mill)
            return Pair(true, Optional.of(Pair(millPos1!!, millPos2!!)))

        // right mill
        rightAdjacent(position).ifPresent {
            if (it.second == type) {
                millPos1 = it.first
                rightAdjacent(it.first).ifPresent {
                    millPos2 = it.first
                    mill = it.second == type
                    return@ifPresent
                }
                leftAdjacent(position).ifPresent {
                    mill = it.second == type
                    millPos2 = it.first
                }
            }
        }
        if (mill)
            return Pair(true, Optional.of(Pair(millPos1!!, millPos2!!)))
        // left mill
        leftAdjacent(position).ifPresent outer@{
            if (it.second == type) {
                millPos1 = it.first
                leftAdjacent(it.first).ifPresent {
                    mill = it.second == type
                    millPos2 = it.first
                }
            }
        }
        if (mill)
            return Pair(true, Optional.of(Pair(millPos1!!, millPos2!!)))
        // bottom mill
        bottomAdjacent(position).ifPresent outer@{
            if (it.second == type) {
                millPos1 = it.first
                bottomAdjacent(it.first).ifPresent {
                    mill = it.second == type
                    millPos2 = it.first
                }
            }
        }

        if (mill)
            return Pair(true, Optional.of(Pair(millPos1!!, millPos2!!)))
        else
            return Pair(false, Optional.empty())
    }

    fun isAClosedMill(position: Position, typeToMill: Type? = null): Pair<Boolean, Optional<Pair<Position, Position>>> {
        require(typeToMill != Type.INVALID)
        val type = when (typeToMill != null) {
            true -> typeToMill
            false -> when (isWhiteTurn) {
                true -> Type.WHITE
                false -> Type.BLACK
            }
        }
        if (grid.get(position) == Type.EMPTY)
            return Pair(false, Optional.empty())
        if (grid.get(position) != type)
            throw IllegalArgumentException("Casella dell'avversario")
        return millTest(position, type)
    }

    /**
     * True se la pedina nella posizione indicata forma un mulino, altrimenti false
     */
    fun closeAMill(position: Position, typeToMill: Type? = null): Pair<Boolean, Optional<Pair<Position, Position>>> {
        require(grid.get(position) == Type.EMPTY, { "Casella non vuota" })
        return millTest(position, typeToMill)
    }


    // restituisce la griglia, e se ha chiuso un it.unibo.ai.didattica.mulino. Se azione non valida errore
    fun simulateAction(action: Action): ActionResult {
        //val cached = cache.get(this,action)
        //if(cached != null)
        //    return cached

        val newStateGrid = grid.deepCopy()
        val newState: State
        if (!action.from.isPresent) {
            // fase 1
            if (isWhiteTurn) {
                // diminuisco i bianchi
                newState = State(newStateGrid, !isWhiteTurn, max(whiteHandCount - 1, 0), blackHandCount)
            } else {
                //diminuisco i neri
                newState = State(newStateGrid, !isWhiteTurn, whiteHandCount, max(blackHandCount - 1, 0))
            }
        } else {
            // fase 2
            newState = State(newStateGrid, !isWhiteTurn, whiteHandCount, blackHandCount)
        }

        var closedMill = false

        val fromType: Type = when (action.from.isPresent) {
            true -> grid.get(action.from.get())
            false -> Type.INVALID
        }

        if (action.from.isPresent &&
                ((fromType != Type.WHITE && isWhiteTurn) || (fromType != Type.BLACK && !isWhiteTurn) || !action.to.isPresent))
            dumpAndThrow(this, action, Optional.empty(), IllegalStateException("La mossa from non è valida"))

        if (action.to.isPresent && grid.get(action.to.get()) != Type.EMPTY)
            dumpAndThrow(this, action, Optional.empty(), IllegalStateException("La mossa to non è valida"))

        val removeType: Type = when (action.remove.isPresent) {
            true -> grid.get(action.remove.get())
            false -> Type.INVALID
        }

        if (action.remove.isPresent &&
                ((removeType == Type.WHITE && isWhiteTurn) || (removeType == Type.BLACK && !isWhiteTurn)))
            dumpAndThrow(this, action, Optional.empty(), IllegalStateException("La mossa remove non è valida"))

        // rimuovo la pedina nel nuovo stato
        action.from.ifPresent { newStateGrid[it] = Type.EMPTY }

        // metto la pedina nel nuovo stato
        action.to.ifPresent {
            val typeToInsert = when (isWhiteTurn) {
                true -> Type.WHITE
                false -> Type.BLACK
            }
            newStateGrid[action.to.get()] = typeToInsert
            closedMill = newState.isAClosedMill(action.to.get(), typeToInsert).first
        }

        // ho fatto un mill ma non ho specificato la remove oppure non ho fatto un mill e ho specificato la remove
        if ((!closedMill && action.remove.isPresent) || (closedMill && when (isWhiteTurn) {
                    true -> grid.filterCellIndexed { it == Type.BLACK }.filter { !isAClosedMill(Position(it.first.first, it.first.second), it.second).first }.any()
                    false -> grid.filterCellIndexed { it == Type.WHITE }.filter { !isAClosedMill(Position(it.first.first, it.first.second), it.second).first }.any()
                } && !action.remove.isPresent))
            dumpAndThrow(this, action, Optional.of(newState), IllegalStateException("La mossa non è valida. Closed Mill ${closedMill}"))

        // ho fatto mill e devo rimuovere la pedina
        action.remove.ifPresent { newStateGrid[action.remove.get()] = Type.EMPTY }

        val enemyType: Type
        val enemyHandCount: Int
        val enemyBoardCount: Int

        when (isWhiteTurn) {
            true -> {
                enemyType = Type.BLACK
                enemyHandCount = newState.blackHandCount
                enemyBoardCount = newState.blackBoardCount()
            }
            false -> {
                enemyType = Type.WHITE
                enemyHandCount = newState.whiteHandCount
                enemyBoardCount = newState.whiteBoardCount()
            }
        }

        val actionResult = ActionResult(newState, closedMill, (!newState.playerCanMove(enemyType) || (enemyHandCount == 0 && enemyBoardCount <= 2)))

        //if(cached == null)
        //    cache.put(this,action,actionResult)

        return actionResult
    }

    private fun rightAdjacent(pos: Position, includeEmpty: Boolean = false): Optional<Pair<Position, Type>> {
        val offset = when (pos.y == middleLine) {
            true -> 1
            else -> Math.abs(pos.y - middleLine)
        }
        val rightPosition = Position(pos.x + offset, pos.y)
        if (rightPosition.x >= boardSize || (rightPosition.y != middleLine && rightPosition.x >= (boardSize - middleLine + offset)) || rightPosition == midllePoint)
            return Optional.empty()
        assert(grid[rightPosition] != Type.INVALID)
        if (!includeEmpty && grid[rightPosition] == Type.EMPTY)
            return Optional.empty()
        return Optional.of(Pair(rightPosition, grid[rightPosition]))
    }

    private fun leftAdjacent(pos: Position, includeEmpty: Boolean = false): Optional<Pair<Position, Type>> {
        val offset = when (pos.y == middleLine) {
            true -> 1
            else -> Math.abs(pos.y - middleLine)
        }
        val leftPosition = Position(pos.x - offset, pos.y)
        if (leftPosition.x < 0 || (leftPosition.y != middleLine && leftPosition.x < (middleLine - offset)) || leftPosition == midllePoint)
            return Optional.empty()
        assert(grid[leftPosition] != Type.INVALID)
        if (!includeEmpty && grid[leftPosition] == Type.EMPTY)
            return Optional.empty()
        return Optional.of(Pair(leftPosition, grid[leftPosition]))
    }

    private fun topAdjacent(pos: Position, includeEmpty: Boolean = false): Optional<Pair<Position, Type>> {
        val offset = when (pos.x == middleLine) {
            true -> 1
            else -> Math.abs(pos.x - middleLine)
        }
        val topPosition = Position(pos.x, pos.y + offset)
        if (topPosition.y >= boardSize || (topPosition.x != middleLine && topPosition.y >= (boardSize - middleLine + offset)) || topPosition == midllePoint)
            return Optional.empty()
        assert(grid[topPosition] != Type.INVALID)
        if (!includeEmpty && grid[topPosition] == Type.EMPTY)
            return Optional.empty()
        return Optional.of(Pair(topPosition, grid[topPosition]))
    }

    private fun bottomAdjacent(pos: Position, includeEmpty: Boolean = false): Optional<Pair<Position, Type>> {
        val offset = when (pos.x == middleLine) {
            true -> 1
            else -> Math.abs(pos.x - middleLine)
        }
        val bottomPosition = Position(pos.x, pos.y - offset)
        if (bottomPosition.y < 0 || (bottomPosition.x != middleLine && bottomPosition.y < (middleLine - offset)) || bottomPosition == midllePoint)
            return Optional.empty()
        assert(grid[bottomPosition] != Type.INVALID)
        if (!includeEmpty && grid[bottomPosition] == Type.EMPTY)
            return Optional.empty()
        return Optional.of(Pair(bottomPosition, grid[bottomPosition]))
    }

    fun adjacent(pos: Position, includeEmpty: Boolean = false): Set<Pair<Position, Type>> {
        require(pos.x >= 0 && pos.y >= 0 && pos.x < boardSize && pos.y < boardSize)
        val adjacentSet = mutableSetOf<Pair<Position, Type>>()
        rightAdjacent(pos, includeEmpty).ifPresent { adjacentSet.add(it) }
        leftAdjacent(pos, includeEmpty).ifPresent { adjacentSet.add(it) }
        bottomAdjacent(pos, includeEmpty).ifPresent { adjacentSet.add(it) }
        topAdjacent(pos, includeEmpty).ifPresent { adjacentSet.add(it) }
        return adjacentSet
    }

    fun enemyCanMove(): Boolean {
        val enemyType = when (isWhiteTurn) {
            true -> Type.BLACK
            false -> Type.WHITE
        }
        return playerCanMove(enemyType)
    }

    fun iCanMove(): Boolean {
        val myType = when (isWhiteTurn) {
            false -> Type.BLACK
            true -> Type.WHITE
        }
        return playerCanMove(myType)
    }

    fun playerCanMove(type: Type): Boolean {

        var playerCanMove = false

        val (handCount, boardCount) = when (isWhiteTurn) {
            true -> Pair(whiteHandCount, whiteBoardCount())
            false -> Pair(blackHandCount, blackBoardCount())
        }
        // TODO("Metodo per sapere la fase")


        if (handCount > 0) {
            // fase 1
            playerCanMove = true
        } else if (handCount <= 0 && boardCount > 3) {
            // fase 2
            grid.forEachIndexed { xIndex, yIndex, value ->
                if (value == type &&
                        adjacent(Position(xIndex, yIndex), true).filter { it.second == Type.EMPTY }.isNotEmpty()) {
                    playerCanMove = true
                    return@forEachIndexed
                }
            }
        } else if (handCount <= 0 && boardCount <= 3) {
            // fase 3
            playerCanMove = true
        }

        return playerCanMove
    }

    fun <T> Array<Array<T>>.get(pos: Position): T = this[pos.x][pos.y]
    fun <T> Array<Array<T>>.put(pos: Position, value: T) {
        this[pos.x][pos.y] = value
    }

    /*
    override fun toString(): String {
        val sb = StringBuilder()
        grid.forEach {
            it.forEach {
                sb.append(
                        when (it) {
                            Type.INVALID -> "⋯⋯"
                            Type.EMPTY -> ""
                            Type.WHITE -> " \u25a1 "
                            Type.BLACK -> " ∎ "
                        } + "\t"
                )
            }
            sb.append(System.lineSeparator())
        }
        sb.append(System.lineSeparator())
        return sb.toString()
    }*/

    operator fun <T> Matrix<T>.get(position: Position) = this[position.x, position.y]

    operator fun <T> Matrix<T>.set(position: Position, value: T) {
        this[position.x, position.y] = value
    }

    private fun dumpAndThrow(oldState: State, action: Action, newState: Optional<State>, ex: Throwable): Nothing {
        val sb = StringBuilder()
        sb.append(System.lineSeparator() + " !! DUMP !! " + System.lineSeparator())
        sb.append("    OLD STATE" + System.lineSeparator())
        sb.append(oldState.toString())
        sb.append(System.lineSeparator() + "Action : ${action}" + System.lineSeparator())
        if (newState.isPresent) {
            sb.append("    NEW STATE " + System.lineSeparator())
            sb.append(newState.get().toString())
        }
        println(sb.toString())
        throw ex
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(System.lineSeparator())
        sb.append("White Turn : $isWhiteTurn" + System.lineSeparator())
        sb.append(" 0\t1\t2\t3\t4\t5\t6" + System.lineSeparator())
        for (cIndex in boardSize - 1 downTo 0) {
            sb.append("$cIndex ")
            for (rIndex in 0 until boardSize) {
                sb.append(when (grid[rIndex, cIndex]) {
                    Type.WHITE -> "W"
                    Type.BLACK -> "B"
                    Type.EMPTY -> "O"
                    else -> " "
                } + "\t")
            }
            sb.append(System.lineSeparator())
        }
        sb.append(" 0\t1\t2\t3\t4\t5\t6" + System.lineSeparator())
        sb.append(System.lineSeparator())
        return sb.toString()
    }

    fun gamePhase(): GamePhase = when {
        this.whiteHandCount > 0 -> GamePhase.PHASE1
        this.whiteHandCount == 0 && this.whiteBoardCount() > 3 -> GamePhase.PHASE2
        this.whiteHandCount == 0 && this.whiteBoardCount() <= 3 -> GamePhase.PHASE3
        else -> throw IllegalStateException("Fase non riconosciuta ${this.whiteHandCount}, ${this.whiteBoardCount()}, ${this.blackHandCount},${this.blackBoardCount()}")
    }

    enum class Type {
        WHITE,
        BLACK,
        EMPTY,
        INVALID
    }

    enum class GamePhase {
        PHASE1,
        PHASE2,
        PHASE3
    }

    companion object {
        private val lBlocks = setOf(
                // Livello esterno
                arrayOf(Position(0, 3), Position(0, 6), Position(3, 6)), // angolo alto sx
                arrayOf(Position(0, 6), Position(3, 6), Position(3, 5)), // superiore centrale sx
                arrayOf(Position(6, 6), Position(3, 6), Position(3, 5)), // superiore centrale dx
                arrayOf(Position(3, 6), Position(6, 6), Position(6, 3)), // angolo alto dx
                arrayOf(Position(6, 6), Position(6, 3), Position(5, 3)), // destro sopra
                arrayOf(Position(6, 0), Position(6, 3), Position(5, 3)), // destro sotto
                arrayOf(Position(3, 3), Position(6, 0), Position(6, 3)), // angolo basso dx
                arrayOf(Position(6, 0), Position(3, 0), Position(3, 1)), // sotto centrale dx
                arrayOf(Position(0, 0), Position(3, 0), Position(3, 1)), // sotto centrale sx
                arrayOf(Position(0, 0), Position(0, 3), Position(3, 0)), // angolo basso sx
                arrayOf(Position(0, 0), Position(0, 3), Position(1, 3)), // sinitro sopra
                arrayOf(Position(0, 6), Position(0, 3), Position(1, 3)), // sinitro sotto

                // Livello intermedio
                arrayOf(Position(1, 3), Position(1, 5), Position(3, 5)), // angolo alto sx
                arrayOf(Position(1, 5), Position(3, 5), Position(3, 6)), // superiore centrale sx sopra
                arrayOf(Position(1, 5), Position(3, 5), Position(3, 4)), // superiore centrale sx sotto
                arrayOf(Position(3, 5), Position(3, 6), Position(5, 5)), // superiore centrale dx sopra
                arrayOf(Position(3, 5), Position(3, 4), Position(5, 5)), // superiore centrale dx sotto
                arrayOf(Position(3, 5), Position(5, 5), Position(5, 3)), // angolo alto dx
                arrayOf(Position(5, 5), Position(5, 3), Position(4, 3)), // destro sopra sx
                arrayOf(Position(5, 5), Position(5, 3), Position(6, 3)), // destro sopra dx
                arrayOf(Position(5, 1), Position(5, 3), Position(4, 3)), // destro sotto sx
                arrayOf(Position(5, 1), Position(5, 3), Position(6, 3)), // destro sotto dx
                arrayOf(Position(5, 3), Position(5, 1), Position(3, 1)), // angolo basso dx
                arrayOf(Position(5, 1), Position(3, 1), Position(3, 2)), // sotto centrale dx sopra
                arrayOf(Position(5, 1), Position(3, 1), Position(3, 0)), // sotto centrale dx sotto
                arrayOf(Position(1, 1), Position(3, 1), Position(3, 2)), // sotto centrale sx sopra
                arrayOf(Position(1, 1), Position(3, 1), Position(3, 0)), // sotto centrale sx sotto
                arrayOf(Position(3, 1), Position(1, 1), Position(1, 3)), // angolo basso sx
                arrayOf(Position(1, 3), Position(1, 5), Position(0, 3)), // sinitro sopra sx
                arrayOf(Position(1, 3), Position(1, 5), Position(2, 3)), // sinitro sopra dx
                arrayOf(Position(1, 3), Position(1, 1), Position(0, 3)), // sinitro sotto sx
                arrayOf(Position(1, 3), Position(1, 1), Position(2, 3)), // sinitro sotto dx

                // Livello interno
                arrayOf(Position(1, 3), Position(2, 3), Position(2, 4)),
                arrayOf(Position(1, 3), Position(2, 3), Position(2, 2)),
                arrayOf(Position(2, 3), Position(2, 4), Position(3, 4)),
                arrayOf(Position(2, 3), Position(2, 2), Position(3, 2)),
                arrayOf(Position(2, 2), Position(3, 2), Position(3, 1)),
                arrayOf(Position(3, 1), Position(3, 2), Position(4, 2)),
                arrayOf(Position(2, 4), Position(3, 4), Position(3, 5)),
                arrayOf(Position(3, 5), Position(3, 4), Position(4, 4)),
                arrayOf(Position(5, 3), Position(4, 3), Position(4, 4)),
                arrayOf(Position(5, 3), Position(4, 3), Position(4, 2)),
                arrayOf(Position(3, 2), Position(4, 2), Position(4, 3)),
                arrayOf(Position(4, 3), Position(4, 4), Position(3, 4))
        )

    }
}

