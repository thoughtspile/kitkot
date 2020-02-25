package sample.storage

import sample.events.Event
import sample.models.*
import sample.events.eventChannel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.collections.HashMap

private val dateFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
    .withZone(ZoneId.systemDefault())
private fun isoNow() = dateFormatter.format(Instant.now())
object Storage {
    val games = HashMap<String, Game>()

    @Synchronized
    suspend fun startGame(user: User) {
        val key = games.size.toString()
        val game = Game(key, user, isoNow())
        games[key] = game
        eventChannel.send(Event.NewGameEvent(game))
    }

    @Synchronized
    suspend fun processMove(gameId: String, move: Move) {
        if (!games.containsKey(gameId)) {
            throw Exception("invalid game ID")
        }
        games[gameId]!!.processMove(move)
        eventChannel.send(Event.MoveEvent(move))
    }
}
