package sample.app

import sample.models.Game
import sample.api.Api
import kotlin.js.*
import react.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.WebSocket
import react.dom.*

val store = Store()

class App : RComponent<RProps, RState>() {
    override fun componentDidMount() {
        store.init()
        store.onChange { forceUpdate() }
        val webSocket = WebSocket("ws://localhost:8080/events")
        webSocket.onmessage = {
            store.loadGames()
        }
    }

    override fun RBuilder.render() {
        store.state.user?.let {
            div {
                +"Привет, ${store.state.user!!.color} ${store.state.user!!.symbol}!"
            }
        }
        div {
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
    div {
        arrayOf(game.field).mapIndexed { row, cells ->
            div {
                cells.mapIndexed { col, cell ->
                    span {
                        attrs.onClickFunction = { store.move(Api.MovePayload(gameId.toString(), row, col)) }
                        +(if (cell == null) "." else "x")
                    }
                }
            }
        }
    }
}

fun RBuilder.app() = child(App::class) {}
