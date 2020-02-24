package sample.events

import kotlinx.coroutines.channels.Channel

val eventChannel = Channel<Event>()