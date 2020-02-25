package sample.models

import kotlinx.serialization.Serializable

enum class COLORS {
    Red,
    Orange,
    Yellow,
    Green,
    Cyan,
    Blue,
    Purple
}
enum class SYMBOLS {
    Paw,
    Hippo,
    Dog,
    Spider,
    KiwiBird,
    Horse,
    Frog,
    Fish,
    Dragon,
    Dove,
    Crow,
    Cat
}

@Serializable
data class User(val id: String, val symbol: SYMBOLS, val color: COLORS) {
    companion object {
        private val users = mutableMapOf<String, User>()

        fun create(): User {
            val index = users.size
            val uid = index.toString()
            val user = User(
                uid,
                SYMBOLS.values().let { it[index % it.size] },
                COLORS.values().let { it[index % it.size] }
            )
            users[uid] = user
            return user
        }

        fun get(uid: String) = users[uid]
    }
}