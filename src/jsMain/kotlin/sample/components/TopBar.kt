package sample.components

import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.dom.div
import react.dom.i
import react.dom.jsStyle
import react.dom.span
import sample.models.User
import sample.utils.pastelColor


fun RBuilder.topBar(user: User?, isOnline: Boolean, toggleOnline: (e: Event) -> Unit) {
    div("TopBar") {
        attrs.jsStyle {
            background = user?.pastelColor()
        }
        user?.let {
            span("TopBar-title") {
                +"Hello, ${it.color} ${it.symbol}!"
            }
        }
        onlineIndicator(isOnline, toggleOnline)
    }
}

fun RBuilder.onlineIndicator(isOnline: Boolean, toggleOnline: (e: Event) -> Unit) {
    span("TopBar-online") {
        attrs.onClickFunction = toggleOnline
        if (isOnline)
            i("fa fa-wifi") {}
        else
            i("fa fa-exclamation-triangle") {}
        +" "
        +(if (isOnline) " Disconnect" else "Reconnect")
    }
}