package sample.api

import org.w3c.fetch.CORS
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import org.w3c.fetch.Response
import sample.errors.ErrorMessage
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

external fun fetch(url: String, config: RequestInit): Promise<Response>


class HttpClient {
    var token: String? = null
    private fun getHeaders(): Json {
        val headers = json("Content-Type" to "application/json")
        token?.let { headers["Authorization"] = "Bearer $token" }
        return headers
    }
    private fun getConfig(method: String? = "GET", body: Any? = null) = object : RequestInit {
        override var headers = getHeaders()
        override var method = method
        override var body = body?.let { JSON.stringify(body) } ?: undefined
        override var mode = RequestMode.CORS as RequestMode?
    }
    private fun run(path: String, config: RequestInit) = fetch("//$HOST$path", config).then { res ->
        if (res.ok)
            res.text()
        else
            // XXX: These casts are nasty, but everything else seems to break typing
            res.json().then { Promise.reject(Throwable(it.asDynamic().message as String)).asDynamic() }
    }
//    private fun <Res> runTyped(path: String, config: RequestInit)

    fun get(path: String) =
        run(path, getConfig())
    fun post(path: String, data: Any? = null) =
        run(path, getConfig(method = "POST", body = data))
}
