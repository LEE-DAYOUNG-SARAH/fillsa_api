# Fillsa API

Fillsa API는 Kotlin 기반의 Spring Boot 애플리케이션으로, Fillsa 서비스의 백엔드를 담당합니다.
회원 관리, 명언 제공, 공지사항 등의 기능을 위한 RESTful API를 제공하며, JWT 기반 인증과 외부 OAuth 로그인 연동을 지원합니다.

## 기술 스택

- **Language**: Kotlin 1.9
- **Framework**: Spring Boot 3
- **Database**: TiDB (MySQL 호환), Redis
- **ORM**: Spring Data JPA
- **Authentication**: JWT, OAuth 2.0
- **Documentation**: Swagger/OpenAPI
- **Build Tool**: Gradle
- **Containerization**: Docker

## 서버 아키텍처
<img width="1077" alt="스크린샷 2025-06-13 오후 5 26 04" src="https://github.com/user-attachments/assets/2da0f777-49fd-499b-a24d-a42fed7cfe6a" />
<br>

## 주요 기능

- Kotlin 1.9 & Spring Boot 3
- JWT 기반 인증 및 OAuth 로그인 지원
- 회원, 명언, 공지사항 관련 CRUD API 제공
- AWS S3를 통한 파일 저장
- Redis를 활용한 캐시 및 토큰 관리
- Spring Data JPA 기반 TiDB 연동 (MySQL 드라이버·방언 사용)
- Swagger/OpenAPI 문서 제공
- Docker 기반 배포 지원

## 시작하기

### 전제 조건

- JDK 17 이상
- Docker (선택사항)

### 빌드 및 실행

1. **프로젝트 빌드**
   ```bash
   ./gradlew build
   ```

2. **로컬 실행**
   ```bash
   ./gradlew bootRun
   ```

3. **Docker로 실행**
   ```bash
   docker build -t fillsa_api .
   docker run -p 8080:8080 fillsa_api
   ```

4. **테스트 실행**
   ```bash
   ./gradlew test
   ```

## API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 프로젝트 구조 (멀티모듈 BFF)

```
fillsa_api/                  # 루트(애그리게이터) — settings.gradle.kts / build.gradle.kts
├── util/                    # 전 모듈 공통 (응답 래퍼, 공통 예외, BaseEntity 등)
├── service/                 # 도메인 로직 모듈 (Entity/Repository/Service, 컨트롤러 없음)
│   ├── member/
│   ├── quote/               # 명언(필사 콘텐츠)
│   ├── notice/
│   ├── popup/
│   ├── appVersion/
│   └── admin/               # 어드민 계정(별도 admins 테이블)
└── bff/                     # 프론트별 BFF (부팅 가능한 Spring Boot 앱)
    ├── app/                 # 기존 모바일 앱 API (현재 기존 코드 전체 보유, :8080)
    └── admin/               # 필사 어드민 API (:8081)
```

> 의존 방향: `bff:*` → `service:*` → `util` (단방향).
> 도메인 로직은 `bff:app` 에서 `service:*` 로 점진 추출합니다.

### 빌드/실행 (멀티모듈)

```bash
./gradlew build                 # 전체 모듈 빌드
./gradlew :bff:app:bootRun      # 앱 BFF 실행 (8080)
./gradlew :bff:admin:bootRun    # 어드민 BFF 실행 (8081, 로컬은 H2)
```

## 환경 설정

설정 파일은 다음 위치에 있습니다:
- 메인 설정: `src/main/resources/application.yml`
- 테스트 설정: `src/test/resources/application-test.yml`

환경별 설정은 `application-{profile}.yml` 형태로 관리됩니다.

## 라이선스

이 프로젝트는 Fillsa 내부 사용을 목적으로 합니다.
