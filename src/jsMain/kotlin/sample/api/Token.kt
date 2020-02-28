package sample.api

import kotlin.browser.localStorage

object Token {
    private const val key = "KITKOT_TOKEN"
    var token: String?
        get() = localStorage.getItem(key)
        set(value) = value?.let { localStorage.setItem(key, it) } ?: localStorage.removeItem(key)
    val hasToken: Boolean
        get() = token != null
}