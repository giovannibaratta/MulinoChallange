package it.unibo.mulino.player

import it.unibo.ai.didattica.mulino.actions.Action
import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.client.MulinoClient
import it.unibo.ai.didattica.mulino.domain.State

class AIClient (val playerType : State.Checker,
                val aiPlayer : AIPlayer
                ) : MulinoClient(playerType){

    constructor(playerType : State.Checker,
                playPhase1 : (State) -> Phase1Action,
                playPhase2 : (State) -> Phase2Action,
                playPhaseFinal : (State) -> PhaseFinalAction
    ) : this(playerType, object : AIPlayer{
                override fun playPhase1(state: State): Phase1Action = playPhase1(state)
                override fun playPhase2(state: State): Phase2Action = playPhase2(state)
                override fun playPhaseFinal(state: State): PhaseFinalAction = playPhaseFinal(state)
                })

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

    private fun phaseMethod(phase : State.Phase) : (State) -> Action =
            when(phase){
                State.Phase.FIRST -> aiPlayer::playPhase1
                State.Phase.SECOND -> aiPlayer::playPhase2
                State.Phase.FINAL -> aiPlayer::playPhaseFinal
            }

}