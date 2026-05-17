# ============================================
# AI 纪检监察智能审理一体化平台
# Dockerfile — auth 模块
# ============================================
# 构建: docker build -t trial-auth -f docker/auth/Dockerfile .
# 运行: docker run -d -p 8081:8081 --name trial-auth trial-auth
# ============================================

# ── Stage 1: Build ──
FROM maven:3.9-eclipse-temurin-8-alpine AS builder

WORKDIR /app

# Copy parent POM first (for dependency resolution)
COPY pom.xml .
COPY common/pom.xml common/pom.xml

# Copy module-specific POMs
COPY auth/pom.xml auth/pom.xml

# Copy remaining module POMs needed for the build
COPY repository/pom.xml repository/pom.xml
COPY document/pom.xml document/pom.xml
COPY report/pom.xml report/pom.xml
COPY promotion/pom.xml promotion/pom.xml
COPY workflow/pom.xml workflow/pom.xml
COPY punishment/pom.xml punishment/pom.xml
COPY readingnote/pom.xml readingnote/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY case/pom.xml case/pom.xml

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl auth -am -B || true

# Copy source code
COPY common/src common/src
COPY auth/src auth/src

# Build the module (skip tests for Docker build — CI handles tests)
RUN mvn package -pl auth -am -DskipTests -B

# ── Stage 2: Runtime ──
FROM eclipse-temurin:8-jre-alpine

LABEL maintainer="intelligent-trial-system"
LABEL description="AI纪检监察智能审理一体化平台 — auth 模块"

# Create non-root user
RUN addgroup -S trial && adduser -S trial-user -G trial

WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/auth/target/intelligent-trial-auth-*.jar app.jar

# JVM options for Java 8
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Expose the service port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=60s \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# Run as non-root user
USER trial-user

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
