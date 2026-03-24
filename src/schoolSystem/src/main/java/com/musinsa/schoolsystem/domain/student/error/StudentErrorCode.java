package com.musinsa.schoolsystem.domain.student.error;

import com.musinsa.schoolsystem.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum StudentErrorCode implements ErrorCode {
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDENT_404", "학생을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    StudentErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
