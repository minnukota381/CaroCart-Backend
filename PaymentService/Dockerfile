# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---- Runtime Stage (distroless) ----
FROM gcr.io/distroless/java17
WORKDIR /app
# safer to wildcard in case version changes
COPY --from=build /app/target/*PaymentService*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=prod"]
