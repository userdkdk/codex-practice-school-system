# TEST

## 1. 목적

현재 테스트는 체크리스트 항목을 기준으로 다음 세 축을 검증한다.

- 단위 테스트: 시간표 충돌 판정 같은 도메인 규칙의 핵심 로직을 검증한다.
- 통합 테스트: 실제 Spring Boot 서버 또는 애플리케이션 컨텍스트 기준으로 API와 초기 데이터가 요구사항대로 동작하는지 검증한다.
- 동시성 테스트: 여러 스레드가 동시에 요청할 때 좌석 수, 학점 제한, 시간 충돌 규칙이 깨지지 않는지 검증한다.

## 2. 테스트 파일 구성

### 단위 테스트

#### `src/schoolSystem/src/test/java/com/musinsa/schoolsystem/unit/course/CourseScheduleSlotUnitTest.java`

검증 내용:

- 같은 요일에서 시간 구간이 겹치면 `overlaps()`가 `true`를 반환하는지 확인한다.
- 요일이 다르면 `overlaps()`가 `false`를 반환하는지 확인한다.
- 시작/종료 시간이 맞닿기만 하고 겹치지 않으면 `overlaps()`가 `false`를 반환하는지 확인한다.

### 통합 테스트

#### `src/schoolSystem/src/test/java/com/musinsa/schoolsystem/integration/health/HealthApiIntegrationTest.java`

검증 내용:

- 초기 데이터 준비 완료 후 `GET /health`가 HTTP 200과 `ok` 상태값을 반환하는지 확인한다.

#### `src/schoolSystem/src/test/java/com/musinsa/schoolsystem/integration/data/DataInitializationIntegrationTest.java`

검증 내용:

- 학과 10개 이상, 교수 100명 이상, 학생 10,000명 이상, 강좌 500개 이상이 생성되는지 확인한다.
- 초기 데이터 생성 시간이 1분 이내인지 확인한다.
- 학생 학번, 이름, 학과명, 학년과 강좌 코드, 이름, 시간표가 의미 있는 패턴을 가지는지 확인한다.
- 학생 학년, 강좌 학점, 강좌 정원, `enrolledCount`가 기본 비즈니스 규칙 범위를 벗어나지 않는지 확인한다.

#### `src/schoolSystem/src/test/java/com/musinsa/schoolsystem/integration/readiness/ReadinessIntegrationTest.java`

검증 내용:

- 초기 데이터 준비 전 상태에서는 `health`와 조회 API가 `503 Service Unavailable`로 차단되는지 확인한다.
- 초기 데이터 준비 완료 상태에서는 `health`가 다시 `200 OK`를 반환하는지 확인한다.

#### `src/schoolSystem/src/test/java/com/musinsa/schoolsystem/integration/query/QueryApiIntegrationTest.java`

검증 내용:

- 학생 목록 조회 API가 페이지 응답으로 동작하는지 확인한다.
- 교수 목록 조회 API가 페이지 응답으로 동작하는지 확인한다.
- 강좌 목록 조회 API가 페이지 응답으로 동작하는지 확인한다.
- 강좌 목록 응답에 `id`, `name`, `credits`, `capacity`, `enrolled`, `schedule`이 포함되는지 확인한다.
- `departmentId` 조건으로 학과별 강좌 조회가 가능한지 확인한다.

#### `src/schoolSystem/src/test/java/com/musinsa/schoolsystem/integration/enrollment/EnrollmentApiIntegrationTest.java`

검증 내용:

- 수강신청 성공 후 시간표 조회에 해당 강좌가 포함되는지 확인한다.
- 동일 학생이 동일 강좌를 다시 신청하면 중복신청 에러코드가 반환되는지 확인한다.
- 신청 내역 없이 취소하면 `취소할 강의가 없습니다.` 메시지와 에러코드가 반환되는지 확인한다.
- 이미 신청한 강좌와 시간이 겹치는 강좌를 신청하면 시간 충돌 에러코드가 반환되는지 확인한다.
- 18학점을 넘겨 신청하면 학점 초과 에러코드가 반환되는지 확인한다.
- 정원이 가득 찬 강좌를 신청하면 정원 초과 에러코드가 반환되는지 확인한다.
- 취소 후 빈 좌석이 다시 열리고 다른 학생이 재신청할 수 있는지 확인한다.

### 동시성 테스트

#### `src/schoolSystem/src/test/java/com/musinsa/schoolsystem/concurrency/enrollment/EnrollmentConcurrencyTest.java`

검증 내용:

- 잔여 좌석이 1개인 강좌에 여러 학생이 동시에 신청해도 성공은 정확히 1건만 발생하는지 확인한다.
- 그 상황에서도 최종 `enrolledCount`와 실제 `Enrollment` 건수가 정원을 넘지 않는지 확인한다.
- 같은 학생이 동시에 두 강좌를 신청할 때 학점 제한이 깨지지 않는지 확인한다.
- 같은 학생이 동시에 시간이 겹치는 두 강좌를 신청할 때 시간표 충돌 제한이 깨지지 않는지 확인한다.

## 3. 실행 방법

전체 테스트 실행:

```bash
cd src/schoolSystem
./gradlew test
```

특정 테스트만 실행:

```bash
cd src/schoolSystem
./gradlew test --tests "com.musinsa.schoolsystem.integration.data.DataInitializationIntegrationTest"
./gradlew test --tests "com.musinsa.schoolsystem.integration.readiness.ReadinessIntegrationTest"
./gradlew test --tests "com.musinsa.schoolsystem.concurrency.enrollment.EnrollmentConcurrencyTest"
```

## 4. 현재 자동화된 체크리스트 범위

자동 테스트로 확인되는 항목:

- 헬스체크 응답
- 초기 데이터 최소 개수
- 초기 데이터 생성 시간 1분 이내 여부
- 초기 데이터 준비 전 API 차단 여부
- 초기 데이터 기본 패턴과 규칙 범위
- 학생/교수/강좌 조회 API
- 학과별 강좌 조회
- 시간표 조회
- 수강신청/수강취소
- 중복신청, 학점 초과, 시간 충돌, 정원 초과 차단
- 취소 후 좌석 복구
- 같은 강좌 경쟁 상황의 동시성 제어
- 같은 학생 동시 신청 상황의 학점/시간 충돌 제어

## 5. 아직 수동 확인 또는 별도 방식이 필요한 항목

현재 자동 테스트로 직접 증명하지 않는 항목:

- `./gradlew bootRun`으로 장시간 서버가 유지되는지 여부
- Swagger 문서와 실제 응답 예시의 상세 일치 여부

이 항목들은 실행 환경이나 문서 비교 방식이 필요해 별도 수동 점검 또는 추가 측정 코드가 필요하다.
