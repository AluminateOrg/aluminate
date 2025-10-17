# --------------------------------------------------------------------------
# STAGE 1: Build stage with Maven
# --------------------------------------------------------------------------
FROM maven:3.9 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies for caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the application, skipping tests
COPY src ./src
RUN mvn clean package -DskipTests

# --------------------------------------------------------------------------
# STAGE 2: Runtime stage with OpenJDK 21
# --------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS runtime
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# IMPORTANT FIX: The application port is provided by the environment variable
# ORGANIZATION_PORT from docker-compose, not a hardcoded value.
# We set the Spring Boot convention (SERVER_PORT) using the value of the
# ORGANIZATION_PORT environment variable set by Docker Compose at runtime.

# Since we are setting the final ENV in the runtime stage, we rely on the
# value passed via the 'environment:' block in docker-compose.yml at runtime.

# The Spring Boot JAR will automatically read the ORGANIZATION_PORT environment
# variable directly from the OS environment, and the application.properties
# should reference the standard Spring property for the port, which is `server.port`.
# But since we use server.port=8098 in application.properties, let's ensure
# the EXPOSE command is correct and no ARG is needed here.

# The application port is fixed internally to 8098 (as per your application.properties).
# We should expose the port used internally by the Spring Boot server.
ENV SERVER_PORT=8098
EXPOSE 8098

# Run the application
CMD ["java", "-jar", "app.jar"]
