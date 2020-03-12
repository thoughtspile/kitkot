package sample.app

import sample.events.Event
import sample.models.*
import sample.api.Api
import redux.*
import kotlin.js.Promise
import sample.utils.*

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
    class AddGame(val game: Game, val revision: Int): RAction
    class Move(val move: sample.models.Move, val revision: Int? = null): RAction
    class SetRevision(val revision: Int): RAction
    class ToggleOnline: RAction
    class Error(val message: String?): RAction
    class FocusGame(val id: Int?): RAction
    class CancelPending(val gameId: Int): RAction

    // Thunks
    fun init() = pThunkify { dispatch ->
        Api.ensureUser()
            .then { dispatch(SetUser(it)) }
            .then { Api.loadState() }
            .then { dispatch(ApplySnapshot(it)) }
    }

    fun move(move: AnonymousMove) = thunkify { dispatch, getState: () -> AppState ->
        dispatch(Move(sample.models.Move(getState().user!!, move.x, move.y, move.gameId, true)))
        Api.move(move).catch {
            dispatch(CancelPending(move.gameId))
            dispatch(Error(it.message))
        }
    }

    fun processEvent(e: Event): RThunk = thunkify { dispatch ->
        when(e) {
            is Event.NewGameEvent -> dispatch(AddGame(e.game, e.order))
            is Event.MoveEvent -> dispatch(Move(e.move, e.order))
            is Event.ConnectEvent -> dispatch(syncChanges(e.order))
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
                it.processWithPending(action.move)
            else
                it
        }.toList()
        revision = action.revision ?: revision
    }
    is Actions.AddGame -> state.update {
        games += action.game
        revision = action.revision
        if (state.user == action.game.createdBy) {
            focusedGame = action.game.id
        }
    }
    is Actions.CancelPending -> state.update {
        games = games.map {
            if (it.id == action.gameId) it.cancelPending() else it
        }
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