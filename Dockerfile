# syntax=docker/dockerfile:1

# ---------- Stage 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Cache dependency resolution in its own layer so a source-only change doesn't
# force Maven to re-download the internet.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /build/target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring

EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD wget -qO- http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]