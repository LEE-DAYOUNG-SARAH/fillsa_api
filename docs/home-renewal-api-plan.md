# 홈·캘린더 리뉴얼 API 플랜

> 작성: 2026-09-03
> 대상 브랜치: `feature/home-renewal-api` → `main`
> 근거: Fillsa PRD v1.0 (2026-08-29 갱신) · Figma `리뉴얼 ver.1.0(26.08~)`
> 범위: **홈 · 캘린더** (온보딩 · 마이페이지는 이번 범위 아님)

이 문서가 SSOT. 실행은 §6 순서대로.

---

## 0. 요약

| 구분 | 내용 |
|---|---|
| **추가** | 주간 조회 API · 회고 답변 API · 단일 날짜 조회 API(v2) · `answer`/`answered_at` 컬럼 |
| **확장** | 월간 조회(v2) 응답 필드 8개 |
| **수정** | `dailyQuote()` 미래 날짜 조회 정책 적용 |

제거는 없다. 기존 API·컬럼은 모두 보존한다.

> **버전 정책 (확정)**: **v2 확장으로 간다. v3는 신설하지 않는다.**
> 신규 API는 `/api/v2` 아래 두고, 월간은 기존 v2 응답에 필드를 더한다. 검토 기록은 §7.

---

## 1. 화면 변경으로 생긴 요구

PRD와 Figma에서 확인된 것 중 API에 영향을 주는 항목만 추린다.

| 화면 변경 | API 영향 |
|---|---|
| 홈 상단이 월간 → **롤링 7일**로 변경 | 주간 조회 신규 |
| 문장 카드 좌우 스와이프 = 날짜 이동 | 7일치 콘텐츠 선반영 필요 |
| **오늘의 질문 + 답변** 신설 | 저장소·API 전무 → 신규 |
| 캘린더 날짜 상세가 홈 카드와 동일 구성 | 월간 응답에 상세 필드 통합 |
| 미래 날짜를 화면에 표시(콘텐츠는 비공개) | 미래 날짜 조회 정책 필요 |
| 메모 메뉴 삭제 | 신규 응답에 메모 미포함 (기존 API는 유지) |

### 확정 정책

> 아래는 기획 문서 검증을 거쳐 확정한 내용이다. 재논의 시 §11의 근거를 먼저 확인할 것.

- **필사 완료 판정** — **타이핑 완료(원문 일치) 또는 이미지 업로드** 둘 중 하나. 기존 기획 그대로이며 **변경 없음**. 오늘의 질문 답변은 **필사 완료로 치지 않는다**(근거 §11-1).
- **연속(🔥)** — 오늘 필사만 카운트한다. 과거 날짜 필사는 `done` 표시만 되고 연속에는 반영하지 않는다. 현행 `MemberQuote.complete()` 로직 그대로이며 **변경 없음**.
- **홈 주간** — 달력상의 주가 아니라 **오늘이 마지막 칸인 롤링 7일**(`endDate-6 ~ endDate`). 요일 시작 계산이 없다.
- **연월 뱃지** — 선택한 날짜 기준으로 클라가 표기한다. 서버는 `yearMonth`를 내려주지 않는다.
- **필사 본문**(`typingKorQuote`/`typingEngQuote`) — 주간·월간 응답에서 제외한다. 필사 화면 진입 시 기존 `GET /{dailyQuoteSeq}/typing`을 사용한다.
- **이미지 URL** — 목록 응답에 포함한다. 영구 공개 URL이라 부피가 작고(약 80바이트), 액션바가 이미지 유무를 알아야 하므로 플래그 역할을 겸한다.
- **캘린더 언어** — 한·영 필드를 모두 내려준다. 토글 유무와 무관하게 대응 가능하다.

---

### 답변(answer) 처리 요약

| 항목 | 결정 |
|---|---|
| 필사 완료(`completed`)에 반영 | **아니오** |
| 연속(`currentStreak`)에 반영 | **아니오** |
| 주간 `state: done` 판정에 반영 | **아니오** |
| 응답에 포함 | **예** — 주간·월간·단일 날짜 모두 |
| 조회 필터 통과 | **예** — `isViewQuoteData()`에 조건 추가 필요(§5-2) |

답변은 **별개 기록**이다. "완료로 친다"와 "응답에 실어준다"는 다른 문제이며, 좋아요가 이미 같은 방식으로 처리되고 있다(완료 아님 · 조회는 됨).

---

## 2. DB 변경

