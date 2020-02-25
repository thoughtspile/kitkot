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
private fun isoNow(): String = dateFormatter.format(Instant.now())

object Storage {
    val games = AutoIncrementStore<Game>("games")
    private val users = AutoIncrementStore<User>("users")
    private val events = AutoIncrementStore<Event>("events")
    val revision: Int
        get() = events.items.size

    @Synchronized
    private suspend fun emit(buildEvent: (key: Int) -> Event) {
        eventChannel.send(events.insert { buildEvent(it) })
    }

    suspend fun startGame(user: User) {
        val game = games.insert { Game(it, user, isoNow()) }
        emit { Event.NewGameEvent(game, it) }
    }

    @Synchronized
    suspend fun processMove(move: Move) {
        games.update(move.gameId) { processMove(move) }
        emit { Event.MoveEvent(move, it) }
    }

    fun createUser() = users.insert { index ->
        User(
            index,
            SYMBOLS.values().let { it[index % it.size] },
            COLORS.values().let { it[index % it.size] })
    }

    fun getUser(uid: Int) = users.items[uid]

    fun eventRange(from: Int, to: Int) =
        events.items.subList(maxOf(from, 0), minOf(to, events.items.size - 1))
}
