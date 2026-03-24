package com.musinsa.schoolsystem.integration.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.musinsa.schoolsystem.domain.course.Course;
import com.musinsa.schoolsystem.domain.course.CourseRepository;
import com.musinsa.schoolsystem.domain.department.DepartmentRepository;
import com.musinsa.schoolsystem.domain.professor.Professor;
import com.musinsa.schoolsystem.domain.professor.ProfessorRepository;
import com.musinsa.schoolsystem.domain.student.Student;
import com.musinsa.schoolsystem.domain.student.StudentRepository;
import com.musinsa.schoolsystem.global.readiness.ApplicationReadinessState;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional(readOnly = true)
class DataInitializationIntegrationTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ApplicationReadinessState readinessState;

    @Test
    @DisplayName("애플리케이션 시작 시 요구된 최소 개수의 초기 데이터가 생성된다")
    // 체크리스트: 2. 초기 데이터 생성 > 학과가 10개 이상 생성된다.
    // 체크리스트: 2. 초기 데이터 생성 > 교수 데이터가 100명 이상 생성된다.
    // 체크리스트: 2. 초기 데이터 생성 > 학생 데이터가 10,000명 이상 생성된다.
    // 체크리스트: 2. 초기 데이터 생성 > 강좌 데이터가 500개 이상 생성된다.
    void createsRequiredMinimumDataOnStartup() {
        assertThat(departmentRepository.count()).isGreaterThanOrEqualTo(10);
        assertThat(professorRepository.count()).isGreaterThanOrEqualTo(100);
        assertThat(studentRepository.count()).isGreaterThanOrEqualTo(10_000);
        assertThat(courseRepository.count()).isGreaterThanOrEqualTo(500);
    }

    @Test
    @DisplayName("초기 데이터 생성은 1분 이내에 완료된다")
    // 체크리스트: 2. 초기 데이터 생성 > 애플리케이션 시작 후 1분 이내에 초기 데이터 생성이 완료된다.
    void initializationCompletesWithinOneMinute() {
        assertThat(readinessState.getInitializationStartedAt()).isNotNull();
        assertThat(readinessState.getInitializationCompletedAt()).isNotNull();
        assertThat(readinessState.getInitializationDuration()).isLessThanOrEqualTo(Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("초기 데이터는 의미 있는 이름과 패턴을 가진다")
    // 체크리스트: 2. 초기 데이터 생성 > 생성 데이터가 의미 있는 이름과 패턴을 가진다.
    void generatedDataHasMeaningfulPatterns() {
        Student student = studentRepository.findById(1L).orElseThrow();
        Professor professor = professorRepository.findById(1L).orElseThrow();
        Course course = courseRepository.findById(1L).orElseThrow();

        assertThat(student.getStudentNumber()).matches("2026\\d{5}");
        assertThat(student.getName()).isNotBlank();
        assertThat(student.getDepartment().getName()).isNotBlank();
        assertThat(student.getGrade()).isBetween(1, 4);

        assertThat(professor.getName()).isNotBlank();
        assertThat(professor.getDepartment().getName()).isNotBlank();

        assertThat(course.getCode()).matches("CRS-\\d+");
        assertThat(course.getName()).contains(" ");
        assertThat(course.getDepartment().getName()).isNotBlank();
        assertThat(course.getProfessor().getName()).isNotBlank();
        assertThat(course.getScheduleSlots()).isNotEmpty();
    }

    @Test
    @DisplayName("초기 데이터는 기본 비즈니스 규칙 범위를 벗어나지 않는다")
    // 체크리스트: 2. 초기 데이터 생성 > 생성 데이터가 비즈니스 규칙을 위반하지 않는다.
    void generatedDataDoesNotViolateBusinessRules() {
        List<Student> students = studentRepository.findAll(PageRequest.of(0, 200)).getContent();
        List<Course> courses = courseRepository.findAll();

        assertThat(students).isNotEmpty();
        assertThat(students).allSatisfy(student -> {
            assertThat(student.getName()).isNotBlank();
            assertThat(student.getStudentNumber()).matches("2026\\d{5}");
            assertThat(student.getGrade()).isBetween(1, 4);
        });

        assertThat(courses).isNotEmpty();
        assertThat(courses).allSatisfy(course -> {
            assertThat(course.getCredits()).isBetween(2, 4);
            assertThat(course.getCapacity()).isGreaterThanOrEqualTo(20);
            assertThat(course.getEnrolledCount()).isBetween(0, course.getCapacity());
            assertThat(course.getScheduleSlots()).isNotEmpty();
        });
    }
}
