package sample.events

import sample.models.*
import kotlinx.serialization.*

@Serializable
sealed class Event {
    abstract val order: Int

    @Serializable
    data class MoveEvent(val move: Move, override val order: Int) : Event()

    @Serializable
    data class NewGameEvent(val game: Game, override val order: Int) : Event()
}