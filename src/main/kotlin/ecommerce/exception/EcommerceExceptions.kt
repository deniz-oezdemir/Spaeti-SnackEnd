package ecommerce.exception

class NotFoundException(message: String? = null) : RuntimeException(message)

class OperationFailedException(message: String? = null) : RuntimeException(message)

class AuthorizationException(message: String? = null) : RuntimeException(message)

class ForbiddenException(message: String? = null) : RuntimeException(message)

class InvalidCartItemQuantityException(quantity: Int) :
    RuntimeException("Quantity must be non-negative, got $quantity")

class NoSuchElementException(message: String? = null) : RuntimeException(message)

class InvalidOptionNameException(message: String? = null) : RuntimeException(message)

class InvalidOptionQuantityException(message: String? = null) : RuntimeException(message)

class InsufficientStockException(message: String? = null) : RuntimeException(message)

class MissingProductIdException(message: String) : RuntimeException(message)
