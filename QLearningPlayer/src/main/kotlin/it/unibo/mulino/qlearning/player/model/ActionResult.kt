package it.unibo.mulino.qlearning.player.model

data class ActionResult(
        val newState: State,
        val mill: Boolean,
        val winState: Boolean
)