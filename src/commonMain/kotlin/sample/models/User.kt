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
data class User(val id: Int, val symbol: SYMBOLS, val color: COLORS)
