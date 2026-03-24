package com.musinsa.schoolsystem.integration.health;

import static org.assertj.core.api.Assertions.assertThat;

import com.musinsa.schoolsystem.api.health.dto.HealthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("헬스체크 API는 200 OK와 ok 상태값을 반환한다")
    // 체크리스트: 1. 프로젝트 실행 가능 상태 > GET /health가 200을 반환한다.
    // 체크리스트: 1. 프로젝트 실행 가능 상태 > GET /health 응답 형식이 문서와 일치한다.
    // 체크리스트: 7. 테스트 > 헬스체크 테스트가 있다.
    void healthReturnsOk() {
        ResponseEntity<HealthResponse> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/health",
                HealthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("ok");
    }
}
