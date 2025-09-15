# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---- Runtime Stage (distroless) ----
FROM gcr.io/distroless/java17
WORKDIR /app
# Use the correct JAR name from your target folder
COPY --from=build /app/target/OrderService-0.0.1-SNAPSHOT.jar app.jar
# If version may change, safer:
# COPY --from=build /app/target/*OrderService*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=prod"]
