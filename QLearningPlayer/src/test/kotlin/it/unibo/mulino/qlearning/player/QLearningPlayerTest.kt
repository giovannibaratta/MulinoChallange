package it.unibo.mulino.qlearning.player

import it.unibo.mulino.qlearning.player.model.Action
import it.unibo.mulino.qlearning.player.model.Position
import it.unibo.mulino.qlearning.player.model.State
import it.unibo.utils.filterCellIndexed
import org.junit.Assert
import org.junit.Test
import java.util.*

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

    /* TODO("Test da rivedere perchè non soddisfa le reogle di chesani")
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
    }*/


    @Test
    fun myOpenMorrisTest() {

        val externalState = "MMOOOOOOOOOOOOOOOOOOOOEE7722"
        val internalState = map(externalState)
        val action = Action.buildPhase1(Position(0, 0), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.myOpenMorris(internalState, action, newState.newState)
        Assert.assertEquals(2 * player.myOpenMorrisMultiplier, result, 0.01)
    }

    @Test
    fun myOpenMorrisTest2() {
        val externalState = "MMOOOOOOOOOOOOOOOOOOOOEE7722"
        val internalState = map(externalState)
        val action = Action.buildPhase1(Position(6, 3), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.myOpenMorris(internalState, action, newState.newState)
        Assert.assertEquals(1 * player.myOpenMorrisMultiplier, result, 0.01)
    }

    @Test
    fun myOpenMorrisTest3() {
        val externalState = "MOOOOOOOOOOOOOOOOOOOOOEE8712"
        val internalState = map(externalState)
        val action = Action.buildPhase1(Position(6, 3), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.myOpenMorris(internalState, action, newState.newState)
        Assert.assertEquals(0.0, result, 0.01)
    }

    @Test
    fun myOpenMorrisTest4() {
        val externalState = "MMEOOOOOOOOOOOOOOOOOOOEE7623"
        val internalState = map(externalState)
        val action = Action.buildPhase1(Position(6, 3), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.myOpenMorris(internalState, action, newState.newState)
        Assert.assertEquals(0.0, result, 0.01)
    }

    @Test
    fun deniedEnemyMillTest() {
        val externalState = "EEOOOOOOOOOOOOOOOOOOOOEE9404"
        val internalState = map(externalState)
        val action = Action.buildPhase1(Position(6, 6), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.deniedEnemyMill(internalState, action, newState.newState)
        Assert.assertEquals(player.deniedEnemyMillPositive, result, 0.01)
    }

    @Test
    fun deniedEnemyMillTest2() {
        val externalState = "EEOOOOOOOOOOOOOOOOOOOOEE9404"
        val internalState = map(externalState)
        val action = Action.buildPhase1(Position(6, 3), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.deniedEnemyMill(internalState, action, newState.newState)
        Assert.assertEquals(player.deniedEnemyMillNegative, result, 0.01)
    }

    @Test
    fun enemyCanWinTest() {
        val externalState = "EEOOOOMOOOOOMOOOMOOOOOOE0033"
        val internalState = map(externalState)
        val action = Action.buildPhase3(Position(2, 4), Position(3, 4), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.enemyCanWin(internalState, action, newState.newState)
        Assert.assertEquals(player.enemyCanWinPositive, result, 0.01)
    }

    @Test
    fun enemyCanWinTest2() {
        val externalState = "EEOOOOMOOOOOMOOOMOOOOOEE0034"
        val internalState = map(externalState)
        val action = Action.buildPhase3(Position(2, 4), Position(3, 4), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.enemyCanWin(internalState, action, newState.newState)
        Assert.assertEquals(player.enemyCanWinNegative, result, 0.01)
    }

    @Test
    fun lStructureTest() {
        val externalState = "MMMOOOOOOOOOOOOOOOOOEEEE0034"
        val internalState = map(externalState)
        val action = Action.buildPhase3(Position(6, 6), Position(3, 5), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.struttureAdL(internalState, action, newState.newState)
        Assert.assertEquals(1 * player.struttureAdLMultiplier, result, 0.01)
    }

    @Test
    fun lStructureTest1() {
        val externalState = "MMMOOOOOOOOOOOOOOOOOEEEE0034"
        val internalState = map(externalState)
        val action = Action.buildPhase3(Position(6, 6), Position(3, 4), Optional.empty())
        val newState = internalState.simulateAction(action)
        val result = player.struttureAdL(internalState, action, newState.newState)
        Assert.assertEquals(0 * player.struttureAdLMultiplier, result, 0.01)
    }

    private fun map(stringState: String): State {
        val playerHand = (stringState[24]) - '0'
        val enemyHand = stringState[25] - '0'
        //val playerBoard = stringState[26] - '0'
        //val enemyBoard = stringState[27] - '0'

        val state = State(isWhiteTurn = true, whiteHandCount = playerHand, blackHandCount = enemyHand)
        // prima linea
        state.grid[0, 6] = letterToType(stringState[0])
        state.grid[3, 6] = letterToType(stringState[1])
        state.grid[6, 6] = letterToType(stringState[2])
        // seconda linea
        state.grid[1, 5] = letterToType(stringState[3])
        state.grid[3, 5] = letterToType(stringState[4])
        state.grid[5, 5] = letterToType(stringState[5])
        // terza linea
        state.grid[2, 4] = letterToType(stringState[6])
        state.grid[3, 4] = letterToType(stringState[7])
        state.grid[4, 4] = letterToType(stringState[8])
        // quarta linea
        state.grid[0, 3] = letterToType(stringState[9])
        state.grid[1, 3] = letterToType(stringState[10])
        state.grid[2, 3] = letterToType(stringState[11])
        state.grid[4, 3] = letterToType(stringState[12])
        state.grid[5, 3] = letterToType(stringState[13])
        state.grid[6, 3] = letterToType(stringState[14])
        // quinta linea
        state.grid[2, 2] = letterToType(stringState[15])
        state.grid[3, 2] = letterToType(stringState[16])
        state.grid[4, 2] = letterToType(stringState[17])
        // sesta linea
        state.grid[1, 1] = letterToType(stringState[18])
        state.grid[3, 1] = letterToType(stringState[19])
        state.grid[5, 1] = letterToType(stringState[20])
        // settima linea
        state.grid[0, 0] = letterToType(stringState[21])
        state.grid[3, 0] = letterToType(stringState[22])
        state.grid[6, 0] = letterToType(stringState[23])
        /*
        stringState.slice(0 until 24).forEachIndexed{ index, value->
            val position = Position(6-)
        }*/

        return state
    }

    private fun letterToType(c: Char): State.Type = when (c) {
        'O' -> State.Type.EMPTY
        'E' -> State.Type.BLACK
        'M' -> State.Type.WHITE
        else -> throw IllegalArgumentException("Carattere $c non valido")
    }

}