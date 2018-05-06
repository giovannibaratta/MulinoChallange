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
                val playPhase = phaseMethod(state, playerType)
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

    private fun phaseMethod(state: State, playerType: State.Checker): (State, State.Checker) -> Action {


        val checkersCount = when (playerType) {
            State.Checker.WHITE -> state.whiteCheckersOnBoard
            State.Checker.BLACK -> state.blackCheckersOnBoard
            else -> throw IllegalArgumentException("playerType not valid")
        }


        return when {
            state.currentPhase == State.Phase.FIRST -> aiPlayer::playPhase1
            state.currentPhase == State.Phase.SECOND -> aiPlayer::playPhase2
            state.currentPhase == State.Phase.FINAL && checkersCount > 3 -> { curState: State, checker: State.Checker ->
                aiPlayer.playPhase2(curState, checker).remapToPhaseFinal()
            }
            state.currentPhase == State.Phase.FINAL && checkersCount <= 3 -> aiPlayer::playPhaseFinal
            else -> throw IllegalStateException("Fase non riconosciuta")
        }
    }

    private fun Phase2Action.remapToPhaseFinal(): PhaseFinalAction {
        val action = PhaseFinalAction()
        action.from = this.from
        action.to = this.to
        action.removeOpponentChecker = this.removeOpponentChecker
        return action
    }


}