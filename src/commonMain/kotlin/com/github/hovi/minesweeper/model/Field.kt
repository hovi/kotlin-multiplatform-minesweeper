package com.github.hovi.minesweeper.model

import com.github.hovi.minesweeper.exception.CannotMarkCellException
import com.github.hovi.minesweeper.exception.CheckingMarkedCellException
import com.github.hovi.minesweeper.exception.GameAlreadyOverException
import com.github.hovi.minesweeper.exception.MineHitException
import kotlin.random.Random


class Field(val width: Int = 9, val height: Int = 9, val mines: Int = 10, private val random: Random = Random.Default) {

    var initialized = false
        private set

    var board: Array<Array<Cell>> = arrayOf()
        private set

    init {
        if (mines > width * height - 9) {
            throw IllegalArgumentException("Too much mines asked...Can't go over ${width * height - 9}")
        }

        initializeBoard()
    }

    fun isTheGameWon(): Boolean {
        if (!initialized) {
            return false
        }

        exploreBoard { x, y ->
            val cell = board[x][y]

            // Mine not marked or empty cell not free, makes the game still playing
            if (cell.isNonFreed() || cell.isUnMarkedMine()) {
                return false
            }
        }

        return true
    }

    private fun isTheGameLost(): Boolean {
        exploreBoard { x, y ->
            val cell = board[x][y]

            if (cell.isFreedMine()) {
                return true
            }
        }
        return false
    }

    private fun checkCoordinates(x: Int, y: Int) {
        if (x < 0 || y < 0 || y >= height || x >= width) throw IndexOutOfBoundsException("($x, $y) is not withing the boundaries (0, 0)-(${width - 1},${height - 1})")
    }

    private fun checkGameState() {
        if (isTheGameLost()) {
            throw GameAlreadyOverException()
        }
    }

    fun toggleMark(x: Int, y: Int): CellMarkState {
        checkGameState()
        checkCoordinates(x, y)
        val cell = board[x][y]
        if (cell.state == CellMarkState.FREE) {
            throw CannotMarkCellException(cell, x, y)
        }
        return board[x][y].mark()
    }

    fun check(x: Int, y: Int, force: Boolean): CellType {
        checkGameState()
        checkCoordinates(x, y)
        if (!initialized) {
            initialize(x, y)
        }

        val cell = board[x][y]


        if (!force && cell.state == CellMarkState.MARKED) {
            throw CheckingMarkedCellException(cell, x, y)
        }


        cell.free()
        if (cell.type == CellType.MINE) {
            throw MineHitException(cell, x, y)
        }


        if (cell.type == CellType.NONE) {
            exploreAroundCell(x, y) { otherCellX, otherCellY ->
                if (board[otherCellX][otherCellY].type != CellType.MINE && board[otherCellX][otherCellY].state != CellMarkState.FREE) {
                    check(otherCellX, otherCellY, force = true)
                }
            }
        }
        return cell.type
    }

    fun reveal() {
        exploreBoard { x, y ->
            board[x][y].reveal()
        }
    }

    private fun initialize(firstTurnInputX: Int, firstTurnInputY: Int) {
        initializeCellWithMines(firstTurnInputX, firstTurnInputY)
        initializeCellWithoutMines()
        initialized = true
    }

    private fun initializeBoard() {
        for (i in 1..width) {
            board += Array(height) {
                Cell(
                    type = CellType.NONE,
                    state = CellMarkState.UNMARKED
                )
            }
        }
    }

    private fun initializeCellWithMines(firstTurnInputX: Int, firstTurnInputY: Int) {

        var remainingMines = mines
        val excludedPositions = getExcludedMinePositions(firstTurnInputX, firstTurnInputY)

        while (remainingMines > 0) {

            val x = random.nextInt(0, width)
            val y = random.nextInt(0, height)

            if (!excludedPositions.contains(Pair(x, y)) && board[x][y].type != CellType.MINE) {
                board[x][y] = Cell(CellType.MINE, board[x][y].state)
                remainingMines--
            }
        }
    }

    private fun getExcludedMinePositions(firstTurnInputX: Int, firstTurnInputY: Int): HashSet<Pair<Int, Int>> {
        val excludedValues = HashSet<Pair<Int, Int>>()

        excludedValues.add(Pair(firstTurnInputX, firstTurnInputY))

        exploreAroundCell(firstTurnInputX, firstTurnInputY) { otherCellX, otherCellY ->
            excludedValues.add(Pair(otherCellX, otherCellY))
        }

        return excludedValues
    }

    private fun initializeCellWithoutMines() {

        exploreBoard { x, y ->
            if (board[x][y].type == CellType.MINE) {
                return@exploreBoard
            }

            var minesAround = 0

            exploreAroundCell(x, y) { otherCellX, otherCellY ->
                if (board[otherCellX][otherCellY].type == CellType.MINE) {
                    minesAround++
                }
            }

            if (minesAround != 0) {
                board[x][y] = Cell(CellType.valueOf(minesAround), board[x][y].state)
            }
        }
    }

    private inline fun <T> exploreBoard(action: (Int, Int) -> T): List<T> {
        val result = mutableListOf<T>()
        for (x in board.indices) {
            for (y in board[x].indices) {
                result.add(action(x, y))
            }
        }
        return result
    }

    private fun exploreAroundCell(x: Int, y: Int, action: (Int, Int) -> Any) {
        for (k in -1..1) {
            for (l in -1..1) {
                // The current cell
                if (k == 0 && l == 0) {
                    continue
                }
                // Out of the board
                if (x + k < 0 || y + l < 0 || x + k >= width || y + l >= height) {
                    continue
                }

                action(x + k, y + l)
            }
        }
    }
}
