# member_devices 푸시 컬럼 추가 (2026-07)

iOS 서버 푸시 발송을 위한 FCM 토큰·수신 동의 컬럼. Flyway 미사용이므로 **배포 전 수동 적용 필수** (운영 TiDB).
추가형 컬럼이라 구버전 앱/서버에 무해 — 언제 적용해도 안전하며, **새 서버 배포 전에만** 적용돼 있으면 된다.

```sql
ALTER TABLE member_devices
  ADD COLUMN PUSH_TOKEN     VARCHAR(512) NULL,
  ADD COLUMN PUSH_AGREED_YN CHAR(1)      NOT NULL DEFAULT 'N',
  ADD COLUMN PUSH_AGREED_AT DATETIME     NULL;
```

검증:
```sql
SHOW COLUMNS FROM member_devices LIKE 'PUSH%';
SELECT PUSH_AGREED_YN, COUNT(*) FROM member_devices GROUP BY PUSH_AGREED_YN;  -- 전량 'N' 기대
```

적용 이력:
- 로컬: 2026-07-25 적용
- 운영(TiDB): ⬜ 미적용 — iOS 서버 배포 전 실행
