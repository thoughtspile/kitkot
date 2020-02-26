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
    var key: String
    var onMove: ((move: AnonymousMove) -> Unit)?
}

class GameView : RPureComponent<GameProps, RState>() {
    override fun RBuilder.render() {
        div("Game ${"Game-finished".takeIf { props.game.isFinished } ?: ""}") {
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

fun RBuilder.gameView(game: Game, key: String, isMini: Boolean = false) = child(GameView::class) {
    attrs.game = game
    attrs.key = key
    attrs.isMini = isMini
}