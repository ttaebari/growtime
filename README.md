# 🌱 GrowTime - 산업기능요원 복무 관리 시스템

산업기능요원의 복무기간을 관리하고 회고를 작성할 수 있는 웹 애플리케이션입니다.

## 🚀 주요 기능

- **GitHub OAuth 로그인**: GitHub 계정으로 간편 로그인
- **복무기간 관리**: 남은 복무기간 추적
- **회고 작성**: 복무 중 경험과 성장 기록
- **사용자 프로필**: GitHub 프로필 정보 연동

## 🛠 기술 스택

- **Backend**: Spring Boot 3.5.3, Java 17
- **Database**: H2 Database (개발용)
- **OAuth**: GitHub OAuth 2.0
- **Build Tool**: Gradle
- **Frontend**: HTML, CSS, JavaScript

## 📋 사전 요구사항

- Java 17 이상
- Gradle 7.0 이상
- GitHub OAuth App 설정

## ⚙️ 설정 방법

### 1. GitHub OAuth App 생성

1. [GitHub Developer Settings](https://github.com/settings/developers)에 접속
2. "New OAuth App" 클릭
3. 다음 정보 입력:
   - **Application name**: GrowTime
   - **Homepage URL**: `http://localhost:8080`
   - **Authorization callback URL**: `http://localhost:8080/callback`
4. "Register application" 클릭
5. **Client ID**와 **Client Secret** 복사

### 2. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하거나 환경 변수를 설정:

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

또는 IDE에서 `GrowtimeApplication.java`를 실행

## 🌐 사용 방법

1. 브라우저에서 `http://localhost:8080` 접속
2. "GitHub로 로그인" 버튼 클릭
3. GitHub 인증 페이지에서 권한 승인
4. 로그인 완료 후 메인 페이지로 리다이렉트

## 📁 프로젝트 구조

```
growtime/
├── src/main/java/com/board/growtime/
│   ├── controller/
│   │   └── LoginController.java          # GitHub OAuth 로그인 처리
│   ├── entity/
│   │   └── User.java                     # 사용자 엔티티
│   ├── repository/
│   │   └── UserRepository.java           # 사용자 데이터 접근
│   ├── service/
│   │   └── UserService.java              # 사용자 비즈니스 로직
│   └── GrowtimeApplication.java          # 메인 애플리케이션
├── src/main/resources/
│   ├── application.properties            # 애플리케이션 설정
│   └── static/
│       └── index.html                    # 메인 페이지
└── build.gradle                          # 빌드 설정
```

## 🔧 API 엔드포인트

### 인증 관련

- `GET /login` - GitHub OAuth 로그인 URL 반환
- `GET /callback` - GitHub OAuth 콜백 처리

### 응답 예시

#### 로그인 URL 요청
```json
{
  "authUrl": "https://github.com/login/oauth/authorize?client_id=...&scope=read:user,user:email",
  "message": "GitHub 로그인을 위해 위 URL로 리다이렉트하세요"
}
```

#### 로그인 성공
```json
{
  "message": "GitHub 로그인 성공",
  "user": {
    "id": 1,
    "githubId": "12345678",
    "login": "username",
    "name": "User Name",
    "email": "user@example.com",
    "avatarUrl": "https://avatars.githubusercontent.com/...",
    "htmlUrl": "https://github.com/username",
    "company": "Company Name",
    "location": "Seoul, Korea",
    "bio": "Software Developer",
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "accessToken": "ghp_..."
}
```

## 🔒 보안 고려사항

- GitHub Client Secret은 환경 변수로 관리
- 액세스 토큰은 데이터베이스에 암호화하여 저장 권장
- 프로덕션 환경에서는 HTTPS 사용 필수

## 🚧 향후 개발 계획

- [ ] JWT 토큰 기반 인증 구현
- [ ] 복무기간 계산 및 표시 기능
- [ ] 회고 작성 및 관리 기능
- [ ] 사용자 프로필 편집 기능
- [ ] 데이터베이스 마이그레이션 (H2 → PostgreSQL/MySQL)

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