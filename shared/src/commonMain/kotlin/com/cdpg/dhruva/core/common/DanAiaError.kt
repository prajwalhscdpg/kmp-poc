package com.cdpg.dhruva.core.common

/**
 * Typed failure shared by Android and iOS. The UI maps it to localized text with
 * [com.cdpg.dhruva.core.strings.toUiText]; server message text is never shown raw.
 *
 * [type] values are the backend's stable problem codes (`urn:dhruva:app:*`), shared with iOS,
 * Android and the backend.
 */
sealed class DanAiaError {
    /** No connection, DNS failure, connection reset… */
    data object Network : DanAiaError()

    data object Timeout : DanAiaError()

    /** 401 after a refresh attempt: the session is over. */
    data object Unauthorized : DanAiaError()

    /** 404 with the server's problem type, e.g. [ProblemType.NO_ADDRESSES_FOUND]. */
    data class NotFound(val type: String?) : DanAiaError()

    /** 400 / 409 / 422: the request was understood but rejected. */
    data class Validation(val type: String?) : DanAiaError()

    /** 5xx or any other unexpected status. */
    data class Server(val statusCode: Int) : DanAiaError()

    /** Unparseable response or a bug on our side. */
    data object Unexpected : DanAiaError()

    /** Network and Timeout are worth retrying later (writes go to the outbox). */
    val isConnectivityProblem: Boolean get() = this is Network || this is Timeout
}

/** Problem `type` values from the Dhruva API's error envelope. */
object ProblemType {
    const val SUCCESS = "urn:dhruva:app:Success"
    const val AUTHORIZATION_ERROR = "urn:dhruva:app:AuthorizationError"
    const val NO_ADDRESSES_FOUND = "urn:dhruva:app:NoAddressesFound"
    const val INCORRECT_OTP = "urn:dhruva:app:IncorrectOtp"
    const val HANDLE_TAKEN = "urn:dhruva:app:HandleTaken"
}
