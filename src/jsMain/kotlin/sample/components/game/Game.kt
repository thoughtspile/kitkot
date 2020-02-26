package sample.components.game

import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*
import sample.models.AnonymousMove
import sample.models.Game
import sample.utils.iconClass
import sample.utils.pastelColor

interface GameProps : RProps {
    var game: Game
    var isMini: Boolean
    var onMove: ((move: AnonymousMove) -> Unit)?
    var onClick: (() -> Unit)?
}

class GameView : RComponent<GameProps, RState>() {
    override fun RBuilder.render() {
        div("Game ${"Game-finished".takeIf { props.game.isFinished } ?: ""}") {
            attrs.onClickFunction = { props.onClick?.invoke() }
            table("Field ${ if (props.isMini) "Field-mini" else "" }") {
                tbody {
                    props.game.field.mapIndexed { row, cells ->
                        tr {
                            cells.mapIndexed { col, cell ->
                                td("Field-cell") {
                                    cell?.let {
                                        attrs.jsStyle { color = it.pastelColor() }
                                    }
                                    attrs.onClickFunction = { props.onMove?.invoke(AnonymousMove(props.game.id, row, col)) }
                                    cell?.let {
                                        span("Field-cell-symbol ${it.iconClass()}") {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
