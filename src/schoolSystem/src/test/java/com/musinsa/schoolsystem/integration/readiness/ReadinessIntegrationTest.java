package com.musinsa.schoolsystem.integration.readiness;

import static org.assertj.core.api.Assertions.assertThat;

import com.musinsa.schoolsystem.api.health.dto.HealthResponse;
import com.musinsa.schoolsystem.global.readiness.ApplicationReadinessState;
import com.musinsa.schoolsystem.global.response.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReadinessIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationReadinessState readinessState;

    @Test
    @DisplayName("초기 데이터 준비 전에는 health와 조회 API가 호출되면 안 된다")
    // 체크리스트: 2. 초기 데이터 생성 > 초기 데이터 준비가 끝난 시점에 조회 API 호출이 가능하다.
    // 체크리스트: 1. 프로젝트 실행 가능 상태 > 애플리케이션 기동 직후 오류 없이 서버가 유지된다.
    void blocksApisUntilInitializationIsComplete() {
        readinessState.forceNotReadyForTest();
        try {
            ResponseEntity<ErrorResponse> healthResponse = restTemplate.getForEntity(
                    url("/health"),
                    ErrorResponse.class
            );
            ResponseEntity<ErrorResponse> studentsResponse = restTemplate.getForEntity(
                    url("/students"),
                    ErrorResponse.class
            );

            assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(healthResponse.getBody()).isNotNull();
            assertThat(healthResponse.getBody().code()).isEqualTo("COMMON_503");

            assertThat(studentsResponse.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(studentsResponse.getBody()).isNotNull();
            assertThat(studentsResponse.getBody().code()).isEqualTo("COMMON_503");
        } finally {
            readinessState.forceReadyForTest();
        }
    }

    @Test
    @DisplayName("초기 데이터 준비 완료 후에는 health가 200을 반환한다")
    // 체크리스트: 1. 프로젝트 실행 가능 상태 > GET /health가 200을 반환한다.
    // 체크리스트: 2. 초기 데이터 생성 > 초기 데이터 준비가 끝난 시점에 조회 API 호출이 가능하다.
    void returnsOkAfterInitializationCompletes() {
        readinessState.forceReadyForTest();

        ResponseEntity<HealthResponse> response = restTemplate.getForEntity(
                url("/health"),
                HealthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("ok");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
