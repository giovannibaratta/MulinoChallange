package it.unibo.mulino.player

import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.client.MulinoClient
import it.unibo.ai.didattica.mulino.domain.State

class AIClient(val playerType: State.Checker,
               val aiPlayer: AIPlayer
) : MulinoClient(playerType) {

    constructor(playerType: State.Checker,
                playPhase1: (State, State.Checker) -> Phase1Action,
                playPhase2: (State, State.Checker) -> Phase2Action,
                playPhaseFinal: (State, State.Checker) -> PhaseFinalAction
    ) : this(playerType, object : AIPlayer {
        override fun playPhase1(state: State, playerType: State.Checker): Phase1Action = playPhase1(state, playerType)
        override fun playPhase2(state: State, playerType: State.Checker): Phase2Action = playPhase2(state, playerType)
        override fun playPhaseFinal(state: State, playerType: State.Checker): PhaseFinalAction = playPhaseFinal(state, playerType)
        override fun matchStart() {}
        override fun matchEnd() {}
    })

    fun play() {
        println(playerType)
        aiPlayer.matchStart()
        // lettura stato iniziale
        var state: State = read() ?: throw IllegalStateException("Lo stato è null")

        // attendo che il bianco faccia la prima mossa
        if (playerType == State.Checker.BLACK)
        // read sospensiva
            state = read() ?: throw IllegalStateException("Lo stato è null")

        /* schema identico per entrambi i giocatori
           play
           read (lettura dello stato a seguito della mia mossa)
           read (sospensiva, attesa della mossa dell'avversario)
         */

        try {
            while (true) {
                val playPhase = phaseMethod(state.currentPhase)
                val action = playPhase(state, player)
                write(action)
                // mio update
                state = read()
                state = read()
            }
        } catch (e: Exception) {
            println("Errore : ${e.printStackTrace()}")
        } finally {
            aiPlayer.matchEnd()
        }
    }

    var count = 0

    private fun phaseMethod(phase: State.Phase): (State, State.Checker) -> Action =
            when (phase) {
                State.Phase.FIRST -> aiPlayer::playPhase1
                State.Phase.SECOND -> aiPlayer::playPhase2
                State.Phase.FINAL -> aiPlayer::playPhaseFinal
            }

}