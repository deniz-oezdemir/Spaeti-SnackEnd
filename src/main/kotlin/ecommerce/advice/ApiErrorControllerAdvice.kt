package ecommerce.advice

import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.util.logger
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(annotations = [RestController::class])
class ApiErrorControllerAdvice {
    private val log = logger<ApiErrorControllerAdvice>()

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<String> {
        log.warn("NotFoundException occurred: ${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @ExceptionHandler(OperationFailedException::class)
    fun handleOperationFailedException(e: OperationFailedException): ResponseEntity<String> {
        log.error("OperationFailedException occurred: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Operation failed: ${e.message}")
    }

    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccessException(e: DataAccessException): ResponseEntity<String> {
        log.error("DataAccessException: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected database error")
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(e: DuplicateKeyException): ResponseEntity<String> {
        log.warn("DuplicateKeyException: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate key error")
    }

    @ExceptionHandler(EmptyResultDataAccessException::class)
    fun handleEmptyResultException(e: EmptyResultDataAccessException): ResponseEntity<String> {
        log.warn("EmptyResultDataAccessException: ${e.message}", e)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No result found for the given query")
    }
}
