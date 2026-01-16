# ========================================
# Multi-stage Dockerfile for API Gateway
# ========================================

# Stage 1: Build với Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml trước để cache dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw

# Download dependencies (cached nếu pom.xml không đổi)
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime image (nhẹ hơn nhiều)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Tạo user non-root để chạy app (best practice)
RUN addgroup -g 1001 appgroup && adduser -u 1001 -G appgroup -D appuser

# Copy JAR từ build stage
COPY --from=builder /app/target/*.jar app.jar

# Chuyển sang user non-root
USER appuser

# Port của API Gateway
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]