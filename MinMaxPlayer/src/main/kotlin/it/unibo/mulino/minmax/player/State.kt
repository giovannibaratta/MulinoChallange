package it.unibo.mulino.minmax.player

import java.util.*

// TODO("Override delle hashcode e equals della classe")
data class State(
        val playerType: Int,
        val board: IntArray = intArrayOf(0, 0),
        val checkers: IntArray = intArrayOf(9, 9),
        val checkersOnBoard: IntArray = intArrayOf(0, 0),
        val closedMorris: Boolean = false) {

    val currentPhase = when {
        checkers[playerType] > 0 -> 1
        checkers[playerType] == 0 && checkersOnBoard[playerType] > 3 -> 2
        checkers[playerType] == 0 && checkersOnBoard[playerType] <= 3 -> 3
        else -> throw IllegalStateException("Fase non valida")
    }

    override fun hashCode(): Int = Objects.hash(board[0], board[1], checkersOnBoard[0], checkersOnBoard[1], checkers[0], checkers[1], playerType, closedMorris)

    fun lognHashCode(): Long {
        if (closedMorris)
            return ((((((((1125899906842597L * 31) + board[0]) * 31 + board[1]) * 31 + checkers[0]) * 31 + checkers[1]) * 31 + playerType) * 31 + 1))
        return ((((((((1125899906842597L * 31) + board[0]) * 31 + board[1]) * 31 + checkers[0]) * 31 + checkers[1]) * 31 + playerType) * 31 + 0))
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("CurrentPhase : ${currentPhase}\tPlayerType : $playerType\n")
        sb.append("WhiteHand : ${checkers[0]}\tBlackHand : ${checkers[1]}\n")
        sb.append("WhiteBoard : ${checkersOnBoard[0]}\tBlackBoard : ${checkersOnBoard[1]}\n\n")
        sb.append("board value : ${board[0]} ${board[1]}")
        sb.append("${positionToExternal(board, 2, 0)}\t\t${positionToExternal(board, 3, 0)}\t\t${positionToExternal(board, 4, 0)}\n")
        sb.append("\t${positionToExternal(board, 2, 1)}\t${positionToExternal(board, 3, 1)}\t${positionToExternal(board, 4, 1)}\t\n")
        sb.append("\t\t${positionToExternal(board, 2, 2)}${positionToExternal(board, 3, 2)}${positionToExternal(board, 4, 2)}\t\t\n")
        sb.append("${positionToExternal(board, 1, 0)}${positionToExternal(board, 1, 1)}${positionToExternal(board, 1, 2)}\t${positionToExternal(board, 5, 2)}${positionToExternal(board, 5, 1)}${positionToExternal(board, 5, 0)}\n")
        sb.append("\t\t${positionToExternal(board, 0, 2)}${positionToExternal(board, 7, 2)}${positionToExternal(board, 6, 2)}\t\t\n")
        sb.append("\t${positionToExternal(board, 0, 1)}\t${positionToExternal(board, 7, 1)}\t${positionToExternal(board, 6, 1)}\t\n")
        sb.append("${positionToExternal(board, 0, 0)}\t\t${positionToExternal(board, 7, 0)}\t\t${positionToExternal(board, 6, 0)}\n")
        sb.append("\n\n")
        return sb.toString()
    }

    companion object {

        fun positionToExternal(board: IntArray, x: Int, y: Int): Char = when {
            isSet(board, x, y, 0) -> 'W'
            isSet(board, x, y, 1) -> 'B'
            isNotSet(board, x, y) -> 'O'
            else -> throw IllegalStateException("stato non valido")
        }

        fun isSet(board: IntArray, indexPosition: Int, player: Int): Boolean = board[player].and(position[indexPosition]) > 0
        fun isNotSet(board: IntArray, indexPosition: Int): Boolean = !State.isSet(board, indexPosition, 0) && !State.isSet(board, indexPosition, 1)

        // Controlla se nella board è presente la pedina del giocatore indicato, nella posizione indicata
        fun isSet(board: IntArray, x: Int, y: Int, player: Int): Boolean = board[player].and(position[x * 3 + y]) > 0

        // Controlla se nella board non è presente nessuna pedina (empty)
        fun isNotSet(board: IntArray, x: Int, y: Int): Boolean = !State.isSet(board, x, y, 0) && !State.isSet(board, x, y, 1)

        fun isSet(board: IntArray, posToCheck: Pair<Int, Int>, player: Int): Boolean = board[player].and(position[posToCheck.first * 3 + posToCheck.second]) > 0

        // ogni valore rappresenta la posizione di un bit di un intero
        val position = intArrayOf(1, 8, 64, 2, 16, 128, 4, 32, 256, 2048, 1024, 512, 8388608, 1048576, 131072, 4194304, 524288, 65536, 2097152, 262144, 32768, 4096, 8192, 16384)

    }
}