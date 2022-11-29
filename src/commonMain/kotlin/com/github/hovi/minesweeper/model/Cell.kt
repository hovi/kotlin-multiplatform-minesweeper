package com.github.hovi.minesweeper.model

class Cell(val type: CellType, state: CellMarkState) {

    var state: CellMarkState = state
        private set

    fun stateReadyForFinish(): Boolean {
        return !isNonFreed() && !isUnMarkedMine()
    }

    fun isNonFreed(): Boolean {
        return type != CellType.MINE && state != CellMarkState.FREE
    }

    fun isUnMarkedMine(): Boolean {
        return type == CellType.MINE && state != CellMarkState.MARKED
    }

    fun isFreedMine(): Boolean {
        return type == CellType.MINE && state == CellMarkState.FREE
    }

    internal fun mark(): CellMarkState {
        state = when (state) {
            CellMarkState.MARKED -> CellMarkState.UNMARKED
            CellMarkState.UNMARKED -> CellMarkState.MARKED
            CellMarkState.FREE -> CellMarkState.FREE
        }
        return state
    }

    internal fun free() {
        state = CellMarkState.FREE
    }

    fun reveal() {
        if (type == CellType.MINE) {
            state = CellMarkState.FREE
        }
    }
}
