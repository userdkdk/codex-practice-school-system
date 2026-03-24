package com.musinsa.schoolsystem.api.course.service;

import com.musinsa.schoolsystem.api.course.dto.CourseResponse;
import com.musinsa.schoolsystem.domain.course.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseQueryService {

    private final CourseRepository courseRepository;

    public Page<CourseResponse> getCourses(Long departmentId, Pageable pageable) {
        if (departmentId == null) {
            return courseRepository.findAll(pageable).map(CourseResponse::from);
        }
        return courseRepository.findAllByDepartment_Id(departmentId, pageable).map(CourseResponse::from);
    }
}
