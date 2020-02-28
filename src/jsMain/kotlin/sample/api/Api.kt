package sample.api

import kotlinx.serialization.Serializable
import sample.models.*
import kotlinx.serialization.json.*
import sample.events.Event
import kotlin.browser.window
import kotlin.js.Promise

@Serializable
private data class AuthRes(val user: User, val token: String)

object Api {
    private val client = HttpClient(onAuthError = {
        Token.token = null
        window.location.reload()
    })
    private val json = Json(JsonConfiguration.Stable)
    private fun register(): Promise<User> = client.post("/auth").then {
        val data = json.parse(AuthRes.serializer(), it)
        Token.token = data.token
        data.user
    }
    private fun loadUser() = client.get("/user").then { json.parse(User.serializer(), it) }

    fun ensureUser() =
        if (Token.hasToken) loadUser() else register()
    fun loadState() = client.get("/state").then {
        json.parse(StateSnapshot.serializer(), it)
    }
    fun createGame() = client.post("/games")
    fun move(data: AnonymousMove) = client.post("/move", data)
    fun eventRange(from: Int, to: Int) = client.get("/events/range/$from/$to").then {res ->
        JSON.parse<Array<String>>(res).asList().map { json.parse(Event.serializer(), it) }
    }
}