```sql
ALTER TABLE member_quotes
  ADD COLUMN answer      VARCHAR(200) NULL COMMENT '오늘의 질문 답변',
  ADD COLUMN answered_at DATETIME     NULL COMMENT '답변 최종 수정 시각';
```

- 기존 컬럼은 손대지 않는다. `memo` 컬럼도 유지한다.
- 질문 원문은 이미 `quotes.question_ko` / `question_en`에 있다(커밋 `b05affa`). 어드민 입력 기능도 이미 있어 **어드민은 변경 없음**.
- 답변은 회원×날짜 단위라 별도 테이블 없이 `member_quotes`를 확장한다. 해당 테이블에 `(MEMBER_SEQ, DAILY_QUOTE_SEQ)` UNIQUE가 이미 있어 정합성이 보장된다.

---

## 3. 신규 API

### 3-1. 주간 조회

```
GET /api/v2/member-quotes/weekly?endDate=2026-09-03
```

홈 상단 날짜 영역과 문장 카드 7장을 한 번에 내려준다. 카드 스와이프 시 추가 호출이 없다.

**요청**

| 파라미터 | 필수 | 설명 |
|---|---|---|
| `endDate` | 선택 | 창의 마지막 날짜. 생략 시 **서버 기준 오늘**. 오늘보다 미래면 오늘로 clamp |

- 창은 항상 `endDate-6 ~ endDate` 7일이다.
- 이전 창은 `endDate - 7`, 다음 창은 `endDate + 7`. 모든 창이 `오늘 - 7k`에 정렬되어 겹치거나 빠지는 날짜가 없다.
- 최초 진입에서 `endDate`를 생략하는 것을 권장한다. 클라가 "오늘"을 계산하면 자정 경계와 기기 시간 오차에서 서버 판정(`state: today`)과 어긋난다.

**응답**

```json
{
  "startDate": "2026-08-28",
  "endDate": "2026-09-03",
  "days": [
    {
      "date": "2026-08-28",
      "dayOfWeek": "FRI",
      "state": "done",
      "dailyQuoteSeq": 98,
      "korQuote": "…", "engQuote": "…",
      "korAuthor": "존 우든", "engAuthor": "John Wooden",
      "authorUrl": "https://ko.wikipedia.org/wiki/존_우든",
      "questionKo": "…", "questionEn": "…",
      "answer": "…", "answeredAt": "2026-08-28T21:03:00",
      "likeYn": "Y",
      "imagePath": "https://…/abc.jpg",
      "completed": true
    }
  ]
}
```

- `state` — `none` | `today` | `done`. 오늘이면서 완료면 `done`을 우선한다(PRD 명시). 서버가 판정해 내려준다.
- `dayOfWeek` — 서버가 계산해 내려준다. 롤링 창이라 요일 순서가 매일 바뀌고, Figma 시안에 요일-날짜 불일치가 있어 클라 계산에 맡기지 않는다.
- 창 안에 미래 날짜는 존재할 수 없다(오늘이 마지막 칸). `isFuture` 필드는 두지 않는다.

### 3-2. 회고 답변 저장

```
POST /api/v2/member-quotes/{dailyQuoteSeq}/answer
{ "answer": "친구가 힘들 때 언제든 연락하라고 했는데…" }
```

등록·수정 겸용.

**검증**
- 최대 200자
- 공백만 입력 불가 (PRD 명시)
- 미래 날짜 거부
- 답변 저장은 필사 완료(`completed`)와 무관하다. 연속에도 영향을 주지 않는다.

**응답** — `answeredAt` 포함. 클라가 저장 후 로컬 상태를 갱신할 수 있도록 한다.

### 3-3. 단일 날짜 조회

```
GET /api/v2/member-quotes/daily?quoteDate=2026-09-03
```

**응답은 `weekly.days[]` 원소와 완전히 동일한 `DayQuoteData`**. 클라가 같은 모델·같은 파서를 재사용한다.

홈·캘린더의 정규 흐름에서는 사용하지 않는다(주간·월간 응답에 이미 포함). 아래 상황을 위한 보조 경로다.

- 답변·필사·좋아요 저장 후 해당 날짜만 갱신
- 푸시 딥링크로 특정 날짜 진입
- 부분 실패 후 단일 날짜 재시도

**V1 `/api/v1/member-quotes/daily`는 건드리지 않는다.** 현재 앱이 사용 중이라 필드 추가 시 §7의 파서 리스크가 하나 더 생긴다. 신규 앱은 v2를 사용한다.

