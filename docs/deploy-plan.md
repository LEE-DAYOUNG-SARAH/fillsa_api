# BFF 분리 배포 플랜 (bff:app + bff:admin) — OCI 기준

> 작성: 2026-07-18 · 대상 브랜치: `feature/bff-restructure` → `main`
> 목적: 모놀리식 단일 컨테이너 배포를 **app / admin 2-프로세스 배포**로 전환하기 위한 계획.
> 서버는 AWS EC2가 아니라 **Oracle Cloud(OCI) VM**이다 (deploy.yml의 `EC2_*` 시크릿은 이름만 EC2). 이 문서가 SSOT — 실행은 §7 순서대로.

---

## 0. 인프라 현황 (OCI)

| 항목 | 값 | 근거/비고 |
|---|---|---|
| VM | `fillsa-vm` — **VM.Standard.A1.Flex, 1 OCPU / 6GB RAM** | `.github/workflows/oci-vm-launch.yml` |
| 아키텍처 | **ARM64 (Ampere)** — 이미지 반드시 `linux/arm64` 포함 | 현 deploy.yml buildx가 이미 amd64+arm64 멀티플랫폼 ✅ |
| 확보 방식 | Always Free 용량 부족 → 20분 주기 자동 재시도 런치 워크플로 | VM이 없어지면 재생성되는 전제 → **부트스트랩 재현성 필요(§6)** |
| 접속 | GH Secrets `EC2_HOST`/`EC2_USERNAME`/`EC2_SSH_KEY` (실제로는 OCI VM) | 명칭 정리는 선택(§4-6) |
| DB | 운영 **TiDB** (MySQL 호환, 외부 접속) | prod yml driver `com.mysql.cj` 그대로 |
| 기타 | `fillsa-redis` 컨테이너(`fillsa-net`), Cloudflare DNS | wiki 인프라 문서는 아직 AWS 기준 → 배포 후 갱신 필요 |

⚠️ **6GB RAM에 JVM 2개 + Redis(+nginx)** 가 핵심 제약이다. §3-E에서 결정.

## 1. 현재 배포 파이프라인 (As-Is)

`main` 푸시 → `.github/workflows/deploy.yml`:

```
gradle build (-x test)
  → Docker 이미지 1개 (Dockerfile, bff/app JAR만)  [amd64+arm64]
  → Docker Hub push: fillsaapp/fillsa:main-<run_number>
  → OCI VM SSH
      - secrets.PROD_YML(base64) → ~/app/config/application-prod.yml
      - docker run fillsa-container (-p 80:8080, --network fillsa-net, /config 마운트)
```

- prod yml은 gitignore라 JAR에 없음 → Secret을 VM `./config/`에 풀어 Spring Boot 기본 탐색 경로로 로딩 (이 방식 유지)
- 컨테이너가 80 포트 직접 바인딩 — VM 내 리버스 프록시 없음

## 2. 목표 상태 (To-Be)

| | bff:app (앱 API) | bff:admin (어드민 API) |
|---|---|---|
| JAR | `bff/app/build/libs/*.jar` | `bff/admin/build/libs/*.jar` |
| 포트 | 8080 | 8081 |
| 도메인 | `api.fillsa.com` (불변 🔴) | `admin-api.fillsa.com` (신규) |
| 이미지 | `fillsaapp/fillsa-app:<tag>` | `fillsaapp/fillsa-admin:<tag>` |
| 컨테이너 | `fillsa-app` | `fillsa-admin` |
| 설정 | Secret `PROD_YML_APP` | Secret `PROD_YML_ADMIN` |
| JVM 힙 | `-Xmx1536m` (§3-E) | `-Xmx512m` (§3-E, 어드민 ~3명이라 최소) |

공유: `fillsa-redis`, 운영 TiDB, 동일 VM(`fillsa-net`).

🔴 **최우선 제약**: `api.fillsa.com`의 앱 API **경로·응답 불변**.

## 3. 결정 사항 (선택지 + 권장)

### A. 어드민 API 도메인
- **A1 (권장)**: `admin-api.fillsa.com` 서브도메인 — Cloudflare A 레코드 1개. 정책 분리 용이.
- A2: `api.fillsa.com/api/admin/*` 경로 라우팅 — 앱 도메인에 어드민 노출, 비권장.

