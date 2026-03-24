package com.musinsa.schoolsystem.api.professor.dto;

import com.musinsa.schoolsystem.domain.professor.Professor;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "교수 조회 응답")
public record ProfessorResponse(
        Long id,
        String name,
        String departmentName
) {
    public static ProfessorResponse from(Professor professor) {
        return new ProfessorResponse(
                professor.getId(),
                professor.getName(),
                professor.getDepartment().getName()
        );
    }
}
