package sample.api

import sample.events.Event
import kotlinx.serialization.json.*
import sample.utils.Reconnecting

class WsClient(val processEvent: (e: Event) -> Unit) {
    private val json = Json(JsonConfiguration.Stable)
    private var webSocket: Reconnecting.WebSocket? = null

    fun stop() {
        webSocket?.onmessage = {}
        webSocket?.close()
        webSocket = null
    }

    fun start() {
        if (webSocket != null) return
        webSocket = Reconnecting.WebSocket("ws://localhost:8080/events").also {
            it.onmessage = {
                processEvent(json.parse(Event.serializer(), it.data.toString()))
            }
        }
    }
}