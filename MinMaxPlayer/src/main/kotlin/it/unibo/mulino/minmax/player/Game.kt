package it.unibo.mulino.minmax.player

abstract class Game {

    abstract val initialState: State

    abstract val players: IntArray

    abstract fun getPlayer(state: State): Player

    /**
     * Dato uno stato [state] genera tutte le azioni valide che è possibile eseguire
     */
    abstract fun getActions(state: State): Actions

    /**
     * Dato uno stato [state] e l'hash [actionHash] di un'azione, genera lo stato risultate
     */
    abstract fun getResult(state: State, actionHash: Action): State

    abstract fun isTerminal(state: State): Boolean

    abstract fun getUtility(state: State, player: Player): Double
}