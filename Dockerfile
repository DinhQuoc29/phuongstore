# ===================================
# Stage 1: Build Angular Frontend
# ===================================
FROM node:22-alpine AS frontend-builder

WORKDIR /app/frontend

# Copy package files first for better caching
COPY frontend/package*.json ./

# Upgrade npm to match packageManager requirement (npm@11.x)
RUN npm install -g npm@11

# Install dependencies (use npm install to avoid lock file sync issues cross-platform)
RUN npm install --legacy-peer-deps

# Copy frontend source and build
COPY frontend/ ./
RUN npm run build -- --configuration=production

# ===================================
# Stage 2: Build Spring Boot Backend
# ===================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS backend-builder

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml ./
RUN mvn dependency:go-offline -q

# Copy source and build (skip tests for faster build)
COPY src/ ./src/
RUN mvn package -DskipTests -q

# ===================================
# Stage 3: Final Runtime Image
# ===================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR
COPY --from=backend-builder /app/target/*.jar app.jar

# Copy Angular build output into Spring Boot static resources
COPY --from=frontend-builder /app/frontend/dist/phuongstore-fe/browser/ /app/static/

# Create a startup script
RUN echo '#!/bin/sh' > /start.sh && \
    echo 'exec java -jar /app/app.jar' >> /start.sh && \
    chmod +x /start.sh

EXPOSE 8080

ENTRYPOINT ["/start.sh"]
