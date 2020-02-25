package sample

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import sample.events.Event
import sample.events.eventChannel
import sample.storage.*
import sample.models.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.content.*
import io.ktor.http.content.*
import io.ktor.sessions.*
import io.ktor.features.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.client.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import io.ktor.client.engine.cio.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.channels.consumeEach
import java.security.SecureRandom

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

class BaseJWT {
    private val secret = SecureRandom().ints(6).toString()
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm).build()
    fun getPrincipal(payload: Payload) = UserIdPrincipal(payload.getClaim("uid").asString())
    fun sign(uid: String): String = JWT.create().withClaim("uid", uid).sign(algorithm)
}

val json = Json(JsonConfiguration.Stable)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CORS) {
        allowNonSimpleContentTypes = true
        header("Authorization")
        host("localhost:3000")
    }

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val tokenManager = BaseJWT()
    install(Authentication) {
        jwt {
            verifier(tokenManager.verifier)
            validate { tokenManager.getPrincipal(it.payload) }
        }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    val client = HttpClient(CIO) {
        install(Auth) {
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
    }
    runBlocking {
        // Sample for making a HTTP Client request
        /*
        val message = client.post<JsonSampleClass> {
            url("http://127.0.0.1:8080/path/to/endpoint")
            contentType(ContentType.Application.Json)
            body = JsonSampleClass(hello = "world")
        }
        */
    }

    fun PipelineContext<Unit, ApplicationCall>.getUser(): User {
        val uid = call.principal<UserIdPrincipal>() ?: error("No principal")
        return Storage.getUser(uid.name.toInt())
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        post("/auth") {
            val user = Storage.createUser()
            call.respond(mapOf(
                "token" to tokenManager.sign(user.id.toString()),
                "user" to user
            ))
        }

        get("/games") {
            call.respond(Storage.games.items)
        }


        authenticate {
            post("/games") {
                Storage.startGame(getUser())
                call.respond({})
            }

            post("/move") {
                val data = call.receive<AnonymousMove>()
                Storage.processMove(Move(getUser(), data.x, data.y, data.gameId))
                call.respond({})
            }
        }

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }

        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized)
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden)
            }

        }

        webSocket("/events") { // this: DefaultWebSocketSession
            eventChannel.consumeEach {
                outgoing.send(Frame.Text(json.stringify(Event.serializer(), it)))
            }
        }
    }
}

data class MySession(val count: Int = 0)

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

data class JsonSampleClass(val hello: String)

