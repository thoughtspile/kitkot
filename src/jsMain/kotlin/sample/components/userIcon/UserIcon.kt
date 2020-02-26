package sample.components.userIcon

import react.RBuilder
import react.dom.jsStyle
import react.dom.span
import sample.models.User
import sample.utils.iconClass
import sample.utils.pastelColor

fun RBuilder.userIcon(user: User?, classes: String = "") = user?.let {
    span("$classes ${it.iconClass()}") {
        attrs.jsStyle {
            color = it.pastelColor()
        }
    }
}