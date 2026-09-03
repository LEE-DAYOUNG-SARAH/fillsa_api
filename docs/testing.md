# 테스트 실행 가이드

> 작성: 2026-09-03
> 현재 CI 는 테스트를 실행하지 않는다(`./gradlew clean build -x test`). 그 배경과 해소 조건을 남긴다.

---

## 실행 방법

### 전체

```bash
./gradlew build
```

`bff:app` 통합 테스트는 **Redis 가 필요하다.** 없으면 컨텍스트 로딩부터 실패한다.

```bash
docker run -d --name fillsa-test-redis -p 6380:6379 redis:7-alpine
./gradlew build
docker rm -f fillsa-test-redis
```

포트가 **6380** 인 것에 주의한다(운영·로컬은 6379). `bff/app/src/test/resources/application-test.yml` 에 그렇게 잡혀 있다.

### 모듈별

```bash
./gradlew :bff:admin:test          # Redis 불필요
./gradlew :bff:app:test            # Redis 필요
./gradlew :bff:app:test --tests "*MemberQuoteAnswerServiceTest"
```

## 환경 구성

| | `bff:app` | `bff:admin` |
|---|---|---|
| DB | H2 인메모리 | H2 인메모리 |
| 스키마 | `ddl-auto: create-drop` | `ddl-auto: create-drop` |
| Redis | **필요 (6380)** | 불필요 |
| Flyway | OFF | OFF |
| 외부 연동 | 더미값 (R2·FCM·OAuth) | — |

R2·FCM 등 외부 연동은 더미값으로 빈만 뜨게 해 둔다. 실제 호출은 하지 않는다.

## 알려진 실패 2건

`main` 기준으로 아래 2건이 실패한다. **기능 결함이 아니라 테스트 자체의 문제**다.

### `JwtTokenProviderTest > 토큰 발급용`

`@ActiveProfiles("local")` 로 선언되어 있는데 `application-local.yml` 이 없다. 이름대로 **토큰을 수동으로 발급받기 위한 개발 유틸**로 보인다.

정리 방향은 둘 중 하나다.
- 유틸이 맞다면 `@Disabled` 를 붙여 일반 실행에서 제외
- 계속 쓸 것이면 `test` 프로필로 옮기거나 `application-local.yml` 추가

### `MemberQuoteReadServiceTest > 회원 명언 목록 조회 성공 - 내용이 있는 모든 명언 조회`

2건을 기대하는데 1건이 조회된다. **타이핑만 하고 완료하지 않은 항목**이 목록에서 빠지기 때문이다.

```kotlin
fun isViewQuoteData() = completed || likeYn == "Y" || hasAnswer()
```

테스트가 기대하는 조건은 오히려 `hasContent()`(타이핑 || 이미지 || 좋아요) 쪽인데, 이 메서드는 **정의만 있고 아무 데서도 쓰이지 않는다.** 의미가 바뀌면서 테스트만 남은 것으로 보인다.

**목록 화면 동작에 영향이 있어 제품 판단이 필요하다.** 임의로 바꾸지 않았다.

## CI 에서 테스트를 켜려면

`.github/workflows/deploy.yml` 이 `-x test` 로 건너뛰고 있다. 켜기 전에 아래가 해소되어야 한다.

1. **Redis 확보** — GitHub Actions `services:` 로 Redis 컨테이너를 띄우거나, testcontainers 를 도입한다. 후자가 로컬 실행 편의까지 함께 해결한다
2. **알려진 실패 2건 정리** — 위 참고
3. (선택) **마이그레이션 검증** — 테스트는 H2 라 Flyway 스크립트가 검증되지 않는다. testcontainers 로 MySQL 을 띄우면 이 공백도 메울 수 있다(`docs/flyway-adoption.md`)

1·2 만 해소되면 `-x test` 를 뗄 수 있다.

## 과거에 있었던 문제

같은 함정을 다시 밟지 않도록 기록해 둔다.

**테스트 모듈이 컴파일되지 않던 시기가 있었다.** `-x test` 로 CI 가 건너뛰고 있어 오래 드러나지 않았다. 원인은 두 가지였다.
- 응답 DTO 필드명이 바뀌었는데 테스트가 옛 이름을 참조 (`completed` → `completedChanged`)
- R2 전환 시 `application-test.yml` 에 `cloud.r2.*` 가 추가되지 않아 `s3Client` 빈 생성 실패 → 모든 `@SpringBootTest` 가 죽음

**mockk 의 `every` 에 Mockito 의 `any()` 를 섞어 쓰던 곳이 있었다.** Mockito 매처는 호출 시 매처 스택에 쌓이는데 mockk 이 소비하지 않는다. 남은 매처가 같은 JVM 에서 **뒤이어 실행되는 무관한 테스트**에서 터진다. 단독 실행하면 통과하는 순서 의존 실패라 원인 파악이 어렵다.

```kotlin
// 하지 말 것
import org.mockito.kotlin.any
every { service.foo(any()) } returns any()   // any() 는 값이 아니라 매처다

// 이 저장소는 springmockk 를 쓴다. every 블록 안의 any() 는 mockk 의 것을 쓴다
every { service.foo(any()) } returns mockk()   // 반환값이 있는 경우
every { service.bar(any()) } just Runs         // Unit 반환인 경우
```

캐시(Redis)는 `@Transactional` 롤백으로 정리되지 않는다. 앞선 테스트가 남긴 캐시가 다음 테스트로 새므로, 캐시를 타는 경로를 검증할 때는 `@BeforeEach` 에서 키를 비운다.
