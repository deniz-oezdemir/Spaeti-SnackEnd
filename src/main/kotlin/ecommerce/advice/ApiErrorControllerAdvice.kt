package ecommerce.advice

import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.util.logger
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice(annotations = [RestController::class])
class ApiErrorControllerAdvice {
    private val log = logger<ApiErrorControllerAdvice>()

    /**
     * Custom Exceptions
     */
    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Not Found error"
        log.warn("NotFoundException occurred: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.NOT_FOUND.value(),
                "error" to "Operation failed",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    @ExceptionHandler(OperationFailedException::class)
    fun handleOperationFailedException(e: OperationFailedException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Operation failed"
        log.warn("OperationFailedException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Operation failed",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    /**
     * JDBC Exceptions: DB errors
     */
    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(e: DataAccessException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Data Access Error"
        log.warn("DataAccessException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error" to "Empty result data access error",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(e: DuplicateKeyException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Duplicate key error"
        log.warn("DuplicateKeyException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.CONFLICT.value(),
                "error" to "Duplicate Key Error",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }

    @ExceptionHandler(EmptyResultDataAccessException::class)
    fun handleEmptyResultException(e: EmptyResultDataAccessException): ResponseEntity<Map<String, Any>> {
        val errorMessage = e.message ?: "Empty result for your query"
        log.warn("EmptyResultDataAccessException: $errorMessage", e)
        val body =
            mapOf(
                "status" to HttpStatus.NOT_FOUND.value(),
                "error" to "Empty Result Data Access",
                "message" to errorMessage,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    /**
     * @Valid Exceptions, thrown when validation using jakarta fails.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        log.warn("Validation failed: ${e.message}")

        val errors = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Validation error") }

        val body =
            mapOf(
                "status" to HttpStatus.BAD_REQUEST.value(),
                "error" to "Validation failed",
                "message" to errors,
                "timestamp" to Instant.now(),
            )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }
}
