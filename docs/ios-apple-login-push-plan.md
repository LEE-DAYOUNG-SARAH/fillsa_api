# iOS 대응 플랜: Apple 로그인 + 서버 푸시 발송 + 알림 동의

> 작성: 2026-07-25 · 배경: Flutter iOS 앱 출시 준비
> 요구사항 3가지: ① Sign in with Apple(앱스토어 심사 **의무** — 소셜 로그인 있는 앱은 필수) ② iOS 푸시는 서버가 발송(안드로이드는 현재 앱 로컬 알림) ③ 알림 수신 동의 관리

---

## 0. 현재 상태 (2026-07-25 코드 확인)

| 항목 | 상태 |
|---|---|
| 로그인 방식 | 앱이 OAuth 수행 → `POST /api/v1/auth/login`에 `oAuthProvider + oAuthId + nickname` 전달. **서버는 OAuth 토큰 미검증** (카카오/구글 동일) |
| `Member.OAuthProvider` | `KAKAO, GOOGLE` — APPLE 없음 |
| `MemberDevice.OsType` | `ANDROID, IOS` — iOS 준비됨 ✅ |
| member_devices 스키마 | 푸시 토큰·동의 컬럼 **없음** |
| FCM 서버 코드 | **전무** (Firebase Admin SDK 미도입). 안드로이드 알림은 앱 내 로컬 알림 |
| 탈퇴 | `OAuthWithdrawalService.withdraw(provider, code)` — 카카오/구글 연결 해제 |

## 1. 기능 A — Apple 로그인

### 플로우 (기존 계약 유지 — 최소 변경안)
```
[iOS 앱] Sign in with Apple 수행 → sub(고유 ID)·identityToken 획득
   → POST /api/v1/auth/login { oAuthProvider: "APPLE", oAuthId: <sub>, nickname, ... }
   → [서버] 기존 로그인 로직 그대로 (회원 생성/조회 → JWT 발급)
```
기존 카카오/구글과 완전히 같은 흐름이라 **앱 API 계약 변경 없음** (enum 값 하나 추가 = 하위호환).

### 서버 변경
1. `Member.OAuthProvider`에 `APPLE` 추가 (컬럼이 varchar+`@Enumerated(STRING)`라 DDL 불필요)
2. **탈퇴 revoke 추가 — 앱스토어 심사 필수** (계정 삭제 시 Apple 토큰 폐기 의무):
   - `OAuthWithdrawalService`에 APPLE 분기: 앱이 보낸 `authorizationCode` → client_secret(JWT, **p8 키로 ES256 서명**) 생성 → `appleid.apple.com/auth/token` 교환 → `/auth/revoke`
   - 준비물(다영님): Apple Developer → **Team ID, Key ID, AuthKey_xxx.p8** (Sign in with Apple 키), client_id = iOS 번들 ID → prod yml에 추가 (p8은 base64로 yml에 넣거나 별도 시크릿)
3. (선택) `identityToken` 서버 검증 — Apple JWKS로 서명·aud 검증. 기존 카카오/구글도 미검증이라 **정합성 유지 차원에서 1차 생략, 추후 3사 일괄 도입** 권장

### 주의사항 (앱 쪽 공유 필요)
- Apple은 **이름·이메일을 최초 로그인 1회만** 제공 → 앱이 그때 nickname을 만들어 서버로 보내야 함
- "이메일 가리기" 선택 시 릴레이 주소가 옴 — 우리는 이메일 저장 안 하므로 영향 없음
- 재설치 후 재로그인 시에도 `sub`는 동일 → oAuthId로 기존 회원 매칭 정상 동작

## 2. 기능 B — 푸시 발송 인프라 (FCM 단일 창구)

### 아키텍처 결정: FCM 하나로 iOS까지 발송
FCM은 APNs를 백엔드로 지원 → **Firebase 프로젝트에 APNs 키(p8)를 등록하면 iOS 기기도 FCM 토큰으로 발송 가능**. 서버는 Firebase Admin SDK 하나만 쓰면 되고, APNs 직접 연동이 불필요하다. (기존 안드로이드가 쓰는 Firebase 프로젝트 재사용)

```
[매일 HH:mm 스케줄러(bff:app)] → 발송 대상 조회(푸시 동의 + 활성 디바이스)
   → Firebase Admin SDK sendEachForMulticast(토큰 배치 500개씩)
   → 실패 토큰(UNREGISTERED 등) 자동 정리(push_token null 처리)
```

