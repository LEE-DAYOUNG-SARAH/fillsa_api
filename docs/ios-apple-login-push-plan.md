# iOS 대응: Apple 로그인 + 서버 푸시 발송 + 알림 동의

> 작성: 2026-07-25 · 최종 갱신: 2026-07-25 (백엔드 구현 완료 반영)
> 브랜치: `feature/ios-apple-push` (커밋 `0febce9`, `b716fc4` — 푸시됨, **main 미머지**)

---

## 0. 상태 요약

| 파트 | 상태 |
|---|---|
| 백엔드 구현 (Apple 로그인·탈퇴·푸시 API·FCM·스케줄러) | ✅ **완료 + 로컬 E2E 검증** |
| 로컬 DB DDL (member_devices 푸시 컬럼) | ✅ 적용 |
| 운영 TiDB DDL | ⬜ 배포 전 적용 (언제든 무해) |
| Firebase 준비 (보훈님 3건) | ⬜ 요청함 — §4 |
| prod yml 기입 + 시크릿 재등록 | ⬜ 서비스 계정 JSON 수령 후 |
| PR 머지 → 배포 | ⬜ 위 완료 후 |
| 앱(iOS) 개발 | ⬜ 병렬 진행 가능 (서버 계약 확정) |

## 1. 확정된 결정 사항

1. **푸시**: 매일 **09:00 KST** 스케줄 발송(오늘의 명언), 대상 = **푸시 동의한 iOS** 디바이스 전원
2. **안드로이드**: 현행 앱 로컬 알림 유지 — 서버 발송 안 함
3. **어드민 수동 발송**: 스코프 제외 (추후 검토)
4. **Apple 토큰 서버 검증**: 생략 — 카카오/구글과 동일 신뢰 모델 (앱이 인증 후 sub 전달)
5. **탈퇴 시 Apple revoke: 보류** — 카카오/구글과 동일하게 **우리 DB 탈퇴만** 수행.
   - 근거: 기능상 완전 동일 동작. revoke는 Apple 심사 규정(5.1.1(v)) 대응용일 뿐.
   - 리스크: 심사 리젝 가능성. **코드에는 revoke가 이미 구현돼 있어**, 리젝 시 Apple 키(Team ID·Key ID·p8)를 prod yml에 채우고 시크릿 재등록만 하면 **코드 변경 없이 활성화**됨.

## 2. 구현 내역 (서버 — 완료)

### Apple 로그인 (`0febce9`)
- `Member.OAuthProvider`에 `APPLE` 추가 — 로그인 계약 불변 (앱이 `oAuthProvider=APPLE, oAuthId=<sub>` 전달)
- `AppleAuthClient`: p8 ES256 client_secret → code 교환 → revoke (설정 비면 부팅 무해, 호출 시에만 실패)
- `DELETE /api/v1/auth/withdraw?appleCode=` 옵션 파라미터 — revoke는 best-effort (실패해도 탈퇴 진행 + 경고 로그). **appleCode 미전달 시 = DB 탈퇴만** (현재 확정 동작)
- ✅ 검증: APPLE 로그인→JWT→탈퇴→`WITHDRAWAL_YN=Y` E2E 통과

### 푸시 인프라 (`b716fc4`)
- **DB**: `member_devices`에 `PUSH_TOKEN`(512)·`PUSH_AGREED_YN`·`PUSH_AGREED_AT` — DDL: `docs/migration/2026-07-member-devices-push.md`
- **FCM 토큰 수신 (2경로)**:
  1. **로그인 시**: `DeviceData.pushToken`·`pushAgreed` optional 필드 — 실려 오면 저장, 없으면 무시(구버전 하위호환)
  2. **전용 API**: `PUT /api/v1/member-devices/push` `{deviceId, pushToken, agreed}` — 권한 허용/거부·설정 토글·토큰 리프레시 시
- **발송기**: Firebase Admin SDK(`FirebaseConfig` — 서비스 계정 미설정 시 발송만 비활성), `PushService`(500개 배치, UNREGISTERED/INVALID_ARGUMENT 토큰 자동 정리)
- **스케줄러**: `DailyQuotePushScheduler` — `cron 0 0 9 * * *` (KST, 프로퍼티 `fillsa.push.daily-cron`로 override 가능), 대상 `OS_TYPE=IOS AND ACTIVE_YN=Y AND PUSH_AGREED_YN=Y AND PUSH_TOKEN NOT NULL`, 제목 "오늘의 필사" + 본문 오늘의 명언
- ✅ 검증: 로그인 push 필드 저장 → 토글 API → DB 반영 E2E 통과. FCM 미설정 부팅 무해 확인

## 3. 앱(iOS) 연동 계약 — 보훈님 개발 기준

| 시점 | 호출 |
|---|---|
| Apple 로그인 | 기존 `POST /api/v1/auth/login` — `oAuthProvider: "APPLE"`, `oAuthId: <Apple sub>`, `deviceData.osType: "IOS"` (+가능하면 `pushToken`/`pushAgreed` 동봉) |
| 알림 권한 응답/토글/토큰 갱신 | `PUT /api/v1/member-devices/push` `{deviceId, pushToken, agreed}` |
| 탈퇴 | 기존 `DELETE /api/v1/auth/withdraw` (appleCode 불필요 — 결정 5) |

주의: Apple은 이름을 최초 로그인 1회만 제공 → 그때 nickname 생성해 전달.

## 4. 보훈님 요청 사항 (Firebase 준비 — 전달됨)

1. **Firebase 프로젝트에 iOS 앱 추가** (기존 필사 프로젝트, iOS 번들 ID)
2. **APNs 인증 키(p8) 발급 → Firebase 콘솔에 업로드** (프로젝트 설정→클라우드 메시징→Apple 앱 구성, Key ID·Team ID 입력) — *키 파일을 서버에 줄 필요 없음, Firebase 등록이 전부*
3. **Firebase 서비스 계정 키 JSON**을 다영에게 안전 채널(AirDrop/1Password)로 전달 — **서버가 받는 유일한 물건** (또는 다영을 프로젝트 편집자로 초대)

## 5. 남은 실행 순서

1. ⬜ 보훈님 §4 완료 → **서비스 계정 JSON 수령**
2. ⬜ app prod yml에 `fcm.service-account-json`(base64) 기입 → `PROD_YML_APP` 시크릿 재등록
3. ⬜ 운영 TiDB에 member_devices DDL 적용 (지금 미리 해도 무해)
4. ⬜ PR 머지(`feature/ios-apple-push` → main) = 자동 배포
5. ⬜ TestFlight 검증: Apple 로그인 → 푸시 토큰 등록 → 09시(또는 cron 임시 변경으로 즉시) 실수신 확인
6. (심사 리젝 시에만) Apple 키 발급 → `oauth.apple.*` 기입 → 시크릿 재등록 → revoke 자동 활성화
