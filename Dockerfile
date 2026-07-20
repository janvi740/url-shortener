# Stage 1 - Build the application
FROM gradle:8.10-jdk21 AS builder

WORKDIR /app

COPY . .

RUN gradle clean bootJar --no-daemon

# Stage 2 - Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]