package sample.app

import sample.models.*
import sample.api.Api
import redux.*
import kotlin.js.Promise

data class AppState(
    var games: List<Game> = emptyList(),
    var user: User? = null
) {
    fun update(builder: AppState.() -> Unit): AppState {
        val next = this.copy()
        next.builder()
        return next
    }
}

class Actions: RAction {
    class SetUser(val user: User): RAction
    class SetGames(val games: List<Game>): RAction
    class AddGame(val game: Game): RAction
    class Move(val move: sample.models.Move): RAction
}

class StateManager {
    val store = createStore(::reduce, AppState(), rEnhancer())
    private fun dispatchAsync(p: Promise<RAction>) = p.then { store.dispatch(it) }

    fun init() = dispatchAsync(Api.register().then { Actions.SetUser(it) }).then {
        dispatchAsync(Api.loadGames().then { Actions.SetGames(it) })
    }
    fun move(move: AnonymousMove) = Api.move(move)
    fun processMove(move: Move) = store.dispatch(Actions.Move(move))
    fun addGame(game: Game) = store.dispatch(Actions.AddGame(game))
}

private fun reduce(state: AppState, action: RAction) = when (action) {
    is Actions.SetUser -> state.update { user = action.user }
    is Actions.SetGames -> state.update { games = action.games }
    is Actions.Move -> state.update {
        games = games.map {
            if (it.id == action.move.gameId)
                try {
                    it.processMove(action.move)
                } catch (err: Exception) {}
            it
        }
    }
    is Actions.AddGame -> state.update {
        games += action.game
    }
    else -> state
}