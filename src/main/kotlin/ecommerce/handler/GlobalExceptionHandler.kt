package ecommerce.handler

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ValidationError(val field: String, val message: String)

data class ValidationErrorResponse(val errors: List<ValidationError>)

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
        val error = ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.message ?: "Unauthorized")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.message ?: "Validation failed")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.message ?: "Something went wrong")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    data class ErrorResponse(
        val status: Int,
        val message: String,
    )
}
