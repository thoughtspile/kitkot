package sample.api

import org.w3c.dom.WebSocket
import sample.events.Event
import sample.models.*
import kotlinx.serialization.json.*

class WsClient {
    private val json = Json(JsonConfiguration.Stable)
    private val webSocket = WebSocket("ws://localhost:8080/events")
    private var isStarted = false

    fun start(onMove: (Move) -> Unit, onNewGame: (Game) -> Unit) {
        if (isStarted) {
            return
        }
        isStarted = true

        webSocket.onmessage = {
            when (val data = json.parse(Event.serializer(), it.data.toString())) {
                is Event.MoveEvent -> onMove(data.move)
                is Event.NewGameEvent -> onNewGame(data.game)
            }
        }
    }
}