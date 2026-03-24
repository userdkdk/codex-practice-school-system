package com.musinsa.schoolsystem.integration.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.musinsa.schoolsystem.api.course.dto.CourseResponse;
import com.musinsa.schoolsystem.api.professor.dto.ProfessorResponse;
import com.musinsa.schoolsystem.api.student.dto.StudentResponse;
import com.musinsa.schoolsystem.domain.department.Department;
import com.musinsa.schoolsystem.domain.department.DepartmentRepository;
import com.musinsa.schoolsystem.global.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QueryApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("학생 목록 조회 API는 페이지 형태로 학생 정보를 반환한다")
    // 체크리스트: 3. 조회 API > 학생 목록 조회 API가 동작한다.
    // 체크리스트: 7. 테스트 > 조회 API 테스트가 있다.
    void studentListApiReturnsPagedStudents() {
        ResponseEntity<PageResponse<StudentResponse>> response = restTemplate.exchange(
                url("/students?page=0&size=5"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(5);
        assertThat(response.getBody().totalElements()).isGreaterThanOrEqualTo(10_000);
        assertThat(response.getBody().content()).allSatisfy(student -> {
            assertThat(student.id()).isNotNull();
            assertThat(student.studentNumber()).isNotBlank();
            assertThat(student.name()).isNotBlank();
            assertThat(student.departmentName()).isNotBlank();
            assertThat(student.grade()).isBetween(1, 4);
        });
    }

    @Test
    @DisplayName("교수 목록 조회 API는 페이지 형태로 교수 정보를 반환한다")
    // 체크리스트: 3. 조회 API > 교수 목록 조회 API가 동작한다.
    // 체크리스트: 7. 테스트 > 조회 API 테스트가 있다.
    void professorListApiReturnsPagedProfessors() {
        ResponseEntity<PageResponse<ProfessorResponse>> response = restTemplate.exchange(
                url("/professors?page=0&size=5"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(5);
        assertThat(response.getBody().totalElements()).isGreaterThanOrEqualTo(100);
        assertThat(response.getBody().content()).allSatisfy(professor -> {
            assertThat(professor.id()).isNotNull();
            assertThat(professor.name()).isNotBlank();
            assertThat(professor.departmentName()).isNotBlank();
        });
    }

    @Test
    @DisplayName("강좌 목록 조회 API는 필수 응답 필드를 모두 포함한다")
    // 체크리스트: 3. 조회 API > 강좌 목록 조회 API가 동작한다.
    // 체크리스트: 3. 조회 API > 강좌 목록 응답에 id가 포함된다.
    // 체크리스트: 3. 조회 API > 강좌 목록 응답에 name이 포함된다.
    // 체크리스트: 3. 조회 API > 강좌 목록 응답에 credits가 포함된다.
    // 체크리스트: 3. 조회 API > 강좌 목록 응답에 capacity가 포함된다.
    // 체크리스트: 3. 조회 API > 강좌 목록 응답에 enrolled가 포함된다.
    // 체크리스트: 3. 조회 API > 강좌 목록 응답에 schedule이 포함된다.
    // 체크리스트: 7. 테스트 > 조회 API 테스트가 있다.
    void courseListApiReturnsRequiredFields() {
        ResponseEntity<PageResponse<CourseResponse>> response = restTemplate.exchange(
                url("/courses?page=0&size=5"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(5);
        assertThat(response.getBody().totalElements()).isGreaterThanOrEqualTo(500);
        assertThat(response.getBody().content()).allSatisfy(course -> {
            assertThat(course.id()).isNotNull();
            assertThat(course.name()).isNotBlank();
            assertThat(course.credits()).isPositive();
            assertThat(course.capacity()).isPositive();
            assertThat(course.enrolled()).isBetween(0, course.capacity());
            assertThat(course.schedule()).isNotBlank();
        });
    }

    @Test
    @DisplayName("강좌 목록 조회 API는 학과별 필터링을 지원한다")
    // 체크리스트: 3. 조회 API > 학과별 강좌 조회가 가능하다.
    // 체크리스트: 7. 테스트 > 조회 API 테스트가 있다.
    void courseListApiCanBeFilteredByDepartment() {
        Department department = departmentRepository.findAll().stream().findFirst().orElseThrow();

        ResponseEntity<PageResponse<CourseResponse>> response = restTemplate.exchange(
                url("/courses?departmentId=" + department.getId() + "&page=0&size=10"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).isNotEmpty();
        assertThat(response.getBody().content()).allSatisfy(course ->
                assertThat(course.departmentName()).isEqualTo(department.getName())
        );
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
