# API 문서

## 공통 사항

- 기본 포맷은 JSON이다.
- 조회 API는 `page`, `size` 쿼리 파라미터를 사용한다.
- 초기 데이터 준비가 끝나기 전에는 `health`를 포함한 모든 API가 `503 Service Unavailable`로 응답한다.

## 공통 페이지 응답

조회 API는 아래 구조를 사용한다.

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 10000,
  "totalPages": 500,
  "first": true,
  "last": false
}
```

## 공통 에러 응답

```json
{
  "status": 409,
  "code": "ENROLLMENT_409_1",
  "message": "이미 신청한 강좌입니다.",
  "path": "/enrollments",
  "timestamp": "2026-03-24T12:00:00"
}
```

## 준비 중 에러 응답

초기 데이터 준비 전에는 아래와 같이 응답한다.

```json
{
  "status": 503,
  "code": "COMMON_503",
  "message": "초기 데이터 준비 중입니다. 잠시 후 다시 시도해주세요.",
  "path": "/health",
  "timestamp": "2026-03-24T12:00:00"
}
```

## 1. Health

### `GET /health`

설명:

- 초기 데이터 준비 완료 후 서버 정상 상태를 확인한다.

성공 응답:

```json
{
  "status": "ok"
}
```

에러 응답:

- `COMMON_503`: 초기 데이터 준비 중

## 2. Student

### `GET /students?page=0&size=20`

설명:

- 학생 목록을 페이지 형태로 조회한다.

성공 응답 예시:

```json
{
  "content": [
    {
      "id": 1,
      "studentNumber": "202600001",
      "name": "김민준",
      "departmentName": "컴퓨터공학과",
      "grade": 1
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 10000,
  "totalPages": 500,
  "first": true,
  "last": false
}
```

에러 응답:

- `COMMON_503`: 초기 데이터 준비 중

## 3. Professor

### `GET /professors?page=0&size=20`

설명:

- 교수 목록을 페이지 형태로 조회한다.

성공 응답 예시:

```json
{
  "content": [
    {
      "id": 1,
      "name": "이서연",
      "departmentName": "컴퓨터공학과"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

에러 응답:

- `COMMON_503`: 초기 데이터 준비 중

## 4. Course

### `GET /courses?page=0&size=20`

설명:

- 강좌 목록을 페이지 형태로 조회한다.

### `GET /courses?departmentId=1&page=0&size=20`

설명:

- 특정 학과에 개설된 강좌만 조회한다.

성공 응답 예시:

```json
{
  "content": [
    {
      "id": 10,
      "code": "CRS-1009",
      "name": "자료구조 심화 A",
      "departmentName": "컴퓨터공학과",
      "professorName": "박서준",
      "credits": 3,
      "capacity": 30,
      "enrolled": 12,
      "schedule": "MONDAY 09:00-10:30"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 500,
  "totalPages": 25,
  "first": true,
  "last": false
}
```

응답 필드:

- `id`
- `code`
- `name`
- `departmentName`
- `professorName`
- `credits`
- `capacity`
- `enrolled`
- `schedule`

에러 응답:

- `COMMON_503`: 초기 데이터 준비 중

## 5. Enrollment

### `POST /enrollments`

설명:

- 학생 ID와 강좌 ID를 받아 수강신청을 처리한다.

요청 예시:

```json
{
  "studentId": 1,
  "courseId": 10
}
```

성공 응답 예시:

```json
{
  "enrollmentId": 1,
  "studentId": 1,
  "courseId": 10,
  "enrolledCount": 1,
  "createdAt": "2026-03-24T12:00:00"
}
```

주요 에러 응답:

- `COMMON_400`: 필수 입력 누락
- `COMMON_503`: 초기 데이터 준비 중
- `STUDENT_404`: 학생 없음
- `COURSE_404`: 강좌 없음
- `COURSE_409_1`: 정원 초과
- `ENROLLMENT_409_1`: 중복 신청
- `ENROLLMENT_409_2`: 학점 초과
- `ENROLLMENT_409_3`: 시간 충돌

### `DELETE /enrollments?studentId=1&courseId=10`

설명:

- 특정 학생의 특정 강좌 신청을 취소한다.

성공 응답:

- `204 No Content`

주요 에러 응답:

- `COMMON_503`: 초기 데이터 준비 중
- `STUDENT_404`: 학생 없음
- `COURSE_404`: 강좌 없음
- `ENROLLMENT_404`: 취소할 강의가 없음

## 6. Timetable

### `GET /students/{studentId}/timetable`

설명:

- 특정 학생의 현재 신청 시간표를 조회한다.

성공 응답 예시:

```json
[
  {
    "courseId": 10,
    "courseCode": "CRS-1009",
    "courseName": "자료구조 심화 A",
    "credits": 3,
    "professorName": "박서준",
    "schedule": "MONDAY 09:00-10:30"
  }
]
```

주요 에러 응답:

- `COMMON_503`: 초기 데이터 준비 중
- `STUDENT_404`: 학생 없음
