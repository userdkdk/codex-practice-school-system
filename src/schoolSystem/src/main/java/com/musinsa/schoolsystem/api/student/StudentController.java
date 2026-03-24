package com.musinsa.schoolsystem.api.student;

import com.musinsa.schoolsystem.api.student.dto.StudentResponse;
import com.musinsa.schoolsystem.api.student.service.StudentQueryService;
import com.musinsa.schoolsystem.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
@Tag(name = "Student", description = "학생 API")
public class StudentController {

    private final StudentQueryService studentQueryService;

    @GetMapping
    @Operation(summary = "학생 목록 조회")
    public PageResponse<StudentResponse> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(studentQueryService.getStudents(PageRequest.of(page, size)));
    }
}
