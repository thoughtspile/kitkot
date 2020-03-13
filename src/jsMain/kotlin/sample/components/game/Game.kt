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
        val isFinished = props.game.isFinished
        val awaitMove = !isFinished && props.game.lastPlayer == props.user
        val isInteractive = props.onMove != null && !isFinished && !awaitMove
        fun isWinningCell(x: Int, y: Int) = isFinished && props.game.winningStreak?.contains(x to y) ?: false

        div("Game ${"Game-finished".takeIf { isFinished } ?: ""}") {
            attrs.onClickFunction = { props.onClick?.invoke() }
            table("Field $modeClass") {
                tbody {
                    (0 until props.game.fieldSize).map { row ->
                        tr {
                            (0 until props.game.fieldSize).map { col ->
                                val cell = props.game.field[row to col]
                                td("Field-cell ${if (isWinningCell(row, col)) "Field-cell-win" else ""}") {
                                    cell?.let {
                                        attrs.jsStyle { color = it.pastelColor() }
                                    }
                                    if (isInteractive)
                                        attrs.onClickFunction =
                                            { props.onMove?.invoke(AnonymousMove(props.game.id, row, col)) }
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
            if (props.onMove == null) {
                if (awaitMove) {
                    div("Game-waitIcon fa fa-clock") {}
                }
                div("Game-overlay") {
                    div { +"Players: ${props.game.players.size}" }
                    if (isFinished) {
                        props.game.winner?.let {
                            div {
                                +"Winner: "
                                userIcon(it)
                            }
                        } ?: div { +"Draw" }
                    }
                }
            }
        }
    }
}
