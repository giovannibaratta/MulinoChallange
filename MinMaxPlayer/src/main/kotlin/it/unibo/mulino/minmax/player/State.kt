package it.unibo.mulino.minmax.player

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

        val ALL_MILLS = arrayOf(
                intArrayOf(0, 1, 2),
                intArrayOf(1, 9, 17),
                intArrayOf(2, 3, 4),
                intArrayOf(3, 11, 19),
                intArrayOf(4, 5, 6),
                intArrayOf(6, 7, 0),
                intArrayOf(5, 13, 21),
                intArrayOf(7, 15, 23),
                intArrayOf(8, 9, 10),
                intArrayOf(10, 11, 12),
                intArrayOf(12, 13, 14),
                intArrayOf(14, 15, 8),
                intArrayOf(16, 17, 18),
                intArrayOf(18, 19, 20),
                intArrayOf(20, 21, 22),
                intArrayOf(22, 23, 16)
        )

        val threePieceConfig = arrayOf(
                intArrayOf(0, 1, 7),
                intArrayOf(0, 1, 9),
                intArrayOf(1, 2, 9),
                intArrayOf(1, 2, 3),
                intArrayOf(2, 3, 11),
                intArrayOf(3, 4, 11),
                intArrayOf(3, 4, 5),
                intArrayOf(4, 5, 13),
                intArrayOf(5, 6, 13),
                intArrayOf(5, 6, 7),
                intArrayOf(6, 7, 15),
                intArrayOf(0, 7, 15),
                intArrayOf(8, 9, 15),
                intArrayOf(1, 8, 9),
                intArrayOf(1, 9, 10),
                intArrayOf(9, 10, 11),
                intArrayOf(10, 11, 19),
                intArrayOf(3, 10, 11),
                intArrayOf(3, 11, 12),
                intArrayOf(11, 12, 19),
                intArrayOf(11, 12, 13),
                intArrayOf(5, 12, 13),
                intArrayOf(5, 6, 13),
                intArrayOf(12, 13, 21),
                intArrayOf(13, 14, 21),
                intArrayOf(13, 14, 15),
                intArrayOf(7, 14, 15),
                intArrayOf(7, 8, 15),
                intArrayOf(14, 15, 23),
                intArrayOf(8, 15, 23),
                intArrayOf(16, 17, 23),
                intArrayOf(9, 16, 17),
                intArrayOf(9, 17, 18),
                intArrayOf(17, 18, 19),
                intArrayOf(9, 10, 17),
                intArrayOf(11, 18, 19),
                intArrayOf(19, 20, 21),
                intArrayOf(13, 20, 21),
                intArrayOf(13, 21, 22),
                intArrayOf(21, 22, 23),
                intArrayOf(15, 22, 23),
                intArrayOf(15, 16, 23),
                intArrayOf(14, 15, 23)
        )

        val adjacentLocations = arrayOf(
                intArrayOf(1, 7),
                intArrayOf(0, 2, 9),
                intArrayOf(1, 3),
                intArrayOf(2, 11, 4),
                intArrayOf(3, 5),
                intArrayOf(4, 13, 6),
                intArrayOf(5, 7),
                intArrayOf(0, 6, 15),
                intArrayOf(9, 15),
                intArrayOf(1, 8, 10, 17),
                intArrayOf(9, 11),
                intArrayOf(10, 3, 19, 12),
                intArrayOf(11, 13),
                intArrayOf(12, 14, 5, 21),
                intArrayOf(13, 15),
                intArrayOf(7, 23, 14, 8),
                intArrayOf(17, 23),
                intArrayOf(9, 16, 18),
                intArrayOf(17, 19),
                intArrayOf(18, 11, 20),
                intArrayOf(19, 21),
                intArrayOf(20, 13, 22),
                intArrayOf(21, 23),
                intArrayOf(16, 22, 15)
        )

    }
}