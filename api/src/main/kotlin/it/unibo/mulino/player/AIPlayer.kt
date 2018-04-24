package it.unibo.mulino.player

import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State

interface AIPlayer {
    fun playPhase1(state: State): Phase1Action
    fun playPhase2(state: State): Phase2Action
    fun playPhaseFinal(state: State): PhaseFinalAction
}