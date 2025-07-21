package ecommerce.advice

import ecommerce.exception.NotFoundException
import ecommerce.exception.OperationFailedException
import ecommerce.util.logger
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice(annotations = [Controller::class])
class ViewErrorControllerAdvice {
    private val log = logger<ApiErrorControllerAdvice>()

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        e: NotFoundException,
        model: Model,
    ): String {
        model.addAttribute("status", HttpStatus.NOT_FOUND.value())
        model.addAttribute("message", e.message ?: "Not found")
        return "error"
    }

    @ExceptionHandler(OperationFailedException::class)
    fun handleOpFailed(
        e: OperationFailedException,
        model: Model,
    ): String {
        log.error("OperationFailedException: ${e.message}", e)
        model.addAttribute("message", e.message ?: "Operation failed")
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value())
        return "error"
    }

    @ExceptionHandler(DataAccessException::class)
    fun handleDataAccess(
        e: DataAccessException,
        model: Model,
    ): String {
        log.error("DataAccessException: ${e.message}", e)
        model.addAttribute("message", "Unexpected database error")
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value())
        return "error"
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(
        e: DuplicateKeyException,
        model: Model,
    ): String {
        log.warn("DuplicateKeyException: ${e.message}", e)
        model.addAttribute("message", "Duplicate key error")
        model.addAttribute("status", HttpStatus.CONFLICT.value())
        return "error"
    }

    @ExceptionHandler(EmptyResultDataAccessException::class)
    fun handleEmptyResultException(
        e: EmptyResultDataAccessException,
        model: Model,
    ): String {
        log.warn("EmptyResultDataAccessException: ${e.message}", e)
        model.addAttribute("message", "No result found for the given query")
        model.addAttribute("status", HttpStatus.NOT_FOUND.value())
        return "error"
    }
}
