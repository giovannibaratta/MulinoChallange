package it.unibo.mulino.qlearning.player

import it.unibo.mulino.qlearning.player.model.State
import it.unibo.mulino.qlearning.player.model.State.Type
import java.io.File
import it.unibo.ai.didattica.mulino.domain.State as ExternalState


fun main(args: Array<String>) {

    require(args.size == 1)
    // file with data
    val dataFile = File(args[0])
    if (!dataFile.exists()) {
        println("Il file ${args[0]} non esiste\n")
        System.exit(1)
    }

    val lines = dataFile.readLines()
    println("Data set size : ${lines.size}")
    val trainer = QLearningPlayer(true)
    var count = 0
    trainer.matchStart()

    for (i in 0..1000) {
        val random = (Math.random() * lines.size) % lines.size
        println("random $random")
        val mappedState = map(lines[(random).toInt()])
        if (mappedState.whiteHandCount > 0) {
            // fase 1
            trainer.playPhase1(mappedState)
        } else if (mappedState.whiteHandCount == 0 && mappedState.whiteBoardCount() > 3) {
            // fase 2
            trainer.playPhase2(mappedState)
        } else if (mappedState.whiteHandCount == 0 && mappedState.whiteBoardCount() <= 3) {
            // fase 3
            trainer.playPhase3(mappedState)
        } else {
            throw IllegalStateException("Fase non riconosciuta o non valida")
        }
        count++
        if (count % 1000 == 0) {
            println(" Working ${(100 * count) / lines.size}% ...")
            trainer.printPar()
            println()
        }
    }

    /*
    lines.forEach {
        // converto lo stato e richiamo la funzione giusta
        val mappedState = map(it)
        if (mappedState.whiteHandCount > 0) {
            // fase 1
            trainer.playPhase1(mappedState)
        } else if (mappedState.whiteHandCount == 0 && mappedState.whiteBoardCount() > 3) {
            // fase 2
            trainer.playPhase2(mappedState)
        } else if (mappedState.whiteHandCount == 0 && mappedState.whiteBoardCount() <= 3) {
            // fase 3
            trainer.playPhase3(mappedState)
        } else {
            throw IllegalStateException("Fase non riconosciuta o non valida")
        }
        count++
        if (count % 1000 == 0) {
            println(" Working ${(100 * count) / lines.size}% ...")
            trainer.printPar()
            println()
        }
    }*/
    println("fine")
    trainer.matchEnd()
}

internal fun map(stringState: String): State {
    val playerHand = (stringState[24]) - '0'
    val enemyHand = stringState[25] - '0'
    //val playerBoard = stringState[26] - '0'
    //val enemyBoard = stringState[27] - '0'

    val state = State(isWhiteTurn = true, whiteHandCount = playerHand, blackHandCount = enemyHand)
    // prima linea
    state.grid[0, 6] = letterToType(stringState[0])
    state.grid[3, 6] = letterToType(stringState[1])
    state.grid[6, 6] = letterToType(stringState[2])
    // seconda linea
    state.grid[1, 5] = letterToType(stringState[3])
    state.grid[3, 5] = letterToType(stringState[4])
    state.grid[5, 5] = letterToType(stringState[5])
    // terza linea
    state.grid[2, 4] = letterToType(stringState[6])
    state.grid[3, 4] = letterToType(stringState[7])
    state.grid[4, 4] = letterToType(stringState[8])
    // quarta linea
    state.grid[0, 3] = letterToType(stringState[9])
    state.grid[1, 3] = letterToType(stringState[10])
    state.grid[2, 3] = letterToType(stringState[11])
    state.grid[4, 3] = letterToType(stringState[12])
    state.grid[5, 3] = letterToType(stringState[13])
    state.grid[6, 3] = letterToType(stringState[14])
    // quinta linea
    state.grid[2, 2] = letterToType(stringState[15])
    state.grid[3, 2] = letterToType(stringState[16])
    state.grid[4, 2] = letterToType(stringState[17])
    // sesta linea
    state.grid[1, 1] = letterToType(stringState[18])
    state.grid[3, 1] = letterToType(stringState[19])
    state.grid[5, 1] = letterToType(stringState[20])
    // settima linea
    state.grid[0, 0] = letterToType(stringState[21])
    state.grid[3, 0] = letterToType(stringState[22])
    state.grid[6, 0] = letterToType(stringState[23])
    /*
    stringState.slice(0 until 24).forEachIndexed{ index, value->
        val position = Position(6-)
    }*/

    return state
}

internal fun letterToType(c: Char): State.Type = when (c) {
    'O' -> Type.EMPTY
    'E' -> Type.BLACK
    'M' -> Type.WHITE
    else -> throw IllegalArgumentException("Carattere $c non valido")
}