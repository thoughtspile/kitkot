package sample.app

import sample.events.Event
import sample.models.*
import sample.api.Api
import redux.*
import kotlin.js.Promise

data class AppState(
    var games: List<Game> = emptyList(),
    var user: User? = null,
    var revision: Int = 0
) {
    fun update(builder: AppState.() -> Unit): AppState {
        val next = this.copy()
        next.builder()
        return next
    }
}

class Actions: RAction {
    class SetUser(val user: User): RAction
    class ApplySnapshot(val snapshot: StateSnapshot): RAction
    class AddGame(val game: Game): RAction
    class Move(val move: sample.models.Move): RAction
    class SetRevision(val revision: Int): RAction
}

class StateManager {
    val store = createStore(::reduce, AppState(), rEnhancer())
    private fun dispatchAsync(p: Promise<RAction>) = p.then { store.dispatch(it) }

    fun init() = dispatchAsync(Api.register().then { Actions.SetUser(it) }).then {
        dispatchAsync(Api.loadState().then { Actions.ApplySnapshot(it) })
    }
    fun move(move: AnonymousMove) = Api.move(move)
    fun processMove(move: Move) = store.dispatch(Actions.Move(move))
    fun addGame(game: Game) = store.dispatch(Actions.AddGame(game))
    fun setRevision(revision: Int) = store.dispatch(Actions.SetRevision(revision))
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
    else -> state
}