# Multi-stage build for optimized Docker image
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage - using eclipse-temurin instead of openjdk
FROM eclipse-temurin:17-jre-jammy

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-root user for security
RUN addgroup --system spring && adduser --system --group spring

# Set the working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/bulksmsAPI-*.jar app.jar

# Copy the production environment file
COPY .env.production .env.production

# Create uploads directory for validation images
RUN mkdir -p /app/uploads/validation-images && \
    chown -R spring:spring /app/uploads

# Change ownership to the spring user
RUN chown spring:spring app.jar .env.production

# Switch to the non-root user
USER spring

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set default environment variables from .env.production
ENV SPRING_PROFILES_ACTIVE=production
ENV PORT=8080
ENV DDL_AUTO=update

# Run the JAR file with optimized JVM settings
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
