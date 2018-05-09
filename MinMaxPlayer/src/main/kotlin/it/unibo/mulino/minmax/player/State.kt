package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.domain.State.Checker
import java.util.*

data class State(var checker: Checker,
                 var board : Array<CharArray> = Array(8, { CharArray(3, { 'e' }) }),
                 var checkers : IntArray = intArrayOf(9,9),
                 var checkersOnBoard : IntArray = intArrayOf(0,0),
                 var currentPhase : Int = 1,
                 var closedMorris : Boolean = false) {

    //var board = Array(8, { CharArray(3, { 'e' }) })
    //var checkers = intArrayOf(9,9)
    //var currentPhase : Char = '1'
    //var closedMorris = false

    /*
    companion object {
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
    }

    fun getPiece(position: String): Checker {
        val (vertex, level) = internalPositions.getValue(position)
        val charChecker = board[vertex][level]
        when (charChecker) {
            'w' -> return Checker.WHITE
            'b' -> return Checker.BLACK
        }
        return Checker.EMPTY
    }

    fun addPiece(position: String, checker: Checker) {
        val (vertex, level) = internalPositions.getValue(position)
        board[vertex][level] = checkersToShort.getValue(checker)
    }

    fun removePiece(position: String) {
        val (vertex, level) = internalPositions.getValue(position)
        board[vertex][level] = 'e'
    }

    fun getNumPieces(player: Checker): Int {
        var count = 0
        val intChecker = checkersToShort.getValue(player)
        for (diagonal in board) {
            for (position in diagonal) {
                if (position == intChecker) count++
            }
        }
        return count
    }

    fun getPositions(checker: Checker): List<String> {
        val positions = LinkedList<String>()
        for (position in internalPositions.keys) {
            val (vertex, level) = internalPositions.getValue(position)
            if (board[vertex][level] == checkersToShort.getValue(checker))
                positions.add(position)
        }
        return positions
    }

    fun getEmptyPositions(): List<String> {
        val positions = LinkedList<String>()
        for (position in internalPositions.keys) {
            val (vertex, level) = internalPositions.getValue(position)
            if (board[vertex][level] == 'e')
                positions.add(position)
        }
        return positions
    }

    fun getAdiacentPositions(position: String): List<String> {
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

    fun checkMorris(position: String, checker: Checker): Boolean {
        val (vertex, level) = internalPositions.getValue(position)
        var check = false
        when (vertex) {
            1, 3, 5, 7 -> check = ((board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                    (board[nextVertex(vertex)][level] == checkersToShort.getValue(checker))) ||
                    ((board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)) &&
                            (board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
            0, 2, 4, 6 -> check = ((board[nextVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                    (board[nextVertex(nextVertex(vertex))][level] == checkersToShort.getValue(checker))) ||
                    ((board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                            (board[precVertex(precVertex(vertex))][level] == checkersToShort.getValue(checker)))
        }
        return check
    }

    fun checkMorris(oldPosition: String, newPosition: String, checker: Checker): Boolean {
        val (oldVertex, oldLevel) = internalPositions.getValue(oldPosition)
        val (newVertex, newLevel) = internalPositions.getValue(newPosition)
        var check = false
        if (getAdiacentPositions(newPosition).contains(oldPosition)) {
            when (newVertex) {
                1, 3, 5, 7 -> when (oldVertex) {
                    newVertex -> check = ((board[precVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                            (board[nextVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)))
                    else -> check = ((board[newVertex][nextLevel(newLevel)] == checkersToShort.getValue(checker)) &&
                            (board[newVertex][nextLevel(nextLevel(newLevel))] == checkersToShort.getValue(checker)))
                }
                0, 2, 4, 6 -> when (oldVertex) {
                    nextVertex(newVertex) -> check = ((board[precVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                            (board[precVertex(precVertex(newVertex))][newLevel] == checkersToShort.getValue(checker)))
                    else -> check = ((board[nextVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                            (board[nextVertex(nextVertex(nextVertex(newVertex)))][newLevel] == checkersToShort.getValue(checker)))
                }
            }
        } else {
            when (newVertex) {
                1, 3, 5, 7 -> check = ((board[precVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                        (board[nextVertex(newVertex)][newLevel] == checkersToShort.getValue(checker))) ||
                        ((board[newVertex][nextLevel(newLevel)] == checkersToShort.getValue(checker)) &&
                                (board[newVertex][nextLevel(nextLevel(newLevel))] == checkersToShort.getValue(checker)) &&
                                (internalPositions.getValue(oldPosition) != Pair(newVertex, nextLevel(newLevel)) &&
                                        (internalPositions.getValue(oldPosition) != Pair(newVertex, nextLevel(nextLevel(newLevel))))))
                0, 2, 4, 6 -> check = ((board[nextVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                        (board[nextVertex(nextVertex(nextVertex(newVertex)))][newLevel] == checkersToShort.getValue(checker)) &&
                        (internalPositions.getValue(oldPosition) != Pair(nextVertex(nextVertex(newVertex)), newLevel))) ||
                        ((board[precVertex(newVertex)][newLevel] == checkersToShort.getValue(checker)) &&
                                (board[precVertex(precVertex(newVertex))][newLevel] == checkersToShort.getValue(checker)) &&
                                (internalPositions.getValue(oldPosition) != Pair(precVertex(precVertex(newVertex)), newLevel)))
            }
        }
        return check
    }

    fun checkNoMoves(checker: Checker): Boolean {
        var check = true
        for (position in getPositions(checker)) {
            check = check && checkNoMoves(position, checker)
        }
        //if(check)
        //println("No moves possible for $checker!")
        return check
    }

    fun checkNoMoves(position: String, checker: Checker): Boolean {
        var check = false
        var opposite = Checker.EMPTY
        if (checker == Checker.WHITE) {
            opposite = Checker.BLACK
        } else
            opposite = Checker.WHITE
        val (vertex, level) = internalPositions.getValue(position)
        check = ((board[nextVertex(vertex)][level] == 'w' || (board[nextVertex(vertex)][level] == 'b')) &&
                ((board[precVertex(vertex)][level] == 'w') || (board[precVertex(vertex)][level] == 'b')))
        when (vertex) {
            1, 3, 5, 7 -> for (adiacentLevel in adiacentLevels(level)) {
                check = check && ((board[vertex][adiacentLevel] == 'w') ||
                        (board[vertex][adiacentLevel] == 'b'))
            }
        }
        return check
    }

    fun getBlockedPieces(checker: Checker): Int {
        var count = 0
        for (adversarialPosition in getPositions(checker))
            if (checkNoMoves(adversarialPosition, checker))
                count++
        return count
    }

    fun getNumMorrises(checker: Checker): Int {
        var count = 0
        for (position in getPositions(checker)) {
            val (vertex, level) = internalPositions.getValue(position)
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (board[nextVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) {
                        count++
                    }
                    when (level) {
                        1 -> if ((board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)) &&
                                (board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker))) {
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

    fun hasOpenedMorris(checker: Checker): Boolean {
        for (position in getPositions(checker)) {
            for (adiacentPosition in getAdiacentPositions(position)) {
                val (vertex, level) = internalPositions.getValue(adiacentPosition)
                if (board[vertex][level] != 'e' && checkMorris(position, adiacentPosition, checker))
                    return true
            }
        }
        return false
    }

    fun hasDoubleMorris(checker: Checker): Boolean {
        for (position in getPositions(checker)) {
            val (vertex, level) = internalPositions.getValue(position)
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (board[nextVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            board[nextVertex(nextVertex(vertex))][level] == checkersToShort.getValue(checker) &&
                            board[precVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            board[precVertex(precVertex(vertex))][level] == checkersToShort.getValue(checker))
                        return true

                }
                else -> {
                    if (board[nextVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            board[precVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            board[vertex][nextLevel(level)] == checkersToShort.getValue(checker) &&
                            board[vertex][nextLevel(level)] == checkersToShort.getValue(checker))
                        return true
                }
            }
        }
        return false
    }

    fun getNum2Conf(checker: Checker): Int {
        var count = 0
        for (position in getPositions(checker)) {
            val (vertex, level) = internalPositions.getValue(position)
            when (vertex) {
                1, 3, 5, 7 -> {
                    if (board[nextVertex(vertex)][level] == checkersToShort.getValue(checker))
                        count++
                    if (board[precVertex(vertex)][level] == checkersToShort.getValue(checker))
                        count++
                    when (level) {
                        1 -> {
                            if (board[vertex][nextLevel(level)] == checkersToShort.getValue(checker))
                                count++
                            if (board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker))
                                count++
                        }
                    }
                }
            }
        }
        return count
    }

    fun getNum3Conf(checker: Checker): Int {
        var count = 0
        for (position in getPositions(checker)) {
            val (vertex, level) = internalPositions.getValue(position)
            when (vertex) {
                0, 2, 4, 6 -> {
                    if (board[nextVertex(vertex)][level] == checkersToShort.getValue(checker) &&
                            board[precVertex(vertex)][level] == checkersToShort.getValue(checker))
                        count++

                }
                else -> {
                    when (level) {
                        1 -> {
                            if ((board[nextVertex(vertex)][nextLevel(level)] == checkersToShort.getValue(checker)) &&
                                    (board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)))
                                count++
                            if ((board[nextVertex(vertex)][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)) &&
                                    (board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
                                count++
                            if ((board[precVertex(vertex)][nextLevel(level)] == checkersToShort.getValue(checker)) &&
                                    (board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)))
                                count++
                            if ((board[precVertex(vertex)][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)) &&
                                    (board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
                                count++
                            if ((board[nextVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                                    (board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)))
                                count++
                            if ((board[nextVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                                    (board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
                                count++
                            if ((board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                                    (board[vertex][nextLevel(level)] == checkersToShort.getValue(checker)))
                                count++
                            if ((board[precVertex(vertex)][level] == checkersToShort.getValue(checker)) &&
                                    (board[vertex][nextLevel(nextLevel(level))] == checkersToShort.getValue(checker)))
                                count++
                        }
                    }
                }
            }
        }
        return count
    }

    fun isWinner(checker: Checker): Boolean {
        var opposite = Checker.WHITE
        if (checker==Checker.WHITE) {
            opposite = Checker.BLACK
        }
        var intOpposite = -1
        when(opposite){
            Checker.WHITE -> intOpposite = 0
            Checker.BLACK -> intOpposite = 1
        }
        when(currentPhase){
            '1'->{
                return ((checkers[intOpposite]==0) && (getNumPieces(opposite) < 3))
            }
            '2'->{
                return (getNumPieces(opposite) < 3) || (checkNoMoves(opposite))
            }
            '3'->{
                return (getNumPieces(opposite) < 3)
            }
        }
        return false
    }

    fun opposite(): Checker {
        when (checker) {
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
    */

}

