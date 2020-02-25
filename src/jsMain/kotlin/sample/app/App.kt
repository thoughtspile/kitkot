package sample.app

import sample.models.*
import sample.api.Api
import react.*
import kotlinx.html.js.onClickFunction
import react.dom.*
import react.redux.provider
import react.redux.rConnect
import sample.api.WsClient
import sample.utils.*

val store = StateManager()
val wsClient = WsClient()

class App : RComponent<AppProps, RState>() {
    override fun componentDidMount() {
        store.init()
        wsClient.start({ store.processMove(it) }, { store.addGame(it) })
    }

    override fun RBuilder.render() {
        header(props.user)
        div("Game-list") {
            props.games.map { game(game=it, key=it.id) }
        }
        button {
            +"new game"
            attrs.onClickFunction = { Api.createGame() }
        }
    }
}

fun RBuilder.game(game: Game, key: String = "") {
    div("Game-field") {
        attrs.key = key
        game.field.mapIndexed { row, cells ->
            cells.mapIndexed { col, cell ->
                span("Game-field-cell") {
                    cell?.let {
                        attrs.jsStyle { color = it.pastelColor() }
                    }
                    attrs.onClickFunction = { store.move(Api.MovePayload(game.id, row, col)) }
                    cell?.let {
                        span("Game-field-symbol ${it.iconClass()}") {}
                    }
                }
            }
        }
    }
}

fun RBuilder.header(user: User?) {
    div("Header") {
        attrs.jsStyle {
            background = user?.pastelColor()
        }
        user?.let {
            +"Hello, ${it.color} ${it.symbol}!"
        }
    }
}

interface AppProps: RProps {
    var user: User?
    var games: List<Game>
}
val app: RClass<RProps> = rConnect<AppState, RProps, AppProps>({ state, _ ->
    user = state.user
    games = state.games
})(App::class.rClass)


fun RBuilder.app() = provider(store.store) { app {} }
