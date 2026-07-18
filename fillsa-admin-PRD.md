# FILLSA Admin Console — PRD

| 항목 | 내용 |
|---|---|
| 문서명 | 필사(FILLSA) 어드민 콘솔 제품 요구사항 정의서 |
| 버전 | v0.1 (초안) |
| 작성일 | 2026-06-20 |
| 작성자 | 이다영 |
| 상태 | 검토 중 — 목업 시안 기준 작성 |
| 관련 산출물 | `fillsa-admin-mockup.html` (시안), `fillsa_api` (백엔드) |

---

## 1. 개요

### 1.1 배경
필사(FILLSA)는 매일 제공되는 명언을 사용자가 직접 따라 쓰며(타이핑 또는 손글씨 이미지) 기록하고, 연속 필사(streak)로 습관을 만드는 모바일 서비스다. 현재 백엔드(`fillsa_api`, Kotlin/Spring Boot)는 운영되고 있으나, 콘텐츠(명언·일별 배정·공지·팝업)와 회원·운영 데이터를 관리할 **전용 어드민 화면이 없다.** 명언 등록, 일별 배정, 캐시 갱신 등은 현재 API를 직접 호출하거나 DB를 직접 수정해야 하는 상황으로 추정된다.

### 1.2 목적
운영자가 개발자 도움 없이 다음을 수행할 수 있게 한다.
- 명언 콘텐츠 등록·수정·삭제 및 날짜별 노출 배정
- 공지사항·팝업 등 사용자 노출 콘텐츠 관리
- 회원 조회 및 상태(탈퇴/권한) 관리
- 필사 기록·연속 필사 등 서비스 지표 모니터링
- 앱 버전·Redis 캐시 등 운영 작업 수행

### 1.3 범위
**포함(In-scope)** — 대시보드, 명언 관리, 일별 명언 배정, 공지사항, 팝업, 회원 관리, 필사 기록, 연속 필사, 앱 버전, 캐시 관리 (총 10개 화면).

**제외(Out-of-scope, 추후 검토)** — 푸시 알림 발송 콘솔, A/B 테스트, 매출/결제 관리, 다국어 콘텐츠 관리 UI, 어드민 활동 감사 로그(audit log) 화면.

