package sample.events

import kotlinx.coroutines.channels.BroadcastChannel

val eventChannel = BroadcastChannel<Event>(100)
