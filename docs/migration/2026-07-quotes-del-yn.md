# 마이그레이션: quotes.DEL_YN 소프트 삭제 컬럼 추가

## 배경
어드민 명언 삭제를 소프트 삭제로 처리하기 위해 `quotes` 테이블에 `DEL_YN` 컬럼을 추가한다.
- `DEL_YN = 'Y'` 인 명언은 앱 조회(일별/월별/필사 기록)에서 제외된다.
- 어드민 삭제 시 배정 이력(daily_quotes)이 있으면 409, 없으면 `DEL_YN = 'Y'` 로 전환한다.

## 스키마 관리 방식
이 저장소는 Flyway/Liquibase 를 사용하지 않는다.
- `bff/app` : `spring.jpa.hibernate.ddl-auto=none` → 운영 스키마는 수동 DDL 로 관리.
- `bff/admin` : `ddl-auto=update` (로컬 H2), 테스트: `create-drop`.

따라서 운영 MySQL 에는 아래 DDL 을 수동으로 적용해야 한다.

## DDL (MySQL)
```sql
ALTER TABLE quotes
    ADD COLUMN DEL_YN CHAR(1) NOT NULL DEFAULT 'N';
```

## 롤백
```sql
ALTER TABLE quotes DROP COLUMN DEL_YN;
```

## 영향받은 조회 경로 (delYn='N' 필터 추가)
- `service:quote` `DailyQuoteRepository.findByQuoteDate` / `findAllByQuoteDateBetween`
  / `findByDailQuoteSeq` / `findAllByDailQuoteSeqIn`
- `service:member` `MemberQuoteRepository.findAllByMemberAndQuoteDateBetween`
  / `findAllByMemberAndCreatedAtBetween`