미래 날짜 정책(§5)을 처음부터 적용한다.

---

## 4. 기존 API 확장

### `GET /api/v2/member-quotes/monthly?yearMonth=`

캘린더 날짜 상세를 월간 응답에 통합한다. 날짜를 탭할 때 추가 호출이 없다.

**추가 필드 (날짜별)**

```
+ engQuote, engAuthor, authorUrl
+ questionKo, questionEn
+ answer, answeredAt
+ imagePath
```

**유지** — `dailyQuoteSeq`, `quoteDate`, `quote`, `author`, `completed`, `todayCompleted`, `likeYn` 및 하단 요약(`typingCount`, `likeCount`, `streakCount`).

**미래 날짜** — 현행대로 응답에서 제외된다. `monthlyQuotes()`가 현재 월이면 `endDate`를 오늘로 자르고 있다. 클라는 남은 칸을 빈 상태로 그린다.

**용량** — 31일 전부 답변이 채워진 최악의 경우 raw 약 30KB, gzip 약 10KB. 월 이동 시마다 발생하나 날짜 탭의 로딩을 없애는 값으로 수용한다.

> ⚠️ **V1 monthly는 DTO가 분리되어 있어 영향받지 않는다**(`MemberMonthlyQuoteResponse` vs `MemberMonthlyQuoteResponseV2`).
> 다만 **목록 API는 V1·V2가 `MemberQuotesResponse`를 공유**한다. 이 DTO는 이번 작업에서 건드리지 않는다.

---

## 5. 수정

### 5-1. `dailyQuote()` 미래 날짜 조회 정책 적용

`MemberQuoteReadService.kt`의 `dailyQuote()`에 미래 날짜 처리가 없다. 같은 클래스의 `monthlyQuotes()`에는 있다.

리뉴얼로 미래 날짜가 화면에 표시되면서 클라가 해당 날짜를 조회할 경로가 생긴다. PRD 제품 원칙(*"오늘 날짜 이후 날짜의 문장들은 미리 열람하거나 필사할 수 없다"*)에 맞춰 정책을 적용한다.

- 조회 계열: 미래 날짜면 문장·질문을 반환하지 않는다.
- 저장 계열(타이핑·답변·좋아요·이미지): 미래 날짜 요청을 거부한다.

**적용 순서** — v2에 먼저 적용한다. V1은 동작 변경이므로 구버전 앱 호출 여부를 로그로 확인한 뒤 결정한다.

```
logs.fillsa.com → fillsa-app → "member-quotes/daily" 검색
→ quoteDate 가 오늘보다 미래인 호출이 있는지 확인
```

기존 홈은 월간 기반이고 `monthly`는 미래를 반환하지 않으므로 호출이 없을 가능성이 높다.

### 5-2. `isViewQuoteData()`에 답변 조건 추가 (필수)

```kotlin
// MemberQuote.kt — 현재
fun isViewQuoteData() = completed || likeYn == "Y"

// 변경 후
fun isViewQuoteData() = completed || likeYn == "Y" || !answer.isNullOrBlank()
```

월간·주간 조회가 이 필터를 통과한 `MemberQuote`만 사용한다(`MemberQuoteReadService.kt`의 두 곳).

**이 수정이 없으면 필사도 좋아요도 하지 않고 답변만 작성한 날이 응답에서 통째로 누락된다.** 이번에 추가하는 기능이 반쪽이 되므로 필수다.

답변을 필사 완료로 치지 않는 것과는 별개 문제다. 좋아요도 완료는 아니지만 조회 대상에는 포함되어 있다.

---

## 5-3. Swagger 표기 규격

신규·확장 API 모두 기존 관례를 따른다. **누락 시 앱 팀이 문서만 보고 연동할 수 없다.**

### 컨트롤러 배치와 `@Tag`

| API | 컨트롤러 | `@Tag` |
|---|---|---|
| `weekly` · `daily` | `MemberQuoteReadV2Controller` | `(회원) 명언 조회` (기존) |
| `answer` | `MemberQuoteUpdateV2Controller` | `명언 저장` (기존) |

새 `@Tag`는 만들지 않는다.

### `@Operation(summary = ...)`

기존 관례는 **화면 번호 접두**다. 리뉴얼 화면 번호에 맞춘다.

