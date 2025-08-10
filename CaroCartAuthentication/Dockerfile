# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Package Stage ----
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/CaroCartAuthentication-0.0.1-SNAPSHOT.jar app.jar

# Cloud Run will set $PORT (default is 8080)
EXPOSE 8080

# Pass the dynamic PORT to Spring Boot
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT} --spring.profiles.active=prod"]
