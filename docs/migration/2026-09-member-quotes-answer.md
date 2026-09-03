# 마이그레이션: member_quotes 오늘의 질문 답변 컬럼 추가

## 배경
홈·캘린더 리뉴얼에서 '오늘의 질문' 답변 기능이 추가된다(`docs/home-renewal-api-plan.md`).
질문 원문은 이미 `quotes.QUESTION_KO` / `QUESTION_EN` 에 있으나, 사용자 답변을 저장할 곳이 없다.

답변은 회원 × 날짜 단위라 별도 테이블 없이 `member_quotes` 를 확장한다.
해당 테이블에 `(MEMBER_SEQ, DAILY_QUOTE_SEQ)` UNIQUE 가 이미 있어 정합성이 보장된다.

## 스키마 관리 방식
이 저장소는 Flyway/Liquibase 를 사용하지 않는다.
- `bff/app` : `ddl-auto: none` → 운영 스키마는 수동 DDL 로 관리
- `bff/admin` : `ddl-auto: none` (2026-09 변경, 아래 참고) / 로컬 H2 는 `local` 프로필에서 `update`
- 테스트: `create-drop`

## DDL (MySQL / TiDB)
```sql
ALTER TABLE member_quotes
    ADD COLUMN answer      VARCHAR(200) NULL COMMENT '오늘의 질문 답변',
    ADD COLUMN answered_at DATETIME     NULL COMMENT '답변 최종 수정 시각';
```

## 롤백
```sql
ALTER TABLE member_quotes
    DROP COLUMN answer,
    DROP COLUMN answered_at;
```

## ⚠️ 적용 순서 (중요)

**DDL 을 코드 배포보다 먼저 적용해야 한다.**

`MemberQuote` 엔티티에 `answer` / `answeredAt` 매핑이 추가되었고 `ddl-auto: none` 이므로,
컬럼이 없는 상태로 새 코드가 뜨면 `member_quotes` 를 읽는 **모든 쿼리**가
`Unknown column 'answer'` 로 실패한다 (홈·캘린더·필사·좋아요·목록 전부).

```
1. DDL 적용            ← 기존 코드에는 영향 없음(사용하지 않는 컬럼이 늘 뿐)
2. 코드 배포
3. 검증: 주간 조회 / 월간 조회 / 답변 저장
```

컬럼 추가 후 코드를 롤백해도 컬럼만 남으므로 무해하다.

## 영향받는 경로

| 대상 | 내용 |
|---|---|
| `MemberQuote` | `answer`, `answeredAt` 필드 + `updateAnswer()` |
| `MemberQuote.isViewQuoteData()` | 답변만 작성한 날도 조회 대상에 포함되도록 조건 추가 |
| `POST /api/v2/member-quotes/{dailyQuoteSeq}/answer` | 신규 |
| `GET /api/v2/member-quotes/weekly` · `daily` | 신규 (응답에 answer 포함) |
| `GET /api/v2/member-quotes/monthly` | 응답 필드 확장 (answer 포함) |

답변은 **필사 완료·연속 필사에 반영하지 않는다**(`docs/home-renewal-api-plan.md` §11-1).

---

## 함께 변경: `bff/admin` 의 `ddl-auto`

`bff/admin/src/main/resources/application.yml` 의 `ddl-auto` 를 `update` → **`none`** 으로 변경했다.

**배경** — base 설정은 로컬 H2 전제로 작성되어 있었으나, 운영에서 admin 은 `application-prod.yml`
에서 datasource 만 TiDB 로 교체한다. prod yml 이 `ddl-auto` 를 명시하지 않았다면
**base 의 `update` 가 그대로 적용되어 Hibernate 가 운영 스키마를 자동 변경**하게 된다.
admin 은 앱과 같은 DB 에 붙으므로 영향 범위가 앱 전체다.

base 를 `none` 으로 두면 prod yml 이 무엇을 하든(또는 하지 않든) 안전한 쪽이 기본값이 된다.

로컬 H2 부팅은 `application-local.yml` 로 분리했다.
```bash
./gradlew :bff:admin:bootRun --args='--spring.profiles.active=local'
```

### 확인 필요
- [ ] `PROD_YML_ADMIN` 시크릿에 `spring.jpa.hibernate.ddl-auto` 가 명시되어 있는지 확인.
      `update` 로 명시되어 있다면 이 변경만으로는 막히지 않으므로 시크릿도 수정해야 한다.
- [ ] 그동안 Hibernate 자동 변경이 있었다면 운영 스키마가 문서와 어긋나 있을 수 있다.
      `SHOW CREATE TABLE member_quotes` 등으로 실제 스키마를 한 번 대조할 것.
