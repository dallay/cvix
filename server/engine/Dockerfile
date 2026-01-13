# Stage 1: Build the application
FROM eclipse-temurin:24-jdk-noble AS builder

WORKDIR /workspace

# Copy Git metadata (required for gradle-git-properties plugin)
COPY .git /workspace/.git

# Copy gradle wrapper and configuration files
COPY gradle /workspace/gradle
COPY gradlew /workspace/
COPY gradle.properties /workspace/
COPY settings.gradle.kts /workspace/
COPY build.gradle.kts /workspace/
COPY build-logic /workspace/build-logic

# Copy all subproject build files to leverage Docker layer caching
COPY shared/common/build.gradle.kts /workspace/shared/common/
COPY shared/spring-boot-common/build.gradle.kts /workspace/shared/spring-boot-common/
COPY server/engine/build.gradle.kts /workspace/server/engine/

# Download dependencies with optimizations
# Use --quiet to reduce verbosity and improve build parallelism on ARM
RUN ./gradlew dependencies --no-daemon --quiet --parallel --max-workers="$(nproc)" || true

# Copy source code for all modules
COPY shared/common/src /workspace/shared/common/src
COPY shared/spring-boot-common/src /workspace/shared/spring-boot-common/src
COPY server/engine/src /workspace/server/engine/src

# Build the application with optimizations for container builds
RUN ./gradlew :server:engine:bootJar \
    --no-daemon \
    --quiet \
    --parallel \
    --max-workers="$(nproc)" \
    -Dorg.gradle.jvmargs="-XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled -XX:MaxRAMPercentage=75.0"

# Stage 2: Create the runtime image
FROM eclipse-temurin:24-jre-noble

# Create a non-root user with explicit UID/GID for predictable file permissions
# UID 999 is used to match the containerUser in DockerPdfGeneratorProperties
RUN groupadd -r -g 999 spring && useradd -r -u 999 -g spring spring

# Set working directory
WORKDIR /app


# Copy the built jar from the builder stage
COPY --from=builder /workspace/server/engine/build/libs/*.jar app.jar

# Copy entrypoint script for Docker secrets -> env bridge
COPY server/engine/docker-entrypoint.sh /app/docker-entrypoint.sh

# Setup permissions and user in one RUN instruction for better layer efficiency
RUN chmod +x /app/docker-entrypoint.sh && \
    chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose the application port
EXPOSE 8080

# Health check (uses bash and /dev/tcp to avoid curl dependency)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD bash -c 'echo > /dev/tcp/localhost/8080' || exit 1


# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Run the application via entrypoint script that loads Docker secrets
ENTRYPOINT ["/app/docker-entrypoint.sh"]
