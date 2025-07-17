# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Package Stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# 👇 Change JAR name to your APIGateway jar!
COPY --from=build /app/target/CaroCartAPIGateway-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
