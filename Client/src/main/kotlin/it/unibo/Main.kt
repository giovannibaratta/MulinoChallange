package it.unibo

import it.unibo.ai.didattica.mulino.domain.State
import it.unibo.mulino.minmax.player.MinMaxPlayer
import it.unibo.mulino.player.AIClient
import it.unibo.mulino.qlearning.player.Trainer
import java.net.ConnectException

enum class Algorithm {
    QLEARNING,
    MINMAX,
    QLEARNINGNOSAVE
}

enum class ErrorType {
    PLAYER_TYPE,
    ALGORITHM,
}

val errorType = hashMapOf(
        Pair(ErrorType.PLAYER_TYPE, Pair("Giocatori supportati (White, Black)", 1)),
        Pair(ErrorType.ALGORITHM, Pair("Algortimi supportati (MinMax, qLearning)", 2))
)

val player = hashMapOf(
        Pair(Algorithm.MINMAX, MinMaxPlayer()),
        Pair(Algorithm.QLEARNING, Trainer()),
        Pair(Algorithm.QLEARNINGNOSAVE, Trainer(false))
)

fun main(args: Array<String>) {

    require(args.size == 1 || args.size == 2,
            { "Numero argomenti errato. Specificare il tipo di giocatore (White o Black) e opzionalmente il tipo di algoritmo (MinMax, qLearning)" })

    val playerType = when (args[0]) {
        "White" -> State.Checker.WHITE
        "Black" -> State.Checker.BLACK
        else -> exitWithError(ErrorType.PLAYER_TYPE)
    }

    var selectedAlgorithm = Algorithm.QLEARNING

    if (args.size == 2) {
        selectedAlgorithm = when (args[1]) {
            "qLearning" -> Algorithm.QLEARNING
            "qLearningNoSave" -> Algorithm.QLEARNINGNOSAVE
            "MinMax" -> Algorithm.MINMAX
            else -> exitWithError(ErrorType.ALGORITHM)
        }
    }

    var serverRunning = false

    while (!serverRunning) {
        try {
            AIClient(playerType, player[selectedAlgorithm]!!).play()
            serverRunning = true
        } catch (e: ConnectException) {
            println("Il server non si è ancora attivato")
            Thread.sleep(5000)
        }
    }
}

fun exitWithError(type: ErrorType): Nothing {
    val error = errorType[type]
    if (error != null) {
        println(error.first)
        System.exit(error.second)
    } else {
        println("Errore sconosciuto")
        System.exit(255)
    }
    throw IllegalStateException("Non dovrei essere qui")
}