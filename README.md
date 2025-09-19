# 🌱 GrowTime - 산업기능요원 복무 관리 시스템

산업기능요원의 복무기간을 관리하고 회고를 작성할 수 있는 웹 애플리케이션입니다.

## 🚀 주요 기능

-   **GitHub OAuth 로그인**: GitHub 계정으로 간편 로그인
-   **복무기간 관리**: 입영일/제대일 설정 및 남은 복무기간 추적
-   **D-Day 계산**: 제대까지 남은 일수와 복무 진행률 표시
-   **회고 작성**: 복무 중 경험과 성장 기록, 검색 및 관리
-   **사용자 프로필**: GitHub 프로필 정보 연동

## 🛠 기술 스택

-   **Backend**: Spring Boot 3.5.3, Kotlin
-   **Database**: H2 Database (개발용) / PostgreSQL (프로덕션)
-   **ORM**: Spring Data JPA, Hibernate
-   **OAuth**: GitHub OAuth 2.0
-   **Build Tool**: Gradle with Kotlin DSL
-   **Architecture**: 도메인 기반 패키지 구조

## 📋 사전 요구사항

-   Java 17 이상
-   Gradle 7.0 이상
-   GitHub OAuth App 설정

## ⚙️ 설정 방법

### 1. GitHub OAuth App 생성

