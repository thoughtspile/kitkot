package sample.utils

import kotlinext.js.jsObject
import redux.*

interface RThunk : RAction {
    operator fun invoke(
        dispatch: (RAction) -> WrapperAction,
        getState: () -> Any
    ): WrapperAction
}


fun <S> rThunk() =
    applyMiddleware<S, RAction, WrapperAction, RAction, WrapperAction>(
        {store ->
            {next ->
                {action ->
                    if(action is RThunk)
                        action(store::dispatch) { store.getState() as Any }
                    else
                        next(action)
                }
            }
        }
    )

fun thunkify(thunk: ((RAction) -> WrapperAction) -> Unit) = object : RThunk {
    override fun invoke(dispatch: (RAction) -> WrapperAction, getState: () -> Any): WrapperAction {
        thunk(dispatch)
        return nullAction
    }
}

fun <S> thunkify(thunk: ((RAction) -> WrapperAction, () -> S) -> Unit) = object : RThunk {
    override fun invoke(dispatch: (RAction) -> WrapperAction, getState: () -> Any): WrapperAction {
        thunk(dispatch, { getState().unsafeCast<S>() })
        return nullAction
    }
}

val nullAction = jsObject<WrapperAction>{}