FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /app/backend
COPY backend/pom.xml .
COPY backend/src ./src
COPY frontend ../frontend
RUN mvn clean verify

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/target/dominus-gestor-evolution-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
