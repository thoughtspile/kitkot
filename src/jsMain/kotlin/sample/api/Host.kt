package sample.api

import kotlin.browser.window

val HOST = js("process.env.REACT_APP_API_HOST") as? String ?: window.location.host
