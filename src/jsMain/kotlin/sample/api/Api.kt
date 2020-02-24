package sample.api

import sample.app.User
import kotlin.js.Promise
import sample.models.Game
import kotlinx.serialization.json.*
import kotlinx.serialization.list

object Api {
    private val client = HttpClient()
    private val json = Json(JsonConfiguration.Stable)
    interface AuthRes {
        val user: User
        val token: String
    }
    fun register(): Promise<User> = client.post("/auth").then {
        it.json() as AuthRes
    }.then {
        client.token = it.token
        it.user
    }
    fun loadGames() = client.get("/games").then {
        it.text()
    }.then {
        json.parse(Game.serializer().list, it)
    }
    fun createGame() = client.post("/games")
    data class MovePayload(val gameId: String, val x: Int, val y: Int)
    fun move(data: MovePayload) = client.post("/move", data)
}