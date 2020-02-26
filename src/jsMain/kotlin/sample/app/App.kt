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
import sample.components.game.GameView
import sample.utils.*
import sample.components.topBar
import sample.components.reactToastify.*

val store = createStore { Toast.Show.error(it ?: "Unknown error") }
val wsClient = WsClient { store.dispatch(Actions.processEvent(it)) }

class App : RComponent<AppProps, RState>() {
    override fun componentDidMount() {
        store.dispatch(Actions.init())
        store.subscribe {
            val state = store.getState()
            if (state.isOnline) wsClient.start() else wsClient.stop()
        }
    }

    private fun RBuilder.gameThumb(game: Game) = child(GameView::class) {
        attrs.game = game
        attrs.isMini = true
        attrs.onClick = { store.dispatch(Actions.FocusGame(game.id)) }
    }

    override fun RBuilder.render() {
        val (ownGames, otherGames) = props.games
            .sortedByDescending { it.createdAt }
            .partition { it.players.contains(props.user) || it.createdBy == props.user }
        val joinableGames = otherGames.filter { !it.isFinished }
        fun joinRandom() = store.dispatch(Actions.FocusGame(joinableGames[joinableGames.indices.random()].id))

        topBar(
            user = props.user,
            isOnline = props.isOnline,
            toggleOnline = { store.dispatch(Actions.ToggleOnline()) })
        div("Game-list Game-list-scroller") {
            myGamesControl(onCreate = { Api.createGame() }, user = props.user)
            ownGames.map { gameThumb(it) }
        }
        props.focusedGame?.let { game ->
            div("App-gameScreen") {
                div ("App-gameScreen-back") {
                    attrs.onClickFunction = { store.dispatch(Actions.FocusGame(null)) }
                    i("fa fa-chevron-left") {}
                    +" All games"
                }
                div("App-gameScreen-main") {
                    child(GameView::class) {
                        attrs.game = game
                        attrs.isMini = false
                        attrs.onMove = { mv -> store.dispatch(Actions.move(mv)) }
                        attrs.user = props.user
                    }
                }
            }
        } ?: div("Game-list Game-list-grid") {
            div("Game Game-fake") {
                div { +"All games" }
                if (joinableGames.isNotEmpty()) {
                    attrs.onClickFunction = { joinRandom() }
                    div { +"Join random!" }
                }
            }
            otherGames.map { gameThumb(it) }
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
        i("fa fa-plus Game-fake-plus") {}
    }
}

interface AppProps: RProps {
    var user: User?
    var games: List<Game>
    var isOnline: Boolean
    var focusedGame: Game?
}
val app: RClass<RProps> = rConnect<AppState, RProps, AppProps>({ state, _ ->
    user = state.user
    games = state.games
    isOnline = state.isOnline
    focusedGame = state.games.find { it.id == state.focusedGame }
})(App::class.rClass)


fun RBuilder.app() = provider(store) { app {} }
