package minesweeper

import kotlin.system.exitProcess

class MineSweeper(val size: Int, val numberOfMines: Int) {
    companion object {
        const val UNEXPLORED_SYMBOL = "."
        const val EXPLORED_FREE_SYMBOL = "/"
        const val MINE_SYMBOL = "X"
        const val MARK_SYMBOL = "*"
    }

    private var populated = false

    private class InputException(message: String) : Exception(message)
    private class StepOnAMineException(message: String) : Exception(message)
    private class MarkException(message: String) : Exception(message)

    private data class Cell(
        val x: Int,
        val y: Int,
        var mined: Boolean = false,
        var marked: Boolean = false,
        var explored: Boolean = false,
        var numberOfAdjacentMines: Int = 0
    )

    private val mineField: List<MutableList<Cell>> = List(size) { y ->
        MutableList(size) { x ->
            Cell(x, y)
        }
    }

    private fun Cell.adjacentCells(): MutableList<Cell> {
        val list = mutableListOf<Cell>()
        for (y in this.y - 1..this.y + 1) {
            for (x in this.x - 1..this.x + 1) {
                if (!(x == this.x && y == this.y))
                    try {
                        list.add(mineField[y][x])
                    } catch (e: IndexOutOfBoundsException) {
                    }
            }
        }
        return list
    }

    private fun populateWithMines(safe: Cell) {
        mineField.flatten().filter { it != safe }.shuffled().subList(0, numberOfMines).forEach {
            it.mined = true
        }
        mineField.flatten().forEach { it.updateNumberOfAdjacentMines() }
        populated = true
    }

    private fun Cell.updateNumberOfAdjacentMines() {
        this.numberOfAdjacentMines = this.adjacentCells().count {
            it.mined
        }
    }

    private fun Cell.markMine() {
        if (marked) {
            marked = false
        } else if (explored && !mined) {
            throw MarkException("There is a number here!")
        } else {
            marked = true
        }
    }

    private fun Cell.explore() {
        this.explored = true
        if (this.mined) {
            throw StepOnAMineException("You stepped on a mine and failed!")
        }
        if (this.numberOfAdjacentMines == 0) {
            val queue = mutableListOf<Cell>(this)
            while (queue.isNotEmpty()) {
                queue.removeAt(0).let {
                    it.explored = true
                    if (it.marked) {
                        it.marked = false
                    }
                    if (it.numberOfAdjacentMines == 0) {
                        queue.addAll(it.adjacentCells().filter { !it.explored })
                    }
                }
            }
        }
    }

    private fun printMineField() {
        println()
        print(" │")
        for (x in 1..mineField.size) {
            print(x)
        }
        println("│")
        print("—│")
        for (x in 1..mineField.size) {
            print("—")
        }
        println("│")
        for ((y, row) in mineField.withIndex()) {
            print("${y + 1}│")
            for ((x, cell) in row.withIndex()) {
                if (cell.marked) {
                    print(MARK_SYMBOL)
                } else if (!cell.explored) {
                    print(UNEXPLORED_SYMBOL)
                } else if (cell.mined) {
                    print(MINE_SYMBOL)
                } else if (cell.numberOfAdjacentMines == 0) {
                    print(EXPLORED_FREE_SYMBOL)
                } else {
                    print("${cell.numberOfAdjacentMines}")
                }
            }
            println("│")
        }

        print("—│")
        for (x in 1..mineField.size) {
            print("—")
        }
        println("│")
    }

    fun play() {

        printMineField()
        var x: Int = 0
        var y: Int = 0
        var action = ""
        do {
            try {
                print("Set/unset mines marks or claim a cell as free: ")
                readln().split(" ")
                    .let {
                        if (it.size != 3) throw InputException("wrong input, x y free|mine")
                        try {
                            if (it.first().toInt() !in 1..size) {
                                throw Exception()
                            }
                            x = it.first().toInt().dec()

                            if (it[1].toInt() !in 1..size) {
                                throw Exception()
                            }
                            y = it[1].toInt().dec()
                        } catch (e: Exception) {
                            throw InputException("Coordinates must in [1..$size]")
                        }

                        if (it[2] !in listOf("free", "mine")) {
                            throw InputException("acion must be free or mine")
                        }
                        action = it[2]
                    }

                if (action == "mine") {
                    mineField[y][x].markMine()
                } else {
                    if (!populated) {
                        populateWithMines(mineField[y][x])
                    }
                    mineField[y][x].explore()
                }
                printMineField()
            } catch (exception: InputException) {
                println(exception.message)
            } catch (e: MarkException) {
                println(e.message)
            } catch (exception: StepOnAMineException) {
                mineField.flatten().forEach { it.explored = true }
                printMineField()
                println(exception.message)
                exitProcess(0)
            }
            val correctlyMarked = mineField.flatten().count() { it.marked && it.mined }
        } while (correctlyMarked < numberOfMines)
        println("Congratulations! You found all the mines!")
        exitProcess(0)
    }
}

fun main() {
    print("How many mines do you want on the field? ")
    val nbMines = readln().toIntOrNull() ?: exitProcess(666)
    val mineSweeper = MineSweeper(9, nbMines)
    mineSweeper.play()
}
