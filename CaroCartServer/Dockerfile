# Use an official Java runtime as a parent image
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the built jar file
COPY target/CaroCartServer-0.0.1-SNAPSHOT.jar app.jar

# Expose Eureka's port
EXPOSE 8761

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
