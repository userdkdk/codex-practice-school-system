package com.musinsa.schoolsystem.api.enrollment.service;

import com.musinsa.schoolsystem.api.enrollment.dto.EnrollmentResponse;
import com.musinsa.schoolsystem.api.enrollment.dto.TimetableCourseResponse;
import com.musinsa.schoolsystem.domain.course.Course;
import com.musinsa.schoolsystem.domain.course.CourseRepository;
import com.musinsa.schoolsystem.domain.course.CourseScheduleSlot;
import com.musinsa.schoolsystem.domain.course.error.CourseErrorCode;
import com.musinsa.schoolsystem.domain.enrollment.Enrollment;
import com.musinsa.schoolsystem.domain.enrollment.EnrollmentRepository;
import com.musinsa.schoolsystem.domain.enrollment.error.EnrollmentErrorCode;
import com.musinsa.schoolsystem.domain.student.Student;
import com.musinsa.schoolsystem.domain.student.StudentRepository;
import com.musinsa.schoolsystem.domain.student.error.StudentErrorCode;
import com.musinsa.schoolsystem.global.exception.BusinessException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private static final int MAX_CREDITS = 18;

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentResponse enroll(Long studentId, Long courseId) {
        Student student = studentRepository.findByIdForUpdate(studentId)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.STUDENT_NOT_FOUND));
        Course course = courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new BusinessException(CourseErrorCode.COURSE_NOT_FOUND));

        if (enrollmentRepository.existsByStudent_IdAndCourse_Id(studentId, courseId)) {
            throw new BusinessException(EnrollmentErrorCode.DUPLICATE_ENROLLMENT);
        }

        List<Enrollment> currentEnrollments = enrollmentRepository.findAllByStudent_Id(studentId);
        validateCredits(currentEnrollments, course);
        validateSchedule(studentId, course);

        if (!course.hasRemainingSeat()) {
            throw new BusinessException(CourseErrorCode.COURSE_FULL);
        }

        course.increaseEnrollment();
        Enrollment enrollment = enrollmentRepository.save(new Enrollment(student, course, LocalDateTime.now()));

        return new EnrollmentResponse(
                enrollment.getId(),
                student.getId(),
                course.getId(),
                course.getEnrolledCount(),
                enrollment.getCreatedAt()
        );
    }

    public void cancel(Long studentId, Long courseId) {
        studentRepository.findByIdForUpdate(studentId)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.STUDENT_NOT_FOUND));
        Course course = courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new BusinessException(CourseErrorCode.COURSE_NOT_FOUND));

        Enrollment enrollment = enrollmentRepository.findByStudent_IdAndCourse_Id(studentId, courseId)
                .orElseThrow(() -> new BusinessException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND));

        enrollmentRepository.delete(enrollment);
        course.decreaseEnrollment();
    }

    @Transactional(readOnly = true)
    public List<TimetableCourseResponse> getTimetable(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(StudentErrorCode.STUDENT_NOT_FOUND));

        return enrollmentRepository.findAllByStudent_Id(studentId).stream()
                .map(Enrollment::getCourse)
                .map(TimetableCourseResponse::from)
                .toList();
    }

    private void validateCredits(List<Enrollment> currentEnrollments, Course targetCourse) {
        int totalCredits = currentEnrollments.stream()
                .map(Enrollment::getCourse)
                .mapToInt(Course::getCredits)
                .sum();

        if (totalCredits + targetCourse.getCredits() > MAX_CREDITS) {
            throw new BusinessException(EnrollmentErrorCode.CREDIT_LIMIT_EXCEEDED);
        }
    }

    private void validateSchedule(Long studentId, Course targetCourse) {
        Set<DayOfWeek> targetDays = targetCourse.getScheduleSlots().stream()
                .map(CourseScheduleSlot::getDayOfWeek)
                .collect(Collectors.toSet());

        List<Enrollment> candidates = enrollmentRepository.findScheduleCandidatesByStudentIdAndDayOfWeeks(studentId, targetDays);

        for (Enrollment enrollment : candidates) {
            for (CourseScheduleSlot existing : enrollment.getCourse().getScheduleSlots()) {
                for (CourseScheduleSlot candidate : targetCourse.getScheduleSlots()) {
                    if (existing.overlaps(candidate)) {
                        throw new BusinessException(EnrollmentErrorCode.SCHEDULE_CONFLICT);
                    }
                }
            }
        }
    }
}
