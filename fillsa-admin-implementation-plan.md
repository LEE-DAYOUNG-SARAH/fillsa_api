# FILLSA Admin — 구현 계획서 & 태스크 분해

| 항목 | 내용 |
|---|---|
| 버전 | v0.1 |
| 작성일 | 2026-06-20 |
| 기준 산출물 | `fillsa-admin-PRD.md`, `fillsa-admin-mockup.html` |
| 레포 전략 | 프론트 = **별도 레포(`fillsa-admin-web`)** / 백엔드 = 기존 `fillsa_api`에 admin 엔드포인트 추가 |

---

## 1. 전체 그림

```
요구사항(PRD) ──┐
디자인(목업)  ──┼──▶ OpenAPI 계약 ──▶ [백엔드 admin API]  ◀──연동──▶  [fillsa-admin-web 프론트]
                │         (단일 합의 지점)
변경은 항상 PRD/목업/OpenAPI 먼저 → 코드 반영
```

- **백엔드**(`fillsa_api`): `/api/admin/**` 엔드포인트 추가 + `adminYn` 인가. 이미 있는 자산을 최대한 재사용.
- **프론트**(`fillsa-admin-web`, 신규 레포): React + shadcn/ui. 목업을 컴포넌트로 변환.
- **합의 지점**: OpenAPI 스펙. 백엔드 springdoc 산출물을 프론트가 타입/클라이언트로 소비.

### 1.1 기존 백엔드 자산 (재사용/확장 대상)
| 자산 | 현재 상태 | 어드민 작업 |
|---|---|---|
| `CacheAdminController` (`/api/admin/cache`) | reload·refresh·조회·삭제 **이미 존재** | 그대로 활용, 인가만 확인 |
| `AppVersionController` (PUT) | 수정 API **존재** | 어드민에서 호출 |
| Notice/Popup/Quote Controller | 앱(읽기) 중심 | **admin 쓰기(CRUD) 엔드포인트 신규** |
| Member / MemberQuote / MemberStreak | 앱(회원) 중심 | **admin 조회/관리 엔드포인트 신규** |

---

## 2. 기술 스택 (제안)

**프론트 (`fillsa-admin-web`)**
- Vite + React + TypeScript (어드민은 SEO 불필요 → 가벼운 SPA)
- shadcn/ui + Tailwind CSS
- 라우팅: React Router (또는 TanStack Router)
- 서버 상태: TanStack Query
- 폼/검증: React Hook Form + Zod
- API 클라이언트: OpenAPI 스펙 기반 자동 생성(`openapi-typescript` 등) 권장
- 인증: 액세스 토큰 보관 + `/api/admin/**` 호출 시 주입

> Next.js를 원하면 위 구성에서 라우팅/빌드만 교체. 핵심 컴포넌트 변환 방식은 동일.

**백엔드 (`fillsa_api`, 기존)**
- Kotlin + Spring Boot, JPA, Redis, springdoc(OpenAPI)
- Spring Security 필터에서 `adminYn = 'Y'` 인가

---

## 3. 마일스톤

| 단계 | 목표 | 산출물 |
|---|---|---|
| **Phase 0 — 결정** | PRD §7 미결정 확정, 레포/스택 픽스 | 결정 기록, OpenAPI 초안 |
| **Phase 1 — 기반** | 인증·인가 + 프론트 스캐폴드 + 공통 컴포넌트 | 로그인 후 빈 어드민 셸 동작 |
| **Phase 2 — 슬라이스** | 화면별 풀스택 완성(API+UI+연동) | 화면 단위 머지 |
| **Phase 3 — 마감** | 대시보드 집계, 권한·감사·QA·배포 | 운영 배포 |

진행은 화면 단위 **수직 슬라이스**(백+프론트를 한 화면씩 끝까지). 레이어별 일괄 진행은 지양.

---

## 4. 태스크 분해

태스크 ID 규칙: `P0/F`(기반), `BE-*`(백엔드), `FE-*`(프론트). 완료기준(DoD)은 §6 참조.

### Phase 0 — 결정/준비
| ID | 태스크 | 산출물 | 의존성 |
|---|---|---|---|
| P0-1 | PRD §7 미결정 7개 확정 | 결정 기록 | — |
| P0-2 | `fillsa-admin-web` 레포 생성 + 스택 셋업 | 빈 레포/CI | — |
| P0-3 | admin OpenAPI 스펙 초안(엔드포인트·DTO) | openapi.yaml | P0-1 |

### Phase 1 — 기반 (Foundation)
| ID | 태스크 | 레이어 | 의존성 |
|---|---|---|---|
| F-1 | `adminYn` 인가: `/api/admin/**` 보호(Security 설정) | BE | P0-1 |
| F-2 | 어드민 로그인/세션·토큰 처리 엔드포인트 정리 | BE | F-1 |
| F-3 | 프론트 스캐폴드: 라우팅·인증가드·API클라이언트·Query 설정 | FE | P0-2,P0-3 |
| F-4 | 공통 UI: 사이드바/레이아웃/브레드크럼 (목업 셸 변환) | FE | F-3 |
| F-5 | 공통 컴포넌트: DataTable(정렬·검색·필터·페이지네이션), Dialog, 확인모달, Toast, EmptyState | FE | F-3 |
| F-6 | 로그인 화면 + 인증 플로우 연동 | FE | F-2,F-4 |

