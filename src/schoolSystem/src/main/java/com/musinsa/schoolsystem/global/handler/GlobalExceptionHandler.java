package com.musinsa.schoolsystem.global.handler;

import com.musinsa.schoolsystem.global.error.CommonErrorCode;
import com.musinsa.schoolsystem.global.error.ErrorCode;
import com.musinsa.schoolsystem.global.exception.BusinessException;
import com.musinsa.schoolsystem.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        return toResponse(exception.getErrorCode(), request.getRequestURI());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(Exception exception, HttpServletRequest request) {
        return toResponse(CommonErrorCode.INVALID_INPUT_VALUE, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        return toResponse(CommonErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, String path) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse(
                        errorCode.getHttpStatus().value(),
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        path,
                        LocalDateTime.now()
                ));
    }
}
