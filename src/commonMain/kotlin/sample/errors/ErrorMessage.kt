package sample.errors

import kotlinx.serialization.Serializable

@Serializable
class ErrorMessage(val message: String? = "")

class IllegalMoveException(message: String): Exception(message)
