package com.musinsa.schoolsystem.integration.enrollment;

import static org.assertj.core.api.Assertions.assertThat;

import com.musinsa.schoolsystem.api.enrollment.dto.EnrollmentRequest;
import com.musinsa.schoolsystem.api.enrollment.dto.EnrollmentResponse;
import com.musinsa.schoolsystem.api.enrollment.dto.TimetableCourseResponse;
import com.musinsa.schoolsystem.domain.course.Course;
import com.musinsa.schoolsystem.domain.course.CourseRepository;
import com.musinsa.schoolsystem.domain.student.Student;
import com.musinsa.schoolsystem.domain.student.StudentRepository;
import com.musinsa.schoolsystem.global.response.ErrorResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EnrollmentApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("수강신청 성공 후 시간표 조회에 신청 강좌가 포함된다")
    // 체크리스트: 3. 조회 API > 내 시간표 조회 API가 동작한다.
    // 체크리스트: 4. 수강신청 규칙 > 수강신청 API가 동작한다.
    // 체크리스트: 7. 테스트 > 수강신청 성공 테스트가 있다.
    void enrollAndReadTimetable() {
        Student student = studentAt(0);
        Course course = courseAt(0);

        ResponseEntity<EnrollmentResponse> enrollResponse = restTemplate.postForEntity(
                url("/enrollments"),
                new EnrollmentRequest(student.getId(), course.getId()),
                EnrollmentResponse.class
        );

        assertThat(enrollResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(enrollResponse.getBody()).isNotNull();
        assertThat(enrollResponse.getBody().studentId()).isEqualTo(student.getId());
        assertThat(enrollResponse.getBody().courseId()).isEqualTo(course.getId());
        assertThat(enrollResponse.getBody().enrolledCount()).isEqualTo(1);

        ResponseEntity<List<TimetableCourseResponse>> timetableResponse = restTemplate.exchange(
                url("/students/" + student.getId() + "/timetable"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(timetableResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(timetableResponse.getBody()).isNotNull();
        assertThat(timetableResponse.getBody()).extracting(TimetableCourseResponse::courseId).contains(course.getId());
    }

    @Test
    @DisplayName("동일 강좌 중복 신청 시 중복신청 에러를 반환한다")
    // 체크리스트: 4. 수강신청 규칙 > 동일 강좌 중복 신청이 차단된다.
    void duplicateEnrollmentReturnsDomainErrorCode() {
        Student student = studentAt(1);
        Course course = courseAt(1);
        EnrollmentRequest request = new EnrollmentRequest(student.getId(), course.getId());

        ResponseEntity<EnrollmentResponse> firstResponse = restTemplate.postForEntity(
                url("/enrollments"),
                request,
                EnrollmentResponse.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ErrorResponse> duplicateResponse = restTemplate.exchange(
                url("/enrollments"),
                HttpMethod.POST,
                new HttpEntity<>(request),
                ErrorResponse.class
        );

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicateResponse.getBody()).isNotNull();
        assertThat(duplicateResponse.getBody().code()).isEqualTo("ENROLLMENT_409_1");
    }

    @Test
    @DisplayName("취소 대상이 없으면 취소 불가 에러를 반환한다")
    // 체크리스트: 4. 수강신청 규칙 > 수강취소 API가 동작한다.
    void cancelWithoutEnrollmentReturnsNotFoundError() {
        Student student = studentAt(2);
        Course course = courseAt(2);

        ResponseEntity<ErrorResponse> cancelResponse = restTemplate.exchange(
                url("/enrollments?studentId=" + student.getId() + "&courseId=" + course.getId()),
                HttpMethod.DELETE,
                null,
                ErrorResponse.class
        );

        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(cancelResponse.getBody()).isNotNull();
        assertThat(cancelResponse.getBody().code()).isEqualTo("ENROLLMENT_404");
        assertThat(cancelResponse.getBody().message()).isEqualTo("취소할 강의가 없습니다.");
    }

    @Test
    @DisplayName("시간이 겹치는 강좌는 신청할 수 없다")
    // 체크리스트: 4. 수강신청 규칙 > 시간표 충돌 신청이 차단된다.
    // 체크리스트: 7. 테스트 > 시간 충돌 실패 테스트가 있다.
    void scheduleConflictReturnsDomainErrorCode() {
        Student student = studentAt(3);
        Course firstCourse = courseAt(0);
        Course secondCourse = courseAt(30);

        ResponseEntity<EnrollmentResponse> firstResponse = restTemplate.postForEntity(
                url("/enrollments"),
                new EnrollmentRequest(student.getId(), firstCourse.getId()),
                EnrollmentResponse.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ErrorResponse> conflictResponse = restTemplate.exchange(
                url("/enrollments"),
                HttpMethod.POST,
                new HttpEntity<>(new EnrollmentRequest(student.getId(), secondCourse.getId())),
                ErrorResponse.class
        );

        assertThat(conflictResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(conflictResponse.getBody()).isNotNull();
        assertThat(conflictResponse.getBody().code()).isEqualTo("ENROLLMENT_409_3");
    }

    @Test
    @DisplayName("18학점을 초과하는 신청은 차단된다")
    // 체크리스트: 4. 수강신청 규칙 > 18학점 초과 신청이 차단된다.
    // 체크리스트: 7. 테스트 > 학점 초과 실패 테스트가 있다.
    void creditLimitExceededReturnsDomainErrorCode() {
        Student student = studentAt(4);
        List<Course> acceptedCourses = List.of(courseAt(1), courseAt(2), courseAt(4), courseAt(5), courseAt(7));
        Course exceedCourse = courseAt(8);

        for (Course course : acceptedCourses) {
            ResponseEntity<EnrollmentResponse> response = restTemplate.postForEntity(
                    url("/enrollments"),
                    new EnrollmentRequest(student.getId(), course.getId()),
                    EnrollmentResponse.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        ResponseEntity<ErrorResponse> exceedResponse = restTemplate.exchange(
                url("/enrollments"),
                HttpMethod.POST,
                new HttpEntity<>(new EnrollmentRequest(student.getId(), exceedCourse.getId())),
                ErrorResponse.class
        );

        assertThat(exceedResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exceedResponse.getBody()).isNotNull();
        assertThat(exceedResponse.getBody().code()).isEqualTo("ENROLLMENT_409_2");
    }

    @Test
    @DisplayName("정원이 가득 찬 강좌는 신청할 수 없다")
    // 체크리스트: 4. 수강신청 규칙 > 정원이 가득 찬 강좌 신청이 차단된다.
    // 체크리스트: 7. 테스트 > 정원 초과 실패 테스트가 있다.
    void fullCourseReturnsDomainErrorCode() {
        Course course = courseAt(31);
        List<Student> students = studentRepository.findAll(PageRequest.of(100, course.getCapacity() + 1)).getContent();

        for (int i = 0; i < course.getCapacity(); i++) {
            ResponseEntity<EnrollmentResponse> response = restTemplate.postForEntity(
                    url("/enrollments"),
                    new EnrollmentRequest(students.get(i).getId(), course.getId()),
                    EnrollmentResponse.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        ResponseEntity<ErrorResponse> fullResponse = restTemplate.exchange(
                url("/enrollments"),
                HttpMethod.POST,
                new HttpEntity<>(new EnrollmentRequest(students.get(course.getCapacity()).getId(), course.getId())),
                ErrorResponse.class
        );

        assertThat(fullResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(fullResponse.getBody()).isNotNull();
        assertThat(fullResponse.getBody().code()).isEqualTo("COURSE_409_1");
    }

    @Test
    @DisplayName("수강취소 후 빈 좌석은 다시 신청 가능해야 한다")
    // 체크리스트: 4. 수강신청 규칙 > 수강취소 API가 동작한다.
    // 체크리스트: 4. 수강신청 규칙 > 취소 후에는 다시 잔여 좌석이 반영된다.
    void cancelRestoresSeatAndAllowsAnotherEnrollment() {
        Course course = courseAt(120);
        List<Student> students = studentsByIdRange(7_001L, course.getCapacity() + 1);

        for (int i = 0; i < course.getCapacity(); i++) {
            ResponseEntity<EnrollmentResponse> response = restTemplate.postForEntity(
                    url("/enrollments"),
                    new EnrollmentRequest(students.get(i).getId(), course.getId()),
                    EnrollmentResponse.class
            );
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        ResponseEntity<Void> cancelResponse = restTemplate.exchange(
                url("/enrollments?studentId=" + students.get(0).getId() + "&courseId=" + course.getId()),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(cancelResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<EnrollmentResponse> reEnrollResponse = restTemplate.postForEntity(
                url("/enrollments"),
                new EnrollmentRequest(students.get(course.getCapacity()).getId(), course.getId()),
                EnrollmentResponse.class
        );

        assertThat(reEnrollResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reEnrollResponse.getBody()).isNotNull();
        assertThat(reEnrollResponse.getBody().enrolledCount()).isEqualTo(course.getCapacity());
    }

    private List<Student> studentsByIdRange(long startId, int count) {
        return java.util.stream.LongStream.range(startId, startId + count)
                .mapToObj(id -> studentRepository.findById(id).orElseThrow())
                .toList();
    }

    private Student studentAt(int index) {
        return studentRepository.findById((long) index + 1).orElseThrow();
    }

    private Course courseAt(int index) {
        return courseRepository.findById((long) index + 1).orElseThrow();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
