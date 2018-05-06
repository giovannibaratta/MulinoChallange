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

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("FROM : ")
        sb.append(when (from.isPresent) {
            true -> "[${from.get().x},${from.get().y}]\t"
            false -> " None\t"
        })
        sb.append("TO : ")
        sb.append(when (to.isPresent) {
            true -> "[${to.get().x},${to.get().y}]\t"
            false -> " None\t"
        })
        sb.append("REMOVE : ")
        sb.append(when (remove.isPresent) {
            true -> "[${remove.get().x},${remove.get().y}]\t"
            false -> " None\t"
        })
        return sb.toString()
    }
}
