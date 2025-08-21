# Stage 1: Build the application using Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final, smaller image
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/cards-service-0.1.0.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java","-jar","app.jar"]
