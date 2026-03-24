package com.musinsa.schoolsystem.domain.enrollment;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudent_IdAndCourse_Id(Long studentId, Long courseId);

    @EntityGraph(attributePaths = {"course", "course.department", "course.professor"})
    List<Enrollment> findAllByStudent_Id(Long studentId);

    @EntityGraph(attributePaths = {"course", "course.scheduleSlots"})
    @Query("""
            select distinct e
            from Enrollment e
            join e.course c
            join c.scheduleSlots s
            where e.student.id = :studentId
              and s.dayOfWeek in :dayOfWeeks
            """)
    List<Enrollment> findScheduleCandidatesByStudentIdAndDayOfWeeks(
            @Param("studentId") Long studentId,
            @Param("dayOfWeeks") Set<DayOfWeek> dayOfWeeks
    );

    @EntityGraph(attributePaths = {"course"})
    Optional<Enrollment> findByStudent_IdAndCourse_Id(Long studentId, Long courseId);
}
