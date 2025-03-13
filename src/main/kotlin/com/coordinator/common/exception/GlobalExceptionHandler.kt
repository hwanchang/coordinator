package com.coordinator.common.exception

import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MultipartException

@RestControllerAdvice
class GlobalExceptionHandler {
    /*
    * [실패 시 실패 값과 실패 사유에 대한 해석]
    * 실패 값은 HTTP Status 로 표현한다고 생각하여 ResponseBody 에는 에러 메시지만 표시하고 요청 실패에 대한 HTTP 상태 코드를 적절히 부여
    * 또한, 에러메시지에는 어떤 요청 값이 어떤 문제인지 설명 추가
    * */
    private fun response(errorMessage: String?, httpStatus: HttpStatus): ResponseEntity<*> {
        return ResponseEntity(ErrorResponse(errorMessage), httpStatus)
    }

    @ExceptionHandler(
        NoSuchElementException::class,
        EntityNotFoundException::class,
    )
    fun handleNoHandlerFoundException(e: Exception): ResponseEntity<*> {
        return response(e.message, NOT_FOUND)
    }

    @ExceptionHandler(
        IllegalStateException::class,
        IllegalArgumentException::class,
        TypeMismatchException::class,
        HttpMessageNotReadableException::class,
        MissingServletRequestParameterException::class,
        MultipartException::class
    )
    fun handleBadRequestException(e: Exception): ResponseEntity<*> {
        return response(e.message, BAD_REQUEST)
    }

    @ExceptionHandler(HttpMediaTypeException::class)
    fun handleHttpMediaTypeException(e: Exception): ResponseEntity<*> {
        return response(e.message, UNSUPPORTED_MEDIA_TYPE)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowedException(e: Exception): ResponseEntity<*> {
        return response(e.message, METHOD_NOT_ALLOWED)
    }

    @ExceptionHandler(Exception::class, RuntimeException::class)
    fun handleException(e: Exception): ResponseEntity<*> {
        return response(e.message, INTERNAL_SERVER_ERROR)
    }
}
