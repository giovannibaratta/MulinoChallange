package it.unibo

import it.unibo.ai.didattica.mulino.domain.State
import it.unibo.mulino.player.AIPlayer
import java.net.ConnectException

fun main(args : Array<String>){

    require(args.size == 1,{"Numero argomenti errato. Specificare solo il tipo di giocatore (White o Black)"})

    val playerType = when{
        args[0] == "White" -> State.Checker.WHITE
        args[0] == "Black" -> State.Checker.BLACK
        else -> throw IllegalStateException("Il tipo di giocatore ${args[0]} non è valido.")
    }

    var serverRunning = false

    while(!serverRunning) {
        try {
            AIPlayer(playerType).play()
            serverRunning = true
        } catch (e: ConnectException) {
            println("Il server non si è ancora attivato")
            Thread.sleep(5000)
        }
    }
}