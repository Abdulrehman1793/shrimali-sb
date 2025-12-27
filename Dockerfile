# ---------- Stage 1: build using the Gradle wrapper (JDK 21) ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy gradle wrapper and wrapper config first (helps layer caching)
COPY gradlew .
COPY gradle ./gradle

# Copy build files
COPY build.gradle settings.gradle ./
# if you have gradle.properties, pom-like files, copy them too:
# COPY gradle.properties ./

# Copy source
COPY src ./src

# Ensure gradlew is executable and run it (it will download the required Gradle version)
RUN chmod +x ./gradlew \
 && ./gradlew --no-daemon clean bootJar -x test

# ---------- Stage 2: runtime (minimal JRE) ----------
FROM eclipse-temurin:21-jre-jammy
ARG APP_PORT=8080
ENV PORT=${APP_PORT}

# activate prod profile
ENV SPRING_PROFILES_ACTIVE=prod

WORKDIR /app

# Copy built jar from the build stage
COPY --from=build /app/build/libs/*.jar ./app.jar

# Create non-root user and set ownership
RUN addgroup --system appgroup \
 && adduser --system --ingroup appgroup appuser \
 && chown appuser:appgroup /app/app.jar

USER appuser

EXPOSE ${PORT}

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s \
  CMD wget -qO- --timeout=2 http://localhost:${PORT}/actuator/health || exit 1

#ENTRYPOINT ["java","-jar","/app/app.jar"]
ENV JAVA_OPTS="-Dnetworkaddress.cache.ttl=60"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
