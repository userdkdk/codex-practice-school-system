package com.musinsa.schoolsystem.domain.course.error;

import com.musinsa.schoolsystem.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CourseErrorCode implements ErrorCode {
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_404", "강좌를 찾을 수 없습니다."),
    COURSE_FULL(HttpStatus.CONFLICT, "COURSE_409_1", "정원이 가득 찬 강좌입니다."),
    COURSE_ENROLLMENT_EMPTY(HttpStatus.CONFLICT, "COURSE_409_2", "현재 신청 인원이 0명인 강좌입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CourseErrorCode(HttpStatus httpStatus, String code, String message) {
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
