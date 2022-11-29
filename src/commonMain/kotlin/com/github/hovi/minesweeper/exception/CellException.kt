package com.github.hovi.minesweeper.exception

import com.github.hovi.minesweeper.model.Cell

open class CellException(val cell: Cell, val x: Int, val y: Int, message: String) : RuntimeException(message)

class MineHitException(cell: Cell, x: Int, y: Int) : CellException(cell, x, y, "Mine was hit!")

class CheckingMarkedCellException(cell: Cell, x: Int, y: Int) : CellException(cell, x, y, "Cell ($x, $y) is marked!")

class CannotMarkCellException(cell: Cell, x: Int, y: Int) : CellException(cell, x, y, "Cannot mark cell ($x, $y), it's already free!")
