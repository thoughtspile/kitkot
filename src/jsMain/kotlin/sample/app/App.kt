package sample.app

import sample.models.*
import sample.api.Api
import react.*
import kotlinx.html.js.onClickFunction
import react.dom.*
import react.redux.provider
import react.redux.rConnect
import sample.api.WsClient

val store = StateManager()
val wsClient = WsClient()

class App : RComponent<AppProps, RState>() {
    override fun componentDidMount() {
        store.init()
        wsClient.start({ store.processMove(it) }, { store.addGame(it) })
    }

    override fun RBuilder.render() {
        props.user?.let {
            div {
                +"Привет, ${props.user!!.color} ${props.user!!.symbol}!"
            }
        }
        div("Game-list") {
            props.games.map { game(game=it) }
        }
        button {
            +"new game"
            attrs.onClickFunction = { Api.createGame() }
        }
    }
}

fun RBuilder.game(game: Game) {
    div("Game-field") {
        attrs.key = game.id
        game.field.mapIndexed { row, cells ->
            cells.mapIndexed { col, cell ->
                span("Game-field-cell") {
                    cell?.let {
                        attrs.jsStyle { color = cell.color }
                    }
                    attrs.onClickFunction = { store.move(Api.MovePayload(game.id, row, col)) }
                    cell?.let {
                        span("Game-field-symbol fa fa-${cell.symbol}") {}
                    }
                }
            }
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
