package it.unibo.mulino.qlearning.player

import it.unibo.mulino.qlearning.player.State.Position
import it.unibo.mulino.qlearning.player.State.Type
import org.junit.Assert
import org.junit.Test
import it.unibo.ai.didattica.mulino.domain.State as ExternalState

class StateTest {

    @Test
    fun EmptyParsing() {
        val expectedGrid = arrayOf(
                arrayOf(Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY),
                arrayOf(Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID),
                arrayOf(Type.INVALID, Type.INVALID, Type.EMPTY, Type.EMPTY, Type.EMPTY, Type.INVALID, Type.INVALID),
                arrayOf(Type.EMPTY, Type.EMPTY, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.EMPTY, Type.EMPTY),
                arrayOf(Type.INVALID, Type.INVALID, Type.EMPTY, Type.EMPTY, Type.EMPTY, Type.INVALID, Type.INVALID),
                arrayOf(Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID),
                arrayOf(Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY)
        )

        val state = ExternalState()
        val parsed = State(state)
        Assert.assertArrayEquals(expectedGrid, parsed.grid)
    }

    @Test
    fun Parsing1() {
        val expectedGrid = arrayOf(
                arrayOf(Type.WHITE, Type.INVALID, Type.INVALID, Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY),
                arrayOf(Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID),
                arrayOf(Type.INVALID, Type.INVALID, Type.EMPTY, Type.EMPTY, Type.EMPTY, Type.INVALID, Type.INVALID),
                arrayOf(Type.EMPTY, Type.EMPTY, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.EMPTY, Type.EMPTY),
                arrayOf(Type.INVALID, Type.INVALID, Type.EMPTY, Type.EMPTY, Type.EMPTY, Type.INVALID, Type.INVALID),
                arrayOf(Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID),
                arrayOf(Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY)
        )

        val state = ExternalState()
        state.board.put("a1", ExternalState.Checker.WHITE)
        val parsed = State(state)
        Assert.assertArrayEquals(expectedGrid, parsed.grid)
    }

    @Test
    fun Parsing2() {
        val expectedGrid = arrayOf(
                arrayOf(Type.WHITE, Type.INVALID, Type.INVALID, Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY),
                arrayOf(Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID),
                arrayOf(Type.INVALID, Type.INVALID, Type.EMPTY, Type.EMPTY, Type.EMPTY, Type.INVALID, Type.INVALID),
                arrayOf(Type.EMPTY, Type.WHITE, Type.BLACK, Type.INVALID, Type.EMPTY, Type.EMPTY, Type.EMPTY),
                arrayOf(Type.INVALID, Type.INVALID, Type.EMPTY, Type.EMPTY, Type.EMPTY, Type.INVALID, Type.INVALID),
                arrayOf(Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID, Type.EMPTY, Type.INVALID),
                arrayOf(Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY, Type.INVALID, Type.INVALID, Type.EMPTY)
        )

        val state = ExternalState()
        state.board.put("a1", ExternalState.Checker.WHITE)
        state.board.put("d2", ExternalState.Checker.WHITE)
        state.board.put("d3", ExternalState.Checker.BLACK)
        val parsed = State(state)
        Assert.assertArrayEquals(expectedGrid, parsed.grid)
    }

    @Test(expected = IllegalArgumentException::class)
    fun ParsingWithException() {
        val state = ExternalState()
        state.board.put("a2", ExternalState.Checker.WHITE)
        State(state)
    }

    @Test
    fun NoAdjacentVertex() {
        val state = ExternalState()
        state.board = hashMapOf(
                Pair("a1", ExternalState.Checker.WHITE)
        )
        val remappedState = State(state)
        val adjacentTo00 = remappedState.adjacent(Position(0, 0))
        Assert.assertEquals(0, adjacentTo00.size)
    }

    @Test
    fun NoAdjacentMiddle() {
        val state = ExternalState()
        state.board = hashMapOf(
                Pair("a4", ExternalState.Checker.WHITE)
        )
        val remappedState = State(state)
        val adjacentTo03 = remappedState.adjacent(Position(0, 3))
        Assert.assertEquals(0, adjacentTo03.size)
    }

    @Test
    fun NoAdjacentMiddle2() {
        val state = ExternalState()
        state.board = hashMapOf(
                Pair("d1", ExternalState.Checker.WHITE)
        )
        val remappedState = State(state)
        val adjacentTo30 = remappedState.adjacent(Position(3, 0))
        Assert.assertEquals(0, adjacentTo30.size)
    }

    @Test
    fun NoAdjacentInnerSquare() {
        val state = ExternalState()
        state.board = hashMapOf(
                Pair("d3", ExternalState.Checker.WHITE)
        )
        val remappedState = State(state)
        val adjacentTo03 = remappedState.adjacent(Position(3, 2))
        Assert.assertEquals(0, adjacentTo03.size)
    }

    @Test
    fun NoAdjacentB4() {
        val state = ExternalState()
        state.board = hashMapOf(
                Pair("b4", ExternalState.Checker.WHITE)
        )
        val remappedState = State(state)
        val adjacentTo13 = remappedState.adjacent(Position(1, 3))
        Assert.assertEquals(0, adjacentTo13.size)
    }

    @Test
    fun MillTestNegative() {
        val state = ExternalState()
        val remappedState = State(state)
        Assert.assertFalse(remappedState.closeMill(Position(0, 0)))
    }

    @Test
    fun MillTestNegative2() {
        val state = ExternalState()
        state.board.put("a1", ExternalState.Checker.WHITE)
        val remappedState = State(state)
        Assert.assertFalse(remappedState.closeMill(Position(0, 3)))
    }

    @Test
    fun MillTestNegative3() {
        val state = ExternalState()
        state.board.put("a1", ExternalState.Checker.WHITE)
        state.board.put("a7", ExternalState.Checker.BLACK)
        val remappedState = State(state)
        Assert.assertFalse(remappedState.closeMill(Position(0, 3)))
    }

    @Test
    fun MillTestPositive() {
        val state = ExternalState()
        state.board.put("a1", ExternalState.Checker.WHITE)
        state.board.put("a7", ExternalState.Checker.WHITE)
        val remappedState = State(state)
        Assert.assertTrue(remappedState.closeMill(Position(0, 3)))
    }

    @Test
    fun MillTestPositive2() {
        val state = ExternalState()
        state.board.put("a1", ExternalState.Checker.WHITE)
        state.board.put("g1", ExternalState.Checker.WHITE)
        val remappedState = State(state)
        Assert.assertTrue(remappedState.closeMill(Position(3, 0)))
    }

    @Test
    fun MillTestPositive3() {
        val state = ExternalState()
        state.board.put("c3", ExternalState.Checker.WHITE)
        state.board.put("e3", ExternalState.Checker.WHITE)
        val remappedState = State(state)
        Assert.assertTrue(remappedState.closeMill(Position(3, 2)))
    }

    @Test
    fun MillTestPositive4() {
        val state = ExternalState()
        state.board.put("d3", ExternalState.Checker.WHITE)
        state.board.put("d1", ExternalState.Checker.WHITE)
        val remappedState = State(state)
        Assert.assertTrue(remappedState.closeMill(Position(3, 1)))
    }


}

