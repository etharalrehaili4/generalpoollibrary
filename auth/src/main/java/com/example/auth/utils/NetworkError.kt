package com.example.auth.utils

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class NetworkError(
    val message: String,
    val cause: Throwable? = null,
) {
    class BadRequest(
        msg: String,
    ) : NetworkError(msg)

    class Timeout(
        msg: String = "Request timed out",
    ) : NetworkError(msg)

    class Network(
        msg: String = "Network error",
        cause: Throwable? = null,
    ) : NetworkError(msg, cause)

    class Http(
        val code: Int,
        msg: String,
    ) : NetworkError("HTTP $code: $msg")

    class Unknown(
        msg: String = "Unknown error",
        cause: Throwable? = null,
    ) : NetworkError(msg, cause)

    companion object {
        fun fromHttpCode(
            code: Int,
            msg: String,
        ) = Http(code, msg)

        fun fromException(e: Throwable): NetworkError =
            when (e) {
                is SocketTimeoutException -> Timeout()
                is UnknownHostException,
                is ConnectException,
                    -> Network(cause = e)
                else -> Unknown(e.message ?: "Unexpected exception", e)
            }
    }
}