package sample.api

import kotlinx.serialization.Serializable
import sample.models.*
import kotlinx.serialization.json.*
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
    fun loadState() = client.get("/state").then {
        json.parse(StateSnapshot.serializer(), it)
    }
    fun createGame() = client.post("/games")
    fun move(data: AnonymousMove) = client.post("/move", data)
    fun eventRange(from: Int, to: Int) = client.get("/events/range/$from/$to").then {res ->
        JSON.parse<Array<String>>(res).asList().map { json.parse(Event.serializer(), it) }
    }
}