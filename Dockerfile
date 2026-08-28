FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app/backend
COPY backend/pom.xml .
COPY backend/src ./src
COPY frontend ../frontend
RUN mvn clean verify

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/backend/target/quarkus-app/ /app/quarkus-app/
COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/app/docker-entrypoint.sh"]
