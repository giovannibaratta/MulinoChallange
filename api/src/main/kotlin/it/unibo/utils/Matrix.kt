package it.unibo.utils

open class Matrix<T>(val rows: Int,
                     val columns: Int,
                     private val init: (Int, Int) -> T) : Iterable<T> {

    private lateinit var matrix: Array<Array<T>>

    init {
        val rowsArray = mutableListOf<ArrayList<T>>()
        for (i in 0 until rows) {
            val columnElem = arrayListOf<T>()
            for (y in 0 until columns)
                columnElem.add(init(i, y))
            rowsArray.add(columnElem)
        }
    }

    fun count(predicate: (T) -> Boolean): Int = matrix.sumBy { it.count(predicate) }

    operator fun get(i: Int) = matrix[i].toList()

    operator fun get(i: Int, j: Int) = matrix[i][j]

    operator fun set(i: Int, j: Int, value: T) = matrix[i].set(j, value)

    override fun iterator(): Iterator<T> = object : Iterator<T> {

        var currentRow = 0
        var currentColumns = 0

        override fun hasNext(): Boolean = currentRow < rows && currentColumns < columns

        override fun next(): T {
            val elem = matrix[currentRow][currentColumns]
            currentColumns++
            if (currentColumns >= columns) {
                currentColumns = 0
                currentRow++
            }
            return elem
        }
    }
}

data class SquareMatrix<T>(val size: Int, private val init: (Int, Int) -> T) : Matrix<T>(size, size, init)


fun <T> Matrix<T>.filterCell(predicate: (T) -> Boolean): List<T> {
    val filtered = mutableListOf<T>()
    this.forEach { if (predicate(it)) filtered.add(it) }
    return filtered.toList()
}

/*
fun <T> Matrix<T>.filterCellIndexed ( predicate : (T) -> Boolean) : List<Pair<Pair<Int,Int>,T>>{
    val filtered = mutableListOf<Pair<Pair<Int,Int>,T>>()
    this.for

    this.forEachIndexed { xIndex, value ->
        value.forEachIndexed { yIndex, innerValue ->
            if(predicate(innerValue))
                filtered.add(Pair(Pair(xIndex,yIndex),innerValue))
        }
    }
    return filtered.toList()
}

fun <T> Array<Array<T>>.foreachCell ( action : (T) -> Unit)
        = this.forEach { it.forEach { action(it) } }
*/

fun <T> Array<Array<T>>.filterCell(predicate: (T) -> Boolean): List<T> {
    val filtered = mutableListOf<T>()
    this.forEach { it.forEach { if (predicate(it)) filtered.add(it) } }
    return filtered.toList()
}

fun <T> Array<Array<T>>.filterCellIndexed(predicate: (T) -> Boolean): List<Pair<Pair<Int, Int>, T>> {
    val filtered = mutableListOf<Pair<Pair<Int, Int>, T>>()
    this.forEachIndexed { xIndex, value ->
        value.forEachIndexed { yIndex, innerValue ->
            if (predicate(innerValue))
                filtered.add(Pair(Pair(xIndex, yIndex), innerValue))
        }
    }
    return filtered.toList()
}

fun <T> Array<Array<T>>.foreachCell(action: (T) -> Unit) = this.forEach { it.forEach { action(it) } }
