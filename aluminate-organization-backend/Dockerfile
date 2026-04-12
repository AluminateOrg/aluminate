# --------------------------------------------------------------------------
# STAGE 1: Build stage with Maven + JDK 17
# --------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# --------------------------------------------------------------------------
# STAGE 2: Runtime stage with JDK 17
# --------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk AS runtime
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV SERVER_PORT=8098
EXPOSE 8098

CMD ["java", "-jar", "app.jar"]