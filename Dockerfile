# ===== Build Stage =====
FROM gradle:8.14.5-jdk21 AS builder

WORKDIR /app

#Gradle 파일 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

#의존성 캐싱
RUN ./gradlew dependencies --no-daemon || true

#소스 복사
COPY src ./src

#애플리케이션 빌드
RUN ./gradlew bootJar --no-daemon

#=====Run Stage=====
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]