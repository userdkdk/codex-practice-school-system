package com.musinsa.schoolsystem.api.professor.service;

import com.musinsa.schoolsystem.api.professor.dto.ProfessorResponse;
import com.musinsa.schoolsystem.domain.professor.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessorQueryService {

    private final ProfessorRepository professorRepository;

    public Page<ProfessorResponse> getProfessors(Pageable pageable) {
        return professorRepository.findAll(pageable).map(ProfessorResponse::from);
    }
}