### 1.4 프론트엔드 방침
- UI 컴포넌트는 [shadcn/ui](https://ui.shadcn.com/) 기반으로 구현한다.
- 디자이너 미배정 상태로, 시안(`fillsa-admin-mockup.html`)을 shadcn 컴포넌트로 1:1 변환하는 방식으로 진행한다.
- 시안의 디자인 토큰은 shadcn 기본(zinc) 팔레트를 따른다.

---

## 2. 사용자 및 권한

| 역할 | 정의 | 비고 |
|---|---|---|
| 관리자(Admin) | `members.adminYn = 'Y'` 인 계정 | 어드민 콘솔 전체 접근 |
| 일반 사용자 | 앱 사용자 | 어드민 접근 불가 |

- 어드민 인증은 기존 회원 로그인(OAuth: KAKAO/GOOGLE) + `adminYn` 검증을 재사용하는 것을 기본 가정으로 한다. (인증 방식 최종 확정 필요 — §7 미결정 사항)
- 권한 단계는 현재 단일(Admin) 기준. 세분화(읽기 전용/콘텐츠 운영자 등)는 추후 검토.

---

## 3. 데이터 모델 요약

어드민이 다루는 백엔드 테이블은 다음과 같다. (출처: `fillsa_api` JPA 엔티티)

| 테이블 | 엔티티 | 핵심 컬럼 | 어드민 용도 |
|---|---|---|---|
| `quotes` | Quote | korQuote, engQuote, korAuthor, engAuthor, category | 명언 마스터 관리 |
| `daily_quotes` | DailyQuote | quote(FK), quoteDate, quoteDayOfWeek | 날짜별 노출 명언 배정 |
| `members` | Member | oauthProvider, oauthId, nickname, profileImageUrl, withdrawalYn, withdrawalAt, adminYn | 회원 조회·관리 |
| `member_devices` | MemberDevice | deviceId, osType, deviceModel, appVersion, osVersion, activeYn | 회원 디바이스 조회 |
| `member_quotes` | MemberQuote | member, dailyQuote, typingKorQuote, typingEngQuote, imagePath, memo, likeYn, completed, todayCompleted | 필사 기록 조회 |
| `member_streaks` | MemberStreak | member, currentStreak, maxStreak, lastWrittenDate | 연속 필사 모니터링 |
| `notices` | Notice | title, content | 공지사항 관리 |
| `popups` | Popup | popupType, title, content, imageUrl, startDateTime, endDateTime, isActive, targetVersion | 팝업 관리 |
| `app_versions` | AppVersion | minVersion, nowVersion | 앱 버전 관리 |
| (Redis) | DailyQuoteCache | 일별 명언 캐시 | 캐시 관리 |

> 모든 RDB 엔티티는 `BaseEntity`(createdAt, updatedAt)를 상속한다.

---

## 4. 기능 요구사항

표기: **[필수]** 1차 출시 포함 / **[선택]** 추후. 각 화면은 시안의 동일 화면과 대응한다.

### 4.1 대시보드 (Dashboard)
**목적** — 운영자가 접속 시 서비스 핵심 지표를 한눈에 파악.

- **[필수]** 요약 지표 카드: 전체 회원 수, 오늘 필사 완료 수, 등록 명언 수, 활성 디바이스 수. (전일/전주 대비 증감 표기)
- **[필수]** 최근 14일 필사 완료 추이 차트 (`member_quotes.todayCompleted = true` 일자별 집계).
- **[필수]** 연속 필사 분포 (구간별 회원 수: 30일+, 14–29, 7–13, 1–6, 0).
- **[필수]** 최근 가입 회원 목록 (최신 5건, 회원 관리로 이동).
- **[선택]** 기간 선택형 리포트 내보내기(CSV).

### 4.2 명언 관리 (Quotes)
**목적** — 명언 마스터(`quotes`) CRUD.

- **[필수]** 목록: SEQ, 한/영 명언, 한/영 작가, 카테고리, 등록일. 페이지네이션.
- **[필수]** 검색(명언·작가 텍스트), 카테고리 필터.
- **[필수]** 추가/수정 다이얼로그: korQuote, engQuote, korAuthor, engAuthor, category. (명언/작가는 한·영 모두 nullable이나, 최소 한국어 명언은 입력 권장 — 검증 규칙 §7)
- **[필수]** 삭제. 단, 해당 명언이 `daily_quotes`에 배정되어 있으면 경고/차단(참조 무결성 확인).
- **[선택]** CSV 일괄 업로드/다운로드.
- **[선택]** 카테고리 마스터 관리(현재 category는 자유 문자열).

### 4.3 일별 명언 배정 (Daily Quotes)
**목적** — `daily_quotes`로 날짜별 노출 명언 지정.

- **[필수]** 월 단위 캘린더 뷰: 각 날짜에 배정된 명언 표시, 미배정일 강조.
- **[필수]** 날짜 클릭 → 명언 검색·선택해 배정/변경. `quoteDayOfWeek`는 날짜로부터 자동 계산.
- **[필수]** 월 이동, 배정/미배정 일수 요약.
- **[필수]** 배정 변경 시 해당 날짜 Redis 캐시 갱신 연동(§4.10 참조).
- **[선택]** 자동 배정(미배정일에 미사용 명언 랜덤/순차 배정).
- **[제약]** 한 날짜에는 하나의 명언만 배정(현재 모델 기준).

### 4.4 공지사항 (Notices)
**목적** — 앱 내 공지(`notices`) 관리.

- **[필수]** 목록: SEQ, 제목, 내용 미리보기, 등록일. 제목 검색.
- **[필수]** 작성/수정 다이얼로그: title, content. 삭제.
- **[선택]** 노출 기간/고정(상단 고정) 등 부가 속성(현재 모델에는 없음 — 추가 시 스키마 변경 필요).

### 4.5 팝업 관리 (Popups)
**목적** — 앱 팝업(`popups`) 관리.

- **[필수]** 목록: 제목, 타입(NOTICE/VERSION_UPDATE/EVENT), 노출 기간(start~end), 타겟 버전, 활성 여부.
- **[필수]** 타입·활성 상태 필터.
- **[필수]** 등록/수정 다이얼로그: popupType, title, content, imageUrl, startDateTime, endDateTime, targetVersion, isActive.
- **[필수]** 활성 토글(`isActive`) 즉시 변경. 삭제.
- **[선택]** 기간 중복/겹침 경고.

### 4.6 회원 관리 (Members)
**목적** — 회원(`members`) 및 디바이스(`member_devices`) 조회·관리.

- **[필수]** 요약 지표: 전체/활성/탈퇴/관리자 수.
- **[필수]** 목록: 닉네임, 가입 경로(KAKAO/GOOGLE), 연속 필사 일수, 권한(adminYn), 상태(탈퇴여부), 가입일.
- **[필수]** 검색(닉네임/oauthId), 가입 경로·상태 필터.
- **[필수]** 회원 상세 다이얼로그: 프로필, oauthId, 가입일, 연속/누적 필사, 등록 디바이스 목록(osType, deviceModel, appVersion, osVersion, activeYn).
- **[필수]** 관리 액션: 관리자 지정/해제(`adminYn`), 강제 탈퇴 처리(`withdrawal()` — withdrawalYn='Y', withdrawalAt 기록).
- **[선택]** 회원 내보내기(CSV).
- **[제약]** 개인정보 보호상 oauthId 등 식별자는 마스킹 노출 검토(§6).

### 4.7 필사 기록 (Member Quotes)
**목적** — 회원별 필사 내역(`member_quotes`) 조회.

- **[필수]** 목록: 회원, 명언 일자, 명언, 타이핑 여부, 이미지 여부, 좋아요(likeYn), 완료 상태(completed).
- **[필수]** 날짜·상태 필터, 회원/명언 검색.
- **[필수]** 상세 보기: 원문 vs 타이핑(typingKorQuote/typingEngQuote), 메모(memo), 이미지(imagePath), 완료/오늘완료(todayCompleted) 상태.
- **[필수]** 읽기 전용(운영 모니터링 목적). 수정/삭제는 기본 제외.
- **[선택]** 부적절 이미지 신고 처리(이미지 숨김) — 운영 정책 확정 시.

### 4.8 연속 필사 (Streaks)
**목적** — `member_streaks` 기반 습관 지표 모니터링.

- **[필수]** 요약: 최장 연속 기록, 평균 연속 일수, 오늘 작성 회원 수, 30일+ 유지 회원 수.
- **[필수]** 랭킹 목록: 회원, 현재 연속(currentStreak), 최대 연속(maxStreak), 마지막 작성일(lastWrittenDate), 오늘 작성 여부.
- **[필수]** 정렬(현재/최대 연속/최근 작성일), 닉네임 검색.
- **[필수]** 읽기 전용.

### 4.9 앱 버전 (App Version)
**목적** — `app_versions` 관리(개발자/운영자용).

- **[필수]** 현재 minVersion, nowVersion 조회 및 수정.
- **[필수]** 저장 시 강제 업데이트 기준(minVersion 미만) 명시.
- **[선택]** 버전 변경 시 VERSION_UPDATE 팝업 생성 연동 안내.

### 4.10 캐시 관리 (Cache)
**목적** — Redis 명언 캐시 운영(`/api/admin/cache`).

- **[필수]** 캐시 상태 요약(캐시된 일수, 마지막 갱신, Redis 연결 상태).
- **[필수]** 전체 캐시 새로고침(reload), 특정 날짜 갱신(refresh/{date}), 캐시 조회, 캐시 삭제.
- **[필수]** 파괴적 작업(삭제) 시 확인 모달.
- **[선택]** 캐시 적중률 등 메트릭(별도 수집 필요).

---

## 5. 공통 UX 요구사항

- **레이아웃** — 좌측 고정 사이드바(그룹: 개요/콘텐츠/회원/운영) + 상단 브레드크럼/검색/알림.
- **테이블** — 정렬·검색·필터·페이지네이션 일관 제공. 빈 상태(empty state) 디자인 포함.
- **폼/다이얼로그** — 생성·수정은 모달 다이얼로그, 필수값 검증·에러 메시지 인라인 표기.
- **파괴적 작업** — 삭제·캐시 삭제·강제 탈퇴는 확인 모달 필수.
- **피드백** — 작업 성공/실패 토스트.
- **반응형** — 데스크톱 우선, 태블릿(≥768px)까지 대응. 모바일은 비대상.
- **접근성** — shadcn 기본 컴포넌트의 키보드/포커스 동작 유지.

---

## 6. 비기능 요구사항

- **보안/권한** — 모든 어드민 API는 `adminYn = 'Y'` 검증. 회원 식별자(oauthId) 마스킹 노출 검토. 파괴적 액션은 권한 재확인.
- **개인정보** — 회원/필사 데이터는 운영 목적 외 노출 금지. 필사 이미지(imagePath) 접근 권한 통제.
- **성능** — 목록 화면 서버 사이드 페이지네이션. 대시보드 집계는 캐시/사전 집계 고려.
- **감사** — 어드민 변경 작업(삭제/권한 변경/배정 변경) 로그 적재 권장(추후 화면화).
- **국제화** — 명언은 한/영 병행 데이터. UI 카피는 한국어 기준.

---

## 7. 미결정 사항 (확인 필요)

1. 어드민 인증 방식 — 기존 OAuth + adminYn 재사용 vs 별도 어드민 로그인.
2. 명언 입력 검증 규칙 — 한/영 중 필수 항목, category 자유입력 vs 선택형.
3. `daily_quotes` 한 날짜 1명언 제약 유지 여부.
4. 필사 기록·이미지에 대한 운영 개입(숨김/삭제) 정책.
5. 공지사항에 노출 기간·고정 기능 추가 여부(스키마 변경 동반).
6. 회원 개인정보 마스킹 수준.
7. 감사 로그(audit) 도입 시점.

---

## 8. 화면 ↔ 요구사항 ↔ 데이터 매핑 (요약)

| # | 화면 | 주 데이터 | 쓰기 작업 |
|---|---|---|---|
| 1 | 대시보드 | 집계 전반 | 없음 |
| 2 | 명언 관리 | quotes | 생성·수정·삭제 |
| 3 | 일별 명언 배정 | daily_quotes (+캐시) | 배정·변경 |
| 4 | 공지사항 | notices | 생성·수정·삭제 |
| 5 | 팝업 관리 | popups | 생성·수정·삭제·토글 |
| 6 | 회원 관리 | members, member_devices | 권한 변경·강제 탈퇴 |
| 7 | 필사 기록 | member_quotes | 없음(읽기) |
| 8 | 연속 필사 | member_streaks | 없음(읽기) |
| 9 | 앱 버전 | app_versions | 수정 |
| 10 | 캐시 관리 | Redis cache | reload·refresh·delete |
