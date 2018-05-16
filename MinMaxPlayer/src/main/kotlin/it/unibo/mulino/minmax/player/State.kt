package it.unibo.mulino.minmax.player

// TODO("Override delle hashcode e equals della classe")
data class State(
        val playerType: Int,
        val board: IntArray = intArrayOf(0, 0),
        val checkers: IntArray = intArrayOf(9, 9),
        val checkersOnBoard: IntArray = intArrayOf(0, 0),
        val currentPhase: Int = 1,
        val closedMorris: Boolean = false) {

    companion object {

        // Controlla se nella board è presente la pedina del giocatore indicato, nella posizione indicata
        fun isSet(board: IntArray, x: Int, y: Int, player: Int): Boolean = board[player].and(position[x * 3 + y]) > 0

        // Controlla se nella board non è presente nessuna pedina (empty)
        fun isNotSet(board: IntArray, x: Int, y: Int): Boolean = !State.isSet(board, x, y, 0) && !State.isSet(board, x, y, 1)

        fun isSet(board: IntArray, posToCheck: Pair<Int, Int>, player: Int): Boolean = board[player].and(position[posToCheck.first * 3 + posToCheck.second]) > 0

        // ogni valore rappresenta la posizione di un bit di un intero
        val position = intArrayOf(1, 8, 64, 2, 16, 128, 4, 32, 256, 2048, 1024, 512, 8388608, 1048576, 131072, 4194304, 524288, 65536, 2097152, 262144, 32768, 4096, 8192, 16384)

    }
}