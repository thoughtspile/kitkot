package sample.utils

import sample.models.COLORS
import sample.models.User

fun User.pastelColor() = when (color) {
    COLORS.Red -> "#FF6961"
    COLORS.Blue -> "#AEC6CF"
    COLORS.Orange -> "#FFB347"
    COLORS.Yellow -> "#FFBF00"
    COLORS.Green -> "#77DD77"
    COLORS.Cyan -> "#7CB9E8"
    COLORS.Purple -> "#B39EB5"
}

fun User.iconClass(): String {
    val faName = symbol
        .toString()
        .split(Regex("(?=[A-Z])"))
        .filter { it.isNotEmpty() }
        .joinToString("-", transform = { it.toLowerCase() })
    return "fa fa-$faName"
}