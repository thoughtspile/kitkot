package sample.api

import kotlinx.serialization.Serializable
import sample.models.*
import kotlinx.serialization.json.*
import kotlinx.serialization.list
import sample.events.Event

@Serializable
private data class AuthRes(val user: User, val token: String)

object Api {
    private val client = HttpClient()
    private val json = Json(JsonConfiguration.Stable)
    fun register() = client.post("/auth").then {
        val data = json.parse(AuthRes.serializer(), it)
        client.token = data.token
        data.user
    }
    fun loadGames() = client.get("/games").then {
        json.parse(Game.serializer().list, it)
    }
    fun createGame() = client.post("/games")
    fun move(data: AnonymousMove) = client.post("/move", data)
    fun eventRange(from: Int, to: Int) = client.get("/events/range/$from/$to").then {res ->
        JSON.parse<Array<String>>(res).asList().map { json.parse(Event.serializer(), it) }
    }
}