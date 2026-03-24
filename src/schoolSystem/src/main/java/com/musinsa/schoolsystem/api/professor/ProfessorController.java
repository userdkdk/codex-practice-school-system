package com.musinsa.schoolsystem.api.professor;

import com.musinsa.schoolsystem.api.professor.dto.ProfessorResponse;
import com.musinsa.schoolsystem.api.professor.service.ProfessorQueryService;
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
@RequestMapping("/professors")
@Tag(name = "Professor", description = "교수 API")
public class ProfessorController {

    private final ProfessorQueryService professorQueryService;

    @GetMapping
    @Operation(summary = "교수 목록 조회")
    public PageResponse<ProfessorResponse> getProfessors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return PageResponse.from(professorQueryService.getProfessors(PageRequest.of(page, size)));
    }
}
