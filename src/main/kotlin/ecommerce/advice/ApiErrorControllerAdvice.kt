package ecommerce.advice

import ecommerce.exception.AuthorizationException
import ecommerce.exception.ForbiddenException
import ecommerce.exception.InsufficientStockException
import ecommerce.exception.InvalidCartItemQuantityException
import ecommerce.exception.InvalidOptionNameException
import ecommerce.exception.InvalidOptionQuantityException
import ecommerce.exception.MissingProductIdException
import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.exception.PaymentFailedException
import ecommerce.util.logger
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(annotations = [RestController::class])
class ApiErrorControllerAdvice {
    private val log = logger<ApiErrorControllerAdvice>()

    private fun buildResponse(error: ApiError): ResponseEntity<ApiError> = ResponseEntity.status(error.status).body(error)

    /**
     * Custom Exceptions
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Not Found error"
        log.warn("NotFoundException occurred: $errorMessage", e)
        val apiError = ApiError.notFound(errorMessage)
        return buildResponse(apiError)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotSuchElementException(e: NoSuchElementException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Not such element"
        log.warn("NoSuchElementException occurred: $errorMessage", e)
        val apiError = ApiError.notFound(errorMessage)
        return buildResponse(apiError)
    }

    @ExceptionHandler(OperationFailedException::class)
    fun handleOperationFailedException(e: OperationFailedException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Operation failed"
        log.warn("OperationFailedException: $errorMessage", e)
        val apiError = ApiError.badRequest(errorMessage, e.message ?: "No additional details provided")
        return buildResponse(apiError)
    }

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Authorization failed"
        log.warn("AuthorizationException: $errorMessage", e)
        val apiError = ApiError.unauthorized(errorMessage)
        return buildResponse(apiError)
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Invalid credentials"
        log.warn("ForbiddenException: $errorMessage", e)
        val apiError = ApiError.forbidden(errorMessage)
        return buildResponse(apiError)
    }

    @ExceptionHandler(InvalidCartItemQuantityException::class)
    fun handleInvalidCartItemQuantityException(e: InvalidCartItemQuantityException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Invalid quantity"
        log.warn("InvalidCartItemQuantityException: $errorMessage", e)
        val apiError = ApiError.badRequest(errorMessage, e.message ?: "No additional details provided")
        return buildResponse(apiError)
    }

    @ExceptionHandler(InvalidOptionNameException::class)
    fun handleInvalidOptionNameException(e: InvalidOptionNameException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Invalid option name"
        log.warn("InvalidOptionNameException: $errorMessage", e)
        val apiError = ApiError.badRequest(errorMessage, e.message ?: "No additional details provided")
        return buildResponse(apiError)
    }

    @ExceptionHandler(InvalidOptionQuantityException::class)
    fun handleInvalidOptionQuantityException(e: InvalidOptionQuantityException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Invalid option quantity"
        log.warn("InvalidOptionQuantityException: $errorMessage", e)
        val apiError = ApiError.badRequest(errorMessage, e.message ?: "No additional details provided")
        return buildResponse(apiError)
    }

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStockException(e: InsufficientStockException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Insufficient stock"
        log.warn("InsufficientStockException: $errorMessage", e)
        val apiError = ApiError.conflict(errorMessage, e.message ?: "No additional details provided")
        return buildResponse(apiError)
    }

    /**
     * JDBC Exceptions: DB errors
     */
    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(e: DataAccessException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Data Access Error"
        log.warn("DataAccessException: $errorMessage", e)
        val apiError = ApiError.internalError(errorMessage, e.message ?: "No additional details provided")
        return buildResponse(apiError)
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(e: DuplicateKeyException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Duplicate key error"
        log.warn("DuplicateKeyException: $errorMessage", e)
        val apiError = ApiError.conflict(errorMessage, e.message ?: "No additional details provided")
        return buildResponse(apiError)
    }

    @ExceptionHandler(EmptyResultDataAccessException::class)
    fun handleEmptyResultException(e: EmptyResultDataAccessException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Empty result for your query"
        log.warn("EmptyResultDataAccessException: $errorMessage", e)
        val apiError = ApiError.notFound(errorMessage)
        return buildResponse(apiError)
    }

    @ExceptionHandler(MissingProductIdException::class)
    fun handleMissingProductId(e: MissingProductIdException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "productId is required"
        log.warn("MissingProductIdException: $errorMessage", e)
        val apiError =
            ApiError.badRequest(
                error = "Validation failed",
                message = errorMessage,
            )
        return buildResponse(apiError)
    }

    @ExceptionHandler(PaymentFailedException::class)
    fun handlePaymentFailedException(e: PaymentFailedException): ResponseEntity<ApiError> {
        val errorMessage = e.message ?: "Payment processing failed."
        log.warn("PaymentFailedException: $errorMessage", e)
        val apiError = ApiError.badRequest(
            error = "Payment failed",
            message = errorMessage
        )
        return buildResponse(apiError)
    }

    /**
     * @Valid Exceptions, thrown when validation using jakarta fails.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        log.warn("Validation failed: ${e.message}")

        val errors =
            e.bindingResult.fieldErrors.associate {
                it.field to (it.defaultMessage ?: "Validation error")
            }

        val apiError =
            ApiError.badRequest(
                error = "Validation failed",
                message = "One or more fields are invalid",
                details = errors,
            )
        return buildResponse(apiError)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ApiError> {
        log.error("Unhandled exception", e)
        val apiError = ApiError.internalError("Internal server error", e.message)
        return buildResponse(apiError)
    }
}
