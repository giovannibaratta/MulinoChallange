package it.unibo.mulino.qlearning.player.model

import java.util.*

internal data class Action private constructor(val from: Optional<Position>,
                                               val to: Optional<Position>,
                                               val remove: Optional<Position>) {

    companion object {
        fun buildPhase1(to: Position, remove: Optional<Position>) = Action(Optional.empty(), Optional.of(to), remove)

        fun buildPhase2(from: Position, to: Position, remove: Optional<Position>) = Action(Optional.of(from), Optional.of(to), remove)

        fun buildPhase3(from: Position, to: Position, remove: Optional<Position>) = buildPhase2(from, to, remove)
    }
}
