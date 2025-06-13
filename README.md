# Fillsa API
**Fillsa API**는 Kotlin 기반의 Spring Boot 애플리케이션으로, Fillsa 서비스의 백엔드를 담당합니다.<br>
회원 관리, 명언 제공, 공지사항 등의 기능을 위한 RESTful API를 제공하며, JWT 기반 인증과 외부 OAuth 로그인 연동을 지원합니다.
<br>


## 서버 아키텍처
<img width="1077" alt="스크린샷 2025-06-13 오후 5 26 04" src="https://github.com/user-attachments/assets/2da0f777-49fd-499b-a24d-a42fed7cfe6a" />
<br>

## 주요 기능

* Kotlin 1.9 & Spring Boot 3
* JWT 기반 인증 및 OAuth 로그인 지원
* 회원, 명언, 공지사항 관련 CRUD API 제공
* AWS S3를 통한 파일 저장
* Redis를 활용한 토큰 관리
* Spring Data JPA 기반 MySQL 연동
* Swagger/OpenAPI 문서 제공
* Docker 기반 배포 지원

## 시작하기

1. **프로젝트 빌드**

   ```bash
   ./gradlew build
   ```

2. **로컬 실행**

   ```bash
   ./gradlew bootRun
   ```

   또는 Docker 이미지로 실행:

   ```bash
   docker build -t fillsa_api .
   docker run -p 8080:8080 fillsa_api
   ```

3. **테스트 실행**

   ```bash
   ./gradlew test
   ```

## 프로젝트 구조

* `src/main/kotlin` – 애플리케이션 소스
* `src/main/resources` – 설정 파일
* `src/test` – 테스트 코드
* `docs` – 문서 및 아키텍처 이미지

설정 값은 `src/main/resources/application.yml` 파일에서 확인할 수 있으며, 테스트용 설정은 `src/test/resources/application-test.yml`에 정의되어 있습니다.

## 라이선스

이 저장소는 Fillsa 내부 사용을 목적으로 합니다.
