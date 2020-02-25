package sample.utils

@JsModule("reconnecting-websocket")
external object Reconnecting {
    // NOTE: @JsName should work on top-level external class, but it doesn't
    @JsName("default")
    class WebSocket(url: String): org.w3c.dom.WebSocket
}