package sample.app

import sample.models.Game
import sample.api.Api
import kotlin.js.*
import react.*
import kotlinx.html.js.onClickFunction
import kotlinx.html.style
import org.w3c.dom.WebSocket
import react.dom.*
import sample.api.WsClient

val store = Store()
val wsClient = WsClient()

class App : RComponent<RProps, RState>() {
    override fun componentDidMount() {
        store.init()
        store.onChange { forceUpdate() }
        wsClient.start({ store.processMove(it) }, { store.loadGames() })
    }

    override fun RBuilder.render() {
        store.state.user?.let {
            div {
                +"Привет, ${store.state.user!!.color} ${store.state.user!!.symbol}!"
            }
        }
        div("Game-list") {
            store.state.games.mapIndexed() { i, item ->
                game(game=item, gameId=i)
            }
        }
        button {
            +"new game"
            attrs.onClickFunction = { Api.createGame() }
        }
    }
}

fun RBuilder.game(game: Game, gameId: Int) {
    console.log(game)
    div("Game-field") {
        game.field.mapIndexed { row, cells ->
            cells.mapIndexed { col, cell ->
                span("Game-field-cell") {
                    cell?.let {
                        attrs.jsStyle { color = cell.color }
                    }
                    attrs.onClickFunction = { store.move(Api.MovePayload(gameId.toString(), row, col)) }
                    cell?.let {
                        span("Game-field-symbol fa fa-${cell.symbol}") {}
                    }
                }
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}
