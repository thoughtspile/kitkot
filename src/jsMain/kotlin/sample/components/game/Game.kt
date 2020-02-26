package sample.components.game

import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*
import sample.components.userIcon.userIcon
import sample.models.*
import sample.utils.iconClass
import sample.utils.pastelColor

interface GameProps : RProps {
    var game: Game
    var isMini: Boolean
    var onMove: ((move: AnonymousMove) -> Unit)?
    var onClick: (() -> Unit)?
    var user: User?
}

class GameView : RPureComponent<GameProps, RState>() {
    override fun RBuilder.render() {
        val modeClass = if (props.isMini) "Field-mini" else "Field-full"
        val isInteractive = props.onMove != null

        div("Game ${"Game-finished".takeIf { props.game.isFinished } ?: ""}") {
            attrs.onClickFunction = { props.onClick?.invoke() }
            table("Field $modeClass") {
                tbody {
                    props.game.field.mapIndexed { row, cells ->
                        tr {
                            cells.mapIndexed { col, cell ->
                                td("Field-cell") {
                                    cell?.let {
                                        attrs.jsStyle { color = it.pastelColor() }
                                    }
                                    attrs.onClickFunction = { props.onMove?.invoke(AnonymousMove(props.game.id, row, col)) }
                                    if (cell != null)
                                        userIcon(cell, "Field-cell-symbol")
                                    else if (isInteractive)
                                        userIcon(props.user, "Field-cell-ghost")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
