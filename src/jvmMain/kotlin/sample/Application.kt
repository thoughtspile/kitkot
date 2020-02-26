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
import io.ktor.features.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.serialization.serialization
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.channels.consumeEach
import sample.errors.ErrorMessage
import sample.errors.IllegalMoveException
import java.security.SecureRandom

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

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
        serialization()
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

            @Serializable
            class AuthRes(val token: String, val user: User)

            call.respond(AuthRes(tokenManager.sign(user.id.toString()), user))
        }

        get("/state") {
            call.respond(Storage.snapshot)
        }


        authenticate {
            post("/games") {
                Storage.startGame(getUser())
                call.respond(mapOf("ok" to true))
            }

            post("/move") {
                val data = call.receive<AnonymousMove>()
                Storage.processMove(Move(getUser(), data.x, data.y, data.gameId))
                call.respond(mapOf("ok" to true))
            }

            // Events with order in [from, to] (inclusive)
            get("/events/range/{from}/{to}") {
                fun intParam(name: String) = call.parameters[name]?.toInt() ?: error("Range limits must be Int")

                val diff = Storage.eventRange(intParam("from"), intParam("to"))
                // NOTE: kotlinx.serialization does not accept mixed type lists
                // Here we serialize list items independently
                call.respond(diff.map { json.stringify(Event.serializer(), it) })
            }
        }

        install(StatusPages) {
            exception<AuthenticationException> { cause ->
                call.respond(HttpStatusCode.Unauthorized, ErrorMessage(cause.message))
            }
            exception<AuthorizationException> { cause ->
                call.respond(HttpStatusCode.Forbidden, ErrorMessage(cause.message))
            }
            exception<IllegalMoveException> { cause ->
                call.respond(HttpStatusCode.UnprocessableEntity, ErrorMessage(cause.message))
            }
        }

        webSocket("/events") {
            fun Event.toJson() = Frame.Text(json.stringify(Event.serializer(), this))
            outgoing.send(Event.ConnectEvent(Storage.revision).toJson())
            eventChannel.consumeEach {
                outgoing.send(it.toJson())
            }
        }
    }
}

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()


class BaseJWT {
    private val secret = SecureRandom().ints(6).toString()
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm).build()
    fun getPrincipal(payload: Payload) = UserIdPrincipal(payload.getClaim("uid").asString())
    fun sign(uid: String): String = JWT.create().withClaim("uid", uid).sign(algorithm)
}

val json = Json(JsonConfiguration.Stable)
