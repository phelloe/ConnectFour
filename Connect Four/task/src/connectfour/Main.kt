package connectfour

import java.lang.NumberFormatException
import kotlin.system.exitProcess

const val defaultRows = 6
const val defaultColumns = 7
const val minRows = 5
const val minColumns = 5
const val maxRows = 9
const val maxColumns = 9
const val four = 4

const val readBoardInstructions = """Set the board dimensions (Rows x Columns)
    |Press Enter for default (6 x 7)"""
const val greeting = """Connect Four
    |First player's name:"""
const val multipleGameInstructions = """Do you want to play single or multiple games?
    |For a single game, input 1 or press Enter
    |Input a number of games:"""

val regex = Regex("\\s*(\\d+)\\s*[xX]\\s*(\\d+)\\s*")

fun main() {
    println(greeting.trimMargin())
    val firstPlayer = readln()
    var firstPlayerScore = 0
    println("Second player's name:")
    val secondPlayer = readln()
    var secondPlayerScore = 0

    val (rows, columns) = readBoardDimensions()
    println(multipleGameInstructions.trimMargin())
    val numberOfGames = readNumberOfGames()
    val singleGame = numberOfGames == 1
    println("$firstPlayer VS $secondPlayer")
    println("$rows X $columns board")
    if (singleGame) {
        println("Single game")
    } else println("Total $numberOfGames games")

    var startingPlayer = 0
    for (i in 1..numberOfGames) {
        if (!singleGame) {
            println("Game #$i")
        }
        val gameBoard = GameBoard(columns, rows)
        val result = gameLoop(gameBoard, firstPlayer, secondPlayer, startingPlayer)
        firstPlayerScore += result.first
        secondPlayerScore += result.second
        println("Score")
        println("$firstPlayer: $firstPlayerScore $secondPlayer: $secondPlayerScore")
        startingPlayer = (startingPlayer + 1) % 2
    }
    println("Game over!")
}

fun readNumberOfGames(): Int {
    val input = readln()
    return when {
        input == "" -> return 1
        (input.toIntOrNull() ?: 0) > 0 -> return input.toInt()
        else -> {
            println("Invalid input")
            println(multipleGameInstructions.trimMargin())
            readNumberOfGames()
        }
    }
}

fun gameLoop(gameBoard: GameBoard, firstPlayer: String, secondPlayer: String, startingPlayer: Int): Pair<Int, Int> {
    var currentPlayer = startingPlayer
    var gameRunning = true
    var firstPlayerScore = 0
    var secondPlayerScore = 0
    gameBoard.printBoard()
    while (gameRunning) {
        val rc = readGameInput(currentPlayer, firstPlayer, secondPlayer, gameBoard)
        if (rc == 0) {
            gameRunning = false
        } else {
            if (currentPlayer == 0) {
                gameBoard.put(rc - 1, GameBoard.Field.PLAYER_A)
            } else {
                gameBoard.put(rc - 1, GameBoard.Field.PLAYER_B)
            }
            gameBoard.printBoard()
        }
        when (gameBoard.checkWinCondition()) {
            WinCondition.PLAYER_A_WON -> {
                gameRunning = false
                println("Player $firstPlayer won")
                firstPlayerScore = 2
            }
            WinCondition.PLAYER_B_WON -> {
                gameRunning = false
                println("Player $secondPlayer won")
                secondPlayerScore = 2
            }
            WinCondition.DRAW -> {
                gameRunning = false
                println("It is a draw")
                firstPlayerScore = 1
                secondPlayerScore = 1
            }
            else -> currentPlayer = (currentPlayer + 1) % 2
        }
    }
    return Pair(firstPlayerScore, secondPlayerScore)
}

