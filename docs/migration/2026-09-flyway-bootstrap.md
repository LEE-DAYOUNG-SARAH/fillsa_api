# 마이그레이션: Flyway 부트스트랩 (TiDB)

## 배경

Flyway 를 처음 켰을 때 앱이 기동하지 못했다.

```
java.sql.SQLException: 'CREATE TABLE ... SELECT' is not implemented yet
ErrorCode(1105)
  at JdbcTableSchemaHistory.create(...)
  at DbBaseline.baseline(...)
```

Flyway 의 MySQL 드라이버는 이력 테이블 `flyway_schema_history` 를 만들 때
`CREATE TABLE ... AS SELECT` 를 사용하는데 **TiDB 가 이 구문을 지원하지 않는다.**

Flyway 는 TiDB 를 인식하면 전용 경로를 타지만, **운영 TiDB Cloud 에서는 일반 MySQL 로
인식했다.** 앱 기동 로그에 TiDB 관련 메시지가 없고 `MySQLNamedLockTemplate` 을 사용한 것으로
확인된다. 로컬 TiDB(`pingcap/tidb:v8.5.3`)에서는 TiDB 로 인식되어 같은 설정이 성공한다.
즉 **환경에 따라 Flyway 의 판정이 달라진다.**

## 해법

이력 테이블과 baseline 행을 **미리 만들어 둔다.** 테이블이 이미 있으면 Flyway 는 생성
구문을 타지 않으므로, TiDB 인식 여부와 무관하게 동작한다.

### ⚠️ baseline 행이 반드시 필요하다

빈 이력 테이블만 만들면 Flyway 가 "적용된 마이그레이션이 없는 스키마"로 판단해
**V1 부터 전부 실행한다.** `baseline-on-migrate` 는 이력 테이블이 *없을 때만* 동작하므로
이 상황에서는 개입하지 않는다.

로컬 TiDB 에서 실제로 재현했다 — 빈 테이블만 둔 경우 `Current version: << Empty Schema >>`
가 되면서 모든 마이그레이션이 대상이 된다.

## DDL (운영 TiDB — 코드 배포 전에 적용)

```sql
CREATE TABLE flyway_schema_history (
  installed_rank INT NOT NULL,
  version        VARCHAR(50)   DEFAULT NULL,
  description    VARCHAR(200)  NOT NULL,
  type           VARCHAR(20)   NOT NULL,
  script         VARCHAR(1000) NOT NULL,
  checksum       INT           DEFAULT NULL,
  installed_by   VARCHAR(100)  NOT NULL,
  installed_on   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  execution_time INT           NOT NULL,
  success        TINYINT(1)    NOT NULL,
  PRIMARY KEY (installed_rank),
  KEY flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO flyway_schema_history
  (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
VALUES
  (1, '1', 'existing schema before flyway', 'BASELINE', 'existing schema before flyway', NULL, CURRENT_USER(), 0, 1);
```

테이블 구조는 Flyway 10.20.1 이 직접 생성한 것을 그대로 옮겼고, baseline 행도 Flyway 가
스스로 기록한 값을 따랐다(`type=BASELINE`, `script` = description 과 동일, `checksum=NULL`).

## 적용 순서

```
1. 위 DDL 적용            ← 기존 코드에 영향 없음(사용하지 않는 테이블이 하나 늘 뿐)
2. 코드 배포 (flyway.enabled=true)
3. 기동 로그 확인
```

## 롤백

```sql
DROP TABLE flyway_schema_history;
```

코드는 `spring.flyway.enabled: false` 로 되돌린다. 기존 스키마에는 영향이 없다.

## 검증 (로컬 TiDB v8.5.3)

운영과 같은 버전의 TiDB 를 `pingcap/tidb:v8.5.3` 로 띄워 확인했다.

| 확인 | 결과 |
|---|---|
| 부트스트랩 없이 실행 | 로컬에서는 성공(TiDB 로 인식) — 운영과 판정이 다름을 확인 |
| 빈 이력 테이블만 생성 | `<< Empty Schema >>` — **V1 부터 실행 대상이 됨** |
| 이력 테이블 + baseline 행 | `Current version: 1` → **V2 만 적용** |
| `V1` 에 `DROP TABLE` 카나리 | **실행되지 않음** — 데이터 2행 그대로 |

마지막 항목이 안전 근거다. baseline 이하 버전은 실행되지 않는다.

## 배포 후 확인

```sql
SELECT installed_rank, version, description, type, success
FROM flyway_schema_history ORDER BY installed_rank;
```

BASELINE 행 1건만 있으면 정상이다(현재 `db/migration` 에 스크립트가 없다).

기동 로그에 아래가 보이면 안 된다.
- `Creating Schema History table ...` — 부트스트랩이 적용되지 않았다는 뜻
- `<< Empty Schema >>` — baseline 행이 없다는 뜻
