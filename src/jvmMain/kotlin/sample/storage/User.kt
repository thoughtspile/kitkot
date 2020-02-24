package sample.storage

import kotlinx.serialization.Serializable
import java.awt.Color

val COLORS = listOf(
    "red",
    "orange",
    "yellow",
    "green",
    "cyan",
    "blue",
    "magenta"
)
val SYMBOLS = listOf(
    "paw",
    "hippo",
    "dog",
    "spider",
    "kiwi-bird",
    "horse",
    "frog",
    "fish",
    "dragon",
    "dove",
    "crow",
    "cat"
)

@Serializable
data class User(val id: String, val symbol: String, val color: String) {
    companion object {
        private val users = mutableMapOf<String, User>()

        fun create(): User {
            val index = users.size
            val uid = index.toString()
            val user = User(uid, SYMBOLS[index % SYMBOLS.size], COLORS[index % COLORS.size])
            users[uid] = user
            return user
        }

        fun get(uid: String) = users[uid]
    }
}