package api

import app.Game
import app.User
import kotlin.js.Promise


object Api {
    private val client = HttpClient()
    class AuthRes(val user: User, val token: String)
    fun register(): Promise<User> = client.post<AuthRes>("/auth").then {
        client.token = it.token
        it.user
    }
    interface GameResponse {
        var field: Array<Array<User?>>
    }
    fun loadGames() = client.get<Array<GameResponse>>("/games").then {
        it.asList().map { arrGame -> Game(arrGame.field.toList().map { it.toList() }) }
    }
    fun createGame() = client.post<Unit>("/games")
    data class MovePayload(val gameId: String, val x: Int, val y: Int)
    fun move(data: MovePayload) = client.post<Unit>("/move", data)
}