### B. VM 내 트래픽 라우팅 (핵심)
현재 app이 80을 직접 점유 → 두 도메인을 한 VM에서 나눌 장치 필요.

- **B1 (권장)**: **nginx 리버스 프록시**(컨테이너 `nginx:alpine`, `fillsa-net` 참여)가 80 수신 → `server_name`별 분기: `api.fillsa.com→fillsa-app:8080`, `admin-api.fillsa.com→fillsa-admin:8081`. 표준적·확장 용이. 메모리 ~10MB 수준으로 6GB 제약에 부담 없음. *(Cloudflare SSL 모드가 Flexible로 확인됨(§8) → origin은 80만 받으면 되고 **인증서 불필요**. Full(strict) 전환은 후속 작업)*
- B2: 포트 분리 노출(80/8081) + Cloudflare Origin Rule — Cloudflare 유료 기능 의존 + OCI 인그레스에 8081 오픈. 비권장.
- B3: admin 전용 VM 추가 — A1 용량 확보 자체가 어려운 상황이라 비현실적.

### C. 어드민 프론트(fillsa-admin) 호스팅 — CORS 의존 관계로 함께 결정
- **C1 (권장)**: Cloudflare Pages → `admin.fillsa.com`. 빌드 시 `VITE_API_BASE_URL=https://admin-api.fillsa.com`.
- C2: VM nginx가 정적 서빙 — 프론트 산출물 전송 파이프라인 추가 필요.

### D. 이미지 전략
- **권장**: Docker Hub 리포 2개(`fillsa-app`, `fillsa-admin`), 태그는 기존 관례 `main-<run_number>` 공유(같은 커밋=같은 태그 → 롤백 짝 맞춤).
- Dockerfile 1개를 `ARG MODULE=app|admin`으로 파라미터화.
- **arm64 필수** — 현 buildx 설정 유지. (amd64는 로컬 x86 테스트용으로만 의미)

### E. 메모리 전략 (6GB RAM) — OCI 특화
예산(대략): OS+Docker ~0.8G / Redis ~0.2G / nginx ~0.05G → **JVM 가용 ~4.5G**

- **E1 (권장)**: 현행 6GB 유지 + JVM 힙 상한 명시 — app `-Xmx1536m`, admin `-Xmx512m` (+`-XX:MaxMetaspaceSize=256m`씩). 어드민은 사용자 ~3명이라 최소로 — 단 512m 밑(384m↓)은 Spring Boot+Hibernate 상주 비용·대시보드 집계 스파이크 대비 마진이 없어 비권장. 스왑 2G 추가로 OOM 킬 방어.
- E2: **shape 증설** — A1.Flex는 Always Free 한도 내 **4 OCPU/24GB까지** 무료. `oci-vm-launch.yml`의 `shape-config`를 `{"ocpus":2,"memoryInGBs":12}`로 올려 재생성. 여유는 최고지만 **용량 부족으로 런치 실패 확률이 커지고 VM 재생성(IP 변경·재셋업) 필요**.
- 판단: **일단 E1로 배포, 운영 지표 보고 E2 검토.**

## 4. 코드/파이프라인 변경 (배포 전 필수)

1. **🔴 Admin CORS 하드코딩 해제** — `AdminSecurityConfig.kt` `allowedOrigins = listOf("http://localhost:5173")` → 프로퍼티 주입:
   ```yaml
   # bff/admin application-prod.yml
   admin-cors:
     allowed-origins: https://admin.fillsa.com
   ```
   (local 기본값은 `http://localhost:5173` 유지)
2. **Dockerfile 파라미터화** (`ARG MODULE`) + `ENTRYPOINT`에 `JAVA_OPTS`(힙 상한) 반영.
3. **deploy.yml 개편**: 이미지 2개 빌드/푸시 → VM 스크립트에서 `fillsa-app`·`fillsa-admin` 2컨테이너 기동(`-e JAVA_OPTS`) → nginx conf 배치·`nginx -s reload`. 스텝 명칭 "Deploy to EC2" → "Deploy to OCI VM"으로 정정.
4. **GH Secrets**: `PROD_YML_ADMIN` 추가 (기존 `PROD_YML`은 app용으로 역할 명확화).
5. admin 테스트(`:bff:admin:test`) 로컬 통과 확인 후 머지 (`bff:app` 테스트는 사전 존재 이슈로 깨져 있음 — CI `-x test` 유지).
6. (선택) `EC2_*` 시크릿 → `DEPLOY_HOST` 등으로 개명 — 혼동 방지용, 기능 무관.

