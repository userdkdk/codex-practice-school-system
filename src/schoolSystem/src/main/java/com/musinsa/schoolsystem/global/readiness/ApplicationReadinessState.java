package com.musinsa.schoolsystem.global.readiness;

import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApplicationReadinessState {

    private boolean ready;
    private Instant initializationStartedAt;
    private Instant initializationCompletedAt;
    private Duration initializationDuration = Duration.ZERO;

    public synchronized void markInitializationStarted() {
        this.ready = false;
        this.initializationStartedAt = Instant.now();
        this.initializationCompletedAt = null;
        this.initializationDuration = Duration.ZERO;
    }

    public synchronized void markReady() {
        Instant completedAt = Instant.now();
        this.initializationCompletedAt = completedAt;
        if (initializationStartedAt != null) {
            this.initializationDuration = Duration.between(initializationStartedAt, completedAt);
        }
        this.ready = true;
    }

    public synchronized void forceNotReadyForTest() {
        this.ready = false;
    }

    public synchronized void forceReadyForTest() {
        this.ready = true;
    }
}
