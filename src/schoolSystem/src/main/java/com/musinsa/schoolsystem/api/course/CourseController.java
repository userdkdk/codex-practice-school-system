package com.musinsa.schoolsystem.api.course;

import com.musinsa.schoolsystem.api.course.dto.CourseResponse;
import com.musinsa.schoolsystem.api.course.service.CourseQueryService;
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
@RequestMapping("/courses")
@Tag(name = "Course", description = "강좌 API")
public class CourseController {

    private final CourseQueryService courseQueryService;

    @GetMapping
    @Operation(summary = "강좌 목록 조회")
    public PageResponse<CourseResponse> getCourses(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(courseQueryService.getCourses(departmentId, PageRequest.of(page, size)));
    }
}
