package com.musinsa.schoolsystem.api.enrollment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "수강신청 응답")
public record EnrollmentResponse(
        Long enrollmentId,
        Long studentId,
        Long courseId,
        int enrolledCount,
        LocalDateTime createdAt
) {
}
