package it.unibo.mulino.qlearning.player

import it.unibo.mulino.qlearning.player.model.Position
import it.unibo.mulino.qlearning.player.model.State
import it.unibo.utils.filterCellIndexed
import org.junit.Assert
import org.junit.Test

class QLearningPlayerTest {

    private val player = QLearningPlayer(explorationRate = { 0.0 }, alpha = { 0.0 })

    @Test
    fun actionPhase1Test() {
        // tutte le caselle libere
        val externalState = it.unibo.ai.didattica.mulino.domain.State()
        val state = it.unibo.mulino.qlearning.player.model.State(externalState, true)
        val actionList = player.actionFromStatePhase1(state)
        val emptyCell = state.grid.filterCellIndexed { it == State.Type.EMPTY }.toMutableList()
        actionList.forEach {
            val action = it
            Assert.assertFalse(it.from.isPresent)
            Assert.assertFalse(it.remove.isPresent)
            Assert.assertTrue(it.to.isPresent)
            emptyCell.removeIf { it.first == Pair(action.to.get().x, action.to.get().y) }
        }
        Assert.assertTrue(emptyCell.isEmpty())
    }

    @Test
    fun actionPhase1Test2() {
        // una sola casella libera, no mill
        val externalState = it.unibo.ai.didattica.mulino.domain.State()
        val state = it.unibo.mulino.qlearning.player.model.State(externalState, true)

        state.grid.filterCellIndexed { it == State.Type.EMPTY }.forEach {
            state.grid[it.first.first, it.first.second] = State.Type.BLACK
        }

        state.grid[state.boardSize - 1, state.boardSize - 1] = State.Type.EMPTY

        val actionList = player.actionFromStatePhase1(state)
        Assert.assertEquals(1, actionList.size)
        Assert.assertFalse(actionList[0].from.isPresent)
        Assert.assertFalse(actionList[0].remove.isPresent)
        Assert.assertTrue(actionList[0].to.isPresent)
        Assert.assertEquals(Position(state.boardSize - 1, state.boardSize - 1), actionList[0].to.get())
    }

    @Test
    fun actionPhase1Test3() {
        // una sola casella libera, chiusura mill
        val externalState = it.unibo.ai.didattica.mulino.domain.State()
        val state = it.unibo.mulino.qlearning.player.model.State(externalState, true)

        state.grid.filterCellIndexed { it == State.Type.EMPTY }.forEach {
            state.grid[it.first.first, it.first.second] = State.Type.WHITE
        }

        state.grid[0, 0] = State.Type.EMPTY

        state.grid[state.boardSize - 1, state.boardSize - 1] = State.Type.BLACK

        val actionList = player.actionFromStatePhase1(state)
        Assert.assertEquals(1, actionList.size)
        Assert.assertFalse(actionList[0].from.isPresent)
        Assert.assertTrue(actionList[0].remove.isPresent)
        Assert.assertTrue(actionList[0].to.isPresent)
        Assert.assertEquals(Position(0, 0), actionList[0].to.get())
        Assert.assertEquals(Position(state.boardSize - 1, state.boardSize - 1), actionList[0].remove.get())
    }

    @Test
    fun actionPhase1Test4() {
        // una sola casella libera, chiusura mill ma nessuna possibile pedina da rimuovere
        val externalState = it.unibo.ai.didattica.mulino.domain.State()
        val state = it.unibo.mulino.qlearning.player.model.State(externalState, true)

        state.grid.filterCellIndexed { it == State.Type.EMPTY }.forEach {
            state.grid[it.first.first, it.first.second] = State.Type.WHITE
        }

        state.grid[0, 0] = State.Type.EMPTY

        state.grid[state.boardSize - 1, state.boardSize - 1] = State.Type.BLACK
        state.grid[state.boardSize - 1, 0] = State.Type.BLACK
        state.grid[state.boardSize - 1, 3] = State.Type.BLACK

        val actionList = player.actionFromStatePhase1(state)
        Assert.assertEquals(1, actionList.size)
        Assert.assertFalse(actionList[0].from.isPresent)
        Assert.assertFalse(actionList[0].remove.isPresent)
        Assert.assertTrue(actionList[0].to.isPresent)
        Assert.assertEquals(Position(0, 0), actionList[0].to.get())
    }

}