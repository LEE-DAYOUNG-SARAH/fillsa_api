# 요청당 DB 왕복 축소 — 원인·조치·측정

> 작성: 2026-10-20
> 브랜치: `perf/reduce-db-roundtrips` (PR #35) → `main`
> 상태: **2026-10-20 배포 완료**(merge `4205165`, deploy run 39364036520 성공). 배포 후 운영 실측을 §6 에 기록했다.

---

## 요약

| | 공개 GET (`/api/v1/app-versions`, `/notices`) | 로그인 GET (`/api/v1/member-streaks`) | 버전 헤더 누락 |
|---|---|---|---|
| **변경 전** DB 왕복 | 12회 (트랜잭션 2개) | 18회 (트랜잭션 3개) | 5회 (쿼리 없이 트랜잭션만) |
| **변경 후** DB 왕복 | **2회** | **2회** | **0회** |
| 운영 TTFB 실측 (변경 전) | ≈1.33초 | 미측정 (왕복 18회 기준 추정 ≈1.8초) | ≈0.67초 |
| 운영 TTFB **실측 (변경 후)** | **≈0.27초** | 미측정 (운영 토큰 없음) | **≈0.18초** |

왕복 횟수는 로컬 TiDB general log 로 직접 센 값이고, TTFB 는 배포 전후 같은 PC 에서 같은 스크립트로 6회씩 잰 값이다.
공개 GET 기준 **1.33초 → 0.27초 (약 5배)**. 네트워크 기준선(403, DB 미접근)이 ≈0.19초이므로 서버 처리 시간은 ≈1.14초 → ≈0.08초다.
느려진 원인은 쿼리가 아니라 **트랜잭션 하나를 여닫는 데 드는 왕복 5회**였다.

---

## 1. 처음에 뭐가 문제였나

### 증상

2026-10-20 운영 `api.fillsa.com` 에 요청 종류별로 6회씩 TTFB 를 쟀다(PR 설명의 치앙마이 측정 + 이 문서 작성 시 재측정).

| 요청 | DB 동작 | TTFB (재측정) | TTFB (치앙마이) |
|---|---|---|---|
| 없는 경로 → 403 | 없음 | ≈0.20초 | ≈0.12초 |
| `app-versions` 버전 헤더 없음 | 트랜잭션 1개, 쿼리 0개 | ≈0.67초 | ≈0.60초 |
| `app-versions` 낮은 버전 헤더 | 트랜잭션 1개, 쿼리 1개 | ≈0.77초 | ≈0.68초 |
| `GET /api/v1/app-versions` 정상 | 트랜잭션 2개 | ≈1.33초 | ≈1.25초 |
| `GET /api/v1/notices` 정상 | 트랜잭션 2개 | ≈1.33초 | — |

403 응답(DB 미접근)이 네트워크 기준선이다. **쿼리를 하나도 안 날리는 읽기 트랜잭션 하나가 ≈0.47초**를 더한다.

### 원인 — 트랜잭션 1개 = TiDB 왕복 6회

앱 서버와 TiDB Cloud 사이 왕복 1회가 ≈80~90ms 다(측정값. 리전 간 거리로 추정).
그런데 Spring `@Transactional(readOnly = true)` 하나가 MySQL 드라이버 기본 설정에서 아래 문장을 **각각 따로** 서버에 보낸다.

```
SET @@SESSION.tx_read_only=1      ← readOnly 전파
SET @@SESSION.autocommit=0        ← 트랜잭션 시작
select ...                        ← 실제 쿼리 (이것만 필요한 왕복)
commit
SET @@SESSION.autocommit=1
SET @@SESSION.tx_read_only=0
```

쿼리 1개에 부수 왕복 5개. 그리고 요청 하나가 이런 트랜잭션을 여러 개 연다.

| 요청 경로 | 트랜잭션을 여는 곳 |
|---|---|
| 모든 `/api/**` | `AppVersionInterceptor` → `AppVersionService.verifyAppVersion` (`@Transactional`) |
| 로그인 요청 | `JwtAuthenticationFilter` → `MemberService.getActiveMemberBySeq` |
| 컨트롤러 | 실제 비즈니스 쿼리 |

로그인 사용자의 일반 API 는 **트랜잭션 3개 = 왕복 18회 ≈ 1.6초**가 DB 대기만으로 나간다.
버전 헤더가 없어 예외를 던지는 경우조차 인터셉터의 `@Transactional` 때문에 왕복 5회를 쓴다.

---

## 2. 어떻게 해결했나

커밋 4개. 요청 경로에서 **트랜잭션 2개를 없애고**, 남은 1개는 **왕복 6회 → 2회**로 줄였다.

### ① 메모리 TTL 캐시·커밋 후 무효화 유틸 (`edb7f5e`)

`TtlCache<K, V>` (만료 시간 있는 `ConcurrentHashMap`) 와 `evictNowAndAfterCommit {}`.
후자는 즉시 지우고, 현재 트랜잭션이 있으면 **커밋 직후 한 번 더** 지운다 —
커밋 전에만 지우면 다른 스레드가 커밋 전 옛 값을 다시 캐시에 넣을 수 있다.

### ② 앱 버전 검증 캐시 (`6899142`) — 인터셉터 트랜잭션 제거

`verifyAppVersion` 에서 `@Transactional` 을 떼고 최소 버전을 **60초** 캐시한다.
캐시 미스일 때만 리포지토리가 자체 트랜잭션으로 조회한다.
앱 서버의 `PUT /app-versions` 는 커밋 직후 캐시를 비운다.

```kotlin
val minVersion = minVersionCache.getOrLoad(MIN_VERSION_KEY) {
    appVersionRepository.findTopByOrderByCreatedAtDesc().minVersion
}
```

`@Transactional` 을 남겨두면 캐시 적중이어도 트랜잭션이 열려 왕복이 그대로 발생하므로 반드시 뗀다.

### ③ JWT 필터 회원 캐시 (`b26690d`) — 인증 트랜잭션 제거

`MemberAuthCache` 가 활성 회원을 **5분** 캐시한다. 탈퇴 회원은 캐시에 넣지 않는다.

- 탈퇴(`withdraw`)·재로그인(`signUp`) 시 커밋 직후 무효화 → **탈퇴 토큰은 즉시 차단**
- 캐시된 엔티티를 그대로 내주지 않고 요청마다 사본을 만든다(스레드 간 공유 방지)
- 어드민 콘솔(별도 컨테이너)의 `adminYn` 토글은 최대 5분 뒤 반영. 앱 서버는 `adminYn` 을 쓰지 않는다

### ④ auto-commit 끄기 + prod 드라이버 옵션 (`3d77571`) — 남은 트랜잭션 6회 → 2회

`application.yml` 설정만 바꿨다. `application-prod.yml`(시크릿) 은 손대지 않았다.

| 설정 | 효과 | 적용 범위 |
|---|---|---|
| `hikari.auto-commit=false` + `hibernate.connection.provider_disables_autocommit=true` | `SET autocommit=0/1` 2회 제거 | 전 프로필 |
| `useLocalSessionState=true` | 드라이버가 세션 상태를 기억해 같은 값이면 안 보냄 | prod |
| `useLocalTransactionState=true` | 실행 문장이 없으면 commit/rollback 을 안 보냄 | prod |
| `readOnlyPropagatesToServer=false` | `SET tx_read_only=1/0` 2회 제거 | prod |

prod 한정인 이유: H2 테스트 프로필이 모르는 드라이버 속성을 거부한다.
감수하는 점: DB 서버 레벨 읽기 전용 강제가 사라지고 Hibernate(`FlushMode.MANUAL`) 수준만 남는다.

---

## 3. 측정 — 요청당 DB 왕복 횟수 (로컬 TiDB, 2026-10-20)

운영 배포 전이라 지연시간 대신 **왕복 횟수**를 쟀다. 지연시간은 왕복 횟수에 비례하므로 이 값이 근거다.

**방법**
1. 운영과 같은 `pingcap/tidb:v8.5.3` 을 로컬에 띄우고 `SET GLOBAL tidb_general_log=1`
2. `main` 빌드와 이 브랜치 빌드를 각각 띄움 (`ddl-auto=create`, Flyway off, Redis 6380)
3. 엔드포인트마다 warmup 1회 후 3회 요청, 요청 전후에 마커 `SELECT` 를 넣고 그 사이의 general log 문장 수를 셈
4. 이 브랜치는 prod 드라이버 옵션 **없이**(dev/local 과 동일) 와 **있이**(운영과 동일) 두 번 측정

**결과** (3회 모두 동일)

| 요청 | main | 브랜치 (드라이버 옵션 없음) | 브랜치 + prod 드라이버 옵션 |
|---|---|---|---|
| `GET /api/v1/app-versions` (헤더 정상) | 12 | 4 | **2** |
| `GET /api/v1/app-versions` (헤더 없음 → 예외) | 5 | 0 | **0** |
| `GET /api/v1/notices` | 12 | 4 | **2** |
| `GET /api/v1/member-streaks` (JWT) | 18 | 4 | **2** |

변경 후 로그인 API 에 남은 문장은 비즈니스 쿼리와 commit 둘뿐이다.

```
select ms1_0.member_streak_seq, ... from member_streaks ...
rollback        ← NOT_FOUND 예외 케이스라 rollback. 정상이면 commit
```

드라이버 옵션 없이도 캐시 둘 + auto-commit 설정만으로 18 → 4 다. 나머지 4 → 2 는 `tx_read_only` 2회를 없애는 prod 옵션 몫이다.

**운영 TTFB 예상.** 재측정에서 왕복 1회 ≈ 90ms (0.47초 ÷ 5회, 1.13초 ÷ 12회 모두 ≈ 0.09초로 일관).

| 요청 | 변경 전 (실측) | 변경 후 (예상 = 기준선 0.2초 + 왕복 × 0.09초) |
|---|---|---|
| 공개 GET | 1.33초 | ≈ 0.4초 |
| 로그인 GET | ≈ 1.8초 (추정) | ≈ 0.4초 |
| 버전 헤더 누락 | 0.67초 | ≈ 0.2초 |

---

## 4. 검증

- `./gradlew :bff:app:test --continue` 122건 중 **121건 통과**, 2건 스킵(기존 마이그레이션 테스트)
  - `AuthServiceTest` 6/6, `WithdrawalAndRefreshTest` 6/6 — 캐시 무효화가 탈퇴·재발급 흐름을 깨지 않음
  - 추가된 테스트: `TtlCacheTest`, `MemberAuthCacheTest`, `AppVersionServiceTest`(캐시·무효화), `MemberServiceTest`(탈퇴 시 무효화)
- 같이 고친 것: `MemberQuoteReadServiceTest` "내용이 있는 모든 명언 조회" 는 230e22c 에서
  목록 노출 규칙이 `completed || likeYn == "Y" || hasAnswer()` 로 바뀐 걸 반영 못 한 낡은 테스트였다.
  완료·좋아요·답변 각 1건은 노출되고 미완료 타이핑·빈 건은 제외되는 걸 검증하도록 고쳤다(`98189e6`).
- **Flyway + `auto-commit=false` 기동 확인(로컬 TiDB v8.5.3).** 운영과 같은 조건
  (이력 테이블·baseline 행 부트스트랩, `flyway.enabled=true`, prod 드라이버 옵션 3개)으로 이 브랜치 빌드를 띄웠다.
  `Successfully validated 1 migration` → `Current version of schema: 1` → `Schema is up to date` 로 정상 기동.
- 운영 `application-prod.yml`(로컬 사본 기준)은 `hikari.*`·`hibernate.connection.*`·`flyway.*` 키를 갖지 않아
  이 PR 의 설정과 충돌하지 않는다. 배포는 `SPRING_PROFILES_ACTIVE=prod` 라 prod 전용 드라이버 옵션이 적용된다.
- 트랜잭션 밖 JDBC 쓰기(`JdbcTemplate`·`EntityManager`·네이티브 `@Modifying`)는 없다.
  `auto-commit=false` 에서 이런 코드는 커밋되지 않고 조용히 롤백되므로 확인했다. 스케줄러는 `@Transactional`.
- 남은 실패 1건 `JwtTokenProviderTest` 는 `@ActiveProfiles("local")` 로 실제 로컬 DB 에 붙으려는
  환경 의존 테스트다(Connection refused). 테스트 파일과 `JwtTokenProvider` 모두 이 브랜치에서 건드리지 않았다.

---

## 5. 배포 후 확인

1. dozzle 에서 Flyway 기동 로그 확인 — 로컬 TiDB 로는 검증했지만(§4) 운영은 Flyway 가 TiDB 를 MySQL 로 판정하는 차이가 있다.
   배포 워크플로의 헬스체크(`app=200 admin=200`)는 통과했으므로 기동 자체는 성공했다.
2. 아래로 TTFB 재측정 (→ §6 에 기록함)

```bash
B=https://api.fillsa.com
for i in 1 2 3 4 5 6; do curl -s -o /dev/null -w '%{http_code} %{time_starttransfer}\n' $B/nope; done
for i in 1 2 3 4 5 6; do curl -s -o /dev/null -w '%{http_code} %{time_starttransfer}\n' -H 'X-App-Version: 9.9.9' $B/api/v1/app-versions; done
for i in 1 2 3 4 5 6; do curl -s -o /dev/null -w '%{http_code} %{time_starttransfer}\n' -H 'X-App-Version: 9.9.9' -H "Authorization: Bearer $TOKEN" $B/api/v1/member-streaks; done
```

3. 어드민에서 최소 버전을 바꾼 뒤 앱 서버 반영이 60초 안에 되는지 1회 확인
4. 롤백: 이 PR revert 후 재배포. 스키마·시크릿 변경 없음

### 측정 중 발견한 것 (이 PR 범위 밖)

- `X-App-Version` 헤더가 없거나 낮으면 운영이 **500** 을 돌려준다. 앱이 강제 업데이트 분기를 타려면 4xx + 에러코드가 맞을 것이다. 별도 확인 필요.

---

## 6. 배포 후 운영 실측 (2026-10-20, 배포 직후)

§1 과 같은 PC·같은 스크립트로 6회씩 측정. 첫 회는 캐시 미스·커넥션 워밍이 섞여 제외하고 2~6회 중앙값.

| 요청 | 변경 전 | 변경 후 | 비고 |
|---|---|---|---|
| 없는 경로 → 403 (기준선) | ≈0.20초 | ≈0.19초 | 네트워크 기준선, 변화 없음 |
| `app-versions` 버전 헤더 없음 | ≈0.67초 | **≈0.18초** | DB 왕복 0회 — 기준선과 같음 |
| `app-versions` 낮은 버전 헤더 | ≈0.77초 | **≈0.18초** | 첫 회만 0.82초(캐시 미스), 이후 기준선과 같음 |
| `GET /api/v1/app-versions` 정상 | ≈1.33초 | **≈0.27초** | 남은 왕복 2회 ≈ 0.08초 |
| `GET /api/v1/notices` 정상 | ≈1.33초 | **≈0.27초** | 동일 |

- 공개 GET 의 서버 처리 시간(TTFB − 기준선)은 ≈1.14초 → ≈0.08초.
- §3 의 예상치(≈0.4초)보다 빠르다. prod 드라이버 옵션으로 남은 2회 왕복이 예상한 0.18초가 아니라 ≈0.08초에 끝났다.
- 로그인 GET 은 운영 토큰이 없어 측정하지 못했다. 왕복 횟수가 공개 GET 과 같은 2회이므로 비슷한 수준일 것으로 본다.
