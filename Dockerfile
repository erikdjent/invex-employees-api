# ---- Build stage: compiles and runs the tests, so a broken build never produces an image ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Dependencies first: this layer is cached until pom.xml changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B package

# ---- Runtime stage: only the JRE and the jar, running as a non-root user ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/logs && chown -R app:app /app
USER app

COPY --from=build --chown=app:app /app/target/employees-api.jar app.jar

# Fixed time zone so createdAt means the same thing on a laptop and in the container.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Duser.timezone=America/Mexico_City" \
    LOG_FILE=/app/logs/employees-api.log

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
