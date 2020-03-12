package sample.utils

import sample.models.Game
import sample.models.Move


fun Game.processWithPending(move: Move): Game {
    val lastMove = moves.lastOrNull()
    if (lastMove != null && lastMove.isPending) {
        val base = popMove().processMove(move)
        return try { base.processMove(lastMove) } catch (e: Throwable) { base }
    }
    return processMove(move)
}

fun Game.cancelPending(): Game {
    if (moves.lastOrNull()?.isPending == true) return popMove()
    return this
}