### Phase 2 — 화면 슬라이스 (우선순위 순)
각 슬라이스 = `BE` (admin API) + `FE` (목업→shadcn 변환·연동).

| # | 슬라이스 | BE 태스크 | FE 태스크 | 의존성 |
|---|---|---|---|---|
| S1 | 명언 관리 | BE-1 quotes CRUD + 검색/필터 + 삭제시 daily 참조검증 | FE-1 목록·검색·필터·추가/수정 다이얼로그·삭제 | F-* |
| S2 | 일별 명언 배정 | BE-2 daily_quotes 월조회·배정/변경 + 캐시갱신 연동 | FE-2 캘린더 뷰·배정 다이얼로그 | S1 |
| S3 | 공지사항 | BE-3 notices CRUD | FE-3 목록·작성/수정 | F-* |
| S4 | 팝업 관리 | BE-4 popups CRUD + isActive 토글 + 필터 | FE-4 목록·필터·등록/수정·토글 | F-* |
| S5 | 회원 관리 | BE-5 members 목록/검색/필터 + 상세(+devices) + adminYn 변경 + 강제탈퇴 | FE-5 지표·목록·상세 다이얼로그·관리 액션 | F-* |
| S6 | 필사 기록 | BE-6 member_quotes 조회(날짜·상태 필터)·상세 | FE-6 목록·상세(읽기) | S5 |
| S7 | 연속 필사 | BE-7 member_streaks 랭킹·요약 집계 | FE-7 요약·랭킹·정렬 | S5 |
| S8 | 앱 버전 | BE-8 기존 AppVersion GET/PUT 활용 | FE-8 조회·수정 폼 | F-* |
| S9 | 캐시 관리 | BE-9 기존 CacheAdminController 활용(인가 확인) | FE-9 상태·작업 버튼·캐시 미리보기 | F-* |

### Phase 3 — 마감
| ID | 태스크 | 레이어 | 의존성 |
|---|---|---|---|
| C-1 | 대시보드 집계 API(회원/필사/추이/분포) | BE | S5,S6,S7 |
| C-2 | 대시보드 화면 연동 | FE | C-1 |
| C-3 | (선택) 권한 세분화 / 감사 로그 | BE/FE | — |
| C-4 | E2E·QA, 빈/에러 상태 점검 | QA | 전체 |
| C-5 | 배포 파이프라인(프론트 호스팅, 백 배포) | DevOps | 전체 |

---

## 5. 작업 흐름 (브랜치/PR)

1. 슬라이스 단위 feature 브랜치: `feat/admin-quotes` 등. (백엔드·프론트 각 레포에서)
2. BE 먼저 머지 → OpenAPI 갱신 → FE가 최신 타입으로 연동.
3. PR 체크리스트: PRD 항목 충족 / DoD 충족 / 검증 통과 / 목업과 시각 일치.
4. 변경 요청은 **PRD·목업 먼저 수정 → 태스크 갱신 → 코드** 순서.

---

## 6. Definition of Done (슬라이스 공통)

- [ ] admin API가 `adminYn` 인가 하에 동작하고 OpenAPI에 반영됨
- [ ] 목록: 서버 페이지네이션·검색·필터 동작
- [ ] 생성/수정: 필수값 검증·에러 인라인·성공 토스트
- [ ] 파괴적 작업: 확인 모달 + 참조 무결성 처리
- [ ] 빈 상태·로딩·에러 상태 UI 존재
- [ ] 목업과 레이아웃/컴포넌트 일치
- [ ] 기본 테스트(백 단위테스트 / 프론트 핵심 플로우)

---

## 7. 선행 결정 (Phase 0에서 닫아야 진행 가능)

PRD §7과 동일. 특히 아래는 API/스키마에 직접 영향:
1. 어드민 인증 방식(OAuth+adminYn 재사용 vs 별도 로그인) → F-1/F-2
2. 명언 입력 검증(한/영 필수 항목, category 자유/선택형) → BE-1/FE-1
3. daily_quotes 날짜당 1명언 제약 → BE-2
4. 회원 식별자 마스킹 수준 → BE-5/FE-5
5. 필사 기록 운영 개입(숨김/삭제) 여부 → BE-6/FE-6
6. 공지 노출기간·고정 추가 여부(스키마 변경) → BE-3
7. 감사 로그 도입 시점 → C-3

---

## 8. 권장 첫 3스텝
1. **P0-1** 미결정 7개 확정 (가장 큰 블로커).
2. **P0-3** 명언·일별배정 중심으로 OpenAPI 초안.
3. **F-1~F-5** 인가 + 프론트 기반(스캐폴드·공통 컴포넌트) → 이후 S1부터 슬라이스 시작.
