package sample.models

import kotlinx.serialization.Serializable

@Serializable
class StateSnapshot(val games: List<Game>, val revision: Int)