### DB 변경 (운영 TiDB 수동 DDL — `docs/migration/`에 문서 추가)
```sql
ALTER TABLE member_devices
  ADD COLUMN PUSH_TOKEN     VARCHAR(512) NULL,
  ADD COLUMN PUSH_AGREED_YN CHAR(1)      NOT NULL DEFAULT 'N',
  ADD COLUMN PUSH_AGREED_AT DATETIME     NULL;
```
(추가형이라 기존 앱에 무해 — DEL_YN 때와 같은 패턴, 배포 전 선적용)

### 신규 앱 API
| 메서드 | 경로 | 용도 |
|---|---|---|
| PUT | `/api/v1/member-devices/push` | 푸시 토큰 등록/갱신 + 동의 여부 `{ pushToken, agreed }` — 권한 허용/거부/설정 토글 시마다 호출 |

로그인 `DeviceData`에도 optional 필드(`pushToken?`, `pushAgreed?`)를 추가해 로그인 시 함께 받도록 함 (optional 추가 = 하위호환 안전).

### 서버 구성
- 의존성: `firebase-admin` SDK (bff:app)
- 크리덴셜: Firebase **서비스 계정 JSON** → prod yml 경로 참조 or base64 프로퍼티 (PROD_YML_APP 시크릿에 포함)
- 스케줄러: Spring `@Scheduled(cron = "0 0 9 * * *")` KST(컨테이너 TZ=Asia/Seoul) — VM 1대·컨테이너 1개라 분산락 불필요. 발송 이력 테이블(선택)로 중복 방지·통계
- 대상 쿼리: `PUSH_AGREED_YN='Y' AND ACTIVE_YN='Y' AND PUSH_TOKEN IS NOT NULL AND OS_TYPE='IOS'` (확정: iOS만)

## 3. 기능 C — 알림 동의 관리

- **앱(iOS)**: 첫 실행 시 OS 권한 요청 → 결과(허용/거부)를 위 API로 서버 보고. 설정 화면에 "필사 알림" 토글 → 같은 API로 갱신
- **서버**: `PUSH_AGREED_YN='Y'`인 디바이스에만 발송 (동의 기반 발송 원칙). 서비스 알림 성격(필사 리마인드)이라 광고성 수신동의 규제와는 구분되지만, 야간(21시~8시) 발송은 피하는 것을 기본값으로
- **어드민(후순위)**: 회원 상세에 디바이스별 동의 여부 표시 — 기존 상세 다이얼로그 확장

## 4. 앱(강보훈님) 협업 필요 항목

1. Sign in with Apple 구현 → `oAuthProvider=APPLE, oAuthId=sub`로 기존 로그인 호출 (+ 탈퇴 시 `authorizationCode` 서버 전달)
2. iOS: Firebase SDK 연동 + FCM 토큰 획득 → 신규 push API 호출
3. iOS 알림 권한 요청 UX + 설정 토글 화면
4. (확인) 안드로이드 로컬 알림 유지 여부 — §6 질문 2

## 5. 실행 순서

1. **[다영]** Apple Developer 키 발급(Team ID·Key ID·p8) + Firebase에 APNs 키 등록 + Firebase 서비스 계정 JSON 준비
2. **[백엔드 1차]** `OAuthProvider.APPLE` + 탈퇴 revoke + 테스트 → 앱 개발 병렬 시작 가능
3. **[DDL]** member_devices 푸시 컬럼 선적용 (기존 앱 무해)
4. **[백엔드 2차]** push 등록 API + Firebase Admin SDK + 스케줄러 + 테스트
5. **[시크릿]** PROD_YML_APP에 apple/firebase 설정 추가 → 재등록 → 배포
6. **[통합 검증]** TestFlight 빌드로 Apple 로그인 → 푸시 토큰 등록 → 실제 발송 수신 확인 → 탈퇴 revoke 확인

## 6. 확정 사항 (2026-07-25)

1. **푸시**: 매일 **09:00 KST** 고정 스케줄 발송 (오늘의 명언 리마인드), 대상 = 푸시 동의한 iOS 디바이스 전원
2. **안드로이드**: 현행 로컬 알림 유지 — 서버 발송은 **iOS만** (`OS_TYPE='IOS'` 필터)
3. **어드민 수동 발송**: 이번 스코프 **제외** (배포 속도 우선, 추후 검토)
4. **Apple 토큰 서버 검증**: **생략** — 카카오/구글과 동일 신뢰 모델(앱이 인증 후 sub 전달). 단 **탈퇴 revoke는 서버 필수**(p8 키는 탈퇴용으로만 사용)
