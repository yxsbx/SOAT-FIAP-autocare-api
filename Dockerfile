FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S autocarehub && adduser -S autocarehub -G autocarehub
COPY --from=build --chown=autocarehub:autocarehub /app/target/autocare-hub-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
USER autocarehub
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
