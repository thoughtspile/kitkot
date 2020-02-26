package sample.app

import sample.events.Event
import sample.models.*
import sample.api.Api
import redux.*
import sample.utils.RThunk
import kotlin.js.Promise
import sample.utils.rThunk
import sample.utils.thunkify

data class AppState(
    var games: List<Game> = emptyList(),
    var user: User? = null,
    var revision: Int = 0,
    var isOnline: Boolean = false,
    var focusedGame: Int? = null
) {
    fun update(builder: AppState.() -> Unit) = this.copy().let {
        it.builder()
        it
    }
}

object Actions: RAction {
    // Atomic actions
    class SetUser(val user: User): RAction
    class ApplySnapshot(val snapshot: StateSnapshot): RAction
    class AddGame(val game: Game): RAction
    class Move(val move: sample.models.Move): RAction
    class SetRevision(val revision: Int): RAction
    class ToggleOnline: RAction
    class Error(val message: String?): RAction
    class FocusGame(val id: Int?): RAction

    // Thunks
    fun init() = pThunkify { dispatch ->
        Api.register()
            .then { dispatch(SetUser(it)) }
            .then { Api.loadState() }
            .then { dispatch(ApplySnapshot(it)) }
    }

    fun move(move: AnonymousMove) = pThunkify { Api.move(move) }

    fun processEvent(e: Event): RThunk = thunkify { dispatch ->
        if (e is Event.ConnectEvent) {
            dispatch(syncChanges(e.order))
        } else {
            when(e) {
                is Event.NewGameEvent -> dispatch(AddGame(e.game))
                is Event.MoveEvent -> dispatch(Move(e.move))
            }
            dispatch(SetRevision(e.order))
        }
    }

    private fun syncChanges(currentRevision: Int) = thunkify<AppState> { dispatch, getState ->
        val storeRevision = getState().revision
        if (currentRevision > storeRevision) {
            Api.eventRange(storeRevision + 1, currentRevision).then { changes ->
                changes.forEach { dispatch(processEvent(it)) }
            }
        }
    }
}

fun createStore(onError: (message: String?) -> Unit) = createStore(
    ::reduce,
    AppState(),
    compose(rThunk(), notifyOnError<AppState>(onError), rEnhancer()))

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
                it.copy().processMove(action.move)
            else
                it
        }.toList()
    }
    is Actions.AddGame -> state.update {
        games += action.game
    }
    is Actions.SetRevision -> state.update { revision = action.revision }
    is Actions.ToggleOnline -> state.update { isOnline = !isOnline }
    is Actions.FocusGame -> state.update { focusedGame = action.id }
    else -> state
}

private fun pThunkify(thunk: ((RAction) -> WrapperAction) -> Promise<Any?>) = thunkify { dispatch ->
    thunk(dispatch).catch { dispatch(Actions.Error(it.message)) }
}

private fun <S> notifyOnError(onError: (message: String?) -> Unit) =
    applyMiddleware<S, RAction, WrapperAction, RAction, WrapperAction>({
        { next ->
            { action ->
                if (action is Actions.Error) onError(action.message)
                next(action)
            }
        }
    })