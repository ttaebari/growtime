# 멀티스테이지 빌드로 이미지 크기 최적화
# Stage 1: 빌드 스테이지
FROM gradle:8.14.2-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 설정 파일들 복사 (캐싱 최적화)
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# 의존성 다운로드 (별도 레이어로 캐싱)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드 (테스트 제외)
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: 실행 스테이지
FROM eclipse-temurin:17-jre-alpine

# 메타데이터 추가
LABEL maintainer="growtime-team"
LABEL description="GrowTime - 산업기능요원 복무 관리 시스템"
LABEL version="1.0.0"

# 타임존 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 애플리케이션 사용자 생성 (보안 강화)
RUN addgroup -g 1000 growtime && \
    adduser -D -s /bin/sh -u 1000 -G growtime growtime

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 소유자 변경
RUN chown -R growtime:growtime /app

# 사용자 변경
USER growtime

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 포트 노출
EXPOSE 8080

# JVM 옵션과 애플리케이션 실행
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=docker", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"] 