```kotlin
@Operation(summary = "[2.home] V2.주간 명언 조회 api",
           description = "오늘이 마지막 칸인 롤링 7일. endDate 생략 시 서버 기준 오늘")
@Operation(summary = "[2.home/3.calendar] V2.일별 명언 조회 api")
@Operation(summary = "[2.home] 오늘의 질문 답변 저장 api", description = "등록·수정 겸용")
```

기존 v2 목록 API가 `"[4. list] V2.명언 목록 조회 api"` 형태로 `V2.` 를 붙이고 있으므로 동일하게 맞춘다.

### `@ApiErrorResponses`

이 프로젝트는 `@ApiErrorResponses(...)`에 `ErrorCode`를 나열하면 `SwaggerOperationCustomizer`가 상태코드별 응답 예시를 자동 생성한다. **신규 API에 반드시 붙인다.**

| API | 선언할 ErrorCode |
|---|---|
| `weekly` | `NOT_FOUND` |
| `daily` | `NOT_FOUND` · `INVALID_REQUEST`(미래 날짜) |
| `answer` | `NOT_FOUND` · `INVALID_REQUEST`(200자 초과 · 공백 · 미래 날짜) |

`@AuthenticationPrincipal` 파라미터가 있으면 커스터마이저가 security 요구를 자동 부착하므로 별도 선언은 불필요하다.

### `@Parameter` · `@Schema`

- 쿼리 파라미터에 `@Parameter(description = ..., example = ...)`
  - `endDate` — `"창의 마지막 날짜. 생략 시 오늘"`, example `yyyy-MM-dd`
  - `quoteDate` — example `yyyy-MM-dd`
- 신규 DTO(`WeeklyQuoteResponse` · `DayQuoteData` · `AnswerRequest`)의 **모든 필드**에 `@Schema(description = ...)`. 필수 필드는 `required = true`
- `LocalDate` 필드는 기존과 동일하게 `@JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")`
- `state` 는 enum 후보를 명시: `@Schema(description = "날짜 상태", allowableValues = ["none", "today", "done"])`

### 월간 확장분

`MemberMonthlyQuoteResponseV2`에 추가하는 8개 필드에도 `@Schema(description = ...)`를 빠짐없이 단다. 기존 필드의 설명은 **수정하지 않는다**(§11-2의 용어 정리 참고 — `todayCompleted`의 "연속 필사 여부"는 도메인 용어상 정확한 표현이다).

---

## 6. 작업 순서

| # | 작업 | 산출물 | 검증 |
|---|---|---|---|
| 1 | DDL 적용 | `member_quotes.answer`, `answered_at` | 운영 반영 전 로컬·스테이징 |
| 2 | 엔티티 확장 | `MemberQuote.answer/answeredAt` + `updateAnswer()` | 단위 테스트 |
| 3 | 답변 API | `POST /{seq}/answer` | 200자·공백·미래 날짜 케이스 |
| 4 | 주간 조회 API | `GET /weekly` | `state` 판정, 창 정렬(±7), clamp |
| 5 | 단일 날짜 조회 API | `GET /api/v2/…/daily` | `weekly.days[]` 원소와 스키마 동일 |
| 6 | 월간 응답 확장 | 필드 8개 | 기존 필드 불변 확인 |
| 7 | 미래 날짜 정책 | 조회·저장 계열 | 경계값(오늘/내일) |
| 8 | **Swagger 확인** | §5-3 규격 | `/swagger-ui` 에서 신규 3개 노출 · 에러 응답 · 예시값 |

1~2가 선행이고 3~6은 병렬 가능하다. 4·5는 `DayQuoteData`를 공유하므로 함께 작업한다.
8은 3~7 완료 후 **머지 전 필수**다. 앱 팀이 Swagger만 보고 연동하므로 누락 시 문의가 되돌아온다.

**확인 항목**
- 신규 3개가 올바른 `@Tag` 아래 노출되는가
- `@ApiErrorResponses` 로 생성된 상태코드별 응답 예시가 보이는가
- `endDate` · `quoteDate` 의 `example` 이 `yyyy-MM-dd` 로 표시되는가
- `state` 의 `allowableValues` 가 드롭다운으로 보이는가
- 월간 응답에 추가한 8개 필드에 설명이 붙어 있는가

### 파일별 작업

