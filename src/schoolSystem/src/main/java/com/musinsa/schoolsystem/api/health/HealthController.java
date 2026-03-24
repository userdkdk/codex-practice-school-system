package com.musinsa.schoolsystem.api.health;

import com.musinsa.schoolsystem.api.health.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Tag(name = "Health", description = "서버 상태 확인 API")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "헬스체크", description = "서버가 정상 기동 중인지 확인한다.")
    public HealthResponse health() {
        return new HealthResponse("ok");
    }
}
