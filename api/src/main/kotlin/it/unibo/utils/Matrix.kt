package it.unibo.utils

open class Matrix<T> private constructor(val rows: Int,
                                         val columns: Int,
                                         private val matrix: MutableList<T>) {

    companion object {
        fun <T> buildMatrix(rows: Int, columns: Int, init: (Int, Int) -> T) = Matrix(rows, columns, MutableList(rows * columns, { index ->
            val row = index / columns
            val column = index % columns
            init(row, column)
        }))
    }

    /*
    private val matrix = MutableList(rows * columns, { index ->
        val row = index / columns
        val column = index % columns
        init(row, column)
    })*/

    /*
    /*private constructor(rows: Int, columns: Int, list : MutableList<T>) : this(rows, columns){

    }*/

    constructor(rows: Int,
                        columns: Int) : this(rows, columns, null){

    }

    constructor(rows: Int,
                columns: Int,
                init: (Int, Int) -> T) : this(rows, columns) /*: Iterable<T>*/ {

    }*/
    //private var matrix: MutableList<ArrayList<T>>

/*init {
    varowsArray = mutableListOf<ArrayList<T>>()
    for (i in 0 until rows) {
        val columnElem = arrayListOf<T>()
        for (y in 0 until columns)
            columnElem.add(init(i, y))
        rowsArray.add(columnElem)
    }
    matrix = rowsArray
}*/

    fun count(predicate: (T) -> Boolean): Int = matrix.count(predicate)

//operator fun get(i: Int) = get(i, Orientation.HORIZONTAL)

    operator fun get(i: Int, orientation: Orientation): MutableList<T> =
            when (orientation) {
                Orientation.HORIZONTAL -> {
                    MutableList(columns, { this[i, it] })
                }
                Orientation.VERTICAL -> {
                    MutableList(rows, { this[it, i] })
                }
            }

    operator fun get(i: Int, j: Int) = matrix[i * columns + j]

    operator fun set(i: Int, j: Int, value: T) = matrix.set(i * columns + j, value)

    fun iterator(): Iterator<T> = object : Iterator<T> {

        private var currentRow = 0
        private var currentColumns = 0

        override fun hasNext(): Boolean = currentRow < rows && currentColumns < columns

        override fun next(): T {
            val elem = this@Matrix[currentRow, currentColumns]
            currentColumns++
            if (currentColumns >= columns) {
                currentColumns = 0
                currentRow++
            }
            return elem
        }
    }

    enum class Orientation {
        VERTICAL, // column
        HORIZONTAL // row
    }

    fun forEachIndexed(action: (Int, Int, T) -> Unit) {
        matrix.forEachIndexed { index, t ->
            val row = index / columns
            val column = index % columns
            action(row, column, t)
        }
    }

    fun forEach(action: (T) -> Unit) = matrix.forEach(action)

    private fun linearize(x: Int, y: Int): Int = x * columns + y

    fun deepCopy(): Matrix<T> = Matrix(rows, columns, MutableList(matrix.size, { matrix[it] }))
}


//data class Matrix<T>(val size: Int, private val init: (Int, Int) -> T) : Matrix<T>(size, size, init)


fun <T> Matrix<T>.filterCell(predicate: (T) -> Boolean): List<T> {
    val filtered = mutableListOf<T>()
    this.forEach { if (predicate(it)) filtered.add(it) }
    return filtered.toList()
}


fun <T> Matrix<T>.filterCellIndexed(predicate: (T) -> Boolean): List<Pair<Pair<Int, Int>, T>> {
    val filtered = mutableListOf<Pair<Pair<Int, Int>, T>>()
    this.forEachIndexed { xIndex, yIndex, value ->
        if (predicate(value))
            filtered.add(Pair(Pair(xIndex, yIndex), value))
    }

    return filtered.toList()
}

// TODO("Da testare, possibile bug sull'iterator")
fun <T> Matrix<T>.forEachCell(action: (T) -> Unit) = this.iterator().forEach { action(it) }

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
