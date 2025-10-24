# --------------------------------------------------------------------------
# STAGE 1: Build stage with Maven (Fixed to explicitly use JDK 17)
# --------------------------------------------------------------------------
# Use a specific Maven image based on Temurin JDK 17 to ensure compiler stability.
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies for caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the application, skipping tests
COPY src ./src
# The previous compiler error should be fixed by the base image change.
RUN mvn clean package -DskipTests

# --------------------------------------------------------------------------
# STAGE 2: Runtime stage with OpenJDK 21 (If your runtime REQUIRES 21)
# NOTE: To be consistent, consider using the JDK version your code compiles against.
# If your application is compatible with 17, using temurin:17-jdk is simpler.
# I'll keep 21 as requested by your original file, but recommend 17 if possible.
# --------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS runtime
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Configuration for Spring Boot
ENV SERVER_PORT=8098
EXPOSE 8098

# Run the application
CMD ["java", "-jar", "app.jar"]