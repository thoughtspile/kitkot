package sample.storage

import sample.events.Event
import sample.models.*
import sample.events.eventChannel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
    .withZone(ZoneId.systemDefault())
private fun isoNow() = dateFormatter.format(Instant.now())

object Storage {
    val games = AutoIncrementStore<Game>("games")
    private val users = AutoIncrementStore<User>("users")

    suspend fun startGame(user: User) {
        val game = games.insert { Game(it, user, isoNow()) }
        eventChannel.send(Event.NewGameEvent(game))
    }

    @Synchronized
    suspend fun processMove(move: Move) {
        games.update(move.gameId) { processMove(move) }
        eventChannel.send(Event.MoveEvent(move))
    }

    fun createUser() = users.insert { index ->
        User(
            index,
            SYMBOLS.values().let { it[index % it.size] },
            COLORS.values().let { it[index % it.size] })
    }

    fun getUser(uid: Int) = users.items[uid]
}
