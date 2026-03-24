package com.musinsa.schoolsystem.api.student.service;

import com.musinsa.schoolsystem.api.student.dto.StudentResponse;
import com.musinsa.schoolsystem.domain.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentQueryService {

    private final StudentRepository studentRepository;

    public Page<StudentResponse> getStudents(Pageable pageable) {
        return studentRepository.findAll(pageable).map(StudentResponse::from);
    }
}
