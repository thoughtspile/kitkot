package sample.components.reactToastify

import react.*

interface ToastProps: RProps {
    var position: String?
    var autoClose: Int?
    var hideProgressBar: Boolean?
    var newestOnTop: Boolean?
}

@JsModule("react-toastify")
external object Toast {
    @JsName("ToastContainer")
    class Container : Component<ToastProps, RState> {
        override fun render(): ReactElement?
    }

    @JsName("toast")
    object Show {
        fun invoke(message: String)
        fun success(message: String)
        fun error(message: String)
        fun warn(message: String)
        fun info(message: String)
    }
}


fun RBuilder.toastContainer() = child(Toast.Container::class) {
    attrs.autoClose = 2000
    attrs.position = "bottom-center"
    attrs.hideProgressBar = true
    attrs.newestOnTop = true
}