| 파일 | 작업 |
|---|---|
| `MemberQuote.kt` | 필드 2개 + `updateAnswer()` |
| `MemberQuoteReadV2Controller.kt` | `weekly` · `daily` 추가 |
| `MemberQuoteUpdateV2Controller.kt` | `answer` 추가 |
| `MemberQuoteReadService.kt` | `weekly()` · `dailyQuoteV2()` 신규 · `monthlyQuotesV2()` 확장 · 미래 날짜 정책 |
| `MemberQuoteUpdateService.kt` | `saveAnswer()` |
| `MemberMonthlyQuoteResponseV2.kt` | 필드 8개 |
| DTO 신규 | `WeeklyQuoteResponse` · `DayQuoteData` · `AnswerRequest` |

### 변경하지 않는 것

좋아요 · 이미지 업로드/삭제 · 필사 타이핑(V1·V2) · 연속(streak) · 로그인/탈퇴 · 메모 · **어드민 전체**

---

## 7. 버전 정책 — v2 확장 (v3 미채택)

**결정: v3를 신설하지 않는다.** v1·v2가 이미 혼재한 상태에서 v3까지 더하면 같은 기능이 세 벌이 되고, 수정 시 어느 버전을 손대야 하는지 매번 확인해야 한다. 실제로 목록 API가 V1·V2에서 `MemberQuotesResponse`를 공유하고 있어 이런 혼선이 이미 존재한다.

응답에 필드를 **추가**하는 것은 하위호환이다. 대부분의 JSON 파서가 모르는 키를 무시한다(Gson · Moshi · Swift Codable).

### 확장 전 사전 확인 1건

`kotlinx.serialization`은 기본값이 `ignoreUnknownKeys = false`라 모르는 필드에서 파싱 예외가 발생한다.

```kotlin
Json { ignoreUnknownKeys = true }   // 켜져 있어야 안전
```

**월간(v2)은 현재 운영 중인 앱이 이미 사용하고 있다.** 확장하면 신규 앱이 아니라 지금 설치된 앱이 그 응답을 받는다. 작업 착수 전 안드로이드 파서 설정을 확인한다.

**만약 strict 설정이면** — v3 신설 대신 아래 순으로 검토한다.
1. 앱에서 `ignoreUnknownKeys = true` 적용 (근본 해결. 이후 모든 필드 추가가 안전해진다)
2. 그것이 어려우면 월간만 별도 신규 경로로 분리

### 미래 날짜 정책의 breaking 여부

필드 추가와 달리 **동작 변경**이므로 §5의 로그 확인 절차를 따른다. v2에 먼저 적용하고 V1은 확인 후 결정한다.

---

## 8. 앱 팀 전달 사항

- 홈 진입: `monthly` → **`weekly`**
- **카드 스와이프 시 API 호출 없음** (7일치 선반영)
- **캘린더 날짜 탭 시 API 호출 없음** (월간 응답에 상세 포함)
- 필사 화면 진입 시에만 `GET /{dailyQuoteSeq}/typing`
- 저장 후 단일 날짜 갱신·딥링크 진입이 필요하면 **`GET /api/v2/member-quotes/daily`** (응답이 `weekly.days[]` 원소와 동일)
- 메모 관련 호출 제거 (엔드포인트는 유지되나 신규 화면에서 사용하지 않음)
- 주간 스크롤: 응답의 `endDate`에서 `±7`. 최초 진입은 `endDate` 생략 권장
- 연월 뱃지: 선택 날짜 기준으로 클라가 표기

---

## 9. 미결 항목

- [ ] **안드로이드 JSON 파서 unknown key 처리** — §7. 월간 확장 착수 전 확인
- [ ] **캘린더 상세 한/영 토글 유무** — 한·영 모두 내려주므로 진행에는 지장 없음
- [ ] **`weekly` 명칭** — 달력상의 주가 아니라 최근 7일이다. `recent-days` 등 대안 검토
- [ ] **Figma 시안 요일 표기 확인** — 캘린더 상세 `21 (목)`이나 2025-03-21은 금요일이다. 그리드 배치도 실제 요일과 어긋난다. 더미 데이터로 추정되나 디자이너 확인 필요

---

## 10. 이번 범위 밖

PRD에는 있으나 홈·캘린더가 아니어서 제외한 항목. 별도 논의가 필요하다.

- **비회원(게스트) 시작하기** — 서버에 게스트 개념이 없다. 클라 로컬 저장 후 로그인 시 기존 `LoginRequest.syncData`로 올리는 방식이면 서버 변경이 불필요하다.
- **테마 · 온보딩 완료 상태** — 기기별 설정으로 보고 로컬 저장을 제안한다. 기기 간 동기화가 요구사항이 되면 그때 서버로 올린다.
- **알림** — `MemberDevice.pushAgreedYn`으로 이미 지원 중이다.