1. [GitHub Developer Settings](https://github.com/settings/developers)에 접속
2. "New OAuth App" 클릭
3. 다음 정보 입력:
    - **Application name**: GrowTime
    - **Homepage URL**: `http://localhost:8088`
    - **Authorization callback URL**: `http://localhost:8088/callback`
4. "Register application" 클릭
5. **Client ID**와 **Client Secret** 복사

### 2. 환경 변수 설정

`src/main/resources/application.yml`에서 GitHub OAuth 설정:

```yaml
github:
    clientId: ${GITHUB_CLIENT_ID:your_client_id_here}
    clientSecret: ${GITHUB_CLIENT_SECRET:your_client_secret_here}
```

환경 변수 설정:

```bash
# Windows
set GITHUB_CLIENT_ID=your_client_id_here
set GITHUB_CLIENT_SECRET=your_client_secret_here

# macOS/Linux
export GITHUB_CLIENT_ID=your_client_id_here
export GITHUB_CLIENT_SECRET=your_client_secret_here
```

### 3. 애플리케이션 실행

```bash
# 프로젝트 디렉토리로 이동
cd growtime

# 애플리케이션 실행
./gradlew bootRun
```

또는 IDE에서 `GrowtimeApplication.kt`를 실행

## 🌐 사용 방법

1. 브라우저에서 `http://localhost:8088` 접속
2. "GitHub로 로그인" 버튼 클릭
3. GitHub 인증 페이지에서 권한 승인
4. 로그인 완료 후 메인 페이지로 리다이렉트
5. 입영일/제대일 설정하여 복무 관리 시작
6. 회고 작성 및 관리

## 📁 프로젝트 구조

```
growtime/
├── src/main/kotlin/com/board/growtime/
│   ├── GrowtimeApplication.kt             # 메인 애플리케이션
│   ├── config/
│   │   └── CorsConfig.kt                  # CORS 설정
│   ├── user/                              # 사용자 도메인
│   │   ├── User.kt                        # 사용자 엔티티
│   │   ├── UserRepository.kt              # 사용자 데이터 접근
│   │   ├── UserService.kt                 # 사용자 비즈니스 로직
│   │   ├── UserController.kt              # 사용자 API 컨트롤러
│   │   └── LoginController.kt             # GitHub OAuth 로그인
│   └── note/                              # 회고 도메인
│       ├── Note.kt                        # 회고 엔티티
│       ├── NoteRepository.kt              # 회고 데이터 접근
│       ├── NoteService.kt                 # 회고 비즈니스 로직
│       └── NoteController.kt              # 회고 API 컨트롤러
├── src/main/resources/
│   ├── application.yml                    # 애플리케이션 설정
│   └── static/
│       └── index.html                     # 메인 페이지
└── build.gradle                           # 빌드 설정
```

## 🔧 API 엔드포인트

### 인증 관련

-   `GET /login` - GitHub OAuth 로그인 URL 반환
-   `GET /callback` - GitHub OAuth 콜백 처리

### 사용자 관리

-   `GET /api/user/{githubId}` - 사용자 정보 조회
-   `POST /api/user/{githubId}/service-dates` - 입영/제대 날짜 설정
-   `GET /api/user/{githubId}/d-day` - D-Day 정보 조회

### 회고 관리

-   `POST /api/notes/{githubId}` - 회고 작성
-   `GET /api/notes/{githubId}` - 회고 목록 조회 (페이징)
-   `GET /api/notes/{githubId}/{noteId}` - 회고 상세 조회
-   `PUT /api/notes/{githubId}/{noteId}` - 회고 수정
-   `DELETE /api/notes/{githubId}/{noteId}` - 회고 삭제
-   `GET /api/notes/{githubId}/search` - 회고 검색
-   `GET /api/notes/{githubId}/count` - 회고 개수 조회

### 요청/응답 예시

#### 복무 날짜 설정

```bash
POST /api/user/{githubId}/service-dates
Content-Type: application/x-www-form-urlencoded

entryDate=2024-01-01&dischargeDate=2025-12-31
```

#### D-Day 정보 조회 응답

```json
{
    "dDay": 365,
    "serviceDays": 100,
    "totalServiceDays": 730,
    "entryDate": "2024-01-01",
    "dischargeDate": "2025-12-31",
    "progressPercentage": 13.7
}
```

#### 회고 작성

```json
POST /api/notes/{githubId}
Content-Type: application/json

{
  "title": "오늘의 회고",
  "content": "오늘 배운 것들과 느낀 점들..."
}
```

## 🏗 아키텍처 특징

### 도메인 기반 패키지 구조

-   **user 패키지**: 사용자 관련 모든 클래스
-   **note 패키지**: 회고 관련 모든 클래스
-   높은 응집도와 낮은 결합도를 위한 설계

### Kotlin 활용

-   **널 안전성**: Optional 대신 nullable 타입 활용
-   **데이터 클래스**: 간결한 엔티티 정의
-   **확장 함수**: 코드 재사용성 향상
-   **함수형 프로그래밍**: 컬렉션 처리 최적화

### JPA Auditing

-   생성일/수정일 자동 관리
-   `@CreatedDate`, `@LastModifiedDate` 활용

## 🔒 보안 고려사항

-   GitHub Client Secret은 환경 변수로 관리
-   액세스 토큰은 데이터베이스에 저장
-   CORS 설정으로 허용된 origin만 접근 가능
-   프로덕션 환경에서는 HTTPS 사용 필수

## 🚧 향후 개발 계획

-   [x] ~~JWT 토큰 기반 인증 구현~~ ✅ GitHub OAuth 완료
-   [x] ~~복무기간 계산 및 표시 기능~~ ✅ D-Day 계산 완료
-   [x] ~~회고 작성 및 관리 기능~~ ✅ CRUD 기능 완료
-   [ ] 사용자 프로필 편집 기능
-   [ ] 회고 카테고리/태그 기능
-   [ ] 데이터베이스 마이그레이션 (H2 → PostgreSQL)
-   [ ] 프론트엔드 React/Vue.js 전환
-   [ ] 알림 기능 (제대일 임박 등)

## 🛠 개발 도구

### 빌드 명령어

```bash
# 컴파일 및 빌드
./gradlew build

# 테스트 제외 빌드
./gradlew build -x test

# 개발 서버 실행
./gradlew bootRun

# 테스트 실행
./gradlew test
```

### IDE 설정

-   IntelliJ IDEA 권장
-   Kotlin 플러그인 필수
-   Spring Boot 플러그인 권장

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 생성해 주세요.
