FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ADD https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar /app/newrelic.jar
RUN addgroup -S autocarehub && adduser -S autocarehub -G autocarehub && chown autocarehub:autocarehub /app/newrelic.jar
COPY --from=build --chown=autocarehub:autocarehub /app/target/autocare-hub-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
USER autocarehub
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -javaagent:/app/newrelic.jar"
ENTRYPOINT ["java", "-jar", "app.jar"]