fun main(args: Array<String>) {

    val state = State(Checker.WHITE)
    //println("Turno: ${state.checker}")

    /*state.addPiece(Pair('a',1),Checker.WHITE)
    state.addPiece(Pair('a',4),Checker.WHITE)
    state.addPiece(Pair('b',4),Checker.WHITE)
    state.addPiece(Pair('d',1),Checker.WHITE)
    state.addPiece(Pair('d',2),Checker.WHITE)
    state.addPiece(Pair('e',3),Checker.WHITE)
    state.addPiece(Pair('d',7),Checker.WHITE)
    //println("Numero di pedine bianche: ${state.getNumPieces(Checker.WHITE)}")
    //println("Pedine bianche(adiacenti): ")
    for(position in state.getPositions(Checker.WHITE)){
        print("${position.first}${position.second} (")
        for(adiacentPosition in state.getAdiacentPositions(position))
            print("${adiacentPosition.first}${adiacentPosition.second}, ")
    print(")\n")
}

    state.addPiece(Pair('g',4),Checker.BLACK)
    state.addPiece(Pair('b',2),Checker.BLACK)
    //println("Numero di pedine nere: ${state.getNumPieces(Checker.BLACK )}")
    //println("Pedine nere(adiacenti): ")
    for(position in state.getPositions(Checker.BLACK)){
        print("${position.first}${position.second} (")
        for(adiacentPosition in state.getAdiacentPositions(position))
            print("${adiacentPosition.first}${adiacentPosition.second}, ")
        print(")\n")
    }

    //println("Rimozione pedina nera g4...")
    state.removePiece(Pair('g',4))
    //println("Numero di pedine nere: ${state.getNumPieces(Checker.BLACK )}")
    //println("Pedine nere(adiacenti): ")
    for(position in state.getPositions(Checker.BLACK)){
        print("${position.first}${position.second} (")
        for(adiacentPosition in state.getAdiacentPositions(position))
            print("${adiacentPosition.first}${adiacentPosition.second}, ")
        print(")\n")
    }

    //println("Posizioni libere: ")
    for(emptyPosition in state.getEmptyPositions()){
        //println("${emptyPosition.first}${emptyPosition.second}")
    }

    //println("Giocatore bianco bloccato: ${state.checkNoMoves(Checker.WHITE)}")
    //println("Giocatore nero bloccato: ${state.checkNoMoves(Checker.BLACK)}")

    //println("Morris bianco con a7: ${state.checkMorris(Pair('a',7),Checker.WHITE)}")
    //println("Morris bianco con c4: ${state.checkMorris(Pair('c',4),Checker.WHITE)}")
    //println("Morris bianco con g1: ${state.checkMorris(Pair('g',1),Checker.WHITE)}")
    //println("Morris bianco con d3: ${state.checkMorris(Pair('d',3),Checker.WHITE)}")
    //println("Morris bianco con b6: ${state.checkMorris(Pair('b',6),Checker.WHITE)}")
    //println("Morris bianco con f2: ${state.checkMorris(Pair('f',2),Checker.WHITE)}")
    //println("Morris nero con b6: ${state.checkMorris(Pair('b',6),Checker.BLACK)}")

    //println("Morris bianco con d7->a7: ${state.checkMorris(Pair('d',7),Pair('a',7),Checker.WHITE)}")
    //println("Morris bianco con e3->d3: ${state.checkMorris(Pair('e',3),Pair('d',3),Checker.WHITE)}")
    //println("Morris bianco con d7->g7: ${state.checkMorris(Pair('d',7),Pair('g',7),Checker.WHITE)}")

    //println("Morris bianco con b4->a7: ${state.checkMorris(Pair('b',4),Pair('a',7),Checker.WHITE)}")
    */

    /*
    state.addPiece(Pair('a', 1), Checker.WHITE)
    state.addPiece(Pair('a', 4), Checker.WHITE)
    state.addPiece(Pair('a', 7), Checker.WHITE)
    state.addPiece(Pair('b', 4), Checker.WHITE)
    state.addPiece(Pair('g', 1), Checker.WHITE)
    state.addPiece(Pair('d', 1), Checker.WHITE)
    state.addPiece(Pair('b', 2), Checker.BLACK)
    state.addPiece(Pair('f', 2), Checker.BLACK)
    state.addPiece(Pair('f', 4), Checker.WHITE)
    state.addPiece(Pair('d', 2), Checker.WHITE)
    state.addPiece(Pair('d', 7), Checker.WHITE)
    state.addPiece(Pair('e', 3), Checker.WHITE)
    state.addPiece(Pair('g', 4), Checker.BLACK)
    */

    //println("Morris chiuso: ${state.hasClosedMorris(Checker.WHITE)}")
    //println("Morris aperto: ${state.hasOpenedMorris(Checker.WHITE)}")
    //println("Double Morris: ${state.hasDoubleMorris(Checker.WHITE)}")
    //println("Numero di pedine avversarie bloccate: ${state.getBlockedPieces(Checker.BLACK)}")
    //println("Numero di Morris: ${state.getNumMorrises(Checker.WHITE)}")
    //println("Numero di configurazioni da 2: ${state.getNum2Conf(Checker.WHITE)}")
    //println("Numero di configurazioni da 3: ${state.getNum3Conf(Checker.WHITE)}")
    //println("Configurazione vincente fase 2: ${state.isWinner(Checker.WHITE)}")
    //println("Configurazione vincente fase 1: ${state.isWinner(Checker.WHITE)}")
}