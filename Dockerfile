# 베이스 이미지를 OpenJDK 17로 설정
FROM eclipse-temurin:17-jre

# 빌드 결과물인 JAR 파일을 컨테이너 내부에 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 기본 프로필을 prod 로 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 컨테이너 시작 시 실행될 명령
ENTRYPOINT ["java","-jar","/app.jar"]