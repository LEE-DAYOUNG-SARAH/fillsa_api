# 탈퇴·로그아웃 결함 수정

> 작성: 2026-09-03
> 브랜치: `fix/withdrawal-issues` → `main`

세 가지 결함을 수정한다. **셋 다 사용자에게는 정상으로 보여** 발견이 늦었다.

---

## ① 탈퇴해도 푸시가 계속 발송된다

`MemberService.withdraw()` 가 `members.withdrawal_yn` 만 `Y` 로 바꾸고 `member_devices` 는 손대지 않았다.
그런데 발송 대상 조회는 회원의 탈퇴 여부를 보지 않는다.

```kotlin
// DailyQuotePushScheduler
findAllByOsTypeAndActiveYnAndPushAgreedYn(IOS, "Y", "Y")
```

**증상**
- 탈퇴 후 앱을 삭제하지 않은 iOS 사용자는 매일 09시 푸시를 계속 받는다
- **같은 기기로 재가입하면 더 나쁘다.** 옛 회원의 디바이스 행이 `activeYn='Y'` 로 남아 있어
  동일 `pushToken` 이 발송 목록에 두 번 담긴다 → 푸시 2회 발송

**수정** — 탈퇴 시 해당 회원의 모든 디바이스를 비활성화하고 푸시 토큰을 제거한다.

```kotlin
fun withdrawAllDevices(member: Member) {
    memberDeviceRepository.findAllByMember(member).forEach {
        it.logout()          // activeYn = 'N'
        it.clearPushToken()  // pushToken = null
    }
}
```

발송 쿼리에 회원 조인을 추가하는 방법도 있으나, **탈퇴 시점에 정리하는 쪽을 택했다.**
매일 도는 스케줄러 쿼리를 무겁게 하지 않고, 재가입 시 중복 발송까지 함께 해결된다.

### 함께 수정 — 조용한 no-op

```kotlin
// 이전
val findMember = memberRepository.findByIdOrNull(member.memberSeq)
findMember?.withdrawal()   // 회원이 없어도 예외 없이 200
```

- 회원이 없으면 `NOT_FOUND` 를 던진다
- 이미 탈퇴한 회원이면 조기 반환한다. 그대로 두면 `withdrawal_at` 이 현재 시각으로 덮어써져
  **실제 탈퇴 시점이 유실된다**

---

## ② 로그아웃해도 리프레시 토큰이 살아 있다

`AuthService.refreshToken()` 이 **JWT 서명·만료만 검증**하고 Redis 를 조회하지 않았다.

```kotlin
// 이전
validateRefreshToken(request.refreshToken)          // 서명·만료만
val memberSeq = jwtTokenProvider.getMemberSeqFromToken(...)
val member = memberService.getActiveMemberBySeq(memberSeq)
return createToken(...)
```

**증상** — 로그아웃이 실제로는 무효화되지 않는다.
`deleteRefreshTokenForLogout` 이 Redis 에서 지워도 검증 시 Redis 를 보지 않으므로,
그 토큰은 **유효기간 90일**(`refresh-token-validity: 7776000000`) 내내 재발급에 쓸 수 있다.
로그아웃한 기기에 토큰만 남아 있으면 계속 접근이 가능했다.

`RefreshTokenCacheRepository.findByMemberId` 가 선언만 되어 있고 아무 데서도 쓰이지 않던 것도
이 검증이 처음부터 빠져 있었다는 정황이다.

**수정**

```kotlin
if (!refreshTokenCacheService.isValidRefreshToken(memberSeq, request.deviceId, request.refreshToken)) {
    throw BusinessException(JWT_REFRESH_TOKEN_INVALID)
}
```

Redis 에 저장된 `(회원, 디바이스)` 항목과 토큰 문자열이 일치해야 통과한다.

> **탈퇴는 이전에도 막혀 있었다.** `getActiveMemberBySeq` 가 `WITHDRAWAL_USER` 를 던지기 때문이다.
> 이 수정으로 새로 막히는 것은 **로그아웃** 경로다.

### 클라이언트 영향

로그아웃 후 남은 토큰으로 재발급을 시도하면 `3002 JWT_REFRESH_TOKEN_INVALID` 를 받는다.
앱은 이 코드에서 저장된 토큰을 폐기하고 로그인 화면으로 보내야 한다.

---

## ③ 웹 탈퇴 콜백이 실패 화면을 못 띄운다

`OAuthCallbackController` 에 두 가지 문제가 있었다.

**`code` 누락 시 500** — `@RequestParam code: String` 이 필수라 파라미터가 없으면
**컨트롤러 진입 전에** `MissingServletRequestParameterException` 이 발생한다.
`try/catch` 를 타지 못해 실패 리다이렉트도 안 되고 사용자는 500 화면을 본다.

**catch 안에서 NPE** — `URLEncoder.encode(e.message, "UTF-8")` 에서 `e.message` 가 `null` 인
예외(NPE, no-arg 생성 예외, 일부 WebClient 예외)가 오면 **예외 처리 중에 다시 예외가 난다.**

원시 예외 메시지를 쿼리스트링에 그대로 노출하던 것도 정보 노출이다.

**수정**
- `@RequestParam(required = false) code: String?` → 컨트롤러 안에서 검사해 `INVALID_REQUEST` 로 처리
- 예외 메시지 대신 공통 에러코드(`1006 UNEXPECTED_EXCEPTION`)를 전달
- 인코딩을 `failUrl()` 로 모아 null 이 들어갈 여지를 없앤다

### ⚠️ 별개 문제 — `withdraw.fillsa.com` DNS 레코드 없음

콜백은 성공·실패 모두 `withdraw.fillsa.com` 으로 리다이렉트하는데
**이 도메인에 DNS 레코드가 없다**(2026-09-03, 공개 리졸버 2곳 확인).

```
location: https://withdraw.fillsa.com/fail?message=2001
```

즉 이 수정으로 리다이렉트는 정상 동작하지만, **사용자는 여전히 브라우저 오류 화면을 본다.**
DNS 레코드 추가 또는 `fillsa.withdraw-url` 변경이 필요하다 — 이 PR 범위 밖.

---

## 검증

신규 테스트 6건 (`WithdrawalAndRefreshTest`)

| 항목 | 검증 내용 |
|---|---|
| 탈퇴 → 디바이스 | `activeYn='N'`, `pushToken=null` |
| 탈퇴 → 회원 상태 | `withdrawalYn='Y'` |
| 중복 탈퇴 | `withdrawal_at` 이 덮어써지지 않음 |
| 로그아웃 → 재발급 | 거부 |
| 미등록 디바이스 → 재발급 | 거부 |
| 탈퇴 → 재발급 | 거부 |

전체 111건 중 109건 통과. 나머지 2건은 이 변경과 무관한 기존 실패다(`docs/testing.md`).

> 테스트 작성 중 발견 — **같은 회원이 같은 초에 로그인하면 JWT 문자열이 동일하다**(`sub`·`iat` 동일).
> 토큰 값으로 세션을 구분하는 로직을 만들 때 주의할 것.

## 배포 시 주의

DDL 변경 없음. 다만 ②의 영향으로 **로그아웃 후 재발급을 시도하던 클라이언트가 실패하기 시작한다.**
정상 동작으로의 복귀이지만, 앱이 `3002` 를 재로그인 유도로 처리하는지 확인이 필요하다.
