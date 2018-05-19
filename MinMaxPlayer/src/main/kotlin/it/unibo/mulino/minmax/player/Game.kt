package it.unibo.mulino.minmax.player

abstract class Game {

    abstract val initialState: State

    abstract val players: IntArray

    abstract fun getPlayer(state: State): Player

    abstract fun getActions(state: State): Actions

    abstract fun getResult(state: State, actionHash: Action): State

    abstract fun isTerminal(state: State): Boolean

    abstract fun getUtility(state: State, player: Player): Double
}