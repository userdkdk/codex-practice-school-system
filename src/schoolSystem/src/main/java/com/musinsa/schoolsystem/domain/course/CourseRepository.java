package com.musinsa.schoolsystem.domain.course;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Course c where c.id = :courseId")
    Optional<Course> findByIdForUpdate(@Param("courseId") Long courseId);

    @EntityGraph(attributePaths = {"department", "professor"})
    Page<Course> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"department", "professor"})
    Page<Course> findAllByDepartment_Id(Long departmentId, Pageable pageable);
}
