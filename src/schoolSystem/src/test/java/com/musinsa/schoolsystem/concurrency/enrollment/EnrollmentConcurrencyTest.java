package com.musinsa.schoolsystem.concurrency.enrollment;

import static org.assertj.core.api.Assertions.assertThat;

import com.musinsa.schoolsystem.api.enrollment.dto.EnrollmentResponse;
import com.musinsa.schoolsystem.api.enrollment.service.EnrollmentService;
import com.musinsa.schoolsystem.domain.course.Course;
import com.musinsa.schoolsystem.domain.course.CourseRepository;
import com.musinsa.schoolsystem.domain.enrollment.Enrollment;
import com.musinsa.schoolsystem.domain.enrollment.EnrollmentRepository;
import com.musinsa.schoolsystem.domain.student.Student;
import com.musinsa.schoolsystem.domain.student.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
class EnrollmentConcurrencyTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    @DisplayName("잔여 좌석 1개 강좌에 동시 요청이 와도 1건만 성공한다")
    // 체크리스트: 5. 동시성 제어 > 잔여 좌석 1개인 강좌에 동시 요청이 들어와도 1명만 성공한다.
    // 체크리스트: 5. 동시성 제어 > 동시에 여러 요청이 와도 enrolled 값이 capacity를 초과하지 않는다.
    // 체크리스트: 7. 테스트 > 동시성 시나리오 테스트가 있다.
    void onlyOneRequestSucceedsWhenOneSeatRemains() throws Exception {
        Course course = courseRepository.findById(32L).orElseThrow();

        // 준비 단계: 좌석 1개만 남도록 순차적으로 상태를 만든다.
        fillCourseUntilOneSeatRemains(course);

        // 동시 실행 단계: 서로 다른 학생 20명이 같은 강좌를 동시에 신청한다.
        List<Student> concurrentStudents = studentRepository.findAll(PageRequest.of(300, 20)).getContent();
        int successCount = runConcurrentTasks(createEnrollmentTasks(concurrentStudents, course.getId()));

        Course refreshed = courseRepository.findById(course.getId()).orElseThrow();
        long enrollmentCount = enrollmentRepository.findAll().stream()
                .filter(enrollment -> enrollment.getCourse().getId().equals(course.getId()))
                .count();

        assertThat(successCount).isEqualTo(1);
        assertThat(refreshed.getEnrolledCount()).isEqualTo(course.getCapacity());
        assertThat(enrollmentCount).isEqualTo(course.getCapacity());
    }

    @Test
    @DisplayName("같은 학생의 동시 신청에서도 학점 제한은 깨지지 않는다")
    // 체크리스트: 5. 동시성 제어 > 동일 학생이 동시에 여러 강좌를 신청할 때 학점 제한이 깨지지 않는다.
    void sameStudentConcurrentRequestsDoNotBreakCreditLimit() throws Exception {
        Long studentId = 9_001L;

        // 준비 단계: 동일 학생을 15학점 상태로 맞춘다.
        enrollSequentially(studentId, List.of(2L, 3L, 6L, 9L));

        // 동시 실행 단계: 4학점 강의와 3학점 강의를 동시에 신청시켜 한 건만 통과해야 한다.
        int successCount = runConcurrentTasks(List.of(
                enrollmentTask(studentId, 4L),
                enrollmentTask(studentId, 7L)
        ));

        List<Enrollment> enrollments = enrollmentRepository.findAllByStudent_Id(studentId);
        int totalCredits = enrollments.stream()
                .map(Enrollment::getCourse)
                .mapToInt(Course::getCredits)
                .sum();

        assertThat(successCount).isEqualTo(1);
        assertThat(enrollments).hasSize(5);
        assertThat(totalCredits).isLessThanOrEqualTo(18);
    }

    @Test
    @DisplayName("같은 학생의 동시 신청에서도 시간 충돌 제한은 깨지지 않는다")
    // 체크리스트: 5. 동시성 제어 > 동일 학생이 동시에 여러 강좌를 신청할 때 시간 충돌 제한이 깨지지 않는다.
    void sameStudentConcurrentRequestsDoNotBreakScheduleConflictRule() throws Exception {
        Long studentId = 9_002L;

        // 동시 실행 단계: 시간이 겹치는 두 강좌를 같은 학생이 동시에 신청한다.
        int successCount = runConcurrentTasks(List.of(
                enrollmentTask(studentId, 61L),
                enrollmentTask(studentId, 91L)
        ));

        List<Enrollment> enrollments = enrollmentRepository.findAllByStudent_Id(studentId);

        assertThat(successCount).isEqualTo(1);
        assertThat(enrollments).hasSize(1);
        assertThat(enrollments)
                .extracting(enrollment -> enrollment.getCourse().getId())
                .allMatch(courseId -> courseId.equals(61L) || courseId.equals(91L));
    }

    private void fillCourseUntilOneSeatRemains(Course course) {
        List<Student> preloadStudents = studentRepository.findAll(PageRequest.of(200, course.getCapacity() - 1)).getContent();
        for (Student student : preloadStudents) {
            enrollmentService.enroll(student.getId(), course.getId());
        }
    }

    private void enrollSequentially(Long studentId, List<Long> courseIds) {
        for (Long courseId : courseIds) {
            enrollmentService.enroll(studentId, courseId);
        }
    }

    private List<Callable<Boolean>> createEnrollmentTasks(List<Student> students, Long courseId) {
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (Student student : students) {
            tasks.add(enrollmentTask(student.getId(), courseId));
        }
        return tasks;
    }

    private Callable<Boolean> enrollmentTask(Long studentId, Long courseId) {
        return () -> {
            try {
                EnrollmentResponse response = enrollmentService.enroll(studentId, courseId);
                return response != null;
            } catch (Exception exception) {
                return false;
            }
        };
    }

    private int runConcurrentTasks(List<Callable<Boolean>> tasks) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (Callable<Boolean> task : tasks) {
            futures.add(executorService.submit(() -> {
                ready.countDown();
                start.await();
                return task.call();
            }));
        }

        // 모든 작업이 각자 다른 스레드에서 시작 대기 상태에 들어간 뒤 한 번에 출발시킨다.
        ready.await();
        start.countDown();

        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount += 1;
            }
        }
        executorService.shutdown();
        return successCount;
    }
}
