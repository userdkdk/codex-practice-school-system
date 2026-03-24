# 대학교 수강신청 시스템

이 저장소의 제출 문서는 루트에 두고, 실제 애플리케이션 코드는 `src/schoolSystem` 프로젝트에서 구현한다.

## 실행 위치

```bash
cd src/schoolSystem
```

## 실행 방법

```bash
./gradlew bootRun
```

기본 포트는 `8080`이다.

## 헬스체크

```bash
curl http://localhost:8080/health
```

## 제출물 구성

- `README.md`: 실행 방법
- `AGENTS.md`: AI 에이전트 작업 지침
- `docs/`: 문제 분석 및 API 문서
- `prompts/`: 프롬프트 이력
- `src/schoolSystem`: Spring Boot + Gradle 프로젝트
