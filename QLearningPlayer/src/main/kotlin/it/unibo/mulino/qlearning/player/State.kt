package it.unibo.mulino.qlearning.player

import java.util.*
import java.util.regex.Pattern
import it.unibo.ai.didattica.mulino.domain.State as ExternalState

internal class State(state: ExternalState) {

    data class Position(val x: Int, val y: Int)

    val grid: Array<Array<Type>>
    val boardSize = 7

    var whiteTurn = true
        private set

    private val middleLine = (boardSize - 1) / 2
    private val midllePoint = Position(middleLine, middleLine)

    // undo var
    private var previousFrom: Pair<Position, Type>? = null
    private var previousTo: Pair<Position, Type>? = null
    private var previousRemove: Pair<Position, Type>? = null

    fun blackCount() = grid.sumBy { it.count { it == Type.BLACK } }
    fun whiteCount() = grid.sumBy { it.count { it == Type.WHITE } }

    init {
        grid = Array(boardSize, { Array(boardSize, { Type.INVALID }) })

        // Inserisco le caselle vuote valide
        grid.forEachIndexed { xIndex, columns ->
            columns.forEachIndexed { yIndex, _ ->
                if ((xIndex == middleLine && yIndex != middleLine) ||
                        (xIndex != middleLine && yIndex == middleLine) ||
                        (xIndex == yIndex && xIndex != middleLine) ||
                        (xIndex + yIndex == boardSize - 1 && xIndex != middleLine))
                    grid[xIndex][yIndex] = Type.EMPTY
            }
        }

        // mapping della rappresentazione esterna in rappresentazioen interna
        state.board.forEach {

            val xAxis = it.key.split(Pattern.compile("[0-9]+$"))
            val yAxis = it.key.split(Pattern.compile("^[a-zA-Z]+"))
            if (xAxis.size != 2 && yAxis.size != 2)
                throw IllegalArgumentException("Posizione dello stato non valido")

            // TODO("Aggiungere compatibilità con estensione della board")
            val parsedPosition = Position(xAxis[0].toLowerCase().get(0) - 'a', yAxis[1].toInt() - 1)
            if (parsedPosition.x < 0 || parsedPosition.x >= boardSize
                    || parsedPosition.y < 0 || parsedPosition.y >= boardSize || grid.get(parsedPosition) == Type.INVALID)
                throw IllegalArgumentException("Stai tentando di mettere la pedina dove non dovresti")
            grid[parsedPosition.x][parsedPosition.y] = when (it.value) {
                ExternalState.Checker.EMPTY -> Type.EMPTY
                ExternalState.Checker.WHITE -> Type.WHITE
                ExternalState.Checker.BLACK -> Type.BLACK
                else -> throw IllegalStateException("Il valore ricevuto non è mappabile")
            }
        }
    }

