# Flyway 도입

> 작성: 2026-09-03
> 브랜치: `feature/flyway-baseline` → `main`
> 규칙은 [`bff/app/src/main/resources/db/migration/README.md`](../bff/app/src/main/resources/db/migration/README.md) 참고.
> 이 문서는 **왜 이렇게 도입했는가**를 남긴다.

---

## 배경

스키마를 수동 DDL 로 관리해 왔다. `docs/migration/*.md` 에 SQL 을 적어두고 운영 DB 에 사람이 직접 적용하는 방식이다.

문제는 **적용 여부를 추적할 수단이 없다**는 것이다.

- 어떤 DDL 이 운영에 반영됐는지 문서와 실제가 어긋나도 알 수 없다
- 적용 순서(코드 배포보다 먼저/나중)를 사람이 매번 기억해야 한다
- 스테이징·로컬 DB 가 운영과 같은 상태인지 보장되지 않는다

실제로 홈 리뉴얼(#29) 배포 때 "DDL 을 먼저 적용해야 앱이 죽지 않는다"는 순서를 문서로만 전달해야 했다.

## 결정

**Flyway 를 도입하되, 기존 스키마는 baseline 으로 흡수한다.**

운영 DB 에는 이미 스키마와 데이터가 있다. Flyway 를 그냥 켜면 "마이그레이션이 하나도 적용되지 않은 DB"로 보고 `V1` 부터 실행하려 한다.

```yaml
flyway:
  baseline-on-migrate: true    # 이력 테이블이 없고 스키마가 비어있지 않으면 baseline 을 찍고 시작
  baseline-version: 1          # 기존 스키마 = V1 → 신규 마이그레이션은 V2 부터
  validate-on-migrate: true    # 적용된 파일을 수정하면 부팅 실패
  clean-disabled: true         # 운영 스키마를 임의로 정리하지 않음
```

`baseline-version` 이하 버전의 스크립트는 **실행되지 않는다.** 이것이 이 방식의 안전 근거다.

### 도입 시점의 마이그레이션은 0개다

인프라와 진입 방식만 세팅하고 스크립트는 넣지 않았다. 기존 스키마를 건드리지 않는 것이 이 변경의 핵심이므로, 검증 대상을 "baseline 이 안전한가" 하나로 좁혔다.

## 검증

문서만 믿지 않고 실측했다. MySQL 8 컨테이너에 **기존 테이블 + 데이터 2행**을 만들어 운영 상황을 재현했다.

**1) baseline 진입이 기존 스키마를 건드리지 않는가**

```
Schema history table `flyway_schema_history` does not exist yet
Creating Schema History table with baseline ...
Successfully baselined schema with version: 1
```

기존 테이블·데이터 그대로였고 `flyway_schema_history` 하나만 추가됐다.

**2) baseline 이하 버전이 정말 실행되지 않는가**

안전 속성을 직접 확인하려고 **`V1` 에 `DROP TABLE` 을 넣고** 실행했다.

```
rows_still_here
2                    ← V1 은 실행되지 않았다

version  description                      type      success
1        existing schema before flyway    BASELINE  1
2        add member quote answer          SQL       1
```

`V1` 은 건너뛰고 `V2` 만 적용됐다.

**3) TiDB advisory lock**

Flyway 는 동시 실행을 막으려 `GET_LOCK` 을 쓴다. TiDB 는 과거 이 함수를 지원하지 않아 우회 설정이 필요했던 이력이 있고, 현행 문서에도 지원 여부가 명시되어 있지 않다. 운영 DB(v8.5.3)에서 직접 확인했다.

```sql
SELECT GET_LOCK('flyway_probe', 1), RELEASE_LOCK('flyway_probe');
-- 1 / 1  → 정상 동작
```

`tidb_enable_noop_functions` 우회가 필요 없다.

## 소유권 — `bff:app` 만 마이그레이션을 소유한다

운영에서 app 과 admin 은 **같은 DB(TiDB)** 에 붙는다. 양쪽이 동시에 마이그레이션하면 충돌한다.

`bff:admin` 은 `spring.flyway.enabled: false` 로 명시했다. 현재 admin 에는 flyway 의존성이 없어 자동 설정이 뜨지 않지만, 나중에 누가 의존성을 추가해도 켜지지 않도록 의도를 남긴다.

같은 맥락에서 admin 의 `ddl-auto` 도 `none` 이어야 한다(2026-09 변경, `docs/migration/2026-09-member-quotes-answer.md` 참고). Hibernate 가 스키마를 자동 변경하면 Flyway 와 싸운다.

## 환경별 동작

| 환경 | 스키마 | Flyway |
|---|---|---|
| 운영 | TiDB, `ddl-auto: none` | ON |
| 로컬 | MySQL, `ddl-auto: none` | ON |
| 테스트 | H2, `ddl-auto: create-drop` | **OFF** |

테스트는 엔티티에서 스키마를 만든다. 마이그레이션 스크립트는 MySQL/TiDB 방언이라 H2 에서 실행하면 깨진다.

> **따라서 마이그레이션 스크립트는 테스트로 검증되지 않는다.**
> 운영 반영 전 스테이징이나 로컬 MySQL 에서 한 번 돌려보는 절차가 필요하다.
> 이 공백을 메우려면 testcontainers 로 MySQL 을 띄워 마이그레이션을 검증하는 테스트가 필요하다(후속 과제, `docs/testing.md` 참고).

## 검토했으나 채택하지 않은 것

| 대안 | 이유 |
|---|---|
| **`V1` 에 전체 스키마 덤프** | 운영 스키마를 정확히 뜰 수단이 없었다. 부정확한 덤프는 신규 환경에서 운영과 다른 스키마를 만든다. 신규 환경 구축은 현재 요구사항이 아니다 |
| **`baseline-version: 0` + `V1` 부터 시작** | 위와 같은 이유. `V1` 이 실제로 실행되는 구성이라 덤프 정확도에 의존한다 |
| **Liquibase** | 이미 SQL 로 DDL 을 관리해 왔다. XML/YAML 체인지로그로 다시 쓸 이유가 없다 |
| **도입과 첫 마이그레이션을 한 PR 에** | baseline 설정 실수와 스키마 변경이 섞이면 문제 원인을 가리기 어렵고 롤백 범위가 커진다 |
| **admin 에도 Flyway 활성화** | 같은 DB 라 충돌한다 |

## 후속 과제

- [ ] 신규 환경 구축용 스키마 덤프를 `V1` 로 만들지 결정 (현재는 신규 환경 요구가 없어 보류)
- [ ] testcontainers 로 마이그레이션 스크립트 검증 테스트 추가 (`docs/testing.md`)
- [ ] `PROD_YML_ADMIN` 의 `ddl-auto` 확인 — 명시되어 있다면 프로필이 base 를 이긴다

## 첫 배포 시 확인

1. 앱 기동 로그에 `Creating Schema History table ... with baseline` 이 보이는지
2. `SELECT * FROM flyway_schema_history` 에 `BASELINE` 행 1건만 있는지
3. 기존 테이블·데이터가 그대로인지
