package it.unibo.utils

import org.junit.Assert
import org.junit.Test

class MatrixTest {

    @Test
    fun getTest() {
        val matrix = Matrix.buildMatrix(3, 4, { xIndex, yIndex -> xIndex + yIndex })
        val expectedRow1 = listOf(0, 1, 2, 3)
        val expectedRow2 = listOf(1, 2, 3, 4)
        val expectedRow3 = listOf(2, 3, 4, 5)

        Assert.assertArrayEquals(expectedRow1.toTypedArray(), matrix.get(0, Matrix.Orientation.HORIZONTAL).toTypedArray())
        Assert.assertArrayEquals(expectedRow2.toTypedArray(), matrix.get(1, Matrix.Orientation.HORIZONTAL).toTypedArray())
        Assert.assertArrayEquals(expectedRow3.toTypedArray(), matrix.get(2, Matrix.Orientation.HORIZONTAL).toTypedArray())

        val expectedColumn1 = listOf(0, 1, 2)
        val expectedColumn2 = listOf(1, 2, 3)
        val expectedColumn3 = listOf(2, 3, 4)
        val expectedColumn4 = listOf(3, 4, 5)

        Assert.assertArrayEquals(expectedColumn1.toTypedArray(), matrix.get(0, Matrix.Orientation.VERTICAL).toTypedArray())
        Assert.assertArrayEquals(expectedColumn2.toTypedArray(), matrix.get(1, Matrix.Orientation.VERTICAL).toTypedArray())
        Assert.assertArrayEquals(expectedColumn3.toTypedArray(), matrix.get(2, Matrix.Orientation.VERTICAL).toTypedArray())
        Assert.assertArrayEquals(expectedColumn4.toTypedArray(), matrix.get(3, Matrix.Orientation.VERTICAL).toTypedArray())

    }

}