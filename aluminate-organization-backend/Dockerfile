# aluminate-organization-backend/Dockerfile
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY . .
# Add certificates and run Maven
RUN apt-get update && \
    apt-get install -y ca-certificates && \
    update-ca-certificates && \
    mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8098
ENTRYPOINT ["java", "-jar", "app.jar"]