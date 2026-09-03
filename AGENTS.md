# AGENTS.md — fillsa_api 개발 규칙

fillsa_api(앱 + 어드민 백엔드)의 코드 컨벤션과 아키텍처 규칙이다.
구조·레이어·네이밍은 사내 표준 mono(`cic-lcmd-be-spring-boot-mono`)의 컨벤션을 따르되,
**스택은 Kotlin**, **응답 형식은 fillsa의 OpenAPI 계약**을 유지한다(하이브리드).

> AI 에이전트는 코드를 만들거나 고치기 전에 이 문서의 레이어 배치·네이밍·계약 규칙을 먼저 확인한다.
> 상위 워크스페이스 규칙은 `../AGENTS.md`, 서비스 도메인 지식은 `fillsa-wiki`가 SSOT다.

---

## 0. 스택 (고정)

| 항목 | 값 |
|------|-----|
| 언어 | **Kotlin 1.9.25** (버전 핀 — Spring BOM 강등 방지, `build.gradle.kts` 주석 참고) |
| 프레임워크 | Spring Boot 3.4.4, Spring Data JPA, Spring Security |
| 빌드 | Gradle (Kotlin DSL), 멀티모듈 |
| DB | **TiDB** (TiDB Cloud, MySQL 8.0 호환 — MySQL 이 아니다) / 캐시 Redis / 파일 S3→Cloudflare |
| 인증 | 앱 = OAuth+JWT / 어드민 = ID·PW + 어드민 전용 JWT |
| API 문서 | springdoc (Swagger) |

- **QueryDSL·Lombok 사용 안 함** (참고 프로젝트는 Java+QueryDSL+Lombok이지만 여기선 Kotlin data class + Spring Data JPA). 참고 프로젝트에서 가져오는 건 **구조·네이밍·레이어 규칙**이지 라이브러리가 아니다.
- Java 소스를 추가하지 않는다. 전부 Kotlin.

> ⚠️ **운영 DB 는 MySQL 이 아니라 TiDB 다.**
> MySQL 드라이버(`com.mysql.cj`)와 `MySQLDialect` 를 쓰고 `SELECT VERSION()` 이
> `8.0.11-TiDB-...` 로 나와서 MySQL 처럼 보이지만, 엔진이 다른 분산 DB 다.
> **MySQL 에서 되는 SQL 이 TiDB 에서 안 될 수 있다.**
> 예: `CREATE TABLE ... AS SELECT` 미지원 (ErrorCode 1105)
>
> 스키마·SQL 변경을 검증할 때 **MySQL 컨테이너로 갈음하지 말 것.**
> 운영과 같은 버전으로 띄워서 확인한다.
> ```bash
> docker run -d -p 14000:4000 pingcap/tidb:v8.5.3 --store=unistore --path=""
> ```
> 2026-09 에 이 구분을 놓쳐 배포 장애가 있었다 — `docs/migration/2026-09-flyway-bootstrap.md`

---

## 1. 모듈 구조 (멀티모듈 BFF)

```
fillsa_api/                       # 루트 = 애그리게이터 (산출물 없음, 플러그인 버전만 선언)
├── util/                         # 전 모듈 공용 (BaseEntity 등)
├── service/                      # 도메인 코어 — Entity + JpaRepository (공유)
│   ├── member  quote  notice
│   ├── popup   appVersion  admin
├── bff/
│   ├── app/                      # 앱 클라이언트 BFF  (com.fillsa.bff)
│   └── admin/                    # 어드민 콘솔 BFF   (com.fillsa.bff)
└── docs/admin-api.yaml           # ★ 어드민 API 계약 (OpenAPI 3.0, SSOT)
```

### 모듈 GAV (충돌 방지 — 이미 설정됨)
`bff:*` → `com.fillsa.bff`, `service:*` → `com.fillsa.service`.
`bff:admin`과 `service:admin`이 같은 이름이라 좌표를 상위 경로로 분리한다. `settings.gradle.kts`/루트 `build.gradle.kts` 참고.

### 🔴 최우선 제약 — 앱 API 경로·응답 불변
배포된 앱이 쓰는 `bff:app`의 URL 경로·응답 스키마는 **바꾸지 않는다**. 바뀌면 배포된 앱 전 버전이 깨지고 강제 업데이트(minVersion)가 필요해 사실상 금지 비용이다. `bff:app` 수정 시 경로·응답 호환성을 반드시 확인한다. → `fillsa-wiki [[BFF-재구조화]]`, `[[api]]`.

