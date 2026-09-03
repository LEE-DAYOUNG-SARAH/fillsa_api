# DB 백업과 복구

> 작성: 2026-09-03
> 대상: 운영 TiDB (TiDB Cloud, v8.5.3)

---

## 현재 상태

TiDB Cloud 의 자동 백업이 동작하고 있다. 별도 스냅샷 구성은 필요 없다.

| 항목 | 값 |
|---|---|
| 주기 | Daily |
| **보관 기간** | **1일** |
| 실행 시각 | 17:00 UTC (= 02:00 KST) |

## ⚠️ 보관 1일의 한계

**어제 시점 스냅샷 하나만 존재한다.** 문제를 하루 넘겨 발견하면 복구할 수 없다.

데이터 손상은 즉시 드러나지 않는 경우가 많다. 특정 조건에서만 잘못 저장되는 종류의 결함은 사용자 문의로 며칠 뒤에 알려지기도 한다. 그 시점에는 이미 손상된 상태가 스냅샷에도 덮여 있다.

필사 기록·회고 답변은 **사용자가 직접 작성한 데이터**라 유실 시 대체할 방법이 없다.

**보관 기간을 7일 이상으로 늘리는 것을 권한다.** 비용이 늘지만 그만한 가치가 있다.

## 배포 전 백업 판단

변경 성격에 따라 다르다. 자동 백업이 있으므로 매번 전체 덤프를 뜰 필요는 없다.

| 변경 | 사전 백업 |
|---|---|
| 테이블·컬럼 **추가** | 불필요. 되돌리려면 추가한 것만 제거하면 된다 |
| 컬럼 **타입 변경**, 데이터 **가공** | **전체 덤프 권장.** 되돌릴 수 없다 |
| 컬럼·테이블 **삭제** | **전체 덤프 필수** |
| 코드만 변경 | 불필요 |

Flyway 도입(`docs/flyway-adoption.md`)은 `flyway_schema_history` 테이블 **생성 1건**이므로 첫 분류에 해당한다. 롤백은 아래 한 줄이다.

```sql
DROP TABLE flyway_schema_history;
```

## 스키마 덤프

데이터 없이 스키마만 뜬다. 가볍고, 아래 용도로 쓸 수 있다.

- 문서(`docs/migration/*.md`)와 실제 스키마 대조
- 신규 환경 구축용 기준
- 스키마 변경 전후 diff

```bash
mysqldump --no-data --skip-add-drop-table --skip-comments \
  -h <host> -P 4000 -u <user> -p \
  --ssl-mode=VERIFY_IDENTITY --ssl-ca=<ca-cert-path> \
  <database> > fillsa-schema-$(date +%Y%m%d).sql
```

TiDB Cloud 는 TLS 접속이 필수다. 접속 정보는 GitHub Secret(`PROD_YML_APP`)에 있으며 저장소에 두지 않는다.

> 덤프 파일에는 스키마 구조가 담긴다. 저장소에 커밋하지 말고 필요한 사람만 접근할 수 있는 곳에 둔다.

## 스키마 실태 확인이 필요한 이유

`bff/admin` 이 `ddl-auto: update` 로 운영에 떠 있었을 가능성이 있다(2026-09 에 `none` 으로 변경, `docs/migration/2026-09-member-quotes-answer.md` 참고).

그동안 Hibernate 가 엔티티 기준으로 스키마를 맞춰 왔다면 **실제 스키마가 문서와 어긋나 있을 수 있다.** 한 번 대조해 두는 것이 좋다.

```sql
SHOW CREATE TABLE member_quotes;
SHOW CREATE TABLE members;
SHOW CREATE TABLE member_devices;
```

## 복구 수단

### TiDB Cloud 자동 백업에서 복원

콘솔에서 스냅샷을 골라 **새 클러스터로 복원**한다. 기존 클러스터를 덮어쓰지 않으므로, 복원본에서 필요한 데이터만 뽑아 오는 방식이 안전하다.

### FLASHBACK (GC 윈도우 내)

TiDB 는 MVCC 로 과거 버전을 일정 시간 보관한다(`tidb_gc_life_time`, 기본 10분). 그 안이라면 실수 직후 되돌릴 수 있다.

```sql
-- 삭제한 테이블 복구
FLASHBACK TABLE <table> TO <new_name>;

-- 특정 시점 조회 (Stale Read)
SELECT * FROM <table> AS OF TIMESTAMP '2026-09-03 14:00:00';
```

**GC 윈도우가 짧아 사고 직후에만 유효하다.** 발견이 늦으면 쓸 수 없다.

### 우선순위

1. 사고를 인지한 즉시 **GC 윈도우 안이면 FLASHBACK** 을 먼저 시도
2. 넘겼으면 **자동 백업 스냅샷을 새 클러스터로 복원**해 데이터를 추출
3. 둘 다 불가하면 복구 수단이 없다 — 그래서 보관 기간이 중요하다

## 후속 과제

- [ ] 백업 보관 기간 7일 이상으로 상향 검토
- [ ] 스키마 덤프 1회 확보 → 문서와 대조, `docs/flyway-adoption.md` 의 `V1` 결정 항목 해소
- [ ] 복원 절차를 실제로 한 번 수행해 볼 것 (복구는 해본 적 있는 팀만 성공한다)
