package com.musinsa.schoolsystem.api.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "헬스체크 응답")
public record HealthResponse(
        @Schema(description = "서버 상태", example = "ok")
        String status
) {
}