---

## 2. 레이어 배치 (참고 프로젝트 컨벤션 채택)

| 레이어 | 위치 | 역할 | 공유 |
|--------|------|------|------|
| **Entity** | `service/<domain>` | DB 테이블 매핑 | 공유 |
| **JpaRepository** | `service/<domain>` | 기본 CRUD 리포지토리 | 공유 |
| **Service** | `bff/<app\|admin>/service/<domain>` | 컨트롤러가 부르는 비즈니스 로직 | BFF별 분리 |
| **BFF 조회 Repository** | `bff/<app\|admin>/service/<domain>` | BFF 전용 조회(커스텀 쿼리) | BFF별 분리 |
| **DTO (Request/Response)** | `bff/<app\|admin>/service/<domain>` | 요청·응답 데이터 | BFF별 분리 |
| **Controller** | `bff/<app\|admin>/api/<domain>` | REST 엔드포인트(표면) | BFF별 분리 |

**핵심 원칙 — BFF 코드는 `service/<domain>` 한 폴더에 모은다.**
Entity·기본 리포지토리만 `service/*` 모듈에 두어 앱·어드민이 공유하고, **그 BFF의 Service·조회 Repository·DTO는 전부 `bff/<app|admin>/service/<domain>/` 한 폴더에 함께** 둔다. `api/<domain>`에는 **Controller만** (REST 표면). 앱과 어드민이 같은 도메인을 다뤄도 서비스 폴더는 BFF별로 따로 만든다(경계 격리).

참고 프로젝트의 이 패턴을 그대로 따른다:
```
bff/admin/src/main/kotlin/com/fillsa/admin/service/instagram/
├── InstagramHashtagMediaAdminService.kt        # 컨트롤러가 부르는 서비스
├── InstagramHashtagMediaAdminQueryRepository.kt # BFF 전용 조회
└── InstagramHashtagMediaDto.kt                  # DTO (요청/응답)
```

### 실제 패키지 레이아웃 (`bff/admin` 기준)
```
com/fillsa/admin/
├── AdminApplication.kt
├── config/              # AdminSecurityConfig 등
├── common/
│   ├── dto/             # PageEnvelope 등 공용 응답 구조
│   ├── security/        # AdminJwtTokenProvider, AdminJwtAuthenticationFilter,
│   │                    #   AdminPrincipal, AdminAuthenticationEntryPoint
│   └── exception/       # GlobalExceptionHandler, ErrorResponse
├── api/<domain>/        # <Domain>AdminController  ← 컨트롤러만
└── service/<domain>/    # <Domain>AdminService + <Domain>AdminQueryRepository + DTO
```
새 도메인을 추가할 때 이 레이아웃을 그대로 따른다.

> ⚠️ **기존 코드 정리 대상**: 현재 `quote`·`dailyquote`는 DTO가 `api/<domain>/*Dtos.kt`에, 조회 리포지토리가 별도 `repository/` 폴더에 흩어져 있다. 이는 이 규칙 확정 이전 코드이며, 이후 작업 시 위 레이아웃(`service/<domain>`에 Service·조회 Repository·DTO 통합, `repository/` 폴더 제거)으로 수렴시킨다.

---

## 3. 클래스 네이밍 컨벤션

| 종류 | 규칙 | 예시 |
|------|------|------|
| Entity | 도메인명 그대로 (접미사 없음) | `Quote`, `DailyQuote`, `Member` |
| JpaRepository (service 모듈) | `<Entity>Repository` | `DailyQuoteRepository` |
| BFF 조회 Repository (service/<domain>) | `<Domain>AdminQueryRepository` / `<Domain>QueryRepository` | `QuoteAdminQueryRepository` |
| Service (service/<domain>) | `<Domain>AdminService` / `<Domain>Service` | `QuoteAdminService` |
| Controller (api/<domain>) | `<Domain>AdminController` / `<Domain>Controller` | `QuoteAdminController` |
| 요청 DTO (service/<domain>) | `<Domain><동작>Request` | `QuoteSaveRequest` |
| 응답 DTO (service/<domain>) | `<Domain>Response` | `QuoteResponse` |
| DTO 파일 | 도메인별 묶음 파일 `<Domain>Dto.kt` | `QuoteDto.kt` |