    fun closeMill(position: Position): Boolean {
        require(position.x >= 0 && position.x < boardSize, { "Coordinata X non valida" })
        require(position.y >= 0 && position.y < boardSize, { "Coordinata Y non valida" })
        require(grid.get(position) != Type.INVALID, { "Casella non valida" })
        require(grid.get(position) == Type.EMPTY, { "Casella non vuota" })

        val type = when (whiteTurn) {
            true -> Type.WHITE
            false -> Type.BLACK
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

    // restituisce la griglia, e se ha chiuso un it.unibo.ai.didattica.mulino. Se azione non valida errore
    fun simulateAction(from: Optional<Position>, to: Optional<Position>, remove: Optional<Position>) {
        var closedMill = false
        val prevPrevFrom = previousFrom
        val prevPrevTo = previousTo
        val fromType: Type = when (from.isPresent) {
            true -> grid.get(from.get())
            false -> Type.INVALID
        }

        if (from.isPresent &&
                ((fromType != Type.WHITE && whiteTurn) || (fromType == Type.BLACK && !whiteTurn) || !to.isPresent))
            throw IllegalStateException("La mossa from non è valida")

        if (to.isPresent && grid.get(to.get()) != Type.EMPTY)
            throw IllegalStateException("La mossa to non è valida")

        val removeType: Type = when (remove.isPresent) {
            true -> grid.get(remove.get())
            false -> Type.INVALID
        }

        if (remove.isPresent &&
                ((removeType == Type.WHITE && whiteTurn) || (removeType == Type.BLACK && !whiteTurn)))
            throw IllegalStateException("La mossa remove non è valida")
        whiteTurn = !whiteTurn

        from.ifPresent {
            grid.put(it, Type.EMPTY)
            previousFrom = Pair(it, when (whiteTurn) {
                true -> Type.WHITE
                false -> Type.BLACK
            })
        }
        to.ifPresent {
            closedMill = closeMill(to.get())
            previousTo = Pair(it, Type.EMPTY)
            grid.put(to.get(), when (whiteTurn) {
                true -> Type.WHITE
                false -> Type.BLACK
            })
        }

        if ((!closedMill && remove.isPresent) || (closedMill && when (whiteTurn) {
                    true -> blackCount()
                    false -> whiteCount()
                } > 0 && !remove.isPresent)
        ) {
            // reverse
            from.ifPresent {
                grid.put(it, when (whiteTurn) {
                    true -> Type.WHITE
                    else -> Type.BLACK
                })
            }
            to.ifPresent { grid.put(it, Type.EMPTY) }
            previousFrom = prevPrevFrom
            previousTo = prevPrevTo
            throw IllegalArgumentException("La mossa non è valida")
        }

        remove.ifPresent {
            grid.put(remove.get(), Type.EMPTY)
            previousRemove = Pair(it, when (whiteTurn) {
                true -> Type.BLACK
                false -> Type.WHITE
            })
        }

        whiteTurn = !whiteTurn
    }

    private fun rightAdjacent(pos: Position, includeEmpty: Boolean = false): Optional<Pair<Position, Type>> {
        val offset = when (pos.y == middleLine) {
            true -> 1
            else -> Math.abs(pos.y - middleLine)
        }
        val rightPosition = Position(pos.x + offset, pos.y)
        if (rightPosition.x >= boardSize || (rightPosition.y != middleLine && rightPosition.x >= (boardSize - middleLine + offset)) || rightPosition == midllePoint)
            return Optional.empty()
        assert(grid[rightPosition.x][rightPosition.y] != Type.INVALID)
        if (!includeEmpty && grid[rightPosition.x][rightPosition.y] == Type.EMPTY)
            return Optional.empty()
        return Optional.of(Pair(rightPosition, grid[rightPosition.x][rightPosition.y]))
    }

    private fun leftAdjacent(pos: Position, includeEmpty: Boolean = false): Optional<Pair<Position, Type>> {
        val offset = when (pos.y == middleLine) {
            true -> 1
            else -> Math.abs(pos.y - middleLine)
        }
        val leftPosition = Position(pos.x - offset, pos.y)
        if (leftPosition.x < 0 || (leftPosition.y != middleLine && leftPosition.x < (middleLine - offset)) || leftPosition == midllePoint)
            return Optional.empty()
        assert(grid[leftPosition.x][leftPosition.y] != Type.INVALID)
        if (!includeEmpty && grid[leftPosition.x][leftPosition.y] == Type.EMPTY)
            return Optional.empty()
        return Optional.of(Pair(leftPosition, grid[leftPosition.x][leftPosition.y]))
    }

    private fun topAdjacent(pos: Position, includeEmpty: Boolean = false): Optional<Pair<Position, Type>> {
        val offset = when (pos.x == middleLine) {
            true -> 1
            else -> Math.abs(pos.x - middleLine)
        }
        val topPosition = Position(pos.x, pos.y + offset)
        if (topPosition.y >= boardSize || (topPosition.x != middleLine && topPosition.y >= (boardSize - middleLine + offset)) || topPosition == midllePoint)
            return Optional.empty()
        assert(grid[topPosition.x][topPosition.y] != Type.INVALID)
        if (!includeEmpty && grid[topPosition.x][topPosition.y] == Type.EMPTY)
            return Optional.empty()
        return Optional.of(Pair(topPosition, grid[topPosition.x][topPosition.y]))
    }

    private fun bottomAdjacent(pos: Position, includeEmpty: Boolean = false): Optional<Pair<Position, Type>> {
        val offset = when (pos.x == middleLine) {
            true -> 1
            else -> Math.abs(pos.x - middleLine)
        }
        val bottomPosition = Position(pos.x, pos.y - offset)
        if (bottomPosition.y < 0 || (bottomPosition.x != middleLine && bottomPosition.y < (middleLine - offset)) || bottomPosition == midllePoint)
            return Optional.empty()
        assert(grid[bottomPosition.x][bottomPosition.y] != Type.INVALID)
        if (!includeEmpty && grid[bottomPosition.x][bottomPosition.y] == Type.EMPTY)
            return Optional.empty()
        return Optional.of(Pair(bottomPosition, grid[bottomPosition.x][bottomPosition.y]))
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

    fun undo() {
        if (previousRemove != null)
            grid[previousRemove!!.first.x][previousRemove!!.first.y] = previousRemove!!.second

        if (previousTo != null)
            grid[previousTo!!.first.x][previousTo!!.first.y] = previousTo!!.second

        if (previousFrom != null)
            grid[previousFrom!!.first.x][previousFrom!!.first.y] = previousFrom!!.second

        previousRemove = null
        previousFrom = null
        previousTo = null
        whiteTurn = !whiteTurn
    }

    fun enemyCanMove(): Boolean {
        val enemyType = when (whiteTurn) {
            true -> Type.BLACK
            false -> Type.WHITE
        }
        grid.forEachIndexed { xIndex, row ->
            row.forEachIndexed { yIndex, value ->
                if (value == enemyType &&
                        adjacent(Position(xIndex, yIndex), true).filter { it.second == Type.EMPTY }.isNotEmpty())
                    return true
            }
        }
        return false
    }

    fun <T> Array<Array<T>>.get(pos: Position): T = this[pos.x][pos.y]
    fun <T> Array<Array<T>>.put(pos: Position, value: T) {
        this[pos.x][pos.y] = value
    }

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
    }

    enum class Type {
        WHITE,
        BLACK,
        EMPTY,
        INVALID
    }
}
