package com.musinsa.schoolsystem.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "에러 응답")
public record ErrorResponse(
        @Schema(description = "HTTP 상태 코드", example = "400")
        int status,
        @Schema(description = "애플리케이션 에러 코드", example = "ENROLLMENT_001")
        String code,
        @Schema(description = "에러 메시지", example = "이미 신청한 강좌입니다.")
        String message,
        @Schema(description = "요청 경로", example = "/enrollments")
        String path,
        @Schema(description = "에러 발생 시각")
        LocalDateTime timestamp
) {
}
