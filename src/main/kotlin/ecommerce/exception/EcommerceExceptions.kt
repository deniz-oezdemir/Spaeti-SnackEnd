package ecommerce.exception

class NotFoundException(message: String? = null) : RuntimeException(message)

class OperationFailedException(message: String? = null) : RuntimeException(message)

class AuthorizationException(message: String? = null) : RuntimeException(message)

class ForbiddenException(message: String? = null) : RuntimeException(message)
