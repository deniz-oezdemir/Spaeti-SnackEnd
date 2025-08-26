package ecommerce.handler

import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(e: NoSuchElementException): ResponseEntity<Void> {
        logger.warn("Resource not found: ${e.message}", e)
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        logger.warn("Validation error: ${ex.message}")
        val errors =
            ex.bindingResult.fieldErrors.map { fieldError ->
                ValidationError(
                    field = fieldError.field,
                    message = fieldError.defaultMessage ?: "Invalid value",
                )
            }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ValidationErrorResponse(errors))
    }

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorization(ex: AuthorizationException): ResponseEntity<ErrorResponse> {
        logger.warn("Authorization error: ${ex.message}")
        val error = ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.message ?: "Unauthorized")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): ResponseEntity<ErrorResponse> {
        logger.warn("Bad request: ${ex.message}")
        val error = ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message ?: "Validation failed")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(OptimisticLockingFailureException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleOptimisticConflict(e: OptimisticLockingFailureException): ErrorResponse {
        logger.warn("Optimistic lock conflict: ${e.message}")
        return ErrorResponse(409, "The item was updated by someone else. Please retry.")
    }

    @ExceptionHandler(PessimisticLockingFailureException::class)
    fun handlePessimistic(e: PessimisticLockingFailureException): ResponseEntity<ErrorResponse> {
        logger.warn("Pessimistic lock failure: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(409, "Resource is locked by another operation. Please retry."))
    }

    @ExceptionHandler(CannotAcquireLockException::class)
    fun handleCannotAcquire(e: CannotAcquireLockException): ResponseEntity<ErrorResponse> {
        logger.warn("Cannot acquire lock: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ErrorResponse(409, "Resource busy. Please retry later."))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unhandled exception: ${ex.message}")
        val error = ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.message ?: "Something went wrong")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}

class ValidationError(val field: String, val message: String)

class ValidationErrorResponse(val errors: List<ValidationError>)

class ErrorResponse(
    val status: Int,
    val message: String,
)
