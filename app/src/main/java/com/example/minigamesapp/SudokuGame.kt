package com.example.minigamesapp

import kotlin.random.Random

class SudokuGame {

    private val size = 9
    private val grid = Array(size) { IntArray(size) }

    /**
     * Generates a filled Sudoku and then removes cells.
     * The removalCount defaults to 30 (easier); you can increase for harder puzzles.
     */
    fun generatePuzzle(removalCount: Int = 30): Array<IntArray> {
        // reset grid
        for (r in 0 until size) for (c in 0 until size) grid[r][c] = 0

        fillDiagonalBoxes()
        fillRemaining(0, 3)
        removeDigits(removalCount)
        return grid
    }

    private fun fillDiagonalBoxes() {
        for (i in 0 until size step 3) {
            fillBox(i, i)
        }
    }

    private fun fillBox(row: Int, col: Int) {
        var num: Int
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                do {
                    num = Random.nextInt(1, 10)
                } while (!unusedInBox(row, col, num))
                grid[row + i][col + j] = num
            }
        }
    }

    private fun unusedInBox(rowStart: Int, colStart: Int, num: Int): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (grid[rowStart + i][colStart + j] == num) return false
            }
        }
        return true
    }

    private fun fillRemaining(i: Int, j: Int): Boolean {
        var row = i
        var col = j
        if (col >= size && row < size - 1) {
            row++
            col = 0
        }
        if (row >= size && col >= size) return true
        if (row < 3) {
            if (col < 3) col = 3
        } else if (row < size - 3) {
            if (col == (row / 3) * 3) col += 3
        } else {
            if (col == size - 3) {
                row++
                col = 0
                if (row >= size) return true
            }
        }
        for (num in 1..9) {
            if (isSafe(row, col, num)) {
                grid[row][col] = num
                if (fillRemaining(row, col + 1)) return true
                grid[row][col] = 0
            }
        }
        return false
    }

    private fun isSafe(row: Int, col: Int, num: Int): Boolean {
        return unusedInRow(row, num) &&
                unusedInCol(col, num) &&
                unusedInBox(row - row % 3, col - col % 3, num)
    }

    private fun unusedInRow(row: Int, num: Int) = num !in grid[row]
    private fun unusedInCol(col: Int, num: Int) = (0 until size).none { grid[it][col] == num }

    private fun removeDigits(count: Int) {
        var remaining = count
        while (remaining > 0) {
            val cellId = Random.nextInt(size * size)
            val row = cellId / size
            val col = cellId % size
            if (grid[row][col] != 0) {
                grid[row][col] = 0
                remaining--
            }
        }
    }
}
