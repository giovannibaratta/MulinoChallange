package it.unibo.mulino.player

import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State

interface AIPlayer {
    fun playPhase1(state: State, player : State.Checker): Phase1Action
    fun playPhase2(state: State, player : State.Checker): Phase2Action
    fun playPhaseFinal(state: State, player : State.Checker): PhaseFinalAction
}