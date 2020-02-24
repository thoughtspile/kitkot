package sample.storage

import sample.events.Event
import sample.models.*
import sample.events.eventChannel

object Storage {
    val games = HashMap<String, Game>()

    @Synchronized
    suspend fun startGame() {
        val key = games.size.toString()
        val game = Game()
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
