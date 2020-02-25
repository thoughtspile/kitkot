package sample.app

import sample.models.*
import sample.api.Api
import react.*
import kotlinx.html.js.onClickFunction
import react.dom.*
import react.redux.provider
import react.redux.rConnect
import sample.api.WsClient
import org.w3c.dom.events.Event
import sample.utils.*

val store = StateManager()
val wsClient = WsClient()

class App : RComponent<AppProps, RState>() {
    override fun componentDidMount() {
        store.init()
        wsClient.start({ store.processMove(it) }, { store.addGame(it) })
    }

    override fun RBuilder.render() {
        val (ownGames, otherGames) = props.games
            .sortedByDescending { it.createdAt }
            .partition { it.players.contains(props.user) || it.createdBy == props.user }
        header(props.user)
        div("Game-list Game-list-scroller") {
            myGamesControl(onCreate = { Api.createGame() }, user = props.user)
            ownGames.map { game(game = it, key = it.id, isMini = true) }
        }
        div("Game-list Game-list-grid") {
            otherGames.map { game(game = it, key = it.id, isMini = true) }
        }
    }
}

fun RBuilder.myGamesControl(user: User?, onCreate: (e: Event) -> Unit) {
    div("Game Game-fake") {
        attrs.onClickFunction = onCreate
        attrs.jsStyle {
            color = user?.pastelColor()
        }
        div { +"Your games" }
        i("fa fa-2x fa-plus") {}
    }
}

fun RBuilder.game(game: Game, isMini: Boolean, key: String = "") {
    div("Game ${"Game-finished".takeIf { game.isFinished } ?: ""}") {
        attrs.key = key
        table("Field ${ if (isMini) "Field-mini" else "" }") {
            tbody {
                game.field.mapIndexed { row, cells ->
                    tr {
                        cells.mapIndexed { col, cell ->
                            td("Field-cell") {
                                cell?.let {
                                    attrs.jsStyle { color = it.pastelColor() }
                                }
                                attrs.onClickFunction = { store.move(Api.MovePayload(game.id, row, col)) }
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
