package sample.models

import kotlinx.serialization.Serializable
import sample.errors.IllegalMoveException
import kotlin.math.max

@Serializable
class AnonymousMove(val gameId: Int, val x: Int, val y: Int)

@Serializable
data class Move(val user: User, val x: Int, val y: Int, val gameId: Int)

// NOTE:
// private val fieldSize: Int = 10,
// val field: MutableList<MutableList<User?>> =
//    MutableList(fieldSize) { MutableList<User?>(fieldSize) { null } },
// Did not compile to JS properly
private const val fieldSize = 10

// Data class trickery allows immutable copy() for react pure
@Serializable
data class Game (
    val id: Int,
    val createdBy: User,
    val createdAt: String,
    val field: MutableList<MutableList<User?>> =
        MutableList(fieldSize) { MutableList<User?>(fieldSize) { null } },
    val moves: MutableList<Move> = mutableListOf()
) {
    private val streakSize = 5
    val lastPlayer: User?
        get() = moves.lastOrNull()?.user

    var isFinished = false
        private set
    var winner: User? = null
        private set

    fun validateMove(move: Move) {
        if (isFinished) {
            throw IllegalMoveException("Cannot move in a finished game")
        }
        if (field[move.x][move.y] != null) {
            throw IllegalMoveException("Cell already occupied")
        }
        if (lastPlayer == move.user) {
            throw IllegalMoveException("Cannot move twice in a row")
        }
    }

    fun processMove(move: Move): Game {
        validateMove(move)

        field[move.x][move.y] = move.user
        moves.add(move)

        checkFinished(move)

        return this
    }

    val players: Set<User>
        get() = moves.map { it.user }.toSet()

    private fun checkFinished(move: Move) {
        if (moves.size === fieldSize * fieldSize) {
            isFinished = true
            return
        }
        val (user, x, y) = move
        val streak = ((-streakSize + 1) until streakSize)
        isFinished = listOf(1 to 0, 0 to 1, 1 to 1, 1 to -1).any { (dx, dy) ->
            streak.map {
                x + dx * it to y + dy * it
            } .filter { (x, y) ->
                isLegalPos(x, y)
            }.fold(0 to 0) { (best, prev), (x, y) ->
                val cur = if (field[x][y] == user) prev + 1 else 0
                max(best, cur) to cur
            }.first >= streakSize
        }
        if (isFinished) {
            winner = user
        }
    }

    fun deepCopy() = this.copy(
        field = field.map { it.toMutableList() } .toMutableList(),
        moves = moves.toMutableList())

    private fun isLegalPos(x: Int, y: Int) = x >= 0 && y >= 0 && x < fieldSize && y < fieldSize
}
