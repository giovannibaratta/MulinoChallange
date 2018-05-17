package it.unibo.mulino.minmax.player

import it.unibo.ai.didattica.mulino.domain.State
import org.junit.Assert
import org.junit.Test

class StateTest {

    @Test
    fun testIsSet() {
        val state = State(
                checker = State.Checker.WHITE,
                closedMorris = false,
                currentPhase = 1,
                checkers = intArrayOf(0, 0),
                checkersOnBoard = intArrayOf(0, 0),
                board = intArrayOf(0, 0))

        for (x in 0 until 8) {
            for (y in 0 until 3) {
                Assert.assertFalse(state.isSet(x, y, 0))
                Assert.assertFalse(state.isSet(x, y, 1))
            }
        }
    }

    @Test
    fun testIsSet2() {
        val state = State(
                checker = State.Checker.WHITE,
                closedMorris = false,
                currentPhase = 1,
                checkers = intArrayOf(0, 0),
                checkersOnBoard = intArrayOf(1, 0),
                board = intArrayOf(1, 0))

        for (x in 0 until 8) {
            for (y in 0 until 3) {
                if (x == 0 && y == 0) {
                    Assert.assertFalse(state.isSet(x, y, 1))
                    Assert.assertTrue(state.isSet(x, y, 0))
                } else {
                    Assert.assertFalse(state.isSet(x, y, 0))
                    Assert.assertFalse(state.isSet(x, y, 1))
                }
            }
        }
    }

    @Test
    fun testIsSet3() {
        val state = State(
                checker = State.Checker.WHITE,
                closedMorris = false,
                currentPhase = 1,
                checkers = intArrayOf(0, 0),
                checkersOnBoard = intArrayOf(2, 0),
                board = intArrayOf(1 + 2048, 0))

        for (x in 0 until 8) {
            for (y in 0 until 3) {
                if ((x == 0 && y == 0) || (x == 3 && y == 0)) {
                    Assert.assertFalse(state.isSet(x, y, 1))
                    Assert.assertTrue(state.isSet(x, y, 0))
                } else {
                    Assert.assertFalse(state.isSet(x, y, 0))
                    Assert.assertFalse(state.isSet(x, y, 1))
                }
            }
        }
    }

}