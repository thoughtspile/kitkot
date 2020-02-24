package sample.api

import org.w3c.dom.WebSocket
import sample.events.Event
import sample.models.*
import kotlinx.serialization.json.*

class WsClient {
    private val json = Json(JsonConfiguration.Stable)
    private val webSocket = WebSocket("ws://localhost:8080/events")
    fun start(onMove: (Move) -> Unit, onNewGame: (Game) -> Unit) {
        webSocket.onmessage = {
            val data = json.parse(Event.serializer(), it.data.toString())
            when (data) {
                is Event.MoveEvent -> onMove(data.move)
                is Event.NewGameEvent -> onNewGame(data.game)
            }
        }
    }
}