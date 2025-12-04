# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/ticketBooker-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=default
EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
