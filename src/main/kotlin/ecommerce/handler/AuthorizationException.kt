package ecommerce.handler

class AuthorizationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