---

## 11. 연속 필사 로직 검증 기록

착수 전 기존 구현을 전수 검증했다. **연속 관련 코드는 변경하지 않는다.** 재논의를 막기 위해 근거를 남긴다.

### 11-1. 답변을 필사 완료로 치지 않는 근거

문서 네 곳이 일관되게 필사와 답변을 분리하고 있다.

| 출처 | 내용 |
|---|---|
| `raw/shared/작업/연속필사 기획.md` | **필사 완료** — '오늘의 문장'을 **타이핑 필사 또는 손글씨 업로드** 중 하나라도 완료한 상태 |
| `raw/shared/작업/명언 회고질문 기획.md` | 성공 지표 — **일일 필사 중 회고 작성 비율**. 답변=필사면 항상 100%가 되어 지표가 성립하지 않는다 |
| PRD 제품 원칙 | 필사와 오늘의 질문 답변은 **각각 저장할 수 있다** |
| PRD 날짜 상태 | **완료 `done`** — **필사를 완료한** 날짜 |

PRD의 메시지 분기도 같은 방향이다. `연속 필사 완료!` · `필사가 완료되지 않았어요.`는 **필사 화면에만** 있고, 답변 저장은 `답변을 기록했어요.` 토스트뿐이다. 캘린더 과거 미필사일 문구도 답변 여부와 무관하게 `필사하지 않은 날이에요`다.

**변경하려면** 위 문서 네 곳과 `wiki/Features/연속필사.md` 트리거 표를 함께 갱신해야 하고, 회고질문 기획의 성공 지표를 재정의해야 한다.

### 11-2. 정상 확인된 항목 (변경 불필요)

| 항목 | 결과 |
|---|---|
| 어제 필사 → 오늘 필사 시 `+1` | 정상 |
| 하루 건너뛰면 `1`로 리셋 | 정상 |
| 같은 날 중복 저장 idempotent | 정상 |
| 과거 날짜 필사는 연속 미반영 (`quoteDate != today` early return) | 정상 |
| 조회 시 끊김 보정 (`currentStreakAsOf`) | 정상 |
| `maxStreak` 갱신 | 정상 |
| 이미지 업로드로 완료 처리 | **정상** — 기획 명시 사양 |
| 월간 `streakCount` | **정상** — 그 달에 🔥가 붙은 날(당일 필사)의 개수. 연속 길이가 아니다 |
| `todayCompleted` = "연속 필사 여부" | **정상** — 이 도메인에서 '연속필사'는 *당일 완전 필사라는 행위*를 가리킨다. 연속 길이는 `MemberStreak.currentStreak`로 별도 관리된다 |

> ⚠️ **용어 주의** — '연속필사'가 두 의미로 쓰인다.
> ① **행위**: 당일 완전 필사 → 날짜 셀의 🔥, `todayCompleted`, 월간 `streakCount`
> ② **길이**: 며칠 연속인가 → 홈 상단 `🔥 100일`, `MemberStreak.currentStreak`
> 코드 리뷰 시 이 둘을 혼동하지 말 것.

### 11-3. 자정 배치 부재

`raw/shared/작업/연속필사 기획.md` 3-1에 *"매일 자정 기준으로 확인"* · *"오늘 미완료 → `current_streak = 0`"* 이 있으나 배치는 구현되어 있지 않다. 대신 조회 시 `currentStreakAsOf`로 보정한다.

**현재 구현이 의도에 맞다.** 같은 문서 3-2 UI 항목이 *"오늘 필사 하지 않은 경우 표시되어야 한다"* 와 듀오링고 예시를 들고 있어, 오늘 미완료 시 즉시 0이 되는 것이 아니라 유지되어야 함을 명시한다. `wiki/Features/연속필사.md`가 현재 구현을 반영해 갱신되어 있다.

### 11-4. 별건으로 분리한 항목

이번 리뉴얼과 무관하므로 별도 PR로 처리한다.

- **어드민 연속 랭킹 정렬** — 정렬은 DB 원본 `currentStreak`, 표시는 보정된 `currentStreakAsOf`. 끊긴 기록이 상위에 노출되면서 표시값은 0이 된다(`MemberStreakAdminService.withDefaultSort`). `summary()`의 평균·30일 이상 집계는 쿼리에서 보정하고 있어 정상이다.
- **`MemberQuote.hasContent()`** — 정의만 있고 사용처가 없다.

---
