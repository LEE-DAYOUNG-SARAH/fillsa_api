# 베이스 이미지를 OpenJDK 17로 설정 (amd64/arm64 멀티플랫폼 — OCI A1(Ampere)은 arm64)
FROM eclipse-temurin:17-jre

# 빌드할 BFF 모듈 선택: app | admin (deploy 워크플로에서 --build-arg MODULE=... 로 주입)
ARG MODULE=app
COPY bff/${MODULE}/build/libs/*.jar app.jar

# 기본 프로필을 prod 로 설정
ENV SPRING_PROFILES_ACTIVE=prod
# JVM 옵션(힙 상한 등)은 컨테이너 기동 시 -e JAVA_OPTS 로 주입 (6GB VM에서 2개 JVM 공존용)
ENV JAVA_OPTS=""

# 컨테이너 시작 시 실행될 명령
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app.jar"]
