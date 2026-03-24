package com.musinsa.schoolsystem.api.student.dto;

import com.musinsa.schoolsystem.domain.student.Student;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학생 조회 응답")
public record StudentResponse(
        Long id,
        String studentNumber,
        String name,
        String departmentName,
        int grade
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getName(),
                student.getDepartment().getName(),
                student.getGrade()
        );
    }
}