## 5. 운영 DB 마이그레이션 (배포 "전" — TiDB)

prod `ddl-auto: none` → 수동 DDL 필수:

```sql
-- 1) 어드민 계정 테이블 (AdminEntity 기준)
CREATE TABLE IF NOT EXISTS admins (
  admin_seq     BIGINT PRIMARY KEY AUTO_INCREMENT,
  login_id      VARCHAR(50)  NOT NULL UNIQUE,
  password      VARCHAR(100) NOT NULL,          -- BCrypt
  name          VARCHAR(50)  NOT NULL,
  role          VARCHAR(20)  NOT NULL,
  active_yn     CHAR(1)      NOT NULL DEFAULT 'Y',
  last_login_at DATETIME     NULL,
  created_at    DATETIME     NOT NULL,
  updated_at    DATETIME     NOT NULL
);
-- + 최초 어드민 1행 시드 (BCrypt 해시 수동 INSERT)

-- 2) 명언 소프트삭제 컬럼 (docs/migration/2026-07-quotes-del-yn.md)
ALTER TABLE quotes ADD COLUMN DEL_YN CHAR(1) NOT NULL DEFAULT 'N';
```

검증: `SELECT DEL_YN, COUNT(*) FROM quotes GROUP BY DEL_YN;` → 전량 `'N'`.
⚠️ DEL_YN 누락 상태로 새 app 배포 시 **명언 API 전체 500** (로컬에서 실제 재현된 유형). 구버전 app에는 이 컬럼 추가가 무해(하위호환)하므로 **마이그레이션을 먼저** 한다.

## 6. 인프라 준비 (1회성) — OCI 체크리스트

- [ ] Cloudflare DNS: `admin-api.fillsa.com` A 레코드 → VM 공인 IP (Proxied)
- [ ] (C1) Cloudflare Pages 프로젝트 + `admin.fillsa.com`
- [ ] Docker Hub 리포: `fillsa-app`, `fillsa-admin` 생성
- [ ] GH Secrets: `PROD_YML_APP`(=기존 PROD_YML), `PROD_YML_ADMIN` 등록
- [ ] **🔴 R2 실값 주입**: 리포의 app prod yml은 `cloud.r2.*`가 env 플레이스홀더(`${R2_ACCESS_KEY:}` 등) — `PROD_YML_APP` 시크릿 내용에는 **R2 실값을 채워** 넣거나 docker run에 `-e R2_*` env를 추가해야 함. 누락 시 운영 이미지 업로드 실패
- [ ] **OCI 인그레스**: VCN Security List(또는 NSG)에 80/443 허용 확인 — admin도 nginx 경유(B1)라 **추가 포트 개방 불필요**
- [ ] **호스트 방화벽**: OCI 기본 이미지에는 iptables REJECT 규칙이 있는 경우가 많음 — Security List와 **양쪽 모두** 확인 (`sudo iptables -L INPUT`)
- [ ] 스왑 2G 설정 (`fallocate`+`swapon`, E1 채택 시)
- [x] **VM 부트스트랩 스크립트** `infra/server-setup.sh` ✅ 작성됨: docker 설치 → 스왑 2G → iptables 80/443 → `fillsa-net`/`fillsa-redis` → 배포 디렉터리. *(VM 재생성 시 이 스크립트 1회 실행 + Cloudflare A 레코드 갱신)*

## 7. 배포 실행 순서 (Runbook)

1. **코드 준비**: §4의 1~4 커밋 (feature/bff-restructure) → `:bff:admin:test` 통과 확인
2. **DB 마이그레이션**(§5): 운영 TiDB 실행·검증 — 구버전 app에 무해하므로 선행
3. **인프라 준비**(§6) 완료 확인 — 특히 OCI Security List + iptables 이중 확인
4. **머지 & 자동 배포**: → `main` 머지 → 새 파이프라인이 2 이미지 빌드, VM에 2컨테이너 + nginx 기동
5. **스모크 테스트**:
   - 앱(불변): `GET https://api.fillsa.com/actuator/health` 200 + 기존 핵심 API(일별 명언 등) 응답 형식 동일
   - 어드민: `https://admin-api.fillsa.com/actuator/health` 200 → 로그인 → 명언 목록 200
   - 보안: 앱 JWT로 `/api/admin/*` → 401 (토큰 분리)
   - CORS: `admin.fillsa.com` 오리진 preflight 통과
   - **메모리**: `docker stats`로 두 JVM RSS 확인 — 합계가 한도 내인지
