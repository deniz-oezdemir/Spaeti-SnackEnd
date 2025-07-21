package ecommerce.exception

class NotFoundException(message: String) : RuntimeException(message)

class OperationFailedException(message: String) : RuntimeException(message)