fun readGameInput(currentPlayer: Int, firstPlayer: String, secondPlayer: String, gameBoard: GameBoard): Int {
    if (currentPlayer == 0) {
        println("$firstPlayer's turn:")
    } else println("$secondPlayer's turn:")
    val input = readln()
    return if (input == "end") {
        println("Game over!")
        exitProcess(0)
    } else {
        try {
            if (input.toInt() in 1..gameBoard.columns) {
                if (gameBoard.isColumnFull(input.toInt() - 1)) {
                    println("Column $input is full")
                    readGameInput(currentPlayer, firstPlayer, secondPlayer, gameBoard)
                } else {
                    input.toInt()
                }
            } else {
                println("The column number is out of range (1 - ${gameBoard.columns})")
                readGameInput(currentPlayer, firstPlayer, secondPlayer, gameBoard)
            }
        } catch (e: NumberFormatException) {
            println("Incorrect column number")
            readGameInput(currentPlayer, firstPlayer, secondPlayer, gameBoard)
        }
    }
}

fun readBoardDimensions(): List<Int> {
    val s = repeatedRead()
    if (s == "") return listOf(defaultRows, defaultColumns)

    val (rows, columns) = regex.find(s)!!.destructured.toList().map { it.toInt() }
    return if (rows in minRows..maxRows && columns in minColumns..maxColumns) listOf(rows, columns)
    else {
        println(
            if (rows !in minRows..maxRows) {
                "Board rows should be from $minRows to $maxRows"
            } else "Board columns should be from $minColumns to $maxColumns"
        )
        readBoardDimensions()
    }
}

fun repeatedRead(): String {
    println(readBoardInstructions.trimMargin())
    val s = readln()
    return if (s.matches(regex) || s == "") s
    else {
        println("Invalid input")
        repeatedRead()
    }
}

enum class WinCondition {
    NONE, PLAYER_A_WON, PLAYER_B_WON, DRAW
}

class GameBoard(val columns: Int, private val rows: Int) {
    private val fields = MutableList(columns) { MutableList(rows) { Field.EMPTY } }

    enum class Field(val icon: String) {
        EMPTY(" "), PLAYER_A("o"), PLAYER_B("*");
    }

    fun put(column: Int, field: Field): Int {
        val row = fields[column].indexOfLast { it == Field.EMPTY }
        if (!isColumnFull(column)) {
            fields[column][row] = field
        }
        return row
    }

    fun isColumnFull(column: Int) = rows - getDiscsInColumn(column) <= 0

    fun checkWinCondition(): WinCondition {
        return when {
            (0 until columns).all { isColumnFull(it) } -> WinCondition.DRAW

            checkWinConditionForPlayer(Field.PLAYER_A) -> WinCondition.PLAYER_A_WON
            checkWinConditionForPlayer(Field.PLAYER_B) -> WinCondition.PLAYER_B_WON

            else -> WinCondition.NONE
        }
    }

    fun printBoard() {
        (1..columns).forEach { print(" $it") }
        println()
        for (i in 1..rows) {
            print("║")
            for (j in 1..columns) {
                print(fields[j - 1][i - 1].icon)
                print("║")
            }
            println()
        }
        print("╚")
        for (i in 1 until columns) {
            print("═╩")
        }
        println("═╝")
    }

    private fun checkWinConditionForPlayer(player: Field) =
        (0 until rows).any { rowIndex -> (0..columns - four).any { columnIndex -> (0 until four).all { fields[columnIndex + it][rowIndex] == player } } } ||
                //check vertically
                (0 until columns).any { columnIndex -> (0..rows - four).any { rowIndex -> (0 until four).all { fields[columnIndex][rowIndex + it] == player } } } ||
                //diagonally right-down
                (0..rows - four).any { rowIndex -> (0..columns - four).any { columnIndex -> (0 until four).all { fields[columnIndex + it][rowIndex + it] == player } } } ||
                //diagonally left-up
                (0..rows - four).any { rowIndex -> (0..columns - four).any { columnIndex -> (0 until four).all { fields[columnIndex + four - it - 1][rowIndex + it] == player } } }

    private fun getDiscsInColumn(column: Int) = fields[column].count { it == Field.PLAYER_A || it == Field.PLAYER_B }
}