6. **어드민 프론트 배포**(C1): `VITE_USE_MOCK=false` + `VITE_API_BASE_URL=https://admin-api.fillsa.com` 빌드 → Pages
7. **관찰**: 30분 — 앱 에러 로그·지연·`free -m`/스왑 사용량

### 롤백
- **app 이상**: 구 단일 이미지 `fillsaapp/fillsa:<직전 태그>`로 `-p 80:8080` 원복(nginx 중지) — DEL_YN 컬럼은 구버전에 무해.
- **admin 이상**: `fillsa-admin`만 중지 — 앱 무영향(분리의 이점).
- **메모리 압박(OOM)**: admin 컨테이너 중지 후 힙 조정 재기동, 필요 시 E2(shape 증설) 진행.
- DB 롤백 불필요(추가형 마이그레이션만).

## 8. 리스크 & 미해결 질문

| 항목 | 내용 | 상태 |
|---|---|---|
| 🔴 앱 API 불변 | 재구조화로 경로/응답 변화 없는지 머지 전 최종 대조 (`bff:app` 테스트 부재로 수동) | 머지 전 확인 |
| 🔴 6GB 메모리 | JVM 2개 — 힙 상한 미지정 시 OOM 킬 위험. §3-E1 필수 적용 | 코드 변경 포함 |
| CORS 하드코딩 | 미처리 시 운영 어드민 전면 CORS 차단 | §4-1 |
| OCI 이중 방화벽 | Security List만 열고 호스트 iptables를 안 열면 "됐는데 안 됨" 상태 | §6 체크 |
| VM 휘발성 | Always Free VM 재확보 구조 — 재생성 시 IP 변경·수동 셋업이 유일한 기록이면 복구 불가 | §6 부트스트랩 스크립트 |
| TLS 방식 | **✅ 확정: Flexible** (Cloudflare 대시보드 확인, 2026-07-18). 1차 배포는 유지 — nginx 80만, 인증서 불필요. 후속: Origin CA 인증서 + Full(strict) 전환. ⚠️ 최근 24h 평문 HTTP 요청 42건 관측 — 구버전 앱일 가능성이 있어 "Always Use HTTPS" 리다이렉트는 이번 전환에서 켜지 않는다(301이 앱 POST를 깨뜨릴 수 있음) | 확정 |
| wiki 불일치 | 인프라 문서가 AWS 2-Tier 기준 — 배포 후 OCI 기준으로 갱신 | 후속 작업 |

**확정된 것** (2026-07-18, 전부 확정):
- Cloudflare SSL 모드 → **Flexible 확인** (대시보드). 1차 유지 — nginx 인증서 불필요. Full(strict) 전환은 후속 작업.
- 도메인 → **`admin-api.fillsa.com`(API) + `admin.fillsa.com`(콘솔)** 확정.
- 메모리 → **E1 (6GB 유지 + 힙 상한 app 1536m / admin 512m + 스왑 2G)** 확정.
- `fillsa-vm` → **가동·서비스 중** 확인. Runbook 시작 가능.

**§4 구현 현황** (2026-07-18):
- [x] CORS 프로퍼티화 (`admin-cors.allowed-origins`, base=localhost:5173 / prod=admin.fillsa.com)
- [x] Dockerfile `ARG MODULE` + `JAVA_OPTS` 파라미터화
- [x] `infra/nginx/default.conf` (문법 검증 완료) + `infra/server-setup.sh`
- [x] deploy.yml 개편 (2이미지 → 2컨테이너 + nginx + 헬스체크 게이트)
- [ ] GH Secrets 등록 (사용자 작업): `PROD_YML_APP`(🔴 R2 실값 포함), `PROD_YML_ADMIN` — 각각 해당 prod yml 을 base64 인코딩
