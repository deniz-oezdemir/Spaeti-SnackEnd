package ecommerce.advice

import org.springframework.http.HttpStatus
import java.time.Instant

data class ApiError(
    val status: Int,
    val error: String,
    val message: String?,
    val timestamp: Instant = Instant.now(),
    val details: Any? = null,
) {
    companion object {
        fun notFound(
            message: String?,
            details: Any? = null,
        ) = ApiError(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Operation failed",
            message = message,
            details = details,
        )

        fun badRequest(
            error: String,
            message: String?,
            details: Any? = null,
        ) = ApiError(
            status = HttpStatus.BAD_REQUEST.value(),
            error = error,
            message = message,
            details = details,
        )

        fun unauthorized(
            message: String?,
            details: Any? = null,
        ) = ApiError(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = "Authorization failed",
            message = message,
            details = details,
        )

        fun forbidden(
            message: String?,
            details: Any? = null,
        ) = ApiError(
            status = HttpStatus.FORBIDDEN.value(),
            error = "Authorization failed. Invalid Credentials",
            message = message,
            details = details,
        )

        fun conflict(
            error: String,
            message: String?,
            details: Any? = null,
        ) = ApiError(
            status = HttpStatus.CONFLICT.value(),
            error = error,
            message = message,
            details = details,
        )

        fun internalError(
            error: String,
            message: String?,
            details: Any? = null,
        ) = ApiError(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = error,
            message = message,
            details = details,
        )
    }
}
