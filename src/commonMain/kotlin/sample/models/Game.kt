package sample.models

import kotlinx.serialization.Serializable
import sample.errors.IllegalMoveException
import kotlin.math.max

@Serializable
class AnonymousMove(val gameId: Int, val x: Int, val y: Int)

@Serializable
data class Move(val user: User, val x: Int, val y: Int, val gameId: Int)


// Data class trickery allows immutable copy() for react pure
@Serializable
data class Game (
    val id: Int,
    val createdBy: User,
    val createdAt: String,
    val field: Map<Pair<Int, Int>, User> = emptyMap(),
    val moves: List<Move> = listOf()
) {
    val fieldSize = 10
    private val streakSize = 5
    val lastPlayer: User?
        get() = moves.lastOrNull()?.user

    var isFinished = false
        private set
    var winner: User? = null
        private set
    var winningStreak: Set<Coord>? = null
        private set

    init {
        moves.lastOrNull()?.let(::checkFinished)
    }

    fun validateMove(move: Move) {
        if (isFinished) {
            throw IllegalMoveException("Cannot move in a finished game")
        }
        if (field.contains(move.x to move.y)) {
            throw IllegalMoveException("Cell already occupied")
        }
        if (lastPlayer == move.user) {
            throw IllegalMoveException("Cannot move twice in a row")
        }
    }

    fun processMove(move: Move): Game {
        validateMove(move)

        return this.copy(
            field = field + ((move.x to move.y) to move.user),
            moves = moves + move)
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
        val bestStreak = listOf(1 to 0, 0 to 1, 1 to 1, 1 to -1).map { (dx, dy) ->
            streak.map {
                x + dx * it to y + dy * it
            } .filter { (x, y) ->
                isLegalPos(x, y)
            }.fold(emptyList<Coord>() to emptyList<Coord>()) { (best, prev), pos ->
                val cur = if (field[pos] == user) prev + pos else emptyList()
                val nextBest = if (best.size > cur.size) best else cur
                nextBest to cur
            }.first
        }.maxBy { it.size }
        isFinished = (bestStreak?.size ?: 0) >= streakSize
        if (isFinished) {
            winner = user
            winningStreak = bestStreak?.toSet()
        }
    }

    private fun isLegalPos(x: Int, y: Int) = x >= 0 && y >= 0 && x < fieldSize && y < fieldSize
}

typealias Coord = Pair<Int, Int>
