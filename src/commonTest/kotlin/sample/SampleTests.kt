package sample

import sample.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class GameTests {
    @Test
    fun detectsWin() {
        val winner = createUser(0)
        val loser = createUser(1)
        var game = createGame(0)

        val script = listOf(
            Move(winner, 0, 0, 0),
            Move(loser, 8, 0, 0),
            Move(winner, 1, 1, 0),
            Move(loser, 8, 2, 0),
            Move(winner, 2, 2, 0),
            Move(loser, 8, 3, 0),
            Move(winner, 3, 3, 0),
            Move(loser, 8, 4, 0),
            Move(winner, 4, 4, 0)
        )
        script.forEach {
            game = game.processMove(it)
        }

        assertTrue(game.isFinished)
        assertEquals(game.winner, winner)
        assertEquals(
            game.winningStreak,
            script.filter { it.user == winner } .map { it.x to it.y } .toSet())
    }

    @Test
    fun detectsDraw() {
        var game = createGame(0)

        (0 until game.fieldSize).forEach {row ->
            (0 until game.fieldSize).forEach { col ->
                game = game.processMove(Move(createUser(row * game.fieldSize + col), row, col, 0))
            }
        }

        assertTrue(game.isFinished)
        assertEquals(game.winner, null)
        assertEquals(game.winningStreak, null)
    }

    @Test
    fun noDoubleMove() {
        val u = createUser(0)
        val game = createGame(0)
            .processMove(Move(u, 0, 0, 0))

        assertFails {
            game.processMove(Move(u, 1, 5, 0))
        }
    }

    @Test
    fun noMoveReplace() {
        val game = createGame(0)
            .processMove(Move(createUser(0), 0, 0, 0))

        assertFails {
            game.processMove(Move(createUser(10), 0, 0, 0))
        }
    }

    @Test
    fun noMoveOutside() {
        assertFails {
            createGame(0).processMove(Move(createUser(10), 100, 100, 0))
        }
    }
}

fun createUser(id: Int) = User(id, SYMBOLS.Crow, COLORS.Red)
fun createGame(id: Int = 0) = Game(id, createUser(0), "")
