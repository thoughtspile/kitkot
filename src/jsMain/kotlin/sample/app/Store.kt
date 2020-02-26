package sample.app

import sample.events.Event
import sample.models.*
import sample.api.Api
import redux.*
import kotlin.js.Promise

data class AppState(
    var games: List<Game> = emptyList(),
    var user: User? = null,
    var revision: Int = 0,
    var isOnline: Boolean = false
) {
    fun update(builder: AppState.() -> Unit) = this.copy().let {
        it.builder()
        it
    }
}

class Actions: RAction {
    class SetUser(val user: User): RAction
    class ApplySnapshot(val snapshot: StateSnapshot): RAction
    class AddGame(val game: Game): RAction
    class Move(val move: sample.models.Move): RAction
    class SetRevision(val revision: Int): RAction
    class ToggleOnline(): RAction
    class Error(val message: String?): RAction
}

class StateManager(val onError: (message: String?) -> Unit) {
    private fun notifyOnError() =
        applyMiddleware<AppState, RAction, WrapperAction, RAction, WrapperAction>(
            { _ ->
                { next ->
                    { action ->
                        if (action is Actions.Error) onError(action.message)
                        next(action)
                    }
                }
            }
        )
    val store = createStore(
        ::reduce,
        AppState(),
        compose(notifyOnError(), rEnhancer()))
    private fun dispatchAsync(p: Promise<RAction>) = p
        .then { it?.let { store.dispatch(it) } }
        .catch { store.dispatch(Actions.Error(it.message)) }
    private fun dispatchAsync(p: Promise<Unit>) = p
        .catch { store.dispatch(Actions.Error(it.message)) }

    fun init() = dispatchAsync(Api.register().then { Actions.SetUser(it) }).then {
        dispatchAsync(Api.loadState().then { Actions.ApplySnapshot(it) })
    }
    fun move(move: AnonymousMove) = dispatchAsync(Api.move(move).then {})
    fun processMove(move: Move) = store.dispatch(Actions.Move(move))
    fun addGame(game: Game) = store.dispatch(Actions.AddGame(game))
    fun setRevision(revision: Int) = store.dispatch(Actions.SetRevision(revision))
    fun toggleOnline() = store.dispatch(Actions.ToggleOnline())

    private fun syncChanges(currentRevision: Int) {
        var storeRevision = store.getState().revision
        if (currentRevision > storeRevision) {
            Api.eventRange(storeRevision + 1, currentRevision).then { changes ->
                changes.forEach { processEvent(it) }
            }
        }
    }

    fun processEvent(e: Event) {
        if (e is Event.ConnectEvent) {
            syncChanges(e.order)
            return
        }

        when(e) {
            is Event.NewGameEvent -> addGame(e.game)
            is Event.MoveEvent -> processMove(e.move)
        }
        setRevision(e.order)
    }
}

private fun reduce(state: AppState, action: RAction) = when (action) {
    is Actions.SetUser -> state.update { user = action.user }
    is Actions.ApplySnapshot -> state.update {
        games = action.snapshot.games
        revision = action.snapshot.revision
        // Only start realtime once initial state pulled
        isOnline = true
    }
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
    is Actions.SetRevision -> state.update { revision = action.revision }
    is Actions.ToggleOnline -> state.update { isOnline = !isOnline }
    else -> state
}