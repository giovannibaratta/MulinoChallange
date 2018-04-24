package it.unibo.mulino.qlearning.player

import it.unibo.ai.didattica.mulino.actions.Phase1Action
import it.unibo.ai.didattica.mulino.actions.Phase2Action
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction
import it.unibo.ai.didattica.mulino.domain.State
import it.unibo.mulino.player.AIPlayer

class QLearningPlayer : AIPlayer {


    override fun playPhase1(state: State): Phase1Action {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playPhase2(state: State): Phase2Action {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playPhaseFinal(state: State): PhaseFinalAction {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //private

    private fun State.remap() = State(this)
}