package it.unibo.mulino.player

import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.client.MulinoClient
import it.unibo.ai.didattica.mulino.domain.State


class AIPlayer(val playerType : State.Checker) : MulinoClient(playerType){

    fun play() {
        println(playerType)

        // lettura stato iniziale
        var state : State = read() ?: throw IllegalStateException("Lo stato è null")

        // attendo che il bianco faccia la prima mossa
        if(playerType == State.Checker.BLACK)
            // read sospensiva
            state = read() ?: throw IllegalStateException("Lo stato è null")

        /* schema identico per entrambi i giocatori
           play
           read (lettura dello stato a seguito della mia mossa)
           read (sospensiva, attesa della mossa dell'avversario)
         */

        while(true){
            val playPhase = phaseMethod(state.currentPhase)
            val action = playPhase(state)
            write(action)
            // mio update
            state = read()
            state = read()
        }
    }

    var count = 0

    private fun playPhaseFirst(state : State) : Phase1Action{
        TODO()
    }
    private fun playPhaseSecond(state : State) : Phase1Action{
        TODO()
    }
    private fun playPhaseFinal(state : State) : Phase1Action{
        TODO()
    }

    private fun phaseMethod(phase : State.Phase) : (State) -> Action =
            when(phase){
                State.Phase.FIRST -> this::playPhaseFirst
                State.Phase.SECOND -> this::playPhaseSecond
                State.Phase.FINAL -> this::playPhaseFinal
            }

}