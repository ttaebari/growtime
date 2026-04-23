# GrowTime Backend

산업기능요원 복무 관리와 회고 작성을 위한 GrowTime 백엔드입니다. Spring Boot, Kotlin, PostgreSQL 기반으로 동작하며 Render에 배포합니다.

## 주요 기능

- GitHub OAuth 로그인
- 사용자 프로필 저장 및 조회
- 입영일/전역일 저장
- D-Day와 복무 진행률 계산
- 회고 CRUD, 검색, 개수 조회
- Actuator health check

## 기술 스택

- Kotlin 2.0
- Spring Boot 3.5
- Spring Data JPA
- PostgreSQL
- Gradle
- Docker

## 로컬 실행

```bash
./gradlew bootRun
```

기본 서버 포트는 `8196`입니다.

## 로컬 PostgreSQL

로컬 DB가 필요하면 `docker-compose.yml`을 사용할 수 있습니다.

```bash
docker compose up -d
```

기본 연결 정보는 `application.yml`의 fallback 값과 맞춰져 있습니다.

- URL: `jdbc:postgresql://localhost:5432/growtime`
- Username: `postgres`
- Password: `qwer1234!!`

## 환경 변수

운영 환경에서는 아래 값을 Render 또는 GitHub Secrets에 등록합니다.

```bash
PORT=8196
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,https://growtime-frontend-nine.vercel.app
```

## GitHub OAuth

GitHub OAuth App에는 프론트엔드 주소 기준 callback URL을 등록합니다.

```text
http://localhost:3000/callback
https://growtime-frontend-nine.vercel.app/callback
```

## 빌드와 검증

```bash
./gradlew clean build -x test
./gradlew test
```

## 배포

- Render service: `growtime-backend`
- Health check: `/actuator/health`
- Dockerfile 기반으로 빌드합니다.
- `master` 브랜치 변경 후 GitHub Actions에서 Render deploy hook을 호출합니다.

## 주요 API

- `GET /login`: GitHub OAuth 로그인 URL
- `GET /callback`: GitHub OAuth callback 처리
- `GET /api/user/{githubId}`: 사용자 정보 조회
- `POST /api/user/{githubId}/service-dates`: 복무 날짜 저장
- `GET /api/user/{githubId}/d-day`: D-Day 정보 조회
- `POST /api/notes/{githubId}`: 회고 작성
- `GET /api/notes/{githubId}`: 회고 목록 조회
- `GET /api/notes/{githubId}/{noteId}`: 회고 상세 조회
- `PUT /api/notes/{githubId}/{noteId}`: 회고 수정
- `DELETE /api/notes/{githubId}/{noteId}`: 회고 삭제
- `GET /api/notes/{githubId}/search`: 회고 검색
- `GET /api/notes/{githubId}/count`: 회고 개수 조회
