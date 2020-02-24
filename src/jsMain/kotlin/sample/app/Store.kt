package sample.app

import sample.models.Game
import sample.api.Api
import kotlin.properties.Delegates

interface User {
    val color: String
    val symbol: String
}

data class State(var games: List<Game>, var user: User? = null)

class Store {
    private var observers = listOf<(State) -> Unit>()
    fun onChange(cb: (State) -> Unit) {
        observers += cb
    }
    private fun emitChange() = observers.forEach { it(state) }

    var state: State by Delegates.observable(State(games = listOf())) { _, _, _ ->
        emitChange()
    }
    private fun setState(updater: State.() -> Unit) {
        val nextState = this.state.copy()
        nextState.updater()
        state = nextState
    }

    fun init() =
        Api.register().then { data ->
            setState { user = data }
        } .then {
            loadGames()
        }

    fun loadGames() =
        Api.loadGames().then { data -> setState { games = data } }

    fun move(move: Api.MovePayload) =
        Api.move(move)

    fun processMove(move: MoveEvent) =
        setState {
            games = games.mapIndexed { i, game ->
                if (i.toString() == move.gameId) applyMove(game, move) else game
            }
        }
}

interface MoveEvent {
    val x: Int
    val y: Int
    val user: User
    val gameId: String
}

fun applyMove(game: Game, move: MoveEvent) = game