> ℹ️ 참고 프로젝트는 요청=`Model`·응답=`View`·내부=`Dto`/`Request`, 서비스를 `QueryService`/`CommandService`로 분리한다. fillsa는 **OpenAPI 계약 스키마명과 일치**시키기 위해 **`Request`/`Response`** 를 쓰고, 서비스는 도메인당 단일 `AdminService`로 시작한다. 한 서비스가 비대해지면 참고 프로젝트처럼 **`<Domain>QueryService`(조회) / `<Domain>CommandService`(생성·수정)** 로 분리하는 것을 권장한다.

### DTO 작성 규칙
- 요청/응답은 **Kotlin `data class`**. Entity를 컨트롤러 밖으로 직접 노출하지 않는다.
- Entity → Response 매핑은 **companion `from()` / `of()` 팩토리**로 한다(생성자 직접 호출 금지).
  ```kotlin
  data class QuoteResponse(val quoteSeq: Long, /* ... */) {
      companion object {
          fun from(quote: Quote, assignedCount: Int) = QuoteResponse(/* ... */)
      }
  }
  ```

---

## 4. 컨트롤러 규칙

- 경로: 어드민은 `/api/admin/v1/<resource>`, 앱은 기존 `/api/v1|v2/<resource>` (앱 경로 불변, §1).
- 반환 타입은 **`ResponseEntity<T>`** (참고 프로젝트의 `ApiResult` 래퍼는 쓰지 않는다 — fillsa 계약이 순수 바디 + HTTP 상태코드 기반).
  - 조회 → `ResponseEntity.ok(...)`
  - 생성 → `ResponseEntity.status(HttpStatus.CREATED).body(...)`
  - 삭제/무바디 → `ResponseEntity.noContent().build()`
- **목록은 `PageEnvelope<T>`** 로 감싼다 — 계약의 `{ content, page: { number, size, totalElements, totalPages } }` 구조 (`common/dto/PageEnvelope.kt`). 직접 `Page`를 반환하지 않는다.
- 페이징·정렬은 스프링 `Pageable` 표준 파라미터 `?page=0&size=20&sort=필드,방향`. 화면별 기본 정렬은 `fillsa-wiki [[admin]]` 참고.
- **Swagger 필수**: 컨트롤러에 `@Tag(name, description)`, 각 오퍼레이션에 `@Operation(summary = ...)`.
- 컨트롤러는 얇게 — 검증·매핑·분기는 Service에. 컨트롤러에서 Entity·Repository 직접 접근 금지.

```kotlin
@RestController
@RequestMapping("/api/admin/v1/quotes")
@Tag(name = "quotes", description = "명언 관리")
class QuoteAdminController(
    private val quoteAdminService: QuoteAdminService,
) {
    @GetMapping
    @Operation(summary = "명언 목록 (기본 정렬 quoteSeq,desc)")
    fun getQuotes(pageable: Pageable, @RequestParam(required = false) keyword: String?):
        ResponseEntity<PageEnvelope<QuoteResponse>> =
        ResponseEntity.ok(quoteAdminService.list(pageable, keyword))
}
```

---

## 5. 계약 우선 (contract-first) — 어드민 API

- **`docs/admin-api.yaml`(OpenAPI 3.0)가 어드민 API의 SSOT다.** 프론트(`fillsa-admin`)가 이 파일에서 타입을 생성한다(`npm run gen:api`).
- 구현이 계약과 **드리프트하면 안 된다.** 경로·요청/응답 필드명·페이징 구조를 바꾸면 **먼저 `admin-api.yaml`을 수정**하고 프론트에 알린다.
- 계약 변경 시 `fillsa-wiki [[admin]]`의 API 표와 `raw/api/admin-api.yaml` 사본도 갱신 대상(위키 워크플로우).

---

## 6. 도메인 규칙 (지키지 않으면 데이터 사고)

- **명언 삭제 = soft delete.** `Quote.delYn`(`DEL_YN`) 토글. 물리 삭제 금지. **앱 조회 쿼리는 반드시 `delYn = 'N'` 가드**를 포함한다(삭제 건이 앱에 노출되면 안 됨 — `DailyQuoteRepository` 참고).
- **배정 이력 있는 명언 삭제 금지** → `409 Conflict`.
- **어드민 토큰 ≠ 앱 토큰.** `/api/admin/**`는 어드민 전용 JWT만 허용(별도 서명키 + `tokenType=ADMIN`). 앱 토큰으로 어드민 API 호출 불가가 필수 조건.
- 어드민 계정은 `admins` 테이블(bcrypt 해시, 평문 금지) + 역할 `AdminRole`(SUPER/ADMIN/OPERATOR). 가입 화면 없음(시드 등록).
- `members.ADMIN_YN`(앱 계정 팀원 표시)과 `admins`(콘솔 로그인 계정)를 혼동하지 않는다.

