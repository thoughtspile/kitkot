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
import sample.components.topBar
import kotlin.browser.window
import sample.components.reactToastify.*
import sample.components.game.gameView

val store = createStore { Toast.Show.error(it ?: "Unknown error") }
val wsClient = WsClient { store.dispatch(Actions.processEvent(it)) }

class App : RComponent<AppProps, RState>() {
    override fun componentDidMount() {
        store.dispatch(Actions.init())
        store.subscribe {
            val state = store.getState()
            if (state.isOnline) wsClient.start() else wsClient.stop()
        }
        window.asDynamic().toast = Toast
    }

    override fun RBuilder.render() {
        val (ownGames, otherGames) = props.games
            .sortedByDescending { it.createdAt }
            .partition { it.players.contains(props.user) || it.createdBy == props.user }
        topBar(
            user = props.user,
            isOnline = props.isOnline,
            toggleOnline = { store.dispatch(Actions.ToggleOnline()) })
        div("Game-list Game-list-scroller") {
            myGamesControl(onCreate = { Api.createGame() }, user = props.user)
            ownGames.map { gameView(it, key = it.id.toString(), isMini = true) }
        }
        div("Game-list Game-list-grid") {
            otherGames.map { gameView(it, key = it.id.toString(), isMini = true) }
        }
        toastContainer()
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

interface AppProps: RProps {
    var user: User?
    var games: List<Game>
    var isOnline: Boolean
}
val app: RClass<RProps> = rConnect<AppState, RProps, AppProps>({ state, _ ->
    user = state.user
    games = state.games
    isOnline = state.isOnline
})(App::class.rClass)


fun RBuilder.app() = provider(store) { app {} }
