package com.musinsa.schoolsystem.domain.enrollment.error;

import com.musinsa.schoolsystem.global.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum EnrollmentErrorCode implements ErrorCode {
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ENROLLMENT_404", "취소할 강의가 없습니다."),
    DUPLICATE_ENROLLMENT(HttpStatus.CONFLICT, "ENROLLMENT_409_1", "이미 신청한 강좌입니다."),
    CREDIT_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "ENROLLMENT_409_2", "최대 신청 가능 학점을 초과했습니다."),
    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "ENROLLMENT_409_3", "시간이 겹치는 강좌는 신청할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    EnrollmentErrorCode(HttpStatus httpStatus, String code, String message) {
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
