package it.unibo.mulino.qlearning.player.model

import it.unibo.Matrix
import it.unibo.SquareMatrix
import it.unibo.filterCellIndexed
import java.util.*
import java.util.regex.Pattern
import it.unibo.ai.didattica.mulino.domain.State as ExternalState

internal class State(externalGrid: SquareMatrix<Type>? = null,
                     val isWhiteTurn: Boolean,
                     val whiteHandCount: Int,
                     val blackHandCount: Int) {

    var grid: SquareMatrix<Type>
    val boardSize: Int
    val middleLine: Int
    val midllePoint: Position

    init {
        if (externalGrid == null) {
            grid = SquareMatrix<Type>(7, { _, _ -> Type.INVALID })
            boardSize = grid.size
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
            boardSize = grid.size
            middleLine = (boardSize - 1) / 2
            midllePoint = Position(middleLine, middleLine)
        }
    }

    val blackBoardCount = grid.count { it == Type.BLACK }
    val whiteBoardCount = grid.count { it == Type.WHITE }

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

    private fun millTest(position: Position, typeToMill: Type? = null): Boolean {
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
        // top mill
        topAdjacent(position).ifPresent {
            if (it.second == type) {
                topAdjacent(it.first).ifPresent {
                    mill = it.second == type
                    return@ifPresent
                }
                bottomAdjacent(position).ifPresent {
                    mill = it.second == type
                }
            }
        }
        if (mill)
            return true
        // right mill
        rightAdjacent(position).ifPresent {
            if (it.second == type) {
                rightAdjacent(it.first).ifPresent {
                    mill = it.second == type
                    return@ifPresent
                }
                leftAdjacent(position).ifPresent {
                    mill = it.second == type
                }
            }
        }
        if (mill)
            return true
        // left mill
        leftAdjacent(position).ifPresent outer@{
            if (it.second == type)
                leftAdjacent(it.first).ifPresent {
                    mill = it.second == type
                }
        }
        if (mill)
            return true
        // bottom mill
        bottomAdjacent(position).ifPresent outer@{
            if (it.second == type)
                bottomAdjacent(it.first).ifPresent {
                    mill = it.second == type
                }
        }
        return mill
    }

    fun isAClosedMill(position: Position, typeToMill: Type? = null): Boolean {
        require(typeToMill != Type.INVALID)
        val type = when (typeToMill != null) {
            true -> typeToMill
            false -> when (isWhiteTurn) {
                true -> Type.WHITE
                false -> Type.BLACK
            }
        }
        if (grid.get(position) == Type.EMPTY)
            return false
        if (grid.get(position) != type)
            throw IllegalArgumentException("Casella dell'avversario")
        return millTest(position, type)
    }

    /**
     * True se la pedina nella posizione indicata forma un mulino, altrimenti false
     */
    fun closeAMill(position: Position, typeToMill: Type? = null): Boolean {
        require(grid.get(position) == Type.EMPTY, { "Casella non vuota" })
        return millTest(position, typeToMill)
    }


    // restituisce la griglia, e se ha chiuso un it.unibo.ai.didattica.mulino. Se azione non valida errore
    fun simulateAction(action: Action): ActionResult {
        val newStateGrid = SquareMatrix(boardSize, { xIndex, yIndex -> grid[xIndex, yIndex] })
        val newState: State
        if (!action.from.isPresent) {
            // fase 1
            if (isWhiteTurn) {
                // diminuisco i bianchi
                newState = State(newStateGrid, !isWhiteTurn, whiteHandCount - 1, blackHandCount)
            } else {
                //diminuisco i neri
                newState = State(newStateGrid, !isWhiteTurn, whiteHandCount, blackHandCount - 1)
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
            closedMill = newState.isAClosedMill(action.to.get(), typeToInsert)
        }

        // ho fatto un mill ma non ho specificato la remove oppure non ho fatto un mill e ho specificato la remove
        if ((!closedMill && action.remove.isPresent) || (closedMill && when (isWhiteTurn) {
                    true -> grid.filterCellIndexed { it == Type.BLACK }.filter { !isAClosedMill(Position(it.first.first, it.first.second), it.second) }.any()
                    false -> grid.filterCellIndexed { it == Type.WHITE }.filter { !isAClosedMill(Position(it.first.first, it.first.second), it.second) }.any()
                } && !action.remove.isPresent))
            dumpAndThrow(this, action, Optional.of(newState), IllegalStateException("La mossa non è valida. Closed Mill ${closedMill}"))

        // ho fatto mill e devo rimuovere la pedina
        action.remove.ifPresent { newStateGrid[action.remove.get()] = Type.EMPTY }

        return ActionResult(newState, closedMill)
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
            true -> Pair(whiteHandCount, whiteBoardCount)
            false -> Pair(blackHandCount, blackBoardCount)
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

    operator fun <T> Matrix<T>.get(position: Position) = this[position.x][position.y]

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

    enum class Type {
        WHITE,
        BLACK,
        EMPTY,
        INVALID
    }
}
