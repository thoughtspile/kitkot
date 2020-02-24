package sample.events

import sample.models.*
import kotlinx.serialization.*

enum class EventType { MOVE, NEW_GAME }

@Serializable
sealed class Event(val tag: EventType) {
    @Serializable
    data class MoveEvent(val move: Move) : Event(EventType.MOVE)

    @Serializable
    data class NewGameEvent(val game: Game) : Event(EventType.NEW_GAME)
}