---

## 7. 에러 처리

- 각 BFF가 자체 `GlobalExceptionHandler`(`@ControllerAdvice`) + `ErrorResponse`를 가진다(`common/exception`). 앱과 어드민 핸들러를 공유하지 않는다.
- 응답 형식(앱 기준): `{ timestamp, httpStatus, errorCode, message }`. 에러코드 체계는 `fillsa-wiki [[api]]` 참고.
- 예외를 삼키지 않는다 — 로깅 후 적절한 상태코드로 변환. 컨트롤러에서 `try/catch`로 흐름 제어 금지.

---

## 8. 테스트

- 프레임워크: JUnit5 + Spring Boot Test. 모킹은 MockK(코틀린) 사용, Mockito와 혼용하지 않는다(테스트 간 오염 주의).
- 도메인 로직(Service) 단위 테스트 우선. 새 API는 최소 1개의 정상 + 경계(예: 배정 이력 409, 권한 없음 403) 테스트 동반.
- **테스트를 비활성화/스킵해서 통과시키지 않는다.** 시그니처 불일치는 테스트가 아니라 코드/테스트 정합을 맞춰 해결한다.
- ⚠️ 현재 `bff:app` 테스트에 사전 존재 이슈가 있을 수 있다(`feature/bff-restructure`) → `fillsa-wiki [[BFF-재구조화]]` "미해결 이슈". 앱 회귀 확인의 선행 조건.

---

## 9. Git / 브랜치

- 현재 재구조화 작업 브랜치: `feature/bff-restructure`.
- 커밋은 작게, 의미 단위로. `temp` 같은 미완성 뭉치 커밋에 서로 다른 작업을 섞지 않는다(테스트 깨짐 원인이 된 전례 있음).
- **커밋 메시지**: 한글 conventional commits — `feat:` / `fix:` / `refactor:` / `test:` / `chore:` 접두사 + "무엇을" 한 줄. 필요 시 본문에 상세.
- 🔴 **AI 공동작업 문구 금지**: 커밋 메시지에 `Co-Authored-By: Claude`, `Generated with Claude Code`, `🤖`, "Claude.ai와 함께" 등 **AI/도구 관련 서명·꼬리말을 절대 넣지 않는다.** 기본 템플릿에 그런 줄이 붙으면 항상 제거하고 커밋한다.
- 민감정보(비밀번호·키·토큰) 커밋 금지. 루트의 `application-*.yml`, `.pem`은 절대 노출하지 않는다.
- **로컬 실행 준비 (gitignore된 설정 파일)**: 아래는 git에 올리지 않으므로(gitignore) 클론에 안 딸려온다. 로컬/운영 프로파일을 띄우려면 **같은 경로에 수동 배치**해야 한다. 컴퓨터 간엔 git이 아니라 안전한 채널(AirDrop/1Password 등)로 옮긴다.
  - `bff/admin/src/main/resources/application-local.yml` · `application-prod.yml`
  - `bff/app/src/main/resources/application-local.yml` · `application-prod.yml`
- **저장소는 iCloud 등 클라우드 동기화 폴더 밖에 클론**한다(예: `~/dev/fillsa`). iCloud가 `.git`을 동기화하면 객체가 손상된다(전례: `bad tree object`).

---

## 10. 요약 — 새 어드민 기능 추가 체크리스트

1. `docs/admin-api.yaml`에 엔드포인트·스키마 먼저 정의(또는 확인).
2. Entity/JpaRepository가 없으면 `service/<domain>` 모듈에 추가(공유 코어).
3. `bff/admin/service/<domain>` 한 폴더에 `AdminService` + `AdminQueryRepository` + DTO(Request/Response, `from()` 매핑)를 모두 둔다.
4. `bff/admin/api/<domain>`에는 `Controller`만.
5. 컨트롤러: `ResponseEntity`, 목록은 `PageEnvelope`, `@Tag`/`@Operation`, 경로 `/api/admin/v1/*`.
6. soft delete·409·토큰 분리 등 §6 도메인 규칙 준수.
7. 서비스 단위 테스트(정상 + 경계) 작성, 전체 통과 확인.
8. 계약·구현이 어긋나면 `admin-api.yaml` 갱신 + 프론트 공유 + `fillsa-wiki